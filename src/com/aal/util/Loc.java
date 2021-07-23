package com.aal.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Loc {
    private final double x, y, z;
    private final float yaw, pitch;

    public Loc(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Loc() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.yaw = 0;
        this.pitch = 0;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public double distance(Loc o) {
        double deltaX = this.getX() - o.getX();
        double deltaY = this.getY() - o.getY();
        double deltaZ = this.getZ() - o.getZ();
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    public Location toBukkit(Player player) {
        return new Location(player.getWorld(), x, y, z, yaw, pitch);
    }

    public Loc add(double x, double y, double z) {
        return new Loc(this.x + x, this.y + y, this.z + z, this.yaw, this.pitch);
    }

    public Block blockAt(Player player, double x, double y, double z) {
        return add(x, y, z).toBukkit(player).getBlock();
    }
}
