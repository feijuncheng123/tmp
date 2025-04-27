package com.atguigu;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.TopicSelector;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.connectors.kafka.partitioner.FlinkFixedPartitioner;

import java.util.Properties;

public class lesson07_sink {
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


        //kafka sink
        KafkaSink<String> kafkaSink = KafkaSink.<String>builder()
                .setBootstrapServers("127.0.0.1:2181")
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("topic-name")
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build()
                )
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();

        kafkaStream.map((MapFunction<String, String>) s -> "s" + s).returns(Types.STRING)
                .sinkTo(kafkaSink);


        //kafka序列化器
        SerializationSchema<DataPojo> serializationSchema = new SerializationSchema<DataPojo>() {
            @Override
            public void open(InitializationContext context) throws Exception {
                SerializationSchema.super.open(context);
            }

            @Override
            public byte[] serialize(DataPojo element) {
                return new byte[0];
            }
        };

        KafkaRecordSerializationSchema shanghai = KafkaRecordSerializationSchema.<DataPojo>builder()
                .setTopicSelector(new TopicSelector<DataPojo>() {
                    @Override
                    public String apply(DataPojo dataPojo) {
                        if (dataPojo.city.equals("shanghai"))
                            return "topic_shanghai";
                        else return "topic_other";
                    }
                })
                .setValueSerializationSchema(serializationSchema)
                .setKeySerializationSchema(serializationSchema)
                .setPartitioner(new FlinkFixedPartitioner())
                .build();



    }
}
