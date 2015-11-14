import com.sun.tools.javac.util.Name;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Mapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;

/**
 * Created by user on 22.04.15.
 */
/*public class MyUrlsMap extends Mapper<Object, Text, Text, Text> {

    @Override
    public void map(Object key, Text value, Context context)  {
        JSONObject obj, site, user;
        String domain, name, page, ref;
        String user_id;
        String[] myTextLines = value.toString().split("\n");
        for(String line : myTextLines){
            obj = (JSONObject) JSONValue.parse(line);

            user = (JSONObject) obj.get("user");
            user_id = user.get("id").toString();
            String exchange = obj.get("exchange").toString();
            user_id += ":" + exchange;

            site = (JSONObject) obj.get("site");
            domain = " ";
            name = " ";
            page = " ";
            ref = " ";
            if (!site.get("domain").equals(""))
                domain = site.get("domain").toString();
            if (site.containsKey("name") && !site.get("name").toString().equals(""))
                name = site.get("name").toString();
            if (site.containsKey("page") && !site.get("page").toString().equals(""))
                page = site.get("page").toString();
            if (site.containsKey("ref") && !site.get("ref").toString().equals(""))
                ref = site.get("ref").toString();

            try {
                context.write(new Text(user_id), new Text(domain + "," + name + "," + page + "," + ref));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}*/
public class MyUrlsMap extends Mapper<LongWritable, Text, ImmutableBytesWritable, Put> {

    public static final byte[] CF = "url".getBytes();
    public static final byte[] DOMAIN  = "domain".getBytes();
    public static final byte[] NAME  = "name".getBytes();
    public static final byte[] PAGE  = "page".getBytes();
    public static final byte[] REF  = "ref".getBytes();

    @Override
    public void map(LongWritable key, Text value, Context context)  {
        JSONObject obj, site, user;
        String domain, name, page, ref;
        String user_id;
        String[] myTextLines = value.toString().split("\n");
        int i = 0;
        String line = value.toString();
     //   for(String line : myTextLines){
            obj = (JSONObject) JSONValue.parse(line);

            user = (JSONObject) obj.get("user");
            if (user.containsKey("id")) {
                user_id = user.get("id").toString();
            } else {
                return;
            }
            user_id = user.get("id").toString();
            if (!obj.containsKey("exchange")) {
                int k = 5;
            }
            String exchange = obj.get("exchange").toString();
            user_id += ":" + exchange;

            site = (JSONObject) obj.get("site");
            Put put = new Put(Bytes.toBytes(user_id));
          //  Put put = new Put(Bytes.toBytes(user_id));
            if (!site.get("domain").equals("")) {
                domain = site.get("domain").toString();
                put.addColumn(CF, DOMAIN, Bytes.toBytes(domain));
            }
            if (site.containsKey("name") && !site.get("name").toString().equals("")) {
                name = site.get("name").toString();
                put.addColumn(CF, NAME, Bytes.toBytes(name));
            }
            if (site.containsKey("page") && !site.get("page").toString().equals("")) {
                page = site.get("page").toString();
                put.addColumn(CF, PAGE, Bytes.toBytes(page));
            }
            if (site.containsKey("ref") && !site.get("ref").toString().equals("")) {
                ref = site.get("ref").toString();
                put.addColumn(CF, REF, Bytes.toBytes(ref));
            }

            try {
                context.write(new ImmutableBytesWritable(Bytes.toBytes(user_id)), put);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

   // }
}