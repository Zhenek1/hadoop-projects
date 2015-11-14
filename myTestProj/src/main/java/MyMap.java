import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

/**
 * Created by user on 02.04.15.
 */
public class MyMap extends Mapper<Object,Text,MyMapKey, FloatWritable> {

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        JSONObject obj, site, imp, banner;
        String url;
        int pos, w, h;
        float bidfloor;
        String[] myTextLines = value.toString().split("\n");
        String line = value.toString();
       // for(String line : myTextLines){
            obj = (JSONObject) JSONValue.parse(line);
            site = (JSONObject) obj.get("site");
            url = site.get("domain").toString();


            String exchange = obj.get("exchange").toString();
            if (exchange.equals("republer")) {
                if (!site.containsKey("page") && !site.containsKey("ref")) {
                    int k = 5;
                }
            }

            if (url.equals("") && exchange.equals("republer")) {
               // Не бывает
            }


            if (url.equals("") && site.containsKey("name")) {
                url = site.get("name").toString();
            }
            imp = (JSONObject)((JSONArray) obj.get("imp")).get(0);
            banner = (JSONObject) imp.get("banner");
            pos = Integer.parseInt(banner.get("pos").toString());
            w = Integer.parseInt(banner.get("w").toString());
            h = Integer.parseInt(banner.get("h").toString());
            bidfloor = Float.parseFloat(imp.get("bidfloor").toString());
            context.write(new MyMapKey(new Text(url), new IntWritable(pos),
                    new IntWritable(w), new IntWritable(h)), new FloatWritable(bidfloor));
        }

   // }
}

/*public class MyMap  {

    public static void main(String[] args) {
        byte[] encoded = new byte[0];
        try {
            encoded = Files.readAllBytes(Paths.get("/tmp/test.csv"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] myTextLines = new String[0];
        try {
            myTextLines = new String(encoded, "utf-8").toString().split("\n");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ArrayList<ArrayList<String>> myTestValues = new ArrayList<ArrayList<String>>();
        for (String line : myTextLines) {
            String[] values = line.split(",");
            myTestValues.add(new ArrayList<String>(Arrays.asList(values)));
        }
    }
}*/