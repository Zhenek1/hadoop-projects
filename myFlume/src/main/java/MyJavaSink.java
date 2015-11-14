import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import java.io.*;
import java.util.*;

/**
 * Created by user on 20.05.15.
 */
public class MyJavaSink extends AbstractSink implements Configurable {
    public void configure(Context context) {

    }

   // JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");

    @Override
    public void stop () {
        // Disconnect from the external respository and do any
        // additional cleanup (e.g. releasing resources or nulling-out
        // field values) ..
   //     pool.destroy();
    }

    public Status process() throws EventDeliveryException {

        Status status = null;

        Channel ch = getChannel();
        Transaction txn = ch.getTransaction();
        //File file = new File("/home/user/IdeaProjects/myFlume/the-file-name.txt");
        try {
            txn.begin();
            Event event = ch.take();
            if (event != null) {
                String str = new String(event.getBody());
               /* File file = new File("/var/lib/flume-ng/myTestDir/the-file-name.txt");

                // if file doesnt exist, then create it
                if (!file.exists())
                    file.createNewFile();

                FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(str);
                bw.close();*/

                if (MyJsonParser.parse(str)) {
                    List<String> existingSiteUrls = MyJsonParser.existingSiteUrls;
                    Set<String> existingSiteUrls_set = new HashSet<String>();
                    existingSiteUrls_set.addAll(existingSiteUrls);
                  //  File file = new File("/var/lib/flume-ng/myTestDir/the-file-name.txt");
                    File file = new File("/home/user/IdeaProjects/myFlume/the-file-name.txt");
                    if (!file.exists())
                        file.createNewFile();
               /*     PrintWriter writer = new PrintWriter(file);
                    writer.print("");
                    writer.close();*/

                    FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom().setRedirectStrategy(new LaxRedirectStrategy()).build();
                    System.out.println("Crawl method!");
                    bw.write("aa");
                  /*  try {
                        MyCrawler.crawl(existingSiteUrls_set, bw);
                    } catch (Exception e) {
                        bw.write(e.toString());
                    }*/
                 /*   for (String s : MyCrawler.concatenated_data) {
                        bw.write(s);
                        bw.newLine();
                    }*/
                    bw.close();
               /*     try (Jedis jedis = pool.getResource()) {
                        /// ... do stuff here ... for example
                        jedis.rpush("myUrlsList", existingSiteUrls.toArray(new String[existingSiteUrls.size()]));
                    }
                    for (String url : existingSiteUrls) {
                        root.addNodeRecursive(Arrays.asList(url.split("/")));
                    }*/
                }
                status = Status.READY;
            } else {
                status = Status.BACKOFF;
            }
            txn.commit();
        }catch (Exception e) {
            e.printStackTrace();
            txn.rollback();
        } finally {
            txn.close();
        }
        return status;
    }
}
