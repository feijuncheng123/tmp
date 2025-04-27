package com.atguigu;

import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;

import java.util.HashMap;
import java.util.Map;

public class lesson06_transformation_demo {

    public static void main(String[] args) throws Exception {
        /**
         * 交易对账：
         * 数据1： 用户订单支付日志  OrderLog.csv
         * 数据2： 财务交易流水 ReceiptLog.csv
         *
         * 方案： 将两个流合并，然后根据交易id号keyBy，使用process算子分别处理元素分别放入map中，然后是否另一方map判断是否存在元素判断是否能对上
         */
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);
        // 1. 读取Order流
        SingleOutputStreamOperator<OrderEvent> orderEventDS = env
                .readTextFile("input/OrderLog.csv")
                .map(line -> {
                    String[] datas = line.split(",");
                    return new OrderEvent(
                            Long.valueOf(datas[0]),
                            datas[1],
                            datas[2],
                            Long.valueOf(datas[3]));

                });
        // 2. 读取交易流
        SingleOutputStreamOperator<TxEvent> txDS = env
                .readTextFile("input/ReceiptLog.csv")
                .map(line -> {
                    String[] datas = line.split(",");
                    return new TxEvent(datas[0], datas[1], Long.valueOf(datas[2]));
                });

        // 3. 两个流连接在一起
        ConnectedStreams<OrderEvent, TxEvent> orderAndTx = orderEventDS.connect(txDS);

        // 4. 因为不同的数据流到达的先后顺序不一致，所以需要匹配对账信息.  输出表示对账成功与否
        orderAndTx
                .keyBy("txId", "txId")
                .process(new CoProcessFunction<OrderEvent, TxEvent, String>() {
                    // 存 txId -> OrderEvent
                    Map<String, OrderEvent> orderMap = new HashMap<>();
                    // 存储 txId -> TxEvent
                    Map<String, TxEvent> txMap = new HashMap<>();

                    @Override
                    public void processElement1(OrderEvent value, Context ctx, Collector<String> out) throws Exception {
                        // 获取交易信息
                        if (txMap.containsKey(value.getTxId())) {
                            out.collect("订单: " + value + " 对账成功");
                            txMap.remove(value.getTxId());
                        } else {
                            orderMap.put(value.getTxId(), value);
                        }
                    }

                    @Override
                    public void processElement2(TxEvent value, Context ctx, Collector<String> out) throws Exception {
                        // 获取订单信息
                        if (orderMap.containsKey(value.getTxId())) {
                            OrderEvent orderEvent = orderMap.get(value.getTxId());
                            out.collect("订单: " + orderEvent + " 对账成功");
                            orderMap.remove(value.getTxId());
                        } else {
                            txMap.put(value.getTxId(), value);
                        }
                    }
                })
                .print();
        env.execute();
    }

}

class OrderEvent{
    private Long orderId;
    private String eventType;
    private String txId;
    private Long eventTime;

    public OrderEvent(Long orderId, String eventType, String txId, Long eventTime) {
        this.orderId = orderId;
        this.eventType = eventType;
        this.txId = txId;
        this.eventTime = eventTime;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public void setEventTime(Long eventTime) {
        this.eventTime = eventTime;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getTxId() {
        return txId;
    }

    public Long getEventTime() {
        return eventTime;
    }
}

class TxEvent {
    private String txId;
    private String payChannel;
    private Long eventTime;

    public TxEvent(String txId, String payChannel, Long eventTime) {
        this.txId = txId;
        this.payChannel = payChannel;
        this.eventTime = eventTime;
    }

    public String getTxId() {
        return txId;
    }

    public String getPayChannel() {
        return payChannel;
    }

    public Long getEventTime() {
        return eventTime;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public void setPayChannel(String payChannel) {
        this.payChannel = payChannel;
    }

    public void setEventTime(Long eventTime) {
        this.eventTime = eventTime;
    }
}
