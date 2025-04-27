package com.atguigu;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.operators.SlotSharingGroup;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.MemorySize;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.api.common.functions.MapFunction;

public class lesson03_principle {
    public static void main(String[] args) {
        /**
         * 运行时架构：Flink运行时包含2种进程:1个JobManager和至少1个TaskManager
         *
         * 1、客户端：即提交代码的终端。客户端用于准备和发送dataflow到JobManager。
         * 然后客户端可以断开与JobManager的连接(detached mode), 也可以继续保持与JobManager的连接(attached mode)
         *
         * 2、jobManager
         * 控制一个应用程序执行的主进程。也就是说，每个应用程序都会被一个的JobManager所控制执行
         * （1）JobManager会先接收到要执行的应用程序，这个应用程序会包括：作业图（JobGraph）、逻辑数据流图（logical dataflow graph）和打包了所有的类、库和其它资源的JAR包
         * （2）JobManager会把JobGraph转换成一个物理层面的数据流图，这个图被叫做“执行图”（ExecutionGraph），包含了所有可以并发执行的任务
         * （3）JobManager会向资源管理器（ResourceManager）请求执行任务必要的资源，也就是任务管理器（TaskManager）上的插槽（slot）
         * （4）在运行过程中，JobManager会负责所有需要中央协调的操作，比如说检查点（checkpoints）的协调
         *
         * 2.1 ResourceManager。该组件属于jobManager进程管理
         * 负责资源的管理，在整个 Flink 集群中只有一个 ResourceManager。这个ResourceManager不是Yarn中的ResourceManager, 是Flink中内置的
         * （1）主要负责管理任务管理器（TaskManager）的插槽（slot），TaskManger插槽是Flink中定义的处理资源单元。
         * （2）当JobManager申请插槽资源时，ResourceManager会将有空闲插槽的TaskManager分配给JobManager
         * （3）如果ResourceManager没有足够的插槽来满足JobManager的请求，它还可以向资源提供平台发起会话，以提供启动TaskManager进程的容器
         * （4）ResourceManager还负责终止空闲的TaskManager，释放计算资源
         *
         * 2.2 dispatcher. 该组件属于jobManager进程管理
         * 负责接收用户提供的作业，并且负责为这个新提交的作业启动一个新的JobMaster 组件
         *  (1) Dispatcher也会启动一个Web UI，用来方便地展示和监控作业执行的信息
         *  (2) Dispatcher在架构中可能并不是必需的，这取决于应用提交运行的方式。
         *
         * 2.3 jobMaster
         * JobMaster负责管理单个JobGraph的执行 .多个Job可以同时运行在一个Flink集群中, 每个Job都有一个自己的JobMaster.
         *
         * 3、taskManager
         *  Flink中的工作进程(jvm进程). 通常在Flink中会有多个TaskManager运行，每一个TaskManager都包含了一定数量的插槽（slots），也成为worker
         *  插槽的数量限制了TaskManager能够执行的任务数量。
         *  程序启动后，jobManager会向ResourceManager请求资源，ResourceManager开始启动TaskManager（多个）
         *  TaskManager启动并注册slot，并向资源管理器注册它的插槽(每一个taskManager都包含一定数量的slot)
         *  收到资源管理器的指令后，TaskManager就会将一个或者多个插槽提供给jobMaster调用. jobMaster就可以向插槽分配任务（tasks）来执行了
         *  在执行过程中，一个TaskManager可以跟其它运行同一应用程序的TaskManager交换数据
         *
         * 4、slot
         *  Flink中每一个worker(TaskManager)都是一个JVM进程，它可能会在独立的线程上执行一个Task
         *  worker通过Task Slot来进行控制一个worker能接收多少个task
         *  假如一个TaskManager有三个slot，那么它会将其管理的内存分成三份给各个slot。
         *  slot目前仅仅用来隔离task的受管理的内存，不涉及cpu的隔离（与spark的core不同）。即一个TaskManager时一个jvm进程，cpu该进程共同使用，slot仅仅平分了该jvm的内存
         *  使用下面配置设定每个taskManager拥有的slot的数量
         *  taskManager.numberOfTaskSlots:8
         *  为防止不同任务争抢taskManager的cpu，建议slot数设定为cpu核心数，确保每个slot都有一个核心分配
         */

        /**
         * flink提交yarn（per-job）执行流程：
         * 1.	Flink任务提交后，Client向HDFS上传Flink的Jar包和配置
         * 2.	向Yarn ResourceManager提交任务，Yarn ResourceManager分配Container资源
         * 3.	通知对应的Yarn NodeManager启动ApplicationMaster，ApplicationMaster启动后加载Flink的Jar包和配置构建环境，然后启动JobManager
         * 4.	JobManager中的jobMaster向Flink ResourceManager申请资源启动TaskManager。
         * 5.	Flink ResourceManager根据当前TaskManager空闲情况向yarn ResourceManager申请资源
         * 6.   yarn ResourceManager分配Container资源后，由ApplicationMaster通知资源所在节点的NodeManager启动TaskManager
         * 6.	NodeManager加载Flink的Jar包和配置构建环境并启动TaskManager
         * 7.	TaskManager启动后向JobManager发送心跳包，并等待JobManager向其分配任务。
         */

        /**
         * 一、并行度（parallelism）设置。如果不设置，一般认为并行度为cpu核数
         * 1，全局设置，直接在ExecutionEnvironment环境创建时设置。较少使用，会导致无法动态扩容
         * 2、在算子后setParallelism设置,推荐
         * 3、在启动程序时使用-p 5参数设定，例如此处设定为5（全局设定时推荐该方式）
         * 4、在配置文件conf.yaml中设置默认并行度。parallelism.default：5
         * parallelism与slot：
         * 1、slot是taskManager中的资源管理单元，也是flink中最小的资源申请单元，是静态的数量。parallelism是task执行的并行度，每个task会被分配到slot中执行
         * 2、在standalone模式下，如果parallelism设定比slot数量多，将会报无法allocate足够资源的错误
         * 3、在yarn模式下parallelism超过slot，则会动态申请taskManager
         *
         * 二、算子链
         * 相同并行度的one to one操作，Flink将这样相连的算子链接在一起形成一个task，原来的算子成为里面的一部分。 每个subtask被一个线程执行
         * 将算子链接成task是非常有效的优化：它能减少线程之间的切换和基于缓存区的数据交换，在减少时延的同时提升吞吐量。链接的行为可以在编程API中进行指定。
         *
         * 三、slot共享组
         * 为了防止同一个 slot 包含太多的 task，或者我们希望把计算逻辑复杂的算子单独使用 slot ，提高计算速度，Flink 提供了资源组(group) 的概念
         * group 就是对 operator 进行分组，同一 group 的不同 operator task 可以共享同一个 slot。默认所有 operator 属于同一个组"default"，也就是所有 operator task 可以共享一个 slot。
         * 目的：
         * 1、不同算子需要的资源不同，有的cpu密集有的内存要求高，为充分利用资源，可以结合不同算子既利用cpy也利用内存
         * 2、有的算子资源消耗太高，而其他很小，可以使用slot组单独申请资源
         */
        StreamExecutionEnvironment batchEnv = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> unboundedDataSrc = batchEnv.socketTextStream("localhost", 9999);
        unboundedDataSrc.filter((FilterFunction<String>) s->true)
                .map((MapFunction<String, String>) value -> "chain01")
                .startNewChain()
                .map((MapFunction<String, String>) value -> "chain02"); //startNewChain从前一个map开始组合成算子链，但是filter不会进入算子链

        unboundedDataSrc.filter((FilterFunction<String>) s->true).disableChaining();  //禁用算子链

        //单独申请一个SlotSharingGroup资源a
        SlotSharingGroup ssgA = SlotSharingGroup.newBuilder("a")
                .setCpuCores(1.0)
                .setTaskHeapMemoryMB(100)
                .build();

        //单独申请一个SlotSharingGroup资源b
        SlotSharingGroup ssgWithResource =
                SlotSharingGroup.newBuilder("ssg")
                        .setCpuCores(1.0) // required
                        .setTaskHeapMemoryMB(100) // required
                        .setTaskOffHeapMemoryMB(50)
                        .setManagedMemory(MemorySize.ofMebiBytes(200))
                        .setExternalResource("gpu", 1.0)
                        .build();


        unboundedDataSrc.filter((FilterFunction<String>) s->true).slotSharingGroup("a")  //设定filter算子的slot组为a
                .map((MapFunction<String, String>) s->"axc").slotSharingGroup(ssgWithResource)   //设定map算子的slot组为b
        ;

        batchEnv.registerSlotSharingGroup(ssgA);
        batchEnv.registerSlotSharingGroup(ssgWithResource);


        /**
         * 作业图：
         * Flink 中的执行图可以分成四层：StreamGraph -> JobGraph -> ExecutionGraph -> Physical Graph
         * 1、StreamGraph 是根据用户通过 Stream API 编写的代码生成的最初的DAG图。用来表示程序的拓扑结构
         * 2、JobGraph  StreamGraph经过优化后生成了 JobGraph，是提交给 JobManager 的数据结构。 JobGraph一般也是由客户端生成，在作业提交时提交给JobMaster
         * 主要的优化为: 将多个符合条件的节点 chain 在一起作为一个节点，这样可以减少数据在节点之间流动所需要的序列化/反序列化/传输消耗
         * 3、ExecutionGraph JobManager 根据 JobGraph 生成ExecutionGraph。ExecutionGraph是JobGraph的并行化版本，是调度层最核心的数据结构
         * 4、Physical Graph JobManager 根据 ExecutionGraph 对 Job 进行调度后，在各个TaskManager 上部署 Task 后形成的“图”，并不是一个具体的数据结构
         *
         * 执行计划：作业执行计划可以通过env.getExecutionPlan()方法获取到，格式为json格式。
         * flink提供执行计划可视化工具，可以在线搜索Flink Plan Visualizer粘贴json数据使用
         */
        System.out.println(batchEnv.getExecutionPlan());
    }
}
