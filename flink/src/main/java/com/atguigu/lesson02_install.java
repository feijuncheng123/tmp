package com.atguigu;

public class lesson02_install {
    public static void main(String[] args) {

        //local-cluster模式配置
//        本地模式直接使用bin/start-cluster命令启动即可
//        启动后访问8081端口就可以看到flink资源管理页面
//        如果启动后8081端口无法进入页面，可以将conf/config.yml文件中的rest相关配置的ip地址从localhost更改为0.0.0.0
//       提交方式：
//       bin/flink run -m localhost:8081 -c com.flinktest.Lesson1 /mnt/h/java-project/flink-test/target/flink-test-1.0-SNAPSHOT.jar


//        三种部署模式：
//        1、session-cluster 会话模式，申请一个jobManager,然后多个任务都提交这个jobManager中。适合周期短执行时间短的小任务
//        2、Per-Job-Cluster （将被废弃）每个任务都单独申请自己的jobManager，单独运行。谁提交应用，谁执行main函数（main函数执行服务器和jobManager服务器不一致？）
//        3、Application Mode  同Per-Job-Cluster，但是应用的main函数被提交到jobManager中执行（main函数执行服务器和jobManager服务器一致？）
//        提交命令：
//        1、Per-Job-Cluster模式：
//         /opt/flink-1.20.0/bin/flink run  -t yarn-per-job -m localhost:8088 -Dyarn.application.queue=production -c com.flinktest.Lesson1 /mnt/h/java-project/flink-test/target/flink-test-1.0-SNAPSHOT.jar
//
//        2、Application Mode模式（注意：需要使用run-application参数作为运行Application模式的执行器，否则报错 No ExecutorFactory found to execute the application.）：
//        /opt/flink-1.20.0/bin/flink run-application  -t yarn-application -m localhost:8088 -Dyarn.application.queue=production -c com.flinktest.Lesson1 /mnt/h/java-project/flink-test/target/flink-test-1.0-SNAPSHOT.jar
//
//        3、session-cluster：
//        （1）启动session：bin/yarn-session.sh -d -Dyarn.application.queue=production
//        -d是指后台运行session，如果不指定会一直前台执行
//        （2）提交到session
//         ./flink run -d -t yarn-session -Dyarn.application.id=application_1731034704876_0006 -c com.flinktest.Lesson1 /mnt/h/java-project/flink-test/target/flink-test-1.0-SNAPSHOT.jar
//        同样的，-d指定后台执行。-Dyarn.application.id指定了flink session的yarn id


//        yarn高可用flink设置：
//        对一些重要的长时间流任务，要保证持续执行。
//        1、首先yarn配置中需要重试机制，在yarn-site.xml中添加以下配置：
//        <property>
//          <name>yarn.resourcemanager.am.max-attempts</name>
//          <value>4</value>
//          <description>
//              The maximum number of application master execution attempts.
//          </description>
//        </property>

//        2、在flink的conf.yaml文件中添加以下配置：
//          yarn.application-attempts: 3  #不能超过yarn中配置的重试次数
//          high-availability: zookeeper
//          high-availability.storageDir: hdfs://hadoop162:8020/flink/yarn/ha
//          high-availability.zookeeper.quorum: hadoop162:2181,hadoop163:2181,hadoop164:2181
//          high-availability.zookeeper.path.root: /flink-yarn




    }
}
