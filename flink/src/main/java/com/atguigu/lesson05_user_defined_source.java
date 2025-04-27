package com.atguigu;

import org.apache.flink.api.connector.source.*;
import org.apache.flink.core.io.SimpleVersionedSerializer;

public class lesson05_user_defined_source {
    public static void main(String[] args) {

    }
}


class MySource implements Source<String, MySourceSplit,String>{

    public MySource() {

    }

    @Override
    public Boundedness getBoundedness() {
        return Boundedness.BOUNDED;   //有界流
    }

    @Override
    public SplitEnumerator<MySourceSplit, String> createEnumerator(SplitEnumeratorContext<MySourceSplit> enumContext) throws Exception {
        return null;
    }

    @Override
    public SplitEnumerator<MySourceSplit, String> restoreEnumerator(SplitEnumeratorContext<MySourceSplit> enumContext, String checkpoint) throws Exception {
        return null;
    }

    @Override
    public SimpleVersionedSerializer<MySourceSplit> getSplitSerializer() {
        return null;
    }

    @Override
    public SimpleVersionedSerializer<String> getEnumeratorCheckpointSerializer() {
        return null;
    }

    @Override
    public SourceReader<String, MySourceSplit> createReader(SourceReaderContext readerContext) throws Exception {
        return null;
    }
}

class MySourceSplit implements SourceSplit {

    String id;
    MySourceSplit(String id){
        this.id=id;
    }

    @Override
    public String splitId() {

        return id;
    }
}