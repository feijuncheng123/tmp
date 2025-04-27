package com.flink_and_kafka;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

/**
 * 每一个算子都可以设置并行度（parallelism），即同时执行该算子的线程数（slot相当于进程）。默认并行度为当前计算机的cpu核数
 * 		（1）什么是并行度？
 * 				一个Flink的任务由多个组件组成（source+transformation+sink）。每个组件由多个并行的实例（线程）来执行
 * 				把一个组件的并行实例（线程）数目叫做该组件的并行度。
 *
 * 		（2）设置并行度的方式
 * 			（*）算子级别：.sum("count").setParallelism(4)
 * 			（*）执行环境：senv.setParallelism(2);
 * 			（*）客户端级别： bin/flink run -p 10 *****
 * 			（*）系统级别：flink-conf.yaml的参数 parallelism.default: 1
 */
public class Flink03_Stream {
    public static void main(String[] args) throws Exception {
        //创建一个DataStream的运行环境
        StreamExecutionEnvironment senv = StreamExecutionEnvironment.getExecutionEnvironment();

        senv.setParallelism(2);

        //创建一个输入流
        DataStreamSource<String> source = senv.socketTextStream("bigdata111", 1234);

        //数据 I love Beijing
        source.flatMap(new FlatMapFunction<String, WordWithCount>() {

            public void flatMap(String value, Collector<WordWithCount> out) throws Exception {
                String[] words = value.split(" ");
                for(String w:words) {
                    out.collect(new WordWithCount(w,1));
                }
            }
        }).keyBy(WordWithCount::getWord)
                //.timeWindow(size, slide)
                .timeWindow(Time.seconds(2),Time.seconds(1))  //数据累计时间窗口为2秒，触发时间窗口1秒。即每1秒计算之前两秒的累计数
                .sum("count").setParallelism(4)
                .print().setParallelism(1);

        //启动流式计算
        senv.execute("WordCountDemo with Stream");

    }
}

class WordWithCount{
    private String word;
    private int count;
    public WordWithCount() {}

    public WordWithCount(String word,int count) {
        this.word = word;
        this.count = count;
    }

    @Override
    public String toString() {
        return "WordWithCount [word=" + word + ", count=" + count + "]";
    }

    public String getWord() {
        return word;
    }
    public void setWord(String word) {
        this.word = word;
    }
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }
}
