package com.aal.check.impl;

import com.aal.check.Check;
import com.aal.event.events.MoveEvent;
import com.aal.util.Loc;

public class LegitMove extends Check {
    double deltaX, deltaZ, delta, lastDeltaX, lastDeltaZ, lastDelta;
    int air, ground;
    boolean onGround, lastOnGround;
    int legitMoves = 0;

    @Override
    public void onMove(MoveEvent e) {
        lastOnGround = onGround;
        onGround     = e.isOnGround();

        ground = onGround ? Math.min(ground + 1, 10) : 0;
        air    = onGround ? 0 : Math.min(air + 1, 10);

        lastDeltaX = deltaX;
        lastDeltaZ = deltaZ;
        lastDelta  = delta;

        deltaX = e.getTo().getX() - e.getFrom().getX();
        deltaZ = e.getTo().getZ() - e.getFrom().getZ();
        delta  = hypot(deltaX, deltaZ);

        checkSpeed(e);
        checkFly(e);
        checkJesus(e);
    }

    public void checkSpeed(MoveEvent e) {
        float friction = getFriction(e.getFrom());
        double predictionX = lastDeltaX * friction;
        double predictionZ = lastDeltaZ * friction;
        double diffX = deltaX - predictionX;
        double diffZ = deltaZ - predictionZ;
        double diff = hypot(diffX, diffZ);
        float potion = user.getSpeedLevel() - user.getSlowLevel() * 0.75f;
        if (ground == 1 || ground > 2) {
            double dirX = diff == 0 ? 0 : diffX / diff;
            double dirZ = diff == 0 ? 0 : diffZ / diff;
            boolean hacking = diff > user.getLandMovementFactor(potion) || diff < user.getLandMovementFactor(potion) * 0.95f;
            if (hacking) {
                legitMoves = Math.max(legitMoves - 1, -3);
                deltaX = predictionX + dirX * user.getLandMovementFactor(potion);
                deltaZ = predictionZ + dirZ * user.getLandMovementFactor(potion);
            } else {
                if (++legitMoves > 5)
                    flag("walked legitimately");
            }
        } else if (air > 2) {
            double dirX = diff == 0 ? 0 : diffX / diff;
            double dirZ = diff == 0 ? 0 : diffZ / diff;
            float airSpeed = user.isSprinting() ? 0.0263f : 0.022f;
            float airSpeed2 = user.isSprinting() ? 0.022f : 0.02f;
            boolean hacking = diff > airSpeed || diff < airSpeed2 - 0.005f;
            if (hacking) {
                legitMoves = Math.max(legitMoves - 1, -3);
                deltaX = predictionX + dirX * airSpeed;
                deltaZ = predictionZ + dirZ * airSpeed;
            } else {
                if (++legitMoves > 5)
                    flag("jumped legitimately");
            }
        } else if (air == 2) {
            double dirX = diff == 0 ? 0 : diffX / diff;
            double dirZ = diff == 0 ? 0 : diffZ / diff;
            boolean hacking = diff > (user.isSprinting() ? 0.2f : 0.08f) || diff < (user.isSprinting() ? 0.2f : 0.08f) - 0.02f;
            if (hacking) {
                legitMoves = Math.max(legitMoves - 1, -3);
                deltaX = predictionX + dirX * -(user.isSprinting() ? 0.2f : 0.08f);
                deltaZ = predictionZ + dirZ * -(user.isSprinting() ? 0.2f : 0.08f);
            } else {
                if (++legitMoves > 5)
                    flag("jumped legitimately");
            }
        } else if (air == 1) {
            diff = hypot(diffX, diffZ) - (user.isSprinting() ? 0.2f : 0f) - user.getLandMovementFactor(potion) - 0.06f;
            boolean hacking = diff > 0;
            if (hacking) {
                legitMoves = Math.min((int) -(diff / 0.01f), legitMoves - 1);
            } else {
                if (++legitMoves > 5)
                    flag("jumped legitimately");
            }
        } else if (ground == 2) {
            predictionX = lastDeltaX * 0.91f;
            predictionZ = lastDeltaZ * 0.91f;
            diffX = deltaX - predictionX;
            diffZ = deltaZ - predictionZ;
            diff = hypot(diffX, diffZ);
            double dirX = diff == 0 ? 0 : diffX / diff;
            double dirZ = diff == 0 ? 0 : diffZ / diff;
            boolean hacking = diff > user.getLandMovementFactor(potion) || diff < user.getLandMovementFactor(potion) * 0.95f;
            if (hacking) {
                legitMoves = Math.max(legitMoves - 1, -3);
                deltaX = predictionX + dirX * user.getLandMovementFactor(potion);
                deltaZ = predictionZ + dirZ * user.getLandMovementFactor(potion);
            } else {
                if (++legitMoves > 5)
                    flag("walked legitimately");
            }
        }

        if (user.hasTeleported()) {
            if (e.getTo().getYaw() == user.getTploc().getYaw()) {
                flag("rotated on teleport");
            }
        }
    }

    public void checkFly(MoveEvent e) {

    }

    public void checkJesus(MoveEvent e) {
        if (user.isInWater()) {

        }
    }

    public float getFriction(Loc loc) {
        if (lastOnGround) {
            try {
                String block = loc.blockAt(user.getPlayer(), 0, -1, 0).getType().name().toLowerCase();
                return 0.91f * (block.equals("blue_ice") ? 0.989f : block.contains("ice") ? 0.98f : block.contains("slime") ? 0.8f : 0.6f);
            } catch (Exception ignored) { }
        }
        return 0.91f;
    }
}
