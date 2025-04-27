package com.flink_and_kafka;

import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;

import java.util.ArrayList;

public class Flink05_JoinDemo {
    public static void main(String[] args) throws Exception {
        //创建一个运行环境执行DataSet API
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        //用户信息                      城市ID     用户名字
        ArrayList<Tuple2<Integer, String>> data1 = new ArrayList<>();
        data1.add(new Tuple2<>(1, "Tom"));
        data1.add(new Tuple2<>(2, "Mary"));
        data1.add(new Tuple2<>(3, "Mike"));
        data1.add(new Tuple2<>(4, "Jone"));

        //城市的信息                    城市ID    城市名称
        ArrayList<Tuple2<Integer, String>> data2 = new ArrayList<>();
        data2.add(new Tuple2<>(1, "北京"));
        data2.add(new Tuple2<>(2, "上海"));
        data2.add(new Tuple2<>(3, "重庆"));
        data2.add(new Tuple2<>(4, "深圳"));

        //生成两张表（两个数据集合）
        DataSet<Tuple2<Integer, String>> user = env.fromCollection(data1);
        DataSet<Tuple2<Integer, String>> city = env.fromCollection(data2);

        //执行Join
        //表示：用于user表的第一个字段连接city表的第一字段
        //where user.cityID = city.cityID
        user.join(city)   //leftOuterJoin、rightOuterJoin、fullOuterJoin
                .where(0)
                .equalTo(0)
                //user表  / 表示city表  /  结果表：ID号   用户名字   城市的名字
                .with(new JoinFunction<Tuple2<Integer, String>, Tuple2<Integer, String>, Tuple3<Integer, String, String>>() {
                    public Tuple3<Integer, String, String> join(Tuple2<Integer, String> userTable, Tuple2<Integer, String> cityTable) {
                        return new Tuple3<Integer, String, String>(userTable.f0, userTable.f1, cityTable.f1);
                    }
                })
                .print();


        //生成笛卡尔积
        user.cross(city).print();
    }
}
