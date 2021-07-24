package com.aal.util;

import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class Util18 {
    public static boolean isCollided(Player p, double x, double y, double z) {
        AxisAlignedBB aabb = new AxisAlignedBB(x - 0.3f, y, z - 0.3f, x + 0.3f, y + 1.8f, z + 0.3f);
        EntityPlayer player = ((CraftPlayer)p).getHandle();
        World world = player.getWorld();
        return !world.getCubes(player, aabb).isEmpty();
    }

    public static double fixY(Player p, double x, double y, double z) {
        AxisAlignedBB aabb = new AxisAlignedBB(x - 0.3f, y, z - 0.3f, x + 0.3f, y + 1.8f, z + 0.3f);
        EntityPlayer player = ((CraftPlayer)p).getHandle();
        World world = player.getWorld();
        double bestY = -69;
        for (AxisAlignedBB aabb2: world.getCubes(player, aabb)) {
            if (bestY == -69) {
                if (aabb2.e > y)
                    bestY = aabb2.e;
                else if (aabb2.b < y + 1.8f)
                    bestY = aabb2.b - 1.8f;
            }
            if (bestY > y + 1.8f)
                if (aabb2.b < bestY + 1.8f)
                    bestY = aabb2.b - 1.8f;
            else
                if (aabb2.e > bestY)
                    bestY = aabb2.e;
        }
        return bestY;
    }
}
