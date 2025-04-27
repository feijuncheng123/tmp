package com.atguigu;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.cdc.connectors.base.source.jdbc.JdbcIncrementalSource;
import org.apache.flink.cdc.connectors.mysql.source.MySqlSource;
import org.apache.flink.cdc.connectors.mysql.table.StartupOptions;
import org.apache.flink.cdc.connectors.oracle.source.OracleSourceBuilder;
import org.apache.flink.cdc.debezium.DebeziumSourceFunction;
import org.apache.flink.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.cdc.debezium.StringDebeziumDeserializationSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import org.apache.flink.cdc.connectors.oracle.OracleSource;

import java.util.Properties;

public class lesson_cdc {
    public static void main(String[] args) throws Exception {
        /**
         * cdc: change data capture
         * 1、监测并捕获数据库中的变动（包括数据或数据表的插入、更新以及删除等），将这些变更按发生的顺序完整记录下来，写入到消息中间件以供订阅
         * 2、cdc有基于查询和基于binlog两类：
         * 2.1 基于查询：sqoop、kafka jdbc source。需要配置查询sql，按batch模式查询数据库数据变动，具有高延迟、耗费性能、以及无法将全部变动完整捕获的缺点
         * 2.2 基于binlog： canal、Maxwell、Debezuim（flink基于此）。流式查询，低延迟，且不消耗数据库性能
         * 3、组件：flink-cdc-connectors 。可以连接mysql、postgresql等数据库
         * 4、需要开启checkpoint，否则无法断点续传
         *
         *
         * mysql配置：
         * 1、需要在my.conf中添加以下配置：
         * log-bin=mysql-bin
         * binlog-format=row
         * binlog--do-db=cdc-test  配置binlog的存储库，可以配置多个
         *
         */

        MySqlSource<String> mysqlCDC = MySqlSource.<String>builder()
                .hostname("localhost")
                .port(3306)
                .username("fjc")
                .password("123456")
                .databaseList("dbName")
                .tableList("dbName.tableName")  //如果不加，会监控数据库下全部数据
                .deserializer(new StringDebeziumDeserializationSchema())
                .startupOptions(StartupOptions.earliest())
                //StartupOptions选项：
                //initial:默认的启动模式, 在第一次启动时对受监视的数据库表执行初始快照，并继续读取最新的 binlog。(用的多)
                //latest-offset:  在每次启动时从最新的 binlog 位置开始读取数据,CDC 会忽略之前的 checkpoint，从当前 binlog 文件的最后一个位置开始读取(用的多，只会记录最新变动，历史不记录)
                //earliest-offset：从最早的 binlog 位置开始读取数据，跳过快照阶段.使用此模式需要确保binlog包含了历史完整数据。（initial首次启动会对表数据创建快照）
                //specific-offset:指定具体的 binlog 位置来进行启动.需要提供 scan.startup.specific-offsets 参数，并指定对应的 binlog 文件名和偏移量。CDC 将从指定的位置开始读取数据。
                //timestamp: 从指定的时间戳开始读取数据。启动时，Flink CDC 会从 MySQL 的 binlog 中获取与指定时间戳匹配的日志位置，然后从这个位置开始读取数据。
                .build();

        StreamExecutionEnvironment streamEnv = StreamExecutionEnvironment.getExecutionEnvironment();

        streamEnv.fromSource(mysqlCDC, WatermarkStrategy.noWatermarks(), "cdc Source")
                .print();

        DebeziumSourceFunction<String> oracleCDC = OracleSource.<String>builder()
                .hostname("")
                .port(3306)
                .username("fjc")
                .password("123456")
                .database("dbName")
                .tableList("dbName.tableName")  //如果不加，会监控数据库下全部数据
                .deserializer(new StringDebeziumDeserializationSchema())
                .startupOptions(org.apache.flink.cdc.connectors.base.options.StartupOptions.latest())
                .build();

        streamEnv.addSource(oracleCDC);

        ///////////////////////////////////////////////////////////////////
        Properties debeziumProperties = new Properties();
        debeziumProperties.setProperty("log.mining.strategy", "online_catalog");
        JdbcIncrementalSource<String> oracleChangeEventSource = new OracleSourceBuilder()
                .hostname("host")
                .port(1521)
                .databaseList("ORCLCDB")
                .schemaList("DEBEZIUM")
                .tableList("DEBEZIUM.PRODUCTS")
                .username("username")
                .password("password")
                .deserializer(new JsonDebeziumDeserializationSchema())
                .includeSchemaChanges(true) // output the schema changes as well
                .startupOptions(org.apache.flink.cdc.connectors.base.options.StartupOptions.initial())
                .debeziumProperties(debeziumProperties)
                .splitSize(2)
                .build();


        streamEnv.fromSource(
                        oracleChangeEventSource,
                        WatermarkStrategy.noWatermarks(),
                        "OracleParallelSource")
                .setParallelism(4)
                .print()
                .setParallelism(1);
        streamEnv.execute("Print Oracle Snapshot + RedoLog");

        streamEnv.execute();
    }
}
