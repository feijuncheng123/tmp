package com.atguigu;

import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.configuration.MemorySize;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.core.fs.Path;
import org.apache.flink.orc.writer.OrcBulkWriterFactory;
import org.apache.flink.streaming.api.functions.sink.filesystem.RollingPolicy;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.hive.ql.exec.vector.BytesColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.LongColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public class lesson_orc {
    public static void main(String[] args) {

        FileSink<String> sink = FileSink
                .forRowFormat(new Path(outputPath), new SimpleStringEncoder<String>("UTF-8"))
                .withRollingPolicy(
                        DefaultRollingPolicy.builder()
                                .withRolloverInterval(Duration.ofMinutes(15))
                                .withInactivityInterval(Duration.ofMinutes(5))
                                .withMaxPartSize(MemorySize.ofMebiBytes(1024))
                                .build())
                .build();

        input.sinkTo(sink);

        OrcBulkWriterFactory<Person> writerFactory = new OrcBulkWriterFactory<>(new PersonVectorizer(schema));
    }
}

class PersonVectorizer extends Vectorizer<Person> implements Serializable {
    public PersonVectorizer(String schema) {
        super(schema);
    }
    @Override
    public void vectorize(Person element, VectorizedRowBatch batch) throws IOException {
        BytesColumnVector nameColVector = (BytesColumnVector) batch.cols[0];
        LongColumnVector ageColVector = (LongColumnVector) batch.cols[1];
        int row = batch.size++;
        nameColVector.setVal(row, element.getName().getBytes(StandardCharsets.UTF_8));
        ageColVector.vector[row] = element.getAge();
    }
}