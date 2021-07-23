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
    }

    public void checkSpeed(MoveEvent e) {
        float friction = getFriction(e.getFrom());
        double prediction = lastDelta * friction;
        if (air == 1)
            prediction += 0.45;
        else if (!onGround)
            prediction += 0.026;
        else if (ground == 2)
            prediction += 0.23;
        else
            prediction += user.getLandMovementFactor(user.getSpeedLevel() - user.getSlowLevel() * 0.75f);

    }

    public void checkFly(MoveEvent e) {

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
