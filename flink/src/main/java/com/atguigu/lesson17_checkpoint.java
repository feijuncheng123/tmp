package com.atguigu;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.CheckpointingOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.ExternalizedCheckpointRetention;
import org.apache.flink.configuration.StateBackendOptions;

import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.core.execution.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.contrib.streaming.state.RocksDBStateBackend;

import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

public class lesson17_checkpoint {
    public static void main(String[] args) throws Exception {
        /**
         * checkpoint：
         * 1、flink的checkpoint基于异步 barrier 快照算法 实现
         * 2、flink通过在数据流中插入barrier（栅栏）印记（类似于watermark），每个barrier会将数据切分为前后快照，在达到印记时保存checkpoint。
         * 3、每个barrier携带着快照的id. barrier 不会暂停数据的流动
         * 4、在流中, 同一时间可以有来源于多个不同快照的多个barrier, 这个意味着可以并发的出现不同的快照.
         *
         * checkpoint制作流程：
         * 1、每个需要 checkpoint 的应用在启动时，Flink 的 JobManager 为其创 建一个 CheckpointCoordinator ，CheckpointCoordinator 全权负责本应用 的快照制作。
         * 2、Checkpoint Coordinator 向所有 source 节点触发Checkpoint行为，然后这些source节点在数据流中安插 CheckPoint barrier
         * 3、source 节点向下游广播 barrier，下游的 task 只有收到所有input（多个节点或多个流） 的 barrier 才 会执行相应的 Checkpoint行为（state备份）
         * 4、当 task 完成 state 备份后，会将备份数据的地址（state handle） 通知给 Checkpoint coordinator。
         * 5、下游的 sink 节点收集齐上游两个 input 的 barrier 之后，会执行本地快照
         * 6、sink 节点在完成自己的 Checkpoint 之后，会将 state handle 返回通知 Coordinator。
         * 7、最后，当 Checkpoint coordinator 收集齐所有 task 的 state handle，就认为这一次的 Checkpoint 全局完成了，向持久化存储中再备份一个 Checkpoint meta 文件。
         *
         * barrier对齐与不对齐
         * 1、不同节点source的输入因倾斜度或者速度等原因会导致barrier切片不一致（可能一个节点处理到下一切片而另一个节点还在处理上一切片）
         * 2、如果某个算子不对齐，那么错开的切片可能部分数据已计算处理存入state，而部分数据还在处理，如果此时宕机，Checkpoint会让该切片的全部数据重新处理，会导致重复计算，只能保证至少一次计算。
         * 3、而如果对齐，会等待另一节点统一切片数据处理完毕才再存入Checkpoint中，即使宕机重启，全部重新处理也不会导致重复计算，从而保证严格一次
         *
         * 端到端严格一次：Kafka+Flink+Kafka
         * 1、内部 —— 利用checkpoint机制，把状态存盘，发生故障的时候可以恢复，保证部的状态一致性
         * 2、source —— kafka consumer作为source，可以将偏移量保存下来，如果后续任务出现了故障，恢复的时候可以由连接器重置偏移量，重新消费数据，保证一致性
         * 3、sink —— kafka producer作为sink，采用两阶段提交 sink，需要实现一个 TwoPhaseCommitSinkFunction（每批切片事务性提交）
         *
         *  kafka消费具有隔离级别isolation-level , 默认连未提交的事务消息也会被消费。在严格一次时未提交事务消息可能被回滚
         *
         * 从checkpoint中恢复命令：在命令行中增加： -s checkpoint路劲
         * /opt/flink-1.20.0/bin/flink run-application  -t yarn-application -m localhost:8088 -Dyarn.application.queue=production -c com.flinktest.Lesson1 -s checkpoint路劲 /mnt/h/java-project/flink-test/target/flink-test-1.0-SNAPSHOT.jar
         *
         *
         * savepoint:手动触发存储checkpoint
         *     Savepoint                                                              Checkpoint
         * Savepoint是由命令触发, 由用户创建和删除                                  Checkpoint被保存在用户指定的外部路径中, flink自动触发
         * 保存点存储在标准格式存储中，并且可以升级作业版本并可以更改其配置。              当作业失败或被取消时，将保留外部存储的检查点。
         * 用户必须提供用于还原作业状态的保存点的路径。	                              用户必须提供用于还原作业状态的检查点的路径。 如果是flink的自动重启, 则flink会自动找到最后一个完整的状态
         *
         * savepoint手动触发命令：
         * bin/flink savepoint jobid hdfs://hadoop162:8020/savepoint
         *
         *
         */

        //端到端严格一次代码
        System.setProperty("HADOOP_USER_NAME", "hadoop");

        Configuration config = new Configuration();
        config.set(StateBackendOptions.STATE_BACKEND, "rocksdb");
        config.set(CheckpointingOptions.CHECKPOINTS_DIRECTORY, "hdfs://hadoop162:8020/flink/checkpoints/rocksdb");

        // 高级选项：严格一次（默认值就是）。这是新版本的写法
        config.set(CheckpointingOptions.CHECKPOINTING_CONSISTENCY_MODE, CheckpointingMode.EXACTLY_ONCE);

        //用于指定checkpoint coordinator上一个checkpoint完成之后最小等多久可以出发另一个checkpoint，当指定这个参数时，maxConcurrentCheckpoints的值为1
        config.set(CheckpointingOptions.MIN_PAUSE_BETWEEN_CHECKPOINTS, Duration.ofMillis(500));

        // Checkpoint 必须在一分钟内完成，否则就会被抛弃
        config.set(CheckpointingOptions.CHECKPOINTING_TIMEOUT, Duration.ofMillis(60000));

        // 同一时间只允许一个 checkpoint 进行
        config.set(CheckpointingOptions.MAX_CONCURRENT_CHECKPOINTS, 1);


        //将保留多少个外部化检查点。外部检查点是指保留在hdfs之类外部系统中的检查点，而不是本地状态。默认外部检查点被禁用。CHECKPOINTS_DIRECTORY设置外部检查点
        config.set(CheckpointingOptions.EXTERNALIZED_CHECKPOINT_RETENTION, ExternalizedCheckpointRetention.RETAIN_ON_CANCELLATION);


        //checkpoint间隔的基础设置。env.enableCheckpointing设置就是该配置，
        // 但会受到MAX_CONCURRENT_CHECKPOINTS，MIN_PAUSE_BETWEEN_CHECKPOINTS，CHECKPOINTING_INTERVAL_DURING_BACKLOG的影响而延迟。
        config.set(CheckpointingOptions.CHECKPOINTING_INTERVAL,Duration.ofMillis(100));



        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .createLocalEnvironmentWithWebUI(config)
                .setParallelism(3);

        // 每 1000ms 开始一次 checkpoint。设置CheckpointingOptions.CHECKPOINTING_INTERVAL
        env.enableCheckpointing(1000);

        // 设置模式为精确一次 (这是默认值)，旧版本的写法
//        env.getCheckpointConfig().setCheckpointingMode(org.apache.flink.streaming.api.CheckpointingMode.EXACTLY_ONCE);


        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("")
                .setTopics("input-topic")
                .setGroupId("my-group")
                .setProperty("isolation.level","read_committed")  //kafka消费需要加上该配置，只消费已提交数据，事务消息未提交则不消费
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();
        DataStreamSource<String> kafkaStream = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Source");

        SingleOutputStreamOperator<DataPojo> dataPojoStream = kafkaStream
                .map(new RichMapFunction<String, DataPojo>() {
                    JsonMapper mapper = JsonMapper.builder().configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true).build();

                    @Override
                    public DataPojo map(String value) throws Exception {
                        return mapper.readValue(value, DataPojo.class);
                    }
                });

        //编写逻辑
        //....................


        Properties producerProperties = new Properties();
        // 事务超时时间设置为1分钟
        producerProperties.put("transaction.timeout.ms", "60000");

        KafkaSink<String> kafkaSink = KafkaSink.<String>builder()
                .setBootstrapServers("127.0.0.1:2181")
                .setKafkaProducerConfig(producerProperties)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("topic-name")
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build()
                )
                .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)  //严格一次
                .build();

        kafkaStream.map((MapFunction<String, String>) s -> "s" + s).returns(Types.STRING)
                .sinkTo(kafkaSink);
        env.execute("myJob");

    }
}
