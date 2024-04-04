package dev.dqw4w9wgxcq.pathfinder.pathfinder;

import com.google.gson.Gson;
import dev.dqw4w9wgxcq.pathfinder.commons.domain.step.Step;
import dev.dqw4w9wgxcq.pathfinder.commons.domain.step.WalkStep;
import dev.dqw4w9wgxcq.pathfinder.pathfinder.redis.RedisPlayerDataStore;
import net.runelite.api.Point;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.JedisPooled;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PlayerInfoHandler {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(PlayerInfoHandler.class);
    private final Gson gson = new Gson();
private String host = RemoteTilePathfinder.redHost;
private int port = RemoteTilePathfinder.redPort;
    private final RedisPlayerDataStore redisPlayerDataStore;
    private final JedisPooled jedisPooled;
    private static final String PLAYER_PREFIX = "player:";
    // List to store explorers
    private List<PlayerInfo> explorers = new ArrayList<>();

    // Method to add an explorer to the list
    public void addExplorer(PlayerInfo explorer) {
        explorers.add(explorer);
    }

    // Method to check if a player is an explorer
    private boolean isExplorer(PlayerInfo playerInfo) {
        return explorers.contains(playerInfo);
    }
    public PlayerInfoHandler() {
        var poolConfig = new ConnectionPoolConfig();
        poolConfig.setMaxTotal(8);
        poolConfig.setMaxIdle(8);
        poolConfig.setMinIdle(0);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setMaxWait(Duration.ofSeconds(1));
        poolConfig.setTestWhileIdle(true);
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(1));
        this.jedisPooled = new JedisPooled(poolConfig, host, port);
        this.redisPlayerDataStore = new RedisPlayerDataStore(this.jedisPooled);
    }

    public void handlePlayerInfo(PlayerInfo playerInfo) {
        try {
            logger.info("Received player info: {}", playerInfo);

            // Check if the received player info belongs to an explorer
            if (isExplorer(playerInfo)) {
                // Handle explorer player info
                // String stepsKey = "explorer_steps:" + playerInfo.getName();
                String stepsKey = "explorer_steps:" + playerInfo.getName();
                handleExplorerPlayerInfo(playerInfo, redisPlayerDataStore.getExplorerSteps(stepsKey));
            } else {
                // Handle regular player info
                String key = PLAYER_PREFIX + playerInfo.getName();
                redisPlayerDataStore.savePlayerObject(key, playerInfo);
                logger.info("Stored player info for player {} in Redis", playerInfo.getName());
            }
        } catch (Exception e) {
            logger.error("Error processing player info request", e);
            // Handle the error as needed
        }
    }
    public void handleExplorerPlayerInfo(PlayerInfo playerInfo, List<Step> steps) {
        try {
            logger.info("Received explorer player info: {}", playerInfo);

            // Save the explorer player info to Redis
            String key = PLAYER_PREFIX + playerInfo.getName();
            redisPlayerDataStore.savePlayerObject(key, playerInfo);
            logger.info("Stored explorer player info for player {} in Redis", playerInfo.getName());

            // Convert explorer's current location to a step
            Point currentLocation = new Point(playerInfo.getTimestamp().getX(), playerInfo.getTimestamp().getY());
            Step currentStep = convertLocationToStep(currentLocation);

            // Remove completed steps and steps behind the current location
            List<Step> updatedSteps = removeCompletedAndMissedSteps(steps, currentStep);

            // Save the adjusted list of steps for the explorer
            String stepsKey = "explorer_steps:" + playerInfo.getName();
            redisPlayerDataStore.saveExplorer(stepsKey, updatedSteps);
            logger.info("Stored adjusted steps for explorer player {} in Redis", playerInfo.getName());

            // Add the explorer to the list of explorers
            addExplorer(playerInfo);
        } catch (Exception e) {
            logger.error("Error processing explorer player info request", e);
            // Handle the error as needed
        }
    }
    private List<Step> removeCompletedAndMissedSteps(List<Step> steps, Step currentStep) {
        List<Step> remainingSteps = new ArrayList<>();
        boolean foundCurrentStep = false;
        for (Step step : steps) {
            if (!foundCurrentStep && step.equals(currentStep)) {
                foundCurrentStep = true;
            }
            if (foundCurrentStep) {
                remainingSteps.add(step);
            }
        }
        return remainingSteps;
    }



    private WalkStep convertLocationToStep(Point currentLocation) {
        // Convert net.runelite.api.Point instances to dev.dqw4w9wgxcq.pathfinder.commons.domain.Point instances
        List<dev.dqw4w9wgxcq.pathfinder.commons.domain.Point> path = new ArrayList<>();
        path.add(new dev.dqw4w9wgxcq.pathfinder.commons.domain.Point(currentLocation.getX(), currentLocation.getY()));

        // Create and return a new WalkStep instance
        return new WalkStep(true, 0, 0, path);
    }



    private boolean isStepAhead(WalkStep step, WalkStep currentStep) {
        // Get the last point in the path of the step and currentStep
        dev.dqw4w9wgxcq.pathfinder.commons.domain.Point lastStepPoint = step.path().get(step.path().size() - 1);
        dev.dqw4w9wgxcq.pathfinder.commons.domain.Point lastCurrentStepPoint = currentStep.path().get(currentStep.path().size() - 1);

        // Compare the positions of the last points
        return lastStepPoint.x() > lastCurrentStepPoint.x() ||
                (lastStepPoint.x() == lastCurrentStepPoint.x() && lastStepPoint.y() > lastCurrentStepPoint.y());
    }



    public PlayerInfo getPlayerInfoByName(String playerName) {
        try {
            return redisPlayerDataStore.getPlayerByName(playerName);
        } catch (Exception e) {
            logger.error("Error retrieving player info for player {} from Redis", playerName, e);
            return null;
        }
    }

    public List<PlayerInfo> getPlayersByWorld(int world) {
        try {
            return redisPlayerDataStore.getPlayersByWorld(world);
        } catch (Exception e) {
            logger.error("Error retrieving players for world {} from Redis", world, e);
            return new ArrayList<>();
        }
    }

    public List<PlayerInfo> getPlayersByTimestamp(long timestamp) {
        try {
            return redisPlayerDataStore.getPlayersByTimestamp(timestamp);
        } catch (Exception e) {
            logger.error("Error retrieving players for timestamp {} from Redis", timestamp, e);
            return new ArrayList<>();
        }
    }
}
