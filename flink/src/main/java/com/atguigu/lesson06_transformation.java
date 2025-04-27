package com.atguigu;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.*;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.co.CoMapFunction;
import org.apache.flink.util.Collector;

import java.util.Arrays;

public class lesson06_transformation {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("")
                .setTopics("input-topic")
                .setGroupId("my-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();
        DataStreamSource<String> kafkaStream = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Source");

        //map算子
        kafkaStream.map((MapFunction<String, String>) s -> "s").returns(Types.STRING);

        //filter算子
        kafkaStream.filter((FilterFunction<String>) s -> s.length() >= 5);

        //flatMap算子：注意第二个参数是返回的
        kafkaStream.flatMap((FlatMapFunction<String, String>) (s, collector) -> Arrays.stream(s.split(" ")).forEach(collector::collect))
                .returns(Types.STRING);


        //keyBy算子：会根据key的hash值和分区数计算不同key在分区上的分布。
        //给每一条数据打上key标签。KeySelector传入每一条数据处理（String），然后返回key数据（String）
        KeyedStream<String, Integer> keyStream = kafkaStream.keyBy((KeySelector<String, Integer>) s -> s.length() >= 5 ? 1 : 2);


        //min、minBy、max、maxBy、sum算子。每一条数据都会返回一次满足条件的数据
        keyStream.min(1).print();  //会返回value值中索引为1的最小值，但是，其他字段值指挥停留在第一条数据的值上，只更新指定索引的值的最小值（仅计算指定字段最小值，其他不管）
        keyStream.minBy(1).print(); //与min不同，整条数据都会更新为指定索引最小值所对应的整条数据（把包含最小值的整条数据返回，如果存在多条重复，只返回第一条）
        keyStream.max("col1");//同min
        keyStream.maxBy("col1");//同maxBy
        keyStream.sum("col2");//根据key值加总


        //reduce:规约，需要结合keyBy使用
        keyStream.reduce((ReduceFunction<String>) (s1,s2)->s1+"-"+s2);


        //shuffle: 把流中元素随机打乱
        kafkaStream.shuffle().map((MapFunction<String, String>) s -> "s");
        //rebalance:重新平衡分区数据，但是通过轮询机制进行重分区，效率低（会跨taskManager）
        kafkaStream.rebalance();
        //rescale:也是重新平衡分区数据，但是在同一个taskManager内部重平衡，不会跨网络，效率高
        kafkaStream.rescale();


        //connect: 两个流连接到一起,只能两个流连接在一起，两个流可以类型不一样
        DataStreamSource<String> socketStream = env.socketTextStream("localhost", 9999);
        ConnectedStreams<String, String> connectStream = kafkaStream.connect(socketStream);
        connectStream.map(new CoMapFunction<String, String, String>() {
            @Override
            public String map1(String value) throws Exception {  //处理第一个流数据
                return null;
            }

            @Override
            public String map2(String value) throws Exception { //处理第二个流数据
                return null;
            }
        });


        //union：也是两个流合并在一起，可以合并多个流，但必须类型都一致
        DataStream<String> unionStream = kafkaStream.union(socketStream);


        //process:  对流的每个元素进行处理
        kafkaStream.process(new ProcessFunction<String, Integer>() {
            //对于多个并行度，每个并行度中都有一个sum，各自计算自己的sum，如果要累加需要将并行度设为1。
            int sum=0;
            @Override
            public void processElement(String value,   //需要处理的元素
                                       ProcessFunction<String, Integer>.Context ctx,  //上下文
                                       Collector<Integer> out   //输出集合
            ) throws Exception {
                sum += value.length();
                out.collect(sum);
            }
        });

        //KeyedProcess: 对keyBy中的组进行元素处理
        kafkaStream.keyBy("").process(new KeyedProcessFunction<Tuple, String, Integer>() {
            @Override
            public void processElement(String value,  //对每个组中的元素进行处理
                                       KeyedProcessFunction<Tuple, String, Integer>.Context ctx,
                                       Collector<Integer> out) throws Exception {
            }
        }).returns(Types.INT);



        //rich相关算子：RichMapFunction，RichFilterFunction,RichFlatMapFunction
        kafkaStream.map(new RichMapFunction<String, String>() {  //相当于mapPartition

            //程序开始时打开会话，对于数据库连接有用
            @Override
            public void open(OpenContext openContext) throws Exception {
                super.open(openContext);
            }

            @Override
            public String map(String value) throws Exception {
                return null;
            }

            //程序关闭时打开会话，对于数据库连接有用
            @Override
            public void close() throws Exception {
                super.close();
            }
        });
    }
}
