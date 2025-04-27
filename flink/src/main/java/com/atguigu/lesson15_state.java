package com.atguigu;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;

public class lesson15_state {
    public static void main(String[] args) throws Exception {
        /**
         * 一、状态：需要记住多个事件信息的操作就是有状态的
         * 问题：
         * new ProcessFunction<String, Integer>() {
         *             int sum=0;
         *             @Override
         *             public void processElement(String value,
         *                                        ProcessFunction<String, Integer>.Context ctx,
         *                                        Collector<Integer> out
         *             ) throws Exception {
         *                 sum += value.length();
         *                 out.collect(sum);
         *             }
         *         }
         * 在形如以上代码中，使用sum保存中间状态，具有严重问题：
         * 1、各分区都有自己的实例，无法统一计算
         * 2、如果出现重启，会导致中间状态全部消失，无法重启继续计算
         *
         * 二、需要状态的场景：
         * 1、去重
         * 2、模式检测
         * 3、聚合
         * 4、更新数据
         *
         * 三、状态分类
         * flink中状态分为两类：Managed State和raw state
         * 	               Managed State	                                       Raw State
         * 状态管理方式      Flink Runtime托管, 自动存储, 自动恢复, 自动伸缩	           用户自己管理
         * 状态数据结构      Flink提供多种常用数据结构, 例如:ListState, MapState等	       字节数组: byte[]
         * 使用场景	      绝大数Flink算子	                                           所有算子
         *
         * 四、Managed State分类
         *                      Operator State(算子状态)                                         Keyed State(键控状态)
         * 适用用算子类型         可用于所有算子: 常用于source, sink, 例如 FlinkKafkaConsumer	       只能用于用于KeyedStream上的算子
         * 状态分配              一个算子的子任务对应一个状态	     	     	     	     	       一个Key对应一个State: 一个算子会处理多个Key, 则访问相应的多个State
         * 创建和访问方式         实现CheckpointedFunction或ListCheckpointed(已经过时)接口	       重写RichFunction, 通过里面的RuntimeContext访问w
         * 横向扩展              并发改变时有多重重写分配方式可选: 均匀分配和合并后每个得到全量	     	   并发改变, State随着Key在实例间迁移
         * 支持的数据结构         ListState,UnionListStste和BroadCastState	      	     	       ValueState, ListState,MapState ReduceState, AggregatingState
         *
         * 五、Operator State状态
         * 经常被用在Source或Sink等算子上，用来保存流入数据的偏移量或对输出数据做缓存
         * 注意：
         * 5.1 Operator State可以用在数据链路上的所有算子上，每个算子子任务或者说每个算子实例共享一个状态，流入这个算子子任务的数据可以访问和更新这个状态。
         * 5.2 同一算子的子任务之间的状态不能互相访问
         * 内部数据结构：
         * 5.3 列表状态（List state） ： 将状态表示为一组数据的列表
         * 5.4 联合列表状态（Union list state）： 也是列表，但与列表状态区别是发生故障时从保存点如何恢复：List state是均匀分配给各分区，Union list state是将所有state合并后再分发
         * 5.5 广播状态（Broadcast state）：
         * 5.5.1 一种特殊的算子状态. 如果一个算子有多项任务，而它的每项任务状态又都相同，那么这种特殊情况最适合应用广播状态
         * 5.5.2 Broadcast state只对输入有广播流和无广播流的特定算子可用
         * 5.5.3 Broadcast state是一个map格式
         *
         *
         * 六、Keyed State状态
         * 具有相同key的所有数据都会访问相同的状态
         * 内部数据结构：
         * 6.1	ValueState<T> 保存单个值. 每个key有一个状态值.  设置使用 update(T), 获取使用 T value()
         * 6.2	ListState<T> 保存元素列表.
         * 6.3	ReducingState<T> 存储单个值, 表示把所有元素的聚合结果添加到状态中.  与ListState类似,但是当使用add(T)的时候ReducingState会使用指定的ReduceFunction进行聚合.
         * 6.4	AggregatingState<IN, OUT> 存储单个值. 与ReducingState类似, 都是进行聚合. 不同的是, AggregatingState的聚合的结果和元素类型可以不一样
         * 6.5	MapState<UK, UV> 存储键值对列表
         *
         * 
         */

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment()
                .setParallelism(3);

        //开启自动checkpoint，2000毫秒保存一次
        env.enableCheckpointing(2000);

        /**
         * List state
         */
        SingleOutputStreamOperator<Long> dataStream = env
                .socketTextStream("hadoop102", 9999)
                .map(new MyCountMapper());


        env.execute();


        /**
         * Broadcast state
         */
        DataStreamSource<String> broadcastStream01 = env.socketTextStream("hadoop102", 9999);
        DataStreamSource<String> broadcastStream02 = env.socketTextStream("hadoop102", 8888);

        MapStateDescriptor<String, String> stateDescriptor = new MapStateDescriptor<>("state", String.class, String.class);
        BroadcastStream<String> broadcastStream = broadcastStream02.broadcast(stateDescriptor);  //状态广播

        broadcastStream01
                .connect(broadcastStream)
                .process(new BroadcastProcessFunction<String, String, String>() {
                    @Override
                    public void processElement(String value, ReadOnlyContext ctx, Collector<String> out) throws Exception {
                        // 从广播状态中取值, 不同的值做不同的业务
                        ReadOnlyBroadcastState<String, String> state = ctx.getBroadcastState(stateDescriptor);
                        if ("1".equals(state.get("switch"))) {
                            out.collect("切换到1号配置....");
                        } else if ("0".equals(state.get("switch"))) {
                            out.collect("切换到0号配置....");
                        } else {
                            out.collect("切换到其他配置....");
                        }
                    }

                    /**
                     * 广播流中每一个元素到达都会执行一次，value是广播流的元素。
                     * @param value The stream element.
                     * @param ctx A {@link Context} that allows querying the timestamp of the element, querying the
                     *     current processing/event time and updating the broadcast state. The context is only valid
                     *     during the invocation of this method, do not store it.
                     * @param out The collector to emit resulting elements to
                     * @throws Exception
                     */
                    @Override
                    public void processBroadcastElement(String value, Context ctx, Collector<String> out) throws Exception {
                        BroadcastState<String, String> state = ctx.getBroadcastState(stateDescriptor);
                        // 把值放入广播状态
                        state.put("switch", value);
                    }
                })
                .print();


        /**
         * keyed state:  ValueState,ListState，reduceState
         */
        broadcastStream01.keyBy(s->s.substring(0,3))
                .process(new KeyedProcessFunction<String, String, String>() {
                    ValueState<Integer> state;  //同一个key下面的值都共享该状态

                    private ListState<Integer> vcState;
                    private ReducingState<Integer> rdState;

                    private AggregatingState<Integer, Double> avgState;

                    private MapState<String, String> mapState;


                    @Override
                    public void open(OpenContext openContext) throws Exception {
                        state=getRuntimeContext().getState(new ValueStateDescriptor<Integer>("state", Integer.class));
                        vcState=getRuntimeContext().getListState(new ListStateDescriptor<>("listState",Integer.class)); //创建ListState
                        rdState=getRuntimeContext().getReducingState(new ReducingStateDescriptor<>("reduceState",Integer::sum,Integer.class));  //reduceState,需要添加reduce函数

                        mapState=getRuntimeContext().getMapState(new MapStateDescriptor<String, String>("mapState",String.class,String.class));

                        avgState=getRuntimeContext().getAggregatingState(new AggregatingStateDescriptor<>("aggState",new AggregateFunction<Integer,Tuple2<Integer,Integer>,Double>() {
                            @Override
                            public Tuple2<Integer,Integer> createAccumulator() {
                                return Tuple2.of(0,0);
                            }

                            @Override
                            public Tuple2<Integer,Integer> add(Integer value, Tuple2<Integer,Integer> accumulator) {
                                accumulator.f0 += value;
                                accumulator.f1 += 1;
                                return accumulator;
                            }

                            @Override
                            public Double getResult(Tuple2<Integer,Integer> accumulator) {
                                return accumulator.f0*1d/accumulator.f1;
                            }

                            @Override
                            public Tuple2<Integer,Integer> merge(Tuple2<Integer,Integer> a, Tuple2<Integer,Integer> b) {
                                return Tuple2.of(a.f0+b.f0,a.f1+b.f1);
                            }
                        }, Types.TUPLE(Types.INT, Types.INT)));
                    }

                    @Override
                    public void processElement(String value, KeyedProcessFunction<String, String, String>.Context ctx, Collector<String> out) throws Exception {
                        Integer lastVc = state.value() == null ? 0 : state.value();
                        if(lastVc>10 && value.length()>10) {  //前后元素长度都超过10，就执行操作
                            out.collect(value);
                        }

                        state.update(value.length());

                        vcState.add(value.length());
                        List<Integer> vcs = new ArrayList<>();
                        vcState.addAll(vcs);
                        vcState.update(vcs); //相当于先执行clear，在执行addAll

                        rdState.add(value.length());

                        avgState.add(value.length());

                        mapState.put(value,"key去重"); //对元素去重
                    }

                });


    }

    /**
     * 实现Operator State，需要实现CheckpointedFunction接口
     */
    private static class MyCountMapper implements MapFunction<String, Long>, CheckpointedFunction {
        private Long count = 0L;
        private ListState<Long> state;

        @Override
        public Long map(String value) throws Exception {
            count++;
            return count;
        }

        // 初始化时会调用这个方法，向本地状态中填充数据. 每个子任务调用一次
        @Override
        public void initializeState(FunctionInitializationContext context) throws Exception {
            System.out.println("initializeState...");

            //获取列表状态
            state = context
                    .getOperatorStateStore()
                    .getListState(new ListStateDescriptor<Long>("state", Long.class));

            //state.get()从状态中获取数据
            for (Long c : state.get()) {   //程序重启后，要将状态恢复到count中
                count += c;
            }
        }

        // 保存Checkpoint时会调用这个方法，我们要实现具体的snapshot逻辑，比如将哪些本地状态持久化
        //周期性执行
        @Override
        public void snapshotState(FunctionSnapshotContext context) throws Exception {
            System.out.println("snapshotState...");
            state.clear(); //count是累加更新的，更新后，之前数据就过时了，因此需要清空之前保存的count，更新为最新的count
            state.add(count);

        }

    }


}
