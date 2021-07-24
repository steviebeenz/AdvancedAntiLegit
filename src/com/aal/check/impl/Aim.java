package com.aal.check.impl;

import com.aal.check.Check;
import com.aal.event.events.HitEvent;
import com.aal.event.events.MoveEvent;

public class Aim extends Check {
    float deltaYaw, deltaPitch, lastDeltaYaw, lastDeltaPitch;
    long lastInteract = System.currentTimeMillis();
    long lastCheat = System.currentTimeMillis();

    @Override
    public void onMove(MoveEvent e) {
        lastDeltaYaw = deltaYaw;
        lastDeltaPitch = deltaPitch;
        deltaYaw = e.getTo().getYaw() - e.getFrom().getYaw();
        deltaPitch = e.getTo().getPitch() - e.getFrom().getPitch();

        if (System.currentTimeMillis() - lastInteract < 1000)
            return;

        if (deltaYaw % 1.0 == 0 || deltaPitch % 1.0 == 0 || e.getTo().getYaw() == 0 || e.getFrom().getPitch() == 0)
            lastCheat = System.currentTimeMillis();


    }

    @Override
    public void onHit(HitEvent e) {
        lastInteract = System.currentTimeMillis();
    }
}
