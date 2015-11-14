import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by user on 30.04.15.
 */
public class MyCrawlerMap extends Mapper<LongWritable, Text, ImmutableBytesWritable, Put> {

    public static final byte[] CF = "url".getBytes();
    public static final byte[] USER_ID = "user_id".getBytes();
    public static final byte[] CONTENT = "content".getBytes();
    public static final byte[] DOMAIN  = "domain".getBytes();
    public static final byte[] NAME  = "name".getBytes();
    public static final byte[] PAGE  = "page".getBytes();
    public static final byte[] REF  = "ref".getBytes();
    public static Set<String> url_ads = new HashSet<String>();

    public static List<String> getExistingURLs(JSONObject site) {
        List<String> existing_urls = new ArrayList<String>();
        if (site.containsKey("page")) {
            if (site.get("page").equals("") && site.containsKey("name") && !site.get("name").equals("")) {
                existing_urls.add("http://" + site.get("name").toString());
            }
            else
               // if (!site.get("page").toString().contains("ads.betweendigital.com"))
               existing_urls.add(site.get("page").toString());
        }
        if (site.containsKey("ref") && (site.get("ref").toString().contains("http://") || site.get("ref").toString().contains("https://"))
               // && !site.get("ref").toString().contains("ads.betweendigital.com")
               )
            existing_urls.add(site.get("ref").toString());
        if (!site.get("domain").equals(""))
            existing_urls.add("http://" + site.get("domain").toString());
        return existing_urls;
    }

    public static List<String> removeQueries(List<String> existing_urls) {
        List<String> l = new ArrayList<String>();
        for (String url : existing_urls)
            if (url.contains("?")) {
                l.add(url.substring(0, url.indexOf("?")));
            } else {
                l.add(url);
            }
        return l;
    }

    public void writeToHbase(String user_id, String url, String content, Context context) {
        Put put = new Put(Bytes.toBytes(user_id));
       // put.addColumn(CF, url.getBytes(), Bytes.toBytes(content));
        put.addColumn(CF, "URL".getBytes(), Bytes.toBytes(url));
        put.addColumn(CF, "CONTENT".getBytes(), Bytes.toBytes(content));
        try {
            context.write(new ImmutableBytesWritable(Bytes.toBytes(user_id)), put);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void map(LongWritable key, Text value, Context context) {

        System.out.println("Starting map job");

        JSONObject obj, site, user;
        String domain, name, page, ref;
        String user_id;
        //   String line = value.toString();
        String lines = value.toString();
        String[] lineArr = lines.split("\n");
        Map<String, List<List<String>>> id_urls_map = new HashMap<String, List<List<String>>>();
        Set<String> existingUrls = new HashSet<String>();
        HashMap<String, String> correctUrls_content = new HashMap<String, String>();
        for (String line : lineArr) {
            obj = (JSONObject) JSONValue.parse(line);
            user = (JSONObject) obj.get("user");
            if (user.containsKey("id") && obj.containsKey("site")) {
                user_id = user.get("id").toString();
                site = (JSONObject) obj.get("site");
            } else {
                continue;
            }
            String exchange = obj.get("exchange").toString();
            user_id += ":" + exchange;
            List<String> existingSiteUrls = getExistingURLs(site);
          //  existingUrls.addAll(existingSiteUrls);
            existingUrls.addAll(removeQueries(existingSiteUrls));
            if (!id_urls_map.containsKey(user_id)) {
                List<List<String>> arr = new ArrayList<List<String>>();
                arr.add(existingSiteUrls);
                id_urls_map.put(user_id, arr);
            } else {
                id_urls_map.get(user_id).add(existingSiteUrls);
            }
        }
        int k = 0;
        try {
            correctUrls_content = MyUrlCrawler2.crawl(existingUrls);
           // correctUrls = MyUrlCrawler.crawl(existingUrls);
            k += correctUrls_content.size();
            System.out.println("TOTAL SIZE: " + k);
            System.out.println("TOTAL SIZE: " + k);
            System.out.println("TOTAL SIZE: " + k);
            System.out.println("TOTAL SIZE: " + k);
            System.out.println("TOTAL SIZE: " + k);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Map.Entry<String, List<List<String>>> entry : id_urls_map.entrySet()) {
            String us_id = entry.getKey();
            List<List<String>> urls = entry.getValue();
            for (List<String> l : urls) {
                for (String url : l)
                    if (correctUrls_content.containsKey(url)) {
                        writeToHbase(us_id, url, correctUrls_content.get(url), context);
                        break;
                    }
            }
        }

           // List<String> existingUrls = getExistingURLs(site);
     /*       try {
                correctUrl = MyUrlCrawler.crawl(existingUrls);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (correctUrl != null) {
                Put put = new Put(Bytes.toBytes(user_id));
                put.addColumn(CF, "URL".getBytes(), Bytes.toBytes(correctUrl));
                try {
                    context.write(new ImmutableBytesWritable(Bytes.toBytes(user_id)), put);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/
    }
}
