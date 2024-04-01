package dev.dqw4w9wgxcq.pathfinder.pathfinder.redis;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPooled;

@Slf4j
public class RedisPlayerDataStore {
    private static final Gson gson = new Gson();
    private final JedisPooled jedisPooled;

    public RedisPlayerDataStore(JedisPooled jedisPooled) {
        this.jedisPooled = jedisPooled;
    }

    public void savePlayerObject(String key, Object playerObject) {
        try {
            String json = gson.toJson(playerObject);
            jedisPooled.set(key, json);
        } catch (Exception e) {
            log.error("Error saving player object to Redis", e);
        }
    }

    public <T> T getPlayerObject(String key, Class<T> type) {
        try {
            String json = jedisPooled.get(key);
            return gson.fromJson(json, type);
        } catch (Exception e) {
            log.error("Error retrieving player object from Redis", e);
            return null;
        }
    }

    public JedisPooled getJedisPooled() {
        return jedisPooled;
    }
}
