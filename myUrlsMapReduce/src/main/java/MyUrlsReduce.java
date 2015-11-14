
import org.apache.commons.lang.ObjectUtils;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;

import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by user on 22.04.15.
 */
public class MyUrlsReduce extends TableReducer<Text, Text, Text> {
    public static final byte[] CF = "url".getBytes();
    public static final byte[] DOMAIN  = "domain".getBytes();
    public static final byte[] NAME  = "name".getBytes();
    public static final byte[] PAGE  = "page".getBytes();
    public static final byte[] REF  = "ref".getBytes();

    @Override
    public void reduce(Text key, Iterable<Text> values, Context context) {
        Put put = new Put(Bytes.toBytes(key.toString()));
        for (Text value : values) {
            String[] urls = value.toString().split(",");
            if (!urls[0].equals(" "))
                put.addColumn(CF, DOMAIN, Bytes.toBytes(urls[0]));
            if (!urls[1].equals(" "))
                put.addColumn(CF, NAME, Bytes.toBytes(urls[1]));
            if (!urls[2].equals(" "))
                put.addColumn(CF, PAGE, Bytes.toBytes(urls[2]));
            if (!urls[3].equals(" "))
                put.addColumn(CF, REF, Bytes.toBytes(urls[3]));
           /* put.addColumn(CF, DOMAIN, bytes.get(0));
            put.addColumn(CF, NAME, bytes.get(1));
            put.addColumn(CF, PAGE, bytes.get(2));
            put.addColumn(CF, REF, bytes.get(3));*/
            //  context.write(new ImmutableBytesWritable(Bytes.toBytes(key.toString())), put);
            try {
                context.write(key, put);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
