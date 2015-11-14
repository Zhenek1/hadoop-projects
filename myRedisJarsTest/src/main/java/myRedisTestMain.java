import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;

/**
 * Created by user on 21.05.15.
 */
public class myRedisTestMain {
    static JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");
    public static void main(String[] args) {
        try (Jedis jedis = pool.getResource()) {
            /// ... do stuff here ... for example
            jedis.rpush("a", "b");
        }
    }
}
