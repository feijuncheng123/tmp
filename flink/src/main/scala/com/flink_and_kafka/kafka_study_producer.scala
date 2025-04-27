package com.flink_and_kafka

import java.util.Properties
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.RecordMetadata
import java.util
import org.apache.kafka.clients.producer.Partitioner
import org.apache.kafka.common.Cluster


object kafka_study_producer {
  def main(args: Array[String]): Unit = {
    val props = new Properties()
    // Kafka服务端的主机名和端口号
    props.put("bootstrap.servers", "192.168.231.128:9092")
    // 等待所有副本节点的应答
    props.put("acks", "all")
    // 消息发送最大尝试次数
    props.put("retries", "0")
    // 一批消息处理大小
    props.put("batch.size", "16384")
    // 请求延时
    props.put("linger.ms", "1")
    // 发送缓存区内存大小
    props.put("buffer.memory", "33554432")
    // key序列化
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    // value序列化
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")


    val producer = new KafkaProducer[String, String](props)
    for (i <- 0 until 10) {
//      producer.send(new ProducerRecord[String, String]("firstTopic", Integer.toString(i), "hello world-" + i))

      //带回调函数的生产者
      producer.send(new ProducerRecord[String, String]("firstTopic", "key-"+i ,"hello" + i),
        new Callback() {
          def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {

            if (metadata != null) System.err.println(metadata.partition + "---" + metadata.offset+"---"+metadata.toString)}

        })
    }
    producer.close()
  }
}



//自定义分区生产者: 控制生产者的分区。定义该类后使用以下方法调用：
// 自定义分区
//props.put("partitioner.class", "com.atguigu.kafka.CustomPartitioner");
//定义分区用处：消费者组中用户共同消费一个topic，但是在同时间内，一个分区只能被消费者组中的一个用户读取。即同一时间下，一个分区和消费者组中的一个用户是一对一的关系
class CustomPartitioner extends Partitioner {
  override def close(): Unit = {
  }

  // 控制分区，可以根据key值，value值或者字节大小定义不同分区处理
  override def partition(topic: String, key: Any, keyBytes: Array[Byte], value: Any, valueBytes: Array[Byte], cluster: Cluster): Int = 0

  override def configure(configs: util.Map[String, _]): Unit = {}
}

