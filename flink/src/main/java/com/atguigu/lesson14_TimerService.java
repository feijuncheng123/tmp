package com.atguigu;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.Arrays;

public class lesson14_TimerService {
    public static void main(String[] args) {
        /**
         * 定时器（timerService）
         * 用于基于处理时间或者事件时间处理过一个元素之后, 注册一个定时器, 然后指定的时间执行
         * 必须和keyedBy结合使用
         *
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

        stream.keyBy((KeySelector<String, String>) s -> s.substring(0, 10))
                .process(new KeyedProcessFunction<String, String, Long>() {
                    @Override
                    public void processElement(String value, KeyedProcessFunction<String, String, Long>.Context ctx, Collector<Long> out) throws Exception {
                        long ts=System.currentTimeMillis();
                        if(value.length()>100){   //根据条件注册定时器
                            ctx.timerService().registerProcessingTimeTimer(ts+5000);  //当前时间5s后触发执行onTimer方法，ctx是全局执行的
                        }
                        if(value.substring(0,3).equals("aaa"))ctx.timerService().deleteProcessingTimeTimer(ts+5000);  //ctx是全局执行的,也就是每个slot都会执行删除，所以要小心使用

                    }

                    /**
                     *当定时器触发时执行该方法
                     * @param timestamp The timestamp of the firing timer.
                     * @param ctx An {@link OnTimerContext} that allows querying the timestamp, the {@link
                     *     TimeDomain}, and the key of the firing timer and getting a {@link TimerService} for
                     *     registering timers and querying the time. The context is only valid during the invocation
                     *     of this method, do not store it.
                     * @param out The collector for returning result values.
                     * @throws Exception
                     */
                    @Override
                    public void onTimer(long timestamp, KeyedProcessFunction<String, String, Long>.OnTimerContext ctx, Collector<Long> out) throws Exception {
//                        super.onTimer(timestamp, ctx, out);
                        out.collect(1000L);
                    }
                });


        /**
         * 使用事件事件必须增加waterMark
         * 对于乱序数据，由于当前事件事件已经增长，低于当前事件时间的定时器无法触发
         */
        stream.keyBy((KeySelector<String, String>) s -> s.substring(0, 10))
                .process(new KeyedProcessFunction<String, String, Long>() {
                    @Override
                    public void processElement(String value, KeyedProcessFunction<String, String, Long>.Context ctx, Collector<Long> out) throws Exception {

                        if(value.length()>100){   //根据条件注册定时器
                            ctx.timerService().registerEventTimeTimer(ctx.timestamp()+5000);  //事件时间5s后触发执行onTimer方法，事件时间增长需要新数据进入更新事件时间，ctx是全局执行的
                        }
                        if(value.substring(0,3).equals("aaa"))ctx.timerService().deleteEventTimeTimer(ctx.timestamp()+5000);  //ctx是全局执行的,也就是每个slot都会执行删除，所以要小心使用

                    }

                    /**
                     *当定时器触发时执行该方法
                     * @param timestamp The timestamp of the firing timer.
                     * @param ctx An {@link OnTimerContext} that allows querying the timestamp, the {@link
                     *     TimeDomain}, and the key of the firing timer and getting a {@link TimerService} for
                     *     registering timers and querying the time. The context is only valid during the invocation
                     *     of this method, do not store it.
                     * @param out The collector for returning result values.
                     * @throws Exception
                     */
                    @Override
                    public void onTimer(long timestamp, KeyedProcessFunction<String, String, Long>.OnTimerContext ctx, Collector<Long> out) throws Exception {
//                        super.onTimer(timestamp, ctx, out);
                        out.collect(1000L);
                    }
                });

    }
}
