import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.apache.flume.source.SyslogParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hdfs.DFSClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by user on 20.05.15.
 */
public class MyJavaSink extends AbstractSink implements Configurable {

    CloseableHttpAsyncClient httpclient;
    static JedisPool pool;
    static Connection hBaseConnection;
    static long prev_time;
  //  FileWriter fw;

   // static File file;
    static Path file;
    static FileSystem hdfs;
   // static OutputStream outputStream;

    final int batchSize = 1;

    @Override
    public void configure(Context context) {
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(3000)
                    .setConnectTimeout(3000)
                            //   .setConnectionRequestTimeout(1000)
                    .build();
            httpclient = HttpAsyncClients.custom().
                    setDefaultRequestConfig(requestConfig).setRedirectStrategy(new LaxRedirectStrategy()).build();
            prev_time = System.currentTimeMillis();

            Configuration conf = new Configuration();
            System.setProperty("HADOOP_USER_NAME", "emistukov");
        try {
            hdfs = FileSystem.get(new URI("hdfs://c1s1.realtb.org:8020/"), conf);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        file = new Path("/user/emistukov/ads_output");


       // file = new File("/home/user/IdeaProjects/myFlume/the-file-name.txt");

          //  file = new File("/home/emistukov/myFlume/the-file-name.txt");
         /*   try {
                if (!file.exists())
                    file.createNewFile();
                fw = new FileWriter(file.getAbsoluteFile(), false);
            } catch (IOException e) {
                System.out.println("Exception in creating file!");
                e.printStackTrace();
            }*/

        pool = new JedisPool(new JedisPoolConfig(), "localhost");

        conf = HBaseConfiguration.create();
        //    conf.addResource(new Path("/home/user/Downloads/hbase-1.0.0/conf/hbase-site.xml"));
        conf.set("fs.hdfs.impl",
                org.apache.hadoop.hdfs.DistributedFileSystem.class.getName()
        );
        conf.set("fs.file.impl",
                org.apache.hadoop.fs.LocalFileSystem.class.getName()
        );
        try {
            hBaseConnection = ConnectionFactory.createConnection(conf);
        } catch (IOException e) {
            System.out.println("Can't connect to HBase or find table!");
            e.printStackTrace();
        }
            httpclient.start();
    }


    @Override
    public void stop () {
        try {
            httpclient.close();
           // fw.close();
            pool.destroy();
            hBaseConnection.close();
        } catch (IOException e) {
            System.out.println("Exception in stop!");
            e.printStackTrace();
        }
    }

    @Override
    public Status process() {
        Status status = null;

        Channel ch = getChannel();
        Transaction txn = ch.getTransaction();
        status = Status.READY;
        try {
            txn.begin();
           // Set<String> existingSiteUrls_set = new HashSet<String>();
            Map<String, Set<String>> existingSiteUrls_map = new HashMap<String, Set<String>>();
            for (int i = 0; i < batchSize; i ++) {
                Event event = ch.take();
                if (event != null) {
                    String str = new String(event.getBody());
                    if (MyJsonParser.parse(str)) {
                      //  Set<String> existingSiteUrls = MyJsonParser.existingSiteUrls;
                      //  user_id = MyJsonParser.user_id;
                        for (String s : MyJsonParser.existingSiteUrls) {
                            if (existingSiteUrls_map.containsKey(s))
                                existingSiteUrls_map.get(s).add(MyJsonParser.user_id);
                            else {
                                HashSet<String> added_set = new HashSet<String>();
                                added_set.add(MyJsonParser.user_id);
                                existingSiteUrls_map.put(s, added_set);
                            }
                        }
                     //   existingSiteUrls_set.addAll(existingSiteUrls);
                    }
                } else {
                    status = Status.BACKOFF;
                    break;
                }
            }
           // if (status != Status.BACKOFF) {
                MyCrawler.crawl(existingSiteUrls_map, /*fw*/ httpclient, prev_time);
                existingSiteUrls_map.clear();
              //  status = Status.READY;
          //  }
            txn.commit();
        }catch (Exception e) {
            System.out.println("Exception in process!");
            e.printStackTrace();
            txn.rollback();
        } finally {
            txn.close();
        }
        return status;
    }
}
