package com.flink_and_kafka;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.MapPartitionFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DistinctOperator;
import org.apache.flink.api.java.operators.FlatMapOperator;
import org.apache.flink.api.java.operators.MapOperator;
import org.apache.flink.api.java.operators.MapPartitionOperator;
import org.apache.flink.util.Collector;

import java.util.*;

/**
 * 批处理api
 */
public class Flink04_DataSetApi {
    public static void main(String[] args) throws Exception {
        //创建一个运行环境执行DataSet API
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        ArrayList<String> data = new ArrayList<String>();
        data.add("I love Beijing");
        data.add("I love China");
        data.add("Beijing is the capital of China");

        DataSet<String> source = env.fromCollection(data);


        System.out.println("*************map算子************");
        //把每句话的单词放入List中
        MapOperator<String, List<String>> mapList = source.map(new MapFunction<String, List<String>>() {
            public List<String> map(String value) throws Exception {
                String[] words = value.split(" ");
                //创建一个List
                List<String> result = new ArrayList<String>();
                for (String w : words) {
                    result.add(w);
                }

                return result;
            }
        });

        mapList.print();

        System.out.println("*************flatMap算子************");
        FlatMapOperator<String, String> stringStringFlatMapOperator = source.flatMap(new FlatMapFunction<String, String>() {
            public void flatMap(String value, Collector<String> out) throws Exception {
                String[] words = value.split(" ");
                for (String w : words) {
                    out.collect(w);
                }
            }
        });

        stringStringFlatMapOperator.print();

        System.out.println("*************mapPartition算子************");
        /*
         * 第一个String：每个分区中的元素类型
         * 第二个String：处理完成后的结果类型
         */
        MapPartitionOperator<String, String> stringStringMapPartitionOperator = source.mapPartition(new MapPartitionFunction<String, String>() {
            public void mapPartition(Iterable<String> values, Collector<String> out) throws Exception {
                //Iterable<String> values：代表该分区中的所有的元素
                Iterator<String> partitionData = values.iterator();
                while (partitionData.hasNext()) {
                    String data = partitionData.next();
                    //分词
                    String[] words = data.split(" ");
                    for (String w : words) {
                        out.collect(w);
                    }
                    out.collect("--------------");
                }

            }
        });
        stringStringMapPartitionOperator.print();

        System.out.println("*************distinct算子************");
        DistinctOperator<String> distinctOpt = source.flatMap(new FlatMapFunction<String, String>() {
            public void flatMap(String value, Collector<String> out) throws Exception {
                String[] words = value.split(" ");
                for (String w : words) {
                    out.collect(w);
                }
            }
        }).distinct();//直接对元素distinct
        distinctOpt.print();

        System.out.println("*************filter算子************");
        //选择单词长度大于3的单词
        stringStringFlatMapOperator.filter(new FilterFunction<String>() {
            public boolean filter(String word) throws Exception {
                return word.length() >= 3;
            }
        }).print();


        System.out.println("*************first-n算子************");
        //取出前三条数据，按照插入的顺序
        source.first(3).print();
        //分组：按照第一个字段进行分组，取出每组中的两条记录
        source.groupBy(0).first(2).print();

    }
}
