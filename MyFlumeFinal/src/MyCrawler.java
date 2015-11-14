import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.util.Progressable;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by user on 29.05.15.
 */
public class MyCrawler {


    private static String getHostname(String url) {
        String hostname_path = url.substring(url.indexOf("//") + 2);
        if (!hostname_path.contains("/"))
            return hostname_path;
        else
            return hostname_path.substring(0, hostname_path.indexOf("/"));
    }

    private static Queue create_nodes_from_url(String url) {
        Queue parts_queue = new LinkedList();
        if (url.contains("?"))
            url = url.substring(0, url.indexOf("?"));
        String[] parts = url.split("/");
        String[] domain_parts = parts[0].split("\\.");
        for (int i = domain_parts.length - 1; i > 0; i --) {
            parts_queue.add(domain_parts[i] + "|");
        }
        parts_queue.add(domain_parts[0] + "*");
        for (int i = 1; i < parts.length; i ++) {
            parts_queue.add(parts[i]);
        }
        return parts_queue;
    }

    private static String invert_url_domain(String url) {
        String inverted_domain_url = "";
        String[] parts = url.split("/");
        String[] domain_parts = parts[0].split("\\.");
        for (int i = domain_parts.length - 1; i > 0; i --) {
            inverted_domain_url += domain_parts[i] + ".";
        }
        inverted_domain_url += domain_parts[0] + "/";
        for (int i = 1; i < parts.length; i ++) {
            inverted_domain_url += parts[i] + "/";
        }
        return inverted_domain_url;
    }

    private static void delete_cell_ids_table(Set<String> user_ids, String existingUrl) {
        try (Table table = MyJavaSink.hBaseConnection.getTable(TableName.valueOf("myIdUrlTable"))) {
            for (String user_id : user_ids) {
                Result result = null;
                Get get = new Get(user_id.getBytes());
                get.setMaxVersions(table.getTableDescriptor().getFamily("cf".getBytes()).getMaxVersions());
                result = table.get(get);

                if (result != null && !result.isEmpty()) {
                    List<Cell> cells = result.getColumnCells("cf".getBytes(), "url".getBytes());
                    for (Cell c : cells) {
                        System.out.println("CELL SIZE: " + cells.size());
                        if (Arrays.equals(CellUtil.cloneValue(c), existingUrl.getBytes())) {
                            Delete delete = new Delete(user_id.getBytes());
                            delete.addColumn("cf".getBytes(), "url".getBytes(), c.getTimestamp());
                            table.delete(delete);
                            System.out.println("DELETED: " + existingUrl + "   USER: " + user_id);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void put_cell_ids_table(Set<String> user_ids, String existingUrl) {
            for (String user_id : user_ids) {
                try (Table table = MyJavaSink.hBaseConnection.getTable(TableName.valueOf("myIdUrlTable"))) {
                    Put put = new Put(Bytes.toBytes(user_id));
                    put.addColumn("cf".getBytes(), "url".getBytes(), existingUrl.getBytes());
                    table.put(put);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }





    public static Set<String> concatenated_data = Collections.synchronizedSet(new HashSet<String>());
    public static MyNode root = new MyNode(true);
    public static void crawl(Map<String, Set<String>> existingSiteUrls_map, /*FileWriter fw*/ CloseableHttpAsyncClient httpclient, long prev_time) {
     //   CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom().setRedirectStrategy(new LaxRedirectStrategy()).build();
        System.out.println("Crawl method!");
     //   concatenated_data = Collections.synchronizedSet(new HashSet<String>());
       /* try {
            bw.write("Crawl method");
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        final CountDownLatch latch = new CountDownLatch(existingSiteUrls_map.size());
        for (final Map.Entry<String, Set<String>> entry : existingSiteUrls_map.entrySet()) {
            final String existingUrl = entry.getKey();
            try (Jedis jedis = MyJavaSink.pool.getResource()) {
                String time = jedis.hget("myUrlsHash", existingUrl);
                if (time != null) {
                    delete_cell_ids_table(entry.getValue(), existingUrl);
                    if (System.currentTimeMillis() / 1000 - Integer.parseInt(time) < 604800) {
                        put_cell_ids_table(entry.getValue(), existingUrl);
                        System.out.println("SKIPPED: " + existingUrl);
                        latch.countDown();
                        continue;
                    }
                }
            } catch (Exception e) {
                System.out.println("Exception in jedis SKIP!");
                e.printStackTrace();
            }

            final String tree_url;
            if (existingUrl.startsWith("https://"))
                tree_url = existingUrl.substring(  "https://".length(), existingUrl.length());
            else if (existingUrl.startsWith("http://"))
                tree_url = existingUrl.substring("http://".length(), existingUrl.length());
            else
                tree_url = existingUrl;

            final Queue<String> url_parts = create_nodes_from_url(tree_url);
            try {
                final HttpGet request = new HttpGet(URIUtil.encodeQuery(existingUrl));
                RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(3000)
                        .setConnectTimeout(3000)
                                //   .setConnectionRequestTimeout(1000)
                        .build();
             //   request.setConfig(requestConfig);
                request.addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36");
                httpclient.execute(request, new FutureCallback<HttpResponse>() {
                    public void completed(final HttpResponse response) {
                        latch.countDown();
                        int response_code = response.getStatusLine().getStatusCode();
                        try {
                            byte[] content = EntityUtils.toByteArray(response.getEntity());
                            System.out.println("CONTENT LENGTH: " + content.length);
                            if (response_code == 200) {
                                if (content.length > 3000) {
                                    /// ... do stuff here ... for example

                                    try (Jedis jedis = MyJavaSink.pool.getResource()) {
                                        jedis.hset("myUrlsHash", existingUrl, String.valueOf(System.currentTimeMillis() / 1000));
                                    }

                                    put_cell_ids_table(entry.getValue(), existingUrl);
                                    try (Table table = MyJavaSink.hBaseConnection.getTable(TableName.valueOf("myUrlContentTable"))) {
                                        Put put = new Put(Bytes.toBytes(existingUrl));
                                        put.addColumn("cf".getBytes(), "content".getBytes(), content);
                                        table.put(put);
                                    }
                                    root.addNodeRecursive(url_parts, false);
                                    System.out.println(request.getRequestLine() +
                                            "  ---->  " + response.getStatusLine() + " +++ADDED");
                                } else {
                                    try {
                                        root.addNodeRecursive(url_parts, true);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    System.out.println(request.getRequestLine() +
                                            "  ---->  " + response.getStatusLine() + " -ADS");
                                }
                            } else {
                                System.out.println(request.getRequestLine() +
                                        "  ---->  " + response.getStatusLine() + " Response code not 200");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (OutOfMemoryError er) {
                            System.out.println("OUT OF MEMORY ERROR");
                            System.out.println(request.getRequestLine() +  "  ---->  " + response.getStatusLine() +
                                    "  ---->  "/* + response.getFirstHeader("Content-length").getValue()*/);
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }

                    public void failed(final Exception ex) {
                        latch.countDown();
                        System.err.println(request.getRequestLine() + "->" + ex);
                    }

                    public void cancelled() {
                        latch.countDown();
                        System.err.println(request.getRequestLine() + " cancelled");
                    }

                });
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (URIException e) {
                e.printStackTrace();
            }
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Shutting down");

        long curr_time = System.currentTimeMillis();
        int elapsed_time = (int)((curr_time - prev_time) / 1000);
        System.out.println(elapsed_time);
        if ((elapsed_time % 10 == 0 || elapsed_time / 10 > 0) && elapsed_time != 0) {
            MyJavaSink.prev_time = curr_time;
            concatenated_data.clear();
            root.formAdsSet();
/*
            if (MyJavaSink.file.exists())
                MyJavaSink.file.delete();
            try {
                if (!MyJavaSink.file.exists())
                    MyJavaSink.file.createNewFile();*/

         /*   try {
                fw = new FileWriter(MyJavaSink.file.getAbsoluteFile());
                fw.write("");
            } catch (IOException e) {
                e.printStackTrace();
            }
            BufferedWriter bw = new BufferedWriter(fw);

            for (String s : concatenated_data) {
                try {
                    bw.write(s);
                    bw.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                bw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/

            try (OutputStream outputStream = MyJavaSink.hdfs.create(MyJavaSink.file);
                 BufferedWriter br = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"))) {
                for (String s : concatenated_data) {
                    br.write(s);
                    br.newLine();
                }
                br.flush();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        /*    BufferedWriter br = null;
        OutputStream outputStream = MyJavaSink.hdfs.create(MyJavaSink.file);
            try {
                br = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            for (String s : concatenated_data) {
                try {
                    br.write(s);
                    br.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                br.flush();
               // outputStream.close();
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
      /*  try {
            httpclient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        }
    }
}
