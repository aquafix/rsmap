package dev.dqw4w9wgxcq.pathfinder.pathfinder.redis;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.dqw4w9wgxcq.pathfinder.commons.domain.step.Step;
import dev.dqw4w9wgxcq.pathfinder.pathfinder.PlayerInfo;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPooled;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    // Method to get player info by name
    public PlayerInfo getPlayerByName(String playerName) {
        String key = "player:" + playerName;
        return getPlayerObject(key, PlayerInfo.class);
    }

    // Method to get players on a specific world
    public List<PlayerInfo> getPlayersByWorld(int world) {
        List<PlayerInfo> playersOnWorld = new ArrayList<>();
        try {
            Set<String> keys = jedisPooled.keys("player:*");
            for (String key : keys) {
                PlayerInfo player = getPlayerObject(key, PlayerInfo.class);
                if (player != null && player.getWorld() == world) {
                    playersOnWorld.add(player);
                }
            }
        } catch (Exception e) {
            log.error("Error retrieving players on world {} from Redis", world, e);
        }
        return playersOnWorld;
    }

    // Method to get players by timestamp (PlayerTime.time)
    public List<PlayerInfo> getPlayersByTimestamp(long timestamp) {
        List<PlayerInfo> playersByTimestamp = new ArrayList<>();
        try {
            Set<String> keys = jedisPooled.keys("player:*");
            for (String key : keys) {
                PlayerInfo player = getPlayerObject(key, PlayerInfo.class);
                if (player != null && player.getTimestamp().getTime() == timestamp) {
                    playersByTimestamp.add(player);
                }
            }
        } catch (Exception e) {
            log.error("Error retrieving players by timestamp {} from Redis", timestamp, e);
        }
        return playersByTimestamp;
    }

    public JedisPooled getJedisPooled() {
        return jedisPooled;
    }
    public void saveExplorer(String stepsKey, List<Step> steps) {
        try  {
            // Serialize the list of steps to JSON
            String json = gson.toJson(steps);
            // Save the JSON string to Redis with the specified key
            jedisPooled.set(stepsKey, json);
        } catch (Exception e) {
            log.error("Error saving explorer steps to Redis", e);
        }
    }

    public List<Step> getExplorerSteps(String stepsKey) {
        List<Step> explorerSteps = new ArrayList<>();
        try  {
            // Retrieve the JSON string from Redis using the specified key
            String json = jedisPooled.get(stepsKey);
            // Deserialize the JSON string to a list of steps
            if (json != null) {
                Type listType = new TypeToken<List<Step>>() {}.getType();
                explorerSteps = gson.fromJson(json, listType);
            }
        } catch (Exception e) {
            log.error("Error retrieving explorer steps from Redis", e);
        }
        return explorerSteps;
    }

}
