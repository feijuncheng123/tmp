package com.atguigu;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.time.Duration;

public class lesson10_watermark {
    public static void main(String[] args) throws Exception {

        /**
         * 时间分类：
         * 一、处理时间：每条数据进入flink开始处理的时间
         * 二、事件时间：每条数据产生的时间，数据中携带时间戳
         *
         * watermark：
         * 事件时间通过watermark来进行窗口操作。
         * 一个Watermark(t)表示在这个流里面事件时间已经到了时间t, 意味着此时, 流中不应该存在这样的数据: 他的时间戳t2<=t (时间比较旧或者等于时间戳)
         * 即根据数据的流入，按照数据中的时间戳更新“当前时间”，默认不会存在在“当前时间”之前的事件数据。除非乱序，对于乱序，需要设定迟到时间，超过迟到时间到达的乱序数据，丢弃或者单独处理
         *
         * watermark与并行度（parallelism）
         * 多分区执行的情况下，每个分区都维护有自己的waterMark，但在向下游传递 watermark时，总是以最小的watermark为准（木桶原理）。watermark不对数据进行过滤，只给数据打上标记
         * keyedBy窗口会根据最小waterMark关闭。
         *
         * 侧输出流：
         * 1、专门处理迟到数据的流.可以在windows方法后配置sideOutputLateData算子为迟到数据打上标签，然后通过getSideOutput获取迟到流
         * 2、侧输出流还可以作为分流使用
         *
         */
        StreamExecutionEnvironment streamEnv = StreamExecutionEnvironment.getExecutionEnvironment();

        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("")
                .setTopics("input-topic")
                .setGroupId("my-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();
        DataStreamSource<String> kafkaStream = streamEnv.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Source");

        SingleOutputStreamOperator<DataPojo> dataPojoStream = kafkaStream
                .map(new RichMapFunction<String, DataPojo>() {
                    JsonMapper mapper = JsonMapper.builder().configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true).build();

                    @Override
                    public DataPojo map(String value) throws Exception {
                        return mapper.readValue(value, DataPojo.class);
                    }
                });

        //WatermarkStrategy.forBoundedOutOfOrderness设定最大迟到时间
        SingleOutputStreamOperator<Long> lateData = dataPojoStream.assignTimestampsAndWatermarks(
                        WatermarkStrategy
                                .<DataPojo>forBoundedOutOfOrderness(Duration.ofMinutes(5))   //必须明确指定泛型，否则提取元素时间戳时会无法识别类型
                                .withTimestampAssigner(new SerializableTimestampAssigner<DataPojo>() {  //提取元素中的时间戳
                                    @Override
                                    public long extractTimestamp(DataPojo element,
                                                                 long recordTimestamp   //该参数没用，是指本次处理之前打上的时间戳（两次使用waterMark）
                                    ) {
                                        return 0;
                                    }
                                })
                                .withIdleness(Duration.ofSeconds(10)) //该参数在多分区时使用，如果一个分区数据空闲超过10秒就以其他分区的waterMark为准向下传递
                )
                .keyBy(DataPojo::getCity)
                .window(TumblingEventTimeWindows.of(Duration.ofSeconds(10))) //窗口时间，当窗口开始时间大于waterMark的迟到时间，窗口就会关闭
                .allowedLateness(Duration.ofSeconds(12))  //在watermark设定的基础上，窗口也可以允许迟到，此方法设置窗口等待时间
                .sideOutputLateData(new OutputTag<DataPojo>("late_data"){})  //侧输出流，将迟到数据统一打上late_data标签。使用OutputTag必须使用子类加泛型，否则会擦除
                .process(new ProcessWindowFunction<DataPojo, Long, String, TimeWindow>() {
                    @Override
                    public void process(String s, ProcessWindowFunction<DataPojo, Long, String, TimeWindow>.Context context, Iterable<DataPojo> elements, Collector<Long> out) throws Exception {

                    }
                });

        lateData.getSideOutput(new OutputTag<DataPojo>("late_data")).print(); //在前面打标签时必须使用泛型子类

        streamEnv.execute();
    }
}
