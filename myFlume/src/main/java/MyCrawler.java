import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;

import java.io.BufferedWriter;
import java.io.IOException;
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
    public static void crawl(Set<String> existingUrls, BufferedWriter bw) {
        try {
            bw.write("Start attempt!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom().setRedirectStrategy(new LaxRedirectStrategy()).build();
        System.out.println("Crawl method!");
        concatenated_data = Collections.synchronizedSet(new HashSet<String>());
        httpclient.start();
        try {
            bw.write("Crawl method");
        } catch (IOException e) {
            e.printStackTrace();
        }
        final CountDownLatch latch = new CountDownLatch(existingUrls.size());
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
                bw.write("Nodes created!");
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                                  //      root.addNodeRecursive(url_parts, false);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    System.out.println(request.getRequestLine() +
                                            "  ---->  " + response.getStatusLine() + " +++ADDED");
                                } else {
                                    try {
                                  //      root.addNodeRecursive(url_parts, true);
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
       // root.formAdsSet();
        try {
            httpclient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
