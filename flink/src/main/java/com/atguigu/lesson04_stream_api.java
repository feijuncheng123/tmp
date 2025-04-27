package com.atguigu;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.connector.datagen.source.GeneratorFunction;
import org.apache.flink.connector.file.src.reader.TextLineInputFormat;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.core.fs.Path;
import org.apache.flink.formats.csv.CsvReaderFormat;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.connector.file.src.FileSource;

import java.io.File;
import java.time.Duration;


public class lesson04_stream_api {
    public static void main(String[] args) throws Exception {

        /**
         * 流处理环境
         */
        //getExecutionEnvironment可以根据当前运行的上下文直接得到正确结果（最常用的方式）
        StreamExecutionEnvironment streamEnv = StreamExecutionEnvironment.getExecutionEnvironment();

        //创建本地运行环境
        LocalStreamEnvironment localEnvironment = StreamExecutionEnvironment.createLocalEnvironment();

        //创建集群环境，但需要指定集群jobManager的host和端口以及提交的jar包
        StreamExecutionEnvironment remoteEnv = StreamExecutionEnvironment.createRemoteEnvironment("jobManager的host地址", 8081, "提交jobManager的jar包");

        /**
         * 批处理环境.自1.12版本起，已经实现流批一体，因此批环境已经很少使用
         */
        ExecutionEnvironment batchEnv = ExecutionEnvironment.getExecutionEnvironment();

        //读取文本文件.需要引入flink-connector-files包.
        //注意： 默认情况下，Source为有界/批的模式；
        FileSource<String> source = FileSource.forRecordStreamFormat(new TextLineInputFormat(), new Path("/foo/bar")).build();
        streamEnv.fromSource(source, WatermarkStrategy.noWatermarks(), "FileSource")
                .map((MapFunction<String, String>) line -> line + "aaa").writeAsCsv("").setParallelism(1);

        //设置为流模式，每隔5毫秒检查路径新文件，并读取
        FileSource<String> streamTextSource = FileSource.forRecordStreamFormat(new TextLineInputFormat(), new Path("/foo/bar"))
                .monitorContinuously(Duration.ofMillis(5))
                .build();

        //读取csv。需要引入flink-csv包
        CsvReaderFormat<DataPojo> csvFormat = CsvReaderFormat.forPojo(DataPojo.class);
        FileSource<DataPojo> csv =
                FileSource.forRecordStreamFormat(csvFormat, Path.fromLocalFile(new File(""))).monitorContinuously(Duration.ofMillis(5)).build();


        //读取kafka
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("")
                .setTopics("input-topic")
                .setGroupId("my-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();
        DataStreamSource<String> kafkaStream = streamEnv.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Source");


        //自动生成数据
        //DataGeneratorSource会自动根据设定的时间间隔生成数据，生成方式为从0自增的long数据，然后
        DataGeneratorSource<String> dataGeneratorSource = new DataGeneratorSource<>(
                new GeneratorFunction<Long, String>() {
                    @Override
                    public String map(Long value) throws Exception {
                        return "numbers:" + value;
                    }
                },
                10,  //自增的最大值
                RateLimiterStrategy.perSecond(1),  //每秒生成几个数字，比如此处每秒只生成1条数据
                Types.STRING  //返回类型
        );
        streamEnv.fromSource(dataGeneratorSource, WatermarkStrategy.noWatermarks(),"data Generator Source");

        /**
         * 出发程序执行
         */
        streamEnv.execute();


    }

}
