package com.atguigu;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.*;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

import static org.apache.flink.table.api.Expressions.$;

public class lesson19_sql {
    public static void main(String[] args) throws Exception {
        /**
         * 动态表：
         * flink中流数据通过窗口生成的表，随着时间流动数据是变化的
         * 连续查询(Continuous Query)：
         * 查询动态表产生一个连续查询，即一个查询启动后，会持续执行，同时生成另一个动态表
         *
         *
         */
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("")
                .setTopics("input-topic")
                .setGroupId("my-group")
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


        // 1. 创建表的执行环境
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        // 2. 创建表: 将流转换成动态表. 表的字段名从pojo的属性名自动抽取
        Table table = tableEnv.<DataPojo>fromDataStream(dataPojoStream);

        // 3. 对动态表进行查询
        Table resultTable = table
                .where($("id").isEqual("sensor_1"))
                .select($("id"), $("ts"), $("vc"));

        // 4. 聚合查询
        Table t1 = table
                .where($("vc").isGreaterOrEqual(20))
                .groupBy($("id"))
                .aggregate($("vc").sum().as("vc_sum"))
                .select($("id"), $("vc_sum"));


        // 5. 把动态表转换成流
        DataStream<Row> resultStream = tableEnv.toAppendStream(resultTable, Row.class);

        //注册为sql表
        tableEnv.createTemporaryView("InputTable", table);
        Table t2 = tableEnv.sqlQuery("SELECT UPPER(f0) FROM InputTable");  //sql查询
        t2.execute().print();//阻塞方法，一般做测试用
        /**
         * 表转换为流时有三种类型：
         * 1、Append-only流：仅通过insert插入的表转换的流就是Append-only流
         * 2、Retract流：包含两种类型的元素：add messages 和 retract messages。
         * 通过将INSERT 操作编码为 add message、
         * 将 DELETE 操作编码为 retract message、
         * 将 UPDATE 操作编码为更新(先前)行的 retract message 和更新(新)行的 add message，将动态表转换为 retract 流
         *
         * 3、Upsert流：包含两种类型的 message： upsert messages 和delete messages
         * 转换为 upsert 流的动态表需要(可能是组合的)唯一键
         * 。通过将 INSERT 和 UPDATE 操作编码为 upsert message，
         * 将 DELETE 操作编码为 delete message ，将具有唯一键的动态表转换为流
         * 消费流的算子需要知道唯一键的属性，以便正确地应用 message
         * 与 retract 流的主要区别在于 UPDATE 操作是用单个 message 编码的，因此效率更高。
         *
         * 注意：在将动态表转换为 DataStream 时，只支持 append 流和 retract 流。
         */


        //Retract流：toRetractStream要被废弃，转为toChangelogStream
        DataStream<Row> changelogStream = tableEnv.toChangelogStream(t2, Schema.newBuilder()
                .column("id", DataTypes.BIGINT())  //表第一列时id
                .column("DataPojo", DataTypes.of(DataPojo.class)) //第二列是DataPojo类型
                .build());

        //Append-only流:同样toAppendStream要被废弃
        DataStream<DataPojo> dataStream = tableEnv
                .<DataPojo>toDataStream(t2, DataTypes.of(DataPojo.class))//列类容和pojo字段对应
                .filter((FilterFunction<DataPojo>) t -> t.city.equals(""));


        /**
         * table读写文件：写入文件不能写变化流
         * Connector类型：见
         * https://nightlies.apache.org/flink/flink-docs-release-1.20/zh/docs/connectors/table/overview/
         * 每种类型的连接器参数
         *
         * format类型：见
         * https://nightlies.apache.org/flink/flink-docs-release-1.20/zh/docs/connectors/table/formats/overview/
         * 每种类型的Format参数
         */
        final Schema schema = Schema.newBuilder()
                .column("a", DataTypes.INT())
                .column("b", DataTypes.STRING())
                .column("c", DataTypes.BIGINT())
                .build();

        tableEnv.createTable("table-name",
                TableDescriptor.forConnector("filesystem")     //名称从org.apache.flink.table.factories的继承类中找
                        .schema(schema)
                        .option("path", "/path/to/file")
                        .format(FormatDescriptor.forFormat("csv")  //format见https://nightlies.apache.org/flink/flink-docs-release-1.20/zh/docs/connectors/table/formats/overview/
                                .option("field-delimiter", "|")
                                .build()
                        ).build());

        Table t3 = tableEnv.from("table-name");

        t3.executeInsert("table-name");  //写入到文件中，默认不会覆盖写入

        //也可以覆盖写入
        t3.insertInto(TableDescriptor.forConnector("filesystem").build(),true ).execute();  //指定到具体位置，是否覆盖




        env.execute();

    }
}
