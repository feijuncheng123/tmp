package com.atguigu;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.Arrays;

public class lesson12_side_output {
    public static void main(String[] args) {
        /**
         * 侧输出流：
         * 1、作为处理迟到数据的流使用，见watermark
         * 2、作为分流使用
         */

        StreamExecutionEnvironment streamEnv = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> socketTextStream = streamEnv.socketTextStream("localhost", 9999);

        SingleOutputStreamOperator<String> stream = socketTextStream
                .flatMap((FlatMapFunction<String, String>) (in, out) -> Arrays.stream(in.split(" ")).forEach(out::collect));

        SingleOutputStreamOperator<String> process = stream.process(new ProcessFunction<String, String>() {
            @Override
            public void processElement(String value, ProcessFunction<String, String>.Context ctx, Collector<String> out) throws Exception {
                if (value.length() % 2 == 0) out.collect(value);
                else ctx.output(new OutputTag<String>("odd") {}, value);
            }
        });

        process.getSideOutput(new OutputTag<String>("odd"){}).print();//获取分流



    }
}
