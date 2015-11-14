import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by user on 20.05.15.
 */
public class MyJavaSink extends AbstractSink implements Configurable {
    public void configure(Context context) {

    }

    JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");

    @Override
    public void stop () {
        // Disconnect from the external respository and do any
        // additional cleanup (e.g. releasing resources or nulling-out
        // field values) ..
        pool.destroy();
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

                // if (MyJsonParser.parse(str)) {
                List<String> existingSiteUrls = MyJsonParser.existingSiteUrls;
                try (Jedis jedis = pool.getResource()) {
                    /// ... do stuff here ... for example
                    jedis.rpush("myUrlsList", existingSiteUrls.toArray(new String[existingSiteUrls.size()]));
                }
                //  }
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
