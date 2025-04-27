package com.flink_and_kafka;


import org.apache.commons.io.FileUtils;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.operators.MapOperator;
import org.apache.flink.configuration.Configuration;


import java.io.File;
import java.util.*;


/**
 * 分布式缓存
 */
public class Flink01_DistributedCache {
    public static void main(String[] args) throws Exception {
        //执行离线计算DataSet，创建一个运行环境
        ExecutionEnvironment env=ExecutionEnvironment.getExecutionEnvironment();

        //注册需要缓存的文件，并且其一个别名
        env.registerCachedFile("path","cacheData");
        DataSource<Integer> integerDataSource = env.fromElements(1, 2, 3, 3, 4, 5, 6);

        //对数据进行map处理
        MapOperator<Integer, String> mapString = integerDataSource.map(new RichMapFunction<Integer, String>() {
            private String prefixString="";

            /*
            初始化函数:可以读取当前节点的缓存数据。
             */
            @Override
            public void open(Configuration parameters) throws Exception {
                File cacheData = this.getRuntimeContext().getDistributedCache().getFile("cacheData");
                List<String> ls= FileUtils.readLines(cacheData,"utf8");
                prefixString=ls.get(0);
            }

            @Override
            public String map(Integer value) throws Exception {
                return value + prefixString;
            }
        });

        mapString.print();

    }
}
