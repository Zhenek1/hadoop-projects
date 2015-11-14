import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.api.RpcClient;
import org.apache.flume.api.RpcClientFactory;
import org.apache.flume.event.EventBuilder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Created by user on 15.05.15.
 */
class MyRpcClientFacade {
    private RpcClient client;
    private String hostname;
    private int port;

    public void init(String hostname, int port) {
        // Setup the RPC connection
        this.hostname = hostname;
        this.port = port;
        this.client = RpcClientFactory.getDefaultInstance(hostname, port);
        // Use the following method to create a thrift client (instead of the above line):
        // this.client = RpcClientFactory.getThriftInstance(hostname, port);
    }

    public void sendDataToFlume(String data) {
        // Create a Flume Event object that encapsulates the sample data
        Event event = EventBuilder.withBody(data, Charset.forName("UTF-8"));

        // Send the event
        try {
            client.append(event);
        } catch (EventDeliveryException e) {
            // clean up and recreate the client
            client.close();
            client = null;
            client = RpcClientFactory.getDefaultInstance(hostname, port);
            // Use the following method to create a thrift client (instead of the above line):
            // this.client = RpcClientFactory.getThriftInstance(hostname, port);
        }
    }

    public void cleanUp() {
        // Close the RPC connection
        client.close();
    }

    public static void main(String[] args) {
        MyRpcClientFacade client = new MyRpcClientFacade();

        // Initialize client with the remote Flume agent's host and port
        client.init("localhost", 41414);
        try {
            File file = new File("/home/user/IdeaProjects/myFlume/xaa");

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    client.sendDataToFlume(line);
                }
            } catch (IOException x) {
                System.err.format("IOException: %s%n", x);
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Send 10 events to the remote Flume agent. That agent should be
        // configured to listen with an AvroSource.

       /* String sampleData = "Hello Flume!";
        for (int i = 0; i < 10; i++) {
            client.sendDataToFlume(sampleData);
        }*/

        client.cleanUp();

    }
}