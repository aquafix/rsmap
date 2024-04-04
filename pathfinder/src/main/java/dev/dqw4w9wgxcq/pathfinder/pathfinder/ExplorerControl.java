package dev.dqw4w9wgxcq.pathfinder.pathfinder;


import dev.dqw4w9wgxcq.pathfinder.commons.domain.Agent;
import dev.dqw4w9wgxcq.pathfinder.commons.domain.Position;
import dev.dqw4w9wgxcq.pathfinder.commons.domain.pathfinding.PathfinderResult;
import dev.dqw4w9wgxcq.pathfinder.commons.domain.step.Step;
import dev.dqw4w9wgxcq.pathfinder.pathfinder.redis.RedisPlayerDataStore;
import net.runelite.api.Point;
import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.JedisPooled;

import java.time.Duration;
import java.util.*;

public class ExplorerControl {

    private int tileSize;
    private String host = RemoteTilePathfinder.redHost;
    private int port = RemoteTilePathfinder.redPort;
    private Set<Point> exploredTiles;
    private Map<String, Point> botPaths; // Track bot paths
    private Map<PlayerInfo, Point> botPositions;
    private Random random;
    private f2pCoords F2pCoords;
private Pathfinder aPathFinder = null;
    public ExplorerControl(int tileSize, Pathfinder aPathFinder) {
        this.tileSize = tileSize;

        this.exploredTiles = new HashSet<>();
        this.botPaths = new HashMap<>();
        this.botPositions = (Map<PlayerInfo, Point>) new HashSet<>();
        this.random = new Random();
        this.aPathFinder = aPathFinder;


    }

    public List<Step> getNextPoint(PlayerInfo botInfo) {
        Point explorationPoint;
        Point botPosition = new Point(botInfo.getTimestamp().getX(), botInfo.getTimestamp().getY());
        String botName = botInfo.getName();

        // Keep generating exploration points until a successful path is found
        while (true) {
            explorationPoint = generateExplorationPoint();

            // Update bot position
            botPositions.put(botInfo, botPosition);

            // Find path using the Pathfinder
            Position startPosition = new Position(botPosition.getX(), botPosition.getY(), 0);
            Position endPosition = new Position(explorationPoint.getX(), explorationPoint.getY(), 0);
            Agent tempAgent = new Agent(0, null, null);
            PathfinderResult pathResult = aPathFinder.findPath(startPosition, endPosition, tempAgent, Algo.A_STAR);

            // Handle the path result
            if (pathResult instanceof PathfinderResult.Success) {
                PathfinderResult.Success successResult = (PathfinderResult.Success) pathResult;
                // Return the successful path
                return successResult.steps();
            }
        }
    }


    private Point generateExplorationPoint() {
        // Generate a random index to select a coordinate from the array
        int randomIndex = random.nextInt(f2pCoords.coordinates.length);
        int[] temp = f2pCoords.coordinates[randomIndex];
        return new Point(temp[0],temp[1]);
    }

    private boolean isPointSuitable(Point coordinate) {
        // Check if the coordinate is explored, too close to other bots, or their paths
        return !isPointExplored(coordinate) && !isPointTooCloseToBots(coordinate) &&
                !isPointTooCloseToBotPaths(coordinate);
    }

    private boolean isPointExplored(Point coordinate) {
        return exploredTiles.contains(getTilePoint(coordinate));
    }

    private void markTileAsExplored(Point coordinate) {
        exploredTiles.add(getTilePoint(coordinate));
    }

    private Point getTilePoint(Point coordinate) {
        int tileX = coordinate.getX() / tileSize;
        int tileY = coordinate.getY() / tileSize;
        return new Point(tileX * tileSize + tileSize / 2, tileY * tileSize + tileSize / 2);
    }

    private boolean isPointTooCloseToBots(Point coordinate) {
        for (Point botPosition : botPositions.values()) {
            if (coordinate.distanceTo(botPosition) < tileSize) {
                return true; // Point is too close to an explorer
            }
        }
        return false;
    }


    private boolean isPointTooCloseToBotPaths(Point coordinate) {
        for (Point path : botPaths.values()) {
            if (coordinate.distanceTo(path) < tileSize) {
                return true; // Point is too close to an explorer path
            }
        }
        return false;
    }


    public void updateBotPositions(Map<PlayerInfo, Point> botPositions) {
        this.botPositions = botPositions; // Update bot positions
    }

    public void updateBotPaths(Map<String, Point> botPaths) {
        // Remove old paths of the explorer
        botPaths.forEach((botName, path) -> {
            if (this.botPaths.containsKey(botName)) {
                exploredTiles.remove(this.botPaths.get(botName));
            }
        });

        // Update bot paths
        this.botPaths = botPaths;
    }

}