import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.nio.DefaultHttpClientIODispatch;
import org.apache.http.impl.nio.DefaultNHttpClientConnection;
import org.apache.http.impl.nio.pool.BasicNIOConnFactory;
import org.apache.http.impl.nio.pool.BasicNIOConnPool;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.nio.NHttpConnectionFactory;
import org.apache.http.nio.protocol.BasicAsyncRequestProducer;
import org.apache.http.nio.protocol.BasicAsyncResponseConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestExecutor;
import org.apache.http.nio.protocol.HttpAsyncRequester;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.protocol.*;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InterruptedIOException;

import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by user on 30.04.15.
 */
public class MyUrlCrawler {

    private static class MyUrl {
        String url;
        String hostname;
        String path;
        MyUrl(String url) {
            this.url = url;
            String hostname_path = url.substring(url.indexOf("//") + 2);
           /* try {
                URL uu = new URL(url);
                this.hostname = uu.getHost();
                this.path = uu.getFile();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }*/
            if (!hostname_path.contains("/")) {
                this.hostname = hostname_path;
                this.path = "/";
            } else {
                this.hostname = hostname_path.substring(0, hostname_path.indexOf("/"));
                this.path = hostname_path.substring(hostname_path.indexOf("/"));
            }
        }
    }


    //Follow redirects!!!
    public static Set<String> crawl(Set<String> existingUrls) throws Exception {
        // Create HTTP protocol processing chain
        HttpProcessor httpproc = HttpProcessorBuilder.create()
                // Use standard client-side protocol interceptors
               // .add(new RequestContent())
               // .add(new RequestTargetHost())
               // .add(new RequestConnControl())
               // .add(new RequestUserAgent("Mozilla/5.0 (X11; Linux x86_64) " +
               //         "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36"))
               // .add(new RequestExpectContinue(true))
                .build();
        final NHttpConnectionFactory<DefaultNHttpClientConnection> connFactory;
        // Create client-side HTTP protocol handler
        HttpAsyncRequestExecutor protocolHandler = new HttpAsyncRequestExecutor();
        // Create client-side I/O event dispatch
        final IOEventDispatch ioEventDispatch = new DefaultHttpClientIODispatch(protocolHandler,
                ConnectionConfig.DEFAULT);

        // Create client-side I/O reactor
        IOReactorConfig config = IOReactorConfig.custom()
                .setConnectTimeout(2000)
                .setSoTimeout(2000)
                .build();
        final ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(config);
        // Create HTTP connection pool
        BasicNIOConnPool pool = new BasicNIOConnPool(ioReactor, ConnectionConfig.DEFAULT);
        // Limit total number of connections to just two
        // Run the I/O reactor in a separate thread
        Thread t = new Thread(new Runnable() {

            public void run() {
               try {
                    // Ready to go!
                    ioReactor.execute(ioEventDispatch);
                } catch (InterruptedIOException ex) {
                    System.err.println("Interrupted");
                } catch (IOException e) {
                    System.err.println("I/O error: " + e.getMessage());
                }
                System.out.println("Shutdown");
            }

        });

        ArrayList<MyUrl> targets = new ArrayList<MyUrl>();
        for (String url : existingUrls) {
            targets.add(new MyUrl(url));
        }
        final Set<String> correctUrls = new HashSet<String>();

        // Start the client thread
        t.start();
        // Create HTTP requester
        HttpAsyncRequester requester = new HttpAsyncRequester(httpproc);
        // Execute HTTP GETs to the following hosts and
     /*   HttpHost[] targets = new HttpHost[] {
                new HttpHost("www.apache.org", 80, "http"),
                new HttpHost("www.verisign.com", 80, "http"),
                new HttpHost("www.google.com", 80, "http")
        };*/
        final CountDownLatch latch = new CountDownLatch(targets.size());
        final Map<String, Integer> ads_counter = new HashMap<String, Integer>();
        for (final MyUrl target: targets) {
            if (MyCrawlerMap.url_ads.contains(target.hostname))
                continue;
            BasicHttpRequest request = new BasicHttpRequest("GET", target.path);
            request.addHeader("Host", target.hostname);
            request.addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36");
            HttpCoreContext coreContext = HttpCoreContext.create();
            requester.execute(
                    new BasicAsyncRequestProducer(new HttpHost(target.hostname), request),
                    new BasicAsyncResponseConsumer(),
                    pool,
                    coreContext,
                    // Handle HTTP response from a callback
                    new FutureCallback<HttpResponse>() {

                        public void completed(final HttpResponse response) {
                            latch.countDown();
                         /*       if (response.containsHeader("Content-Length") && Integer.parseInt(response.getFirstHeader("Content-Length").getValue()) > 3000) {
                                    correctUrls.add(target.url);
                                    System.out.println(target.url + "  ---->  " + response.getStatusLine() + " ---ADDED");
                                } else {
                                    if (!response.containsHeader("Content-Length"))
                                        System.out.println(target.url + "  ---->  " + response.getStatusLine() + " ---REJECTED_NO_HEADER");
                                    else
                                        System.out.println(target.url + "  ---->  " + response.getStatusLine() + " ---REJECTED_SMALL");
                                }*/
                            try {
                                int response_code = response.getStatusLine().getStatusCode();
                                if (EntityUtils.toByteArray(response.getEntity()).length > 3000 && (response_code == 200 || response_code == 302)) {
                                    correctUrls.add(target.url);
                                    System.out.println(target.url + "  ---->  " + response.getStatusLine() + " +++ADDED");
                                }
                                else {
                                    System.out.println(target.url + "  ---->  " + response.getStatusLine() + " -ADS");
                                    if (ads_counter.containsKey(target.hostname)) {
                                        int count = ads_counter.get(target.hostname);
                                        ads_counter.put(target.hostname, count + 1);
                                        if (count == 9) {
                                            MyCrawlerMap.url_ads.add(target.hostname);
                                            System.out.println(target.url + "  ---->  " + response.getStatusLine() + " ---ADDED TO URL_ADS ");
                                        }
                                    } else
                                        ads_counter.put(target.hostname, 1);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        public void failed(final Exception ex) {
                            latch.countDown();
                            System.err.println(target.url + "  ---->  " + ex);
                        }

                        public void cancelled() {
                            latch.countDown();
                            System.err.println(target + " cancelled");
                        }


                    });
        }
        latch.await();
        System.out.println("Shutting down I/O reactor");
        ioReactor.shutdown();
        System.out.println("Done");
     /*   for (String url : existingUrls)
            if (correctUrls.contains(url))
                return url;
        return null;*/
        return correctUrls;
    }

}
