//前台执行中的进程切换到后台执行的方法：ctrl+z 先暂停当前程序，然后使用 bg %1命令将程序切换到后台，%号后是暂停时返回的job号
//对于所有运行的程序，我们可以用jobs –l 指令查看
//可以用 fg %[number] 指令把一个程序掉到前台运行
//可以直接终止后台运行的程序，使用 kill 命令 ：kill %1

//Kafka专业术语
//Broker：Kafka 集群包含一个或多个服务器，这种服务器被称为 broker。
//Topic：每条发布到 Kafka 集群的消息都有一个类别，这个类别被称为 Topic。（物理上不同 Topic 的消息分开存储，逻辑上一个 Topic 的消息虽然保存于一个或多个 broker 上，但用户只需指定消息的 Topic 即可生产或消费数据而不必关心数据存于何处）。
//Partition：Partition 是物理上的概念，每个 Topic 包含一个或多个 Partition。
//Producer：负责发布消息到 Kafka broker。
//Consumer：消息消费者，向 Kafka broker 读取消息的客户端。
//Consumer Group：每个 Consumer 属于一个特定的 Consumer Group（可为每个 Consumer 指定 group name，若不指定 group name 则属于默认的 group）。



//1、查看当前kafka服务器钟全部的topic主题：
//bin/kafka-topics.sh --list --zookeeper localhost:2181


//2、创建topic
//bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 3 --topic secondTopic
//topic 定义topic名，replication-factor为定义副本数，partitions为定义分区数
//replication-factor不能大于brokers数

//3、删除topic
// bin/kafka-topics.sh --delete --zookeeper hadoop102:2181 --topic first
//需要server.properties中设置delete.topic.enable=true否则只是标记删除或者直接重启。


//4、发送消息
//bin/kafka-console-producer.sh --broker-list localhost:9092 --topic firstTopic
//会进入一个交互界面，逐行发送消息,broker-list表示由那个节点发送消息


//5、消费消息
//bin/kafka-console-consumer.sh --zookeeper localhost:2181 --from-beginning --topic firstTopic
//Kafka 从 2.2 版本开始将 kafka-topic.sh 脚本中的 −−zookeeper 参数标注为 “过时”，推荐使用 −−bootstrap-server 参数。

//bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --from-beginning --topic firstTopic  [--consumer.config config/consumer.properties]
//bootstrap-servers指的是目标集群的服务器地址，这个和broker-list功能是一样的，只不过我们在console producer要求用后者。

//6、查看某个Topic的详情
// bin/kafka-topics.sh --topic firstTopic --describe --zookeeper localhost:2181
// bin/kafka-topics.sh --topic firstTopic --describe --bootstrap-server localhost:9092


//无论消息是否被消费，kafka都会保留所有消息。有两种策略可以删除旧数据：
//1）基于时间：log.retention.hours=168
//2）基于大小：log.retention.bytes=1073741824
//需要注意的是，因为Kafka读取特定消息的时间复杂度为O(1)，即与文件大小无关，所以这里删除过期文件与提高 Kafka 性能无关。


//本机启动服务：
// bin/zookeeper-server-start.sh config/zookeeper.properties &
// bin/kafka-server-start.sh config/server.properties &
