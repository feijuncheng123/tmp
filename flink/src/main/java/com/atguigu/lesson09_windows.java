package com.atguigu;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.functions.RichAggregateFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.*;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;

public class lesson09_windows {
    public static void main(String[] args) {
        /**
         * 注意：与spark窗口不同，flink窗口即使没有到触发点，每进入一条数据都在内部进行了计算缓存，只是在触发点时才将结果返回（增量计算），可以结合print测试。（spark是触发点时才去消费拉取数据统一计算）
         *
         * 窗口分类：
         * 一、基于时间的窗口(时间驱动）
         * 1.1 滚动窗口：固定时间间隔，对该间隔内的事件统一处理（每一个事件只能属于一个窗口）
         * 1.2 滑动窗口：包含滑动窗口和滑动步长，一个窗口可能包含多个步长，逐个步长往后滑动（滑动平均数）
         * 1.3 会话窗口：
         * 对于数据间断性达到场景使用，设置一个空闲期作为划分会话的启动关闭标准，超过空闲期关闭当前会话，新数据来时开启新会话
         * 会话窗口算子需要合并触发器和合并窗口函数
         * 二、基于元素个数的(数据驱动)
         * 2.1 滚动窗口
         * 2.2 滑动窗口
         *
         * 三、全局窗口：该窗口需要指定自定义的触发器才会执行
         */

        StreamExecutionEnvironment streamEnv = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> socketTextStream = streamEnv.socketTextStream("", 9999);

        KeyedStream<String, Integer> keyedStream = socketTextStream
                .flatMap((FlatMapFunction<String, String>) (value, out) -> Arrays.stream(value.split("")).forEach(out::collect))
                .keyBy((KeySelector<String, Integer>) s->s.length());

        //keyedBy全局窗口
        keyedStream.window(GlobalWindows.create());

        //滚动窗口
        keyedStream
                .window(TumblingProcessingTimeWindows.of(Duration.ofSeconds(5))) //5秒滚动窗口,每5秒触发一次。各自的key计算各自的窗口
                .process(new ProcessWindowFunction<String, String, Integer, TimeWindow>() {
                    @Override
                    public void process(Integer key,
                                        ProcessWindowFunction<String, String, Integer, TimeWindow>.Context context,  //包含了窗口开始结束时间，定时服务器等
                                        Iterable<String> elements,
                                        Collector<String> out
                    ) throws Exception {
                        System.out.println(context.window().getEnd());
                        System.out.println(context.window().getStart());
                        System.out.println(context.window().intersects(new TimeWindow(10,20)));
                    }
                });

        //滑动窗口
        keyedStream.window(SlidingProcessingTimeWindows.of(Duration.ofSeconds(5),Duration.ofSeconds(1)));

        //会话窗口
        keyedStream.window(ProcessingTimeSessionWindows.withGap(Duration.ofSeconds(10)));


        //数量滚动窗口
        keyedStream.countWindow(10);
        //数量滑动窗口
        keyedStream.countWindow(10,2);


        //reduce聚合：
        keyedStream
                .window(ProcessingTimeSessionWindows.withGap(Duration.ofSeconds(10)))
                .reduce(new ReduceFunction<String>() {
                    @Override
                    public String reduce(String value1, String value2) throws Exception {
                        System.out.println(value1 + ", " + value2);   //每来一条都会打印
                        return value1 + ", " + value2;   //到窗口触发点时才会返回最终结果
                    }
                });

        //也可以结合windowFunction使用
        keyedStream
                .window(SlidingProcessingTimeWindows.of(Duration.ofSeconds(5),Duration.ofSeconds(1)))
                .reduce(
                        (ReduceFunction<String>) (s1, s2) -> s1 + "," + s2,
                        new ProcessWindowFunction<String, String, Integer, TimeWindow>() {   // 该函数的输入是前面聚合函数的输出
                            @Override
                            public void process(Integer key,
                                                ProcessWindowFunction<String, String, Integer, TimeWindow>.Context context,
                                                Iterable<String> elements,  //reduce函数的输出！！！！
                                                Collector<String> out) throws Exception {
                                elements.forEach(s->{
                                    System.out.println(s);
                                    out.collect(s+"...");  //会改变reduce的结果？
                                });
                            }   //前面聚合函数的输出是本函数的输入！！！！

                });


        //AggregateFunction累加器
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("")
                .setTopics("input-topic")
                .setGroupId("my-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();
        DataStreamSource<String> kafkaStream = streamEnv.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Source");

        SingleOutputStreamOperator<DataPojo> dataPojoStream = socketTextStream
                .map(new RichMapFunction<String, DataPojo>() {
                    JsonMapper mapper = JsonMapper.builder().configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true).build();

                    @Override
                    public DataPojo map(String value) throws Exception {
                        return mapper.readValue(value, DataPojo.class);
                    }
                });

        dataPojoStream
                .keyBy(DataPojo::getCity)
                .countWindow(10,2)
                .aggregate(new RichAggregateFunction<DataPojo, AvgAccumulator, Double>() {
                    @Override
                    public AvgAccumulator createAccumulator() {   //在slot中创建累加器
                        return new AvgAccumulator();
                    }

                    @Override
                    public AvgAccumulator add(DataPojo value, AvgAccumulator accumulator) {  //将新数据累加到当前slot的累加器中
                        return null;
                    }

                    @Override
                    public AvgAccumulator merge(AvgAccumulator a, AvgAccumulator b) {   //两个slot的累加器合并到一起
                        return null;
                    }

                    @Override
                    public Double getResult(AvgAccumulator accumulator) {   //获取最终结果
                        return null;
                    }
                });

        //全窗口函数：processWindowFunction,最灵活，需要处理窗口内所有元素，然后返回需要的内容
        dataPojoStream.keyBy(DataPojo::getCity).countWindow(34)
                .process(new ProcessWindowFunction<DataPojo, DataPojo, String, GlobalWindow>() {
                    @Override
                    public void process(String s,
                                        ProcessWindowFunction<DataPojo, DataPojo, String, GlobalWindow>.Context context,
                                        Iterable<DataPojo> elements,
                                        Collector<DataPojo> out) throws Exception {


                    }
                });


        //无keyBy全局窗口.较少使用
        dataPojoStream
                .windowAll(SlidingProcessingTimeWindows.of(Duration.ofSeconds(5),Duration.ofSeconds(2)))
                .sum(1)   //全局窗口聚合并行度必须设置为1，否则会报错
                .print();


    }

    /**
     * 自定义累加器
     */
}
class AvgAccumulator{
    private String city;
    private double avg;

    public AvgAccumulator() {
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }

    public String getCity() {
        return city;
    }

    public double getAvg() {
        return avg;
    }
}