package com.flink_and_kafka;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;

public class Flink02_Offline {
    public static void main(String[] args) throws Exception {
        //创建一个运行环境执行DataSet API
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        //构造一份处理的数据
        DataSet<String> source = env.fromElements("I love Beijing","I love China","Beijing is the capital of China");

        //Tuple2不同数据类型的集合                                                                                                                                                                                            k2       v2
        DataSet<Tuple2<String, Integer>> mapOut = source.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {

            public void flatMap(String value, Collector<Tuple2<String, Integer>> out) throws Exception {
                //数据：I love Beijing
                String[] words = value.split(" ");
                for(String w:words) {
                    out.collect(new Tuple2<String, Integer>(w,1));
                }
            }
        });

        DataSet<Tuple2<String, Integer>> reduceOut = mapOut.groupBy(0).sum(1);

        reduceOut.print();
    }
}
