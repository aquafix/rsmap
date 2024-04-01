package dev.dqw4w9wgxcq.pathfinder.pathfinder;

import com.google.gson.Gson;
import dev.dqw4w9wgxcq.pathfinder.pathfinder.redis.RedisPlayerDataStore;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPooled;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class PlayerInfoHandler {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(PlayerInfoHandler.class);
    private final Gson gson = new Gson();
private String host = RemoteTilePathfinder.redHost;
private int port = RemoteTilePathfinder.redPort;
    private final RedisPlayerDataStore redisPlayerDataStore;
private JedisPooled jedisPooled;
    public PlayerInfoHandler() {
        var poolConfig = new ConnectionPoolConfig();
        poolConfig.setMaxTotal(8);
        poolConfig.setMaxIdle(8);
        poolConfig.setMinIdle(0);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setMaxWait(Duration.ofSeconds(1));
        poolConfig.setTestWhileIdle(true);
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(1));
        //new RedisCache("127.0.0.1", 6379);
        this.jedisPooled = new JedisPooled(poolConfig, host, port);
        this.redisPlayerDataStore = new RedisPlayerDataStore(this.jedisPooled);




    }

    public void handlePlayerInfo(PlayerInfo playerInfo) {
        try {
            logger.info("Received player info: {}", playerInfo);


                // If player doesn't exist in Redis, save the new player info directly
                redisPlayerDataStore.savePlayerObject("player:" + playerInfo.getName(), playerInfo);
                logger.info("Stored player info for player {} in Redis", playerInfo.getName());

        } catch (Exception e) {
            logger.error("Error processing player info request", e);
            // Handle the error as needed
        }
    }


    public PlayerInfo getPlayerInfo(int playerId) {
        try  {
            return redisPlayerDataStore.getPlayerObject("player:" + playerId, PlayerInfo.class);
        } catch (Exception e) {
            logger.error("Error retrieving player info from Redis", e);
            return null; // Or handle the error as needed
        }
    }
}
