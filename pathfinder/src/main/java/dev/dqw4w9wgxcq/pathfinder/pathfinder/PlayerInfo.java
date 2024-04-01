package dev.dqw4w9wgxcq.pathfinder.pathfinder;

public class PlayerInfo
{
    private String name;
    private int world;
    private PlayerTime timestamp;

    public PlayerInfo()
    {
        // Default constructor
    }

    public PlayerInfo(String name, int world, PlayerTime timestamp)
    {
        this.name = name;
        this.world = world;
        this.timestamp = timestamp;
    }

    public String getName()
    {
        return name;
    }

    public int getWorld()
    {
        return world;
    }

    public PlayerTime getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(PlayerTime timestamp)
    {
        this.timestamp = timestamp;
    }
}
