package com.atguigu;

import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.api.connector.sink2.WriterInitContext;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.flink.configuration.Configuration;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class lesson08_user_defined_sink {
    public static void main(String[] args) {

    }
}


class MySink extends RichSinkFunction<DataPojo> {
    private HttpSolrClient solrClient;

    private String username;
    private String password;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        client();
    }

    @Override
    public void invoke(DataPojo value, Context context) throws Exception {
        String queryString = "id:1";

        try {
            List<DataPojo> exeples = new QueryRequest(
                    new SolrQuery(queryString).setRows(1)
            ).process(solrClient, "fd_table").getBeans(DataPojo.class);

            for (DataPojo exem : exeples) {
                //将DataPojo转化提交数据
                UpdateRequest request = new UpdateRequest();
                request.setBasicAuthCredentials(username, password);
                request.add(solrClient.getBinder().toSolrInputDocument(exem));
                request.commit(solrClient, "fd_table");
                System.out.println("执行完成");
            }


        } catch (Exception e) {
            if (null != solrClient) {
                solrClient.close();
                solrClient = null;
                System.out.println("里面关闭啦！");
            }
            client();
        }
    }


    @Override
    public void close() throws Exception {
        super.close();
        try {
            if (null != solrClient) {
                solrClient.close();
                solrClient = null;
                System.out.println("关闭啦！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void client() {
        synchronized (this) {
            if (null == solrClient) {

                //加载了配置文件当中solr的username、password
                Map<String, String> keyMap = new HashMap<>();
                username = keyMap.get("username");
                password = keyMap.get("password");
                //加载solr连接的url
                Properties prop = new Properties();
                String solrUrl = prop.get("com.medbook.en.solr.url").toString();
                solrClient = new HttpSolrClient.Builder(solrUrl)
                        //连接超过时间没有跑完就会退出
                        .withConnectionTimeout(360000)
                        .withSocketTimeout(360000)
                        .build();
                System.out.println("创建了连接");
            }
        }
    }
}

/**
 * 新版
 */

class NewSink implements Sink<DataPojo>{

    @Override
    public SinkWriter<DataPojo> createWriter(InitContext context) throws IOException {
        return new MyWriter();
    }

    private class MyWriter implements SinkWriter<DataPojo>{


        @Override
        public void write(DataPojo element, Context context) throws IOException, InterruptedException {

        }

        @Override
        public void flush(boolean endOfInput) throws IOException, InterruptedException {

        }

        @Override
        public void close() throws Exception {

        }
    }
}
