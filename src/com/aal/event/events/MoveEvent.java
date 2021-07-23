package com.aal.event.events;

import com.aal.AAL;
import com.aal.event.Event;
import com.aal.user.User;
import com.aal.util.Loc;
import org.bukkit.entity.Player;

public class MoveEvent extends Event {
    private final Loc to, from;
    private final boolean onGround;
    private float forward, strafe;

    public MoveEvent(Player player, Loc to, Loc from, boolean onGround) {
        super(player);
        User u = AAL.getUserManager().getUser(player);
        this.to = to;
        this.from = from;
        this.onGround = onGround;
        final double moveX = to.getX() - from.getX() - u.getdDeltaX();
        final double moveZ = to.getZ() - from.getZ() - u.getdDeltaZ();
        final double move = Math.hypot(moveX, moveZ);
        if (move > 0.01) {
            double d = Double.MAX_VALUE;
            int f = 0;
            for (int yaw = 0; yaw < 360; yaw += 45) {
                double x = Math.cos((to.getYaw() + yaw) * Math.PI / 180) * move;
                double z = Math.sin((to.getYaw() + yaw) * Math.PI / 180) * move;
                double diff = Math.sqrt((moveX - x) * (moveX - x) + (moveZ - z) * (moveZ - z));
                if (diff < d) {
                    d = diff;
                    f = yaw;
                }
            }
            switch (f) {
                case 0:
                    forward = 0f;
                    strafe = 1f;
                    break;
                case 45:
                    forward = strafe = 1f;
                    break;
                case 90:
                    forward = 1f;
                    strafe = 0f;
                    break;
                case 135:
                    forward = 1f;
                    strafe = -1f;
                    break;
                case 180:
                    forward = 0f;
                    strafe = -1f;
                    break;
                case 225:
                    forward = strafe = -1f;
                    break;
                case 270:
                    forward = -1f;
                    strafe = 0f;
                    break;
                case 315:
                    forward = -1f;
                    strafe = 1f;
                    break;
            }
        } else {
            forward = strafe = 0f;
        }
    }

    public float getStrafe() {
        return strafe;
    }

    public float getForward() {
        return forward;
    }

    public Loc getTo() {
        return to;
    }

    public Loc getFrom() {
        return from;
    }

    public boolean isOnGround() {
        return onGround;
    }
}
