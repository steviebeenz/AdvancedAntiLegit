package com.aal.util;

import com.aal.AAL;
import org.bukkit.entity.Player;

public class Util {
    public static boolean isCollided(Player p, double x, double y, double z) {
        if (AAL.getVersion() == 18) return Util18.isCollided(p, x, y, z);
        return false;
    }
}
