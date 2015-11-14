import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by user on 07.05.15.
 */
public class MyUrlCrawler2 {


    public static String getHostname(String url) {
        String hostname_path = url.substring(url.indexOf("//") + 2);
        if (!hostname_path.contains("/"))
            return hostname_path;
        else
            return hostname_path.substring(0, hostname_path.indexOf("/"));
    }

    private static Queue create_nodes_from_url (String url) {
        Queue parts_queue = new LinkedList();
        String[] parts = url.split("/");
        String[] domain_parts = parts[0].split("\\.");
        for (int i = domain_parts.length - 1; i > 0; i --) {
            parts_queue.add(domain_parts[i] + "|");
        }
        parts_queue.add(domain_parts[0]);
        for (int i = 1; i < parts.length; i ++) {
            parts_queue.add(parts[i]);
        }
        return parts_queue;
    }

    public static Set<String> concatenated_data;
    public static MyNode root = new MyNode(true);
    public static HashMap<String, String> crawl(Set<String> existingUrls) throws InterruptedException, IOException {
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom().setRedirectStrategy(new LaxRedirectStrategy()).build();
        System.out.println("Crawl method!");
        concatenated_data = Collections.synchronizedSet(new HashSet<String>());
        httpclient.start();
        final CountDownLatch latch = new CountDownLatch(existingUrls.size());
        final Set<String> correctUrls = new HashSet<String>();
        final HashMap<String, String> correctUrls_content = new HashMap<String, String>();
        final Map<String, Integer> ads_counter = new HashMap<String, Integer>();
        int counter = 0;
        for (final String existingUrl : existingUrls) {
            final String tree_url;
            if (existingUrl.startsWith("https://"))
                tree_url = existingUrl.substring("https://".length(), existingUrl.length());
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
                request.setConfig(requestConfig);
                request.addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36");
                httpclient.execute(request, new FutureCallback<HttpResponse>() {
                    public void completed(final HttpResponse response) {
                        latch.countDown();
                        int response_code = response.getStatusLine().getStatusCode();
                        try {
                            byte[] content = EntityUtils.toByteArray(response.getEntity());
                            if (response_code == 200) {
                                if (content.length > 3000) {
                                    try {
                                        //     root.addNodeRecursive(Arrays.asList(tree_url.split("/")), false);
                                        if (request.getRequestLine().toString().contains(
                                                "yaplakal.com")) {
                                            root.addNodeRecursive(url_parts, false);
                                        }
                                        root.addNodeRecursive(url_parts, false);
                                    } catch (Exception e) {
                                        int b = 5;
                                    }
                                    String body = Jsoup.parse(new String(content)).body().text();
                                    correctUrls_content.put(existingUrl, body);
                                    System.out.println(request.getRequestLine() + "  ---->  " + response.getStatusLine() + " +++ADDED");
                                } else {
                                    try {
                                        if (request.getRequestLine().toString().contains(
                                                "yaplakal.com")) {
                                            root.addNodeRecursive(url_parts, true);
                                        }
                                        //     root.addNodeRecursive(Arrays.asList(tree_url.split("/")), true);
                                        root.addNodeRecursive(url_parts, true);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        int b = 5;
                                    }
                                    System.out.println(request.getRequestLine() + "  ---->  " + response.getStatusLine() + " -ADS");
                                    if (ads_counter.containsKey(getHostname(existingUrl))) {
                                        int count = ads_counter.get(getHostname(existingUrl));
                                        ads_counter.put(getHostname(existingUrl), count + 1);
                                        if (count == 9) {
                                            MyCrawlerMap.url_ads.add(getHostname(existingUrl));
                                            System.out.println(request.getRequestLine() + "  ---->  " + response.getStatusLine() + " ---ADDED TO URL_ADS ");
                                        }
                                    } else
                                        ads_counter.put(getHostname(existingUrl), 1);
                                }
                            } else {
                                System.out.println(request.getRequestLine() + "  ---->  " + response.getStatusLine() + " Response code not 200");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
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
                boolean b = existingUrls.contains(existingUrl);
                int k = 5;
            }
        }
        latch.await();
        System.out.println("Shutting down");
        root.formAdsSet();
        for (String s :concatenated_data)
            System.out.println(s);
        System.exit(0);
        httpclient.close();
        return correctUrls_content;
    }
}
