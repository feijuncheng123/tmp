package com.flink_and_kafka

import java.util
import java.util.Properties

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer

import scala.collection.JavaConverters._
//import scala.collection.JavaConversions._
//JavaConverters比JavaConversions更新。JavaConversions为隐式转换，而JavaConverters为显式转换


//自动维护offset
object kafka_study_consumer {
  def main(args: Array[String]): Unit = {

    val props = new Properties()
    // 定义kakfa 服务的地址，不需要将所有broker指定上
    props.put("bootstrap.servers", "192.168.231.128:9092")
    // 制定consumer group
    props.put("group.id", "test")
    // 是否自动确认offset
    props.put("enable.auto.commit", "true")
    // 自动确认offset的时间间隔
    props.put("auto.commit.interval.ms", "1000")
    // key的序列化类
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    // value的序列化类
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    // 定义consumer
    val consumer = new KafkaConsumer[String, String](props)


    // 消费者订阅的topic, 可同时订阅多个
    consumer.subscribe(List("firstTopic").asJava)

    while (true) {
      // 读取数据，读取超时时间为100ms
      val records = consumer.poll(java.time.Duration.ofMillis(100)).asScala
      for (record <- records) {
        System.out.printf(s"offset = %d, key = %s, value = %s \n".format(record.offset, record.key, record.value))
      }
    }

  }

}
