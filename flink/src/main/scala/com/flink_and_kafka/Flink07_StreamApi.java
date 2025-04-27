package com.flink_and_kafka;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.co.CoMapFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.ArrayList;

/**
 * 常用算子：
 * map:输入一个元素，然后返回一个元素，中间可以做一些清洗转换等操作
 * flatmap:输入一个元素，可以返回零个，一个或者多个元素
 * filter:过滤函数，对传入的数据进行判断，符合条件的数据会被留下
 * keyBy:根据指定的key进行分组，相同key的数据会进入同一个分区【典型用法见备注】
 * reduce:对数据进行聚合操作，结合当前元素和上一次reduce返回的值进行聚合操作，然后返回一个新的值  aggregations:sum(),min(),max()等
 * window:在后面单独详解
 * Union:合并多个流，新的流会包含所有流中的数据，但是union是一个限制，就是所有合并的流类型必须是一致的。
 * Connect:和union类似，但是只能连接两个流，两个流的数据类型可以不同，会对两个流中的数据应用不同的处理方法。  CoMap, CoFlatMap:在ConnectedStreams中需要使用这种函数，类似于map和flatmap
 * Split(已被移除，使用process低级api):根据规则把一个数据流切分为多个流,每个流只能被切分一次，切分后的流不能在被切分，需要和select配合使用
 * Select:和split配合使用，选择切分后的流(process使用getSideOutput)
 */
public class Flink07_StreamApi {
    void initDemo() throws Exception {
        //创建一个流计算的运行环境
        StreamExecutionEnvironment senv = StreamExecutionEnvironment.getExecutionEnvironment();

        //创建集合
        ArrayList<Integer> data = new ArrayList<>();
        data.add(10);
        data.add(20);
        data.add(30);
        data.add(40);

        //创建数据源Source
        DataStreamSource<Integer> source = senv.fromCollection(data);

        //执行Transformation的操作，每个数字加1
        DataStream<Integer> result = source.map(new MapFunction<Integer, Integer>() {
            public Integer map(Integer value) throws Exception {
                return value + 1;
            }
        });

        //输出
        result.print().setParallelism(2);


        System.out.println("*******************connect*******************");
        DataStreamSource<Integer>  source2=senv.fromElements(1,2,3,4,5);
        ConnectedStreams<Integer, Integer> connect = source.connect(source2);

        connect.map(new CoMapFunction<Integer, Integer, String>() {
            @Override
            public String map1(Integer value) throws Exception {    //第一个流的处理
                return String.valueOf(value);
            }

            @Override
            public String map2(Integer value) throws Exception {    //第二个流的处理
                return  String.valueOf(value);
            }
        });


        System.out.println("*******************split*******************");
        OutputTag<String> o1=new OutputTag<>("even");
        OutputTag<String> o2=new OutputTag<>("odd");

        SingleOutputStreamOperator<String> processSplit = source.process(new ProcessFunction<Integer, String>() {
            @Override
            public void processElement(Integer value, Context ctx, Collector<String> out) throws Exception {
                if (value % 2 == 0) ctx.output(o1, String.valueOf(value));
                else ctx.output(o2, String.valueOf(value));
            }
        });

        SideOutputDataStream<String> even = processSplit.getSideOutput(o1);



        senv.execute("StreamingFromCollection");
    }
}
