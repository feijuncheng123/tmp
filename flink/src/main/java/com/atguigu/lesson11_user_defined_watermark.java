package com.atguigu;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.flink.api.common.eventtime.*;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class lesson11_user_defined_watermark {
    public static void main(String[] args) {
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

        //使用自定义waterMark需要使用new WatermarkStrategy()
        dataPojoStream
                .assignTimestampsAndWatermarks(
                        ((WatermarkStrategy<DataPojo>) context -> new MyWaterMark())
                                .withTimestampAssigner((ele, ts)-> ele.getPopulation()))
                .keyBy(DataPojo::getCity);

    }

    static class MyWaterMark implements WatermarkGenerator<DataPojo> {

        /**
         * 3000设定迟到时间，该值表示当前事件时间，会根据每条数据更新，因为时间戳从1970年开始，异常数据有可能会导致数值溢出，需要用Long.MIN_VALUE来参与校正，保证计算正确
         */
        long maxTimestamp=Long.MIN_VALUE + 3000 + 1;

        /**
         * 每来一条数据执行一次该函数，根据事件时间更新“”
         * @param event
         * @param eventTimestamp
         * @param output
         */
        @Override
        public void onEvent(DataPojo event, long eventTimestamp, WatermarkOutput output) {
            maxTimestamp = Math.max(maxTimestamp, eventTimestamp);
        }

        /**
         * pipeline.auto-watermark-interval参数设定该值，默认200毫秒执行一次该方法
         * @param output
         */
        @Override
        public void onPeriodicEmit(WatermarkOutput output) {
            output.emitWatermark(new Watermark(maxTimestamp - 3000));    //该输出也可以放到onEvent方法中，但此处时200ms更新一次，放到onEvent是每条数据才执行
        }
    }

}
