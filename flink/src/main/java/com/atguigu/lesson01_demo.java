package com.atguigu;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.io.CsvReader;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;


import java.util.Arrays;


public class lesson01_demo {
    public static void main(String[] args) throws Exception {

        /**
         * 批处理环境
         */
        ExecutionEnvironment batchEnv = ExecutionEnvironment.getExecutionEnvironment();
        //批处理执行环境，不能使用create环境，因为无法自动识别当前环境
        batchEnv.setParallelism(5);  //可以设定流的并行度，也可以给每个算子单独设定并行度
        //读取文本数据。按照一行一行读取数据流
//        CsvReader csvReader = env.readCsvFile("");
        DataSource<String> dataSrc = batchEnv.readTextFile("");


        dataSrc.flatMap(new FlatMapFunction<String, String>() {
                    @Override
                    public void flatMap(String s, Collector<String> collector) throws Exception {
                        Arrays.stream(s.split(" ")).forEach(collector::collect);
                    }
                }).map((MapFunction<String, Tuple2<String, Integer>>) value -> Tuple2.of(value, 1))
                .returns(Types.TUPLE(Types.STRING, Types.INT))  //由于map中的lambda表达式返回值Tuple2使用了泛型，需要显式指定返回的类型，否则报错
                .groupBy(0).sum(1).print();  //批处理才有groupBy

        batchEnv.execute();

        /**
         * 有界流处理环境
         */
        StreamExecutionEnvironment streamEnv = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> streamSrc = streamEnv.readTextFile("");
        streamSrc.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public void flatMap(String s, Collector<String> collector) throws Exception {
                Arrays.stream(s.split(" ")).forEach(collector::collect);
            }
        }).map((MapFunction<String, Tuple2<String, Integer>>) value -> Tuple2.of(value, 1))
                .returns(Types.TUPLE(Types.STRING, Types.INT))  //由于map中的lambda表达式返回值Tuple2使用了泛型，需要显式指定返回的类型，否则报错
                .keyBy(new KeySelector<Tuple2<String, Integer>, String>() {//keyBy不会改变数据，只会为每一行输入打上一个key标签
                    @Override
                    public String getKey(Tuple2<String, Integer> value) throws Exception {
                        return value.f0;
                    }
                }).sum(1).print();  //sum是同一个key下面的value值的索引1的元素
        streamEnv.execute();


        /**
         * 无界流
         */
        DataStreamSource<String> unboundedDataSrc = streamEnv.socketTextStream("localhost", 9999);  //监听哪一台服务器的端口
        unboundedDataSrc.flatMap(new FlatMapFunction<String, String>() {
                    @Override
                    public void flatMap(String s, Collector<String> collector) throws Exception {
                        Arrays.stream(s.split(" ")).forEach(collector::collect);
                    }
                }).map((MapFunction<String, Tuple2<String, Integer>>) value -> Tuple2.of(value, 1)).setParallelism(5) //设置map的并行度
                .returns(Types.TUPLE(Types.STRING, Types.INT))  //由于map中的lambda表达式返回值Tuple2使用了泛型，需要显式指定返回的类型，否则报错
                .keyBy(new KeySelector<Tuple2<String, Integer>, String>() {//keyBy不会改变数据，只会为每一行输入打上一个key标签
                    @Override
                    public String getKey(Tuple2<String, Integer> value) throws Exception {
                        return value.f0;
                    }
                }).sum(1).print();



    }

}
