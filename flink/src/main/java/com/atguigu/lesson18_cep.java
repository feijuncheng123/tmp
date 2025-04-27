package com.atguigu;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.cep.*;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.time.Duration;
import java.util.List;
import java.util.Map;


public class lesson18_cep {
    public static void main(String[] args) throws Exception {
        /**
         * cep:复杂事件处理库
         * 编写特定规则，根据事件间的时序关系和聚合关系制定检测规则，持续地从事件流中查询出符合要求的事件序列
         * 需要引入flink-cep库
         *
         */
        StreamExecutionEnvironment streamEnv = StreamExecutionEnvironment.getExecutionEnvironment();

        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("")
                .setTopics("input-topic")
                .setGroupId("my-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();
        DataStreamSource<String> kafkaStream = streamEnv.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Source");

        SingleOutputStreamOperator<DataPojo> dataPojoStream = kafkaStream
                .map(new RichMapFunction<String, DataPojo>() {
                    JsonMapper mapper = JsonMapper.builder().configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true).build();

                    @Override
                    public DataPojo map(String value) throws Exception {
                        return mapper.readValue(value, DataPojo.class);
                    }
                });


        //定义模式
        Pattern<DataPojo, DataPojo> pattern = Pattern
                .<DataPojo>begin("第一个map中的key值") // PatternSelectFunction里面map中的key值，模式名字必须独一无二，不能包含字符":"
                .where(new SimpleCondition<DataPojo>() {
                    @Override
                    public boolean filter(DataPojo value) throws Exception {
                        return false;
                    }
                })
//                .times(2)  //times表示第一个Pattern匹配两个元素（间隔的元素符合条件也会返回），滑动匹配
                .times(2,4) //类似于正则中的2-4次，同样可以间隔查找，但开始元素必须符合条件
                .consecutive() //加上该条件，times会强制连续，不能跳跃，省略后则会间隔匹配
                .timesOrMore(2) //出现两次以上，需要加上终止条件
                .allowCombinations() //类似于followedByAny，将满足times条件的元素分别组合在一起返回，会产生很多返回值
                .until(new SimpleCondition<DataPojo>() {   //终止条件，达到该条件终止匹配。匹配结果不包含该条件
                    @Override
                    public boolean filter(DataPojo value) throws Exception {
                        return false;
                    }
                })
                .next("第二个map中的key值")   //复合模式。next要求严格近邻，只作用当前条件。即满足next后面的条件的元素必须紧跟前一个条件的元素（notNext不能紧跟，后面有值，但不能满足notNext后的条件）。如果使用事件时间会使用时间判断是否连续
                .where(new SimpleCondition<DataPojo>() {
                    @Override
                    public boolean filter(DataPojo value) throws Exception {
                        return false;
                    }
                })
                .within(Duration.ofMinutes(1))  //超时条件，当前条件的元素之后的元素不能超过2分钟，超过就不满足条件
                .followedBy("第三个map中的key值")  //同next，但中间可以间隔其他元素(notFollowedBy 后面不能跟随条件元素，不能作为模式的结尾，需要加上其他条件)
                .where(new SimpleCondition<DataPojo>() {
                    @Override
                    public boolean filter(DataPojo value) throws Exception {
                        return false;
                    }
                })
                .optional()  //这个条件是可选的，有就匹配返回，没有就返回复合其他条件的数据
                .greedy() ;  //贪婪匹配，和正则相似，尽可能长的满足条件


        //组合条件
        Pattern<DataPojo, DataPojo> pattern2 = Pattern.begin(
                Pattern.<DataPojo>begin("a").where(new SimpleCondition<DataPojo>() {

                    @Override
                    public boolean filter(DataPojo value) throws Exception {
                        return false;
                    }
                })
        ).times(2);   //组合条件。times修饰这个组合条件


        //把规则引用到流上,并生成新的模式流
        PatternStream<DataPojo> patternStream = CEP.pattern(dataPojoStream, pattern);
        patternStream.select(new PatternSelectFunction<DataPojo, DataPojo>() {
            @Override
            public DataPojo select(Map<String, List<DataPojo>> pattern) throws Exception {  //参数是个map集合，key是pattern中begin、next中的name参数，value是模式每次匹配到的全部元素
                return null;
            }
        }).print();

        patternStream.flatSelect(new PatternFlatSelectFunction<DataPojo, DataPojo>() {  //flatSelect将符合条件的数据存入集合中以流返回
            @Override
            public void flatSelect(Map<String, List<DataPojo>> pattern, Collector<DataPojo> out) throws Exception {

            }
        });


        //处理withIn超时数据
        SingleOutputStreamOperator<DataPojo> timeOutData = patternStream.select(
                new OutputTag<DataPojo>("late") {
                },  //定义侧输出流
                new PatternTimeoutFunction<DataPojo, DataPojo>() {  //within条件中超时数据会放到侧输出流里
                    @Override
                    public DataPojo timeout(Map<String, List<DataPojo>> pattern, long timeoutTimestamp) throws Exception {
                        return null;
                    }
                },
                new PatternSelectFunction<DataPojo, DataPojo>() {
                    @Override
                    public DataPojo select(Map<String, List<DataPojo>> pattern) throws Exception {
                        return null;
                    }
                }
        );


        timeOutData.getSideOutput(new OutputTag<DataPojo>("late"){}).print();

        streamEnv.execute();

    }
}
