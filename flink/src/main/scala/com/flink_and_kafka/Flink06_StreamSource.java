package com.flink_and_kafka;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.ParallelSourceFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.windowing.time.Time;



public class Flink06_StreamSource {
    public void myNoParalleSourceDemo() throws Exception {
        //创建一个流计算的运行环境
        StreamExecutionEnvironment senv = StreamExecutionEnvironment.getExecutionEnvironment();

        //添加数据源
        DataStreamSource<Long> source = senv.addSource(new MyNoParalleSource());
		DataStream<Long> result = source.map(new MapFunction<Long, Long>() {

			public Long map(Long value) throws Exception {
				//return value + 1;
				return value;
			}
		}).timeWindowAll(Time.seconds(2)).sum(0);

        //实现filter的功能
//        DataStream<Long> result  = source.filter(new FilterFunction<Long>() {
//
//            public boolean filter(Long value) throws Exception {
//                //找出所有的偶数
//                return value % 2 == 0;
//            }
//        });


        result.print();

        senv.execute("MyNoParalleSourceDemo");
    }


    public void  myParalleSourceDemo() throws Exception {
        //创建一个流计算的运行环境
        StreamExecutionEnvironment senv = StreamExecutionEnvironment.getExecutionEnvironment();

        //添加数据源
        DataStreamSource<Long> source = senv.addSource(new MyParalleSource()).setParallelism(2);

        DataStream<Long> result = source.map(new MapFunction<Long, Long>() {

            public Long map(Long value) throws Exception {
                //return value + 1;
                return value;
            }
        }).timeWindowAll(Time.seconds(2)).sum(0);

        //每两秒的数据进行求和

        result.print();

        senv.execute("MyParalleSourceMain");
    }

}


/**
 * 创建一个并行度为1的数据源
 * 泛型表示数据源产生的数据类型
 */
class MyNoParalleSource implements SourceFunction<Long> {
    //定义一个开关
    private boolean isRunning = true;

    //定义一个计数器
    private long count = 1;


    @Override
    public void run(SourceContext<Long> ctx) throws Exception {
        //如何产生数据
        while(isRunning) {
            ctx.collect(count);
            count ++;

            //每秒产生一个数据
            Thread.sleep(1000);
        }
    }

    @Override
    public void cancel() {
        //需要停止数据源的数据源的时候
        isRunning = false;
    }
}


class MyParalleSource implements ParallelSourceFunction<Long> {

    //定义一个开关
    private boolean isRunning = true;

    //定义一个计数器
    private long count = 1;

    public void run(SourceContext<Long> ctx) throws Exception {
        //如何产生数据
        while(isRunning) {
            ctx.collect(count);
            count ++;

            //每秒产生一个数据
            Thread.sleep(1000);
        }
    }

    public void cancel() {
        this.isRunning = false;
    }
}