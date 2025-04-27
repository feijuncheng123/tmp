package com.atguigu;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.Arrays;

public class lesson13_process_function {
    public static void main(String[] args) {

        /**
         * Process相关函数可以访问watermark
         */

        StreamExecutionEnvironment streamEnv = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> socketTextStream = streamEnv.socketTextStream("localhost", 9999);

        SingleOutputStreamOperator<String> stream = socketTextStream
                .flatMap((FlatMapFunction<String, String>) (in, out) -> Arrays.stream(in.split(" ")).forEach(out::collect))
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy
                                .<String>forBoundedOutOfOrderness(Duration.ofMinutes(1))
                                .withTimestampAssigner(new SerializableTimestampAssigner<String>() {
                                    @Override
                                    public long extractTimestamp(String element, long recordTimestamp) {
                                        return 0;
                                    }
                                })

                );


        //process函数：
        stream.process(new ProcessFunction<String, String>() {
            @Override
            public void processElement(String value, ProcessFunction<String, String>.Context ctx, Collector<String> out) throws Exception {
                ctx.timestamp();
            }
        });


        //keyedProcessFunction
        stream.keyBy(new KeySelector<String, String>() {
            @Override
            public String getKey(String value) throws Exception {
                return value.substring(0,3);
            }
        }).process(new KeyedProcessFunction<String, String, Long>() {
            @Override
            public void processElement(String value, KeyedProcessFunction<String, String, Long>.Context ctx, Collector<Long> out) throws Exception {
                ctx.timerService().currentWatermark();
            }
        });


        //CoProcessFunction
        DataStreamSource<String> socketTextStream1 = streamEnv.socketTextStream("", 9999);
        ConnectedStreams<String, String> connectStream = socketTextStream1.connect(stream);

        connectStream.keyBy((KeySelector<String, String>) s->s.substring(0,3),(KeySelector<String, String>) s->s.substring(0,3))
                .process(new CoProcessFunction<String, String, Long>() {
                    @Override
                    public void processElement1(String value, CoProcessFunction<String, String, Long>.Context ctx, Collector<Long> out) throws Exception {

                    }

                    @Override
                    public void processElement2(String value, CoProcessFunction<String, String, Long>.Context ctx, Collector<Long> out) throws Exception {

                    }
                });


        //ProcessJoinFunction
        socketTextStream1
                .join(stream)
                .where(s->s.substring(0,3))
                .equalTo(s->s.substring(0,3))
                .window(TumblingProcessingTimeWindows.of(Duration.ofSeconds(5))) // 必须使用窗口
                .apply(new JoinFunction<String, String, Long>() {
                    @Override
                    public Long join(String first, String second) throws Exception {
                        return null;
                    }
                });



        //BroadcastProcessFunction  之后讲解
        //KeyedBroadcastProcessFunction 之后讲解
        //ProcessWindowFunction  窗口keyedBy后使用
        //ProcessAllWindowFunction  全局窗口使用


    }
}
