package com.atguigu;

import org.apache.flink.configuration.CheckpointingOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.StateBackendOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class lesson16_state_backend {
    public static void main(String[] args) {
        /**
         * 状态后端：
         * 状态的存储、访问以及维护，由一个可插入的组件决定，这个组件就叫做状态后端
         * 状态后端主要负责两件事：
         * 	本地(taskmanager)的状态管理
         * 	将检查点（checkpoint）状态写入远程存储
         *
         * 状态后端分类（1.13之后，只用于管理本地状态)
         * 1、HashMapStateBackend  ： 使用hashMap管理本地状态数据
         * 2、EmbeddedRocksDBStateBackend ：使用嵌入的RocksDB数据库管理状态数据
         *
         * 远程后端管理：通过checkpoint进行管理
         *
         * 配置状态后端：
         * 1、配置文件中：本地状态state.backend设置，远程状态state.backend.checkpoint设置
         * 2、在代码中配置
         */
        Configuration config = new Configuration();
        config.set(StateBackendOptions.STATE_BACKEND, "hashmap");
        config.set(CheckpointingOptions.CHECKPOINTS_DIRECTORY,"");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


    }
}
