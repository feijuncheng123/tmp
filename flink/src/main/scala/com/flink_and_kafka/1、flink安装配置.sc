
//伪分布搭建：
//1、将flink压缩包解压
//2、修改配置文件：flink文件夹下conf/flink-conf.yaml文件
//3、修改slave文件
//4、配置环境变量，设置HADOOP_CONF_DIR变量，设置为hadoop配置文件的目录
//5、执行bin/start-cluster.sh启动集群，也可以单机之下直接启动。

//HA搭建：
//需要备用节点主机监听zookeeper
//要注意两个地方：jobmanager.rpc.address这个jobmanager ip配置，主节点和备用节点都各自填自己的主机名
//需要在masters文件中，按顺序指定谁是主要谁是次要。需要hadoop1:8081这样主机名和端口一起指定

//yarn部署：
//需要配置yarn，将yarn.nodemanager.vmem-check-enabled参数设置为false，含义为内存超过分配值，是否杀掉任务，默认为是


//yarn-session模式（会话模式）：
//该模式为在yarn下持续运行一个flink服务，全部客户端连接到该服务，任务也通过该服务提交。（特别适合有大量小作业的场景）
//启动该模式通过以下命令行：
// ./bin/yarn-session.sh -n 2 -tm 1000 -s 1 -d
// 上述命令中：n表示启动的容器数量，tm表示每个taskmanager申请的内存数（mb），s表示slots数量， d表示在后台运行
//会话启动后，直接通过bin/flink命令提交任务。./bin/flink run xxxx.jar
//关闭会话模式通过yarn命令手动杀死任务进程


//分离模式：
//每一个作业都是一个flink进程，作业完成进程关闭
//直接提交给yarn，对于大作业使用该模式。
//该模式直接通过bin/flink命令提交作业，但需要额外指定master：
// ./bin/flink run -m yarn-cluster -yn
//上述命令中，-m表示jobmater的提交地址，-yn表示申请的taskmaster的个数

//flink四个重要概念：
//checkpoint：快照或者缓存，即使宕机也能恢复，而不用从头计算，提供分布式精确一致性。（消息只能被消费一次）
//state：状态，每次计算后都有一个状态
//Time：时间，通过时间解决数据的无序问题
//Window：窗口，滚动窗口、滑动窗口，会话窗口等

/**
Flink on Yarn：
			（*）内存集中管理模式：所有的Flink任务都将共享事先申请的内存资源
					bin/yarn-session.sh -n 2 -jm 1024 -tm 1024 -d
					错误：
					Container [pid=61432,containerID=container_1583200037872_0001_01_000001] is running beyond virtual memory limits. Current usage: 158.5 MB of 1 GB physical memory used; 2.2 GB of 2.1 GB virtual memory used. Killing container

				 执行任务
					bin/flink run examples/batch/WordCount.jar -input hdfs://bigdata111:9000/input/data.txt -output hdfs://bigdata111:9000/flink/wc1

					bin/flink run examples/batch/WordCount.jar -input hdfs://bigdata111:9000/input/data.txt -output hdfs://bigdata111:9000/flink/wc2

					bin/flink run examples/batch/WordCount.jar -input hdfs://bigdata111:9000/input/data.txt -output hdfs://bigdata111:9000/flink/wc3

			（*）内存Job管理模式：每个任务单独申请资源，彼此互不干扰
					bin/flink run -m yarn-cluster -yn 1 -yjm 1024 -ytm 1024 examples/batch/WordCount.jar

					-m：运行在yarn上
					-yn：分配一个NodeManager
					-yjm: JobManager的内容
					-ytm：TaskManager内存
		*/