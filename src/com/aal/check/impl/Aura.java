package com.aal.check.impl;

import com.aal.check.Check;
import com.aal.event.events.HitEvent;
import com.aal.event.events.MoveEvent;
import com.aal.event.events.PacketReceiveEvent;
import com.aal.util.MovingList;
import com.comphenix.protocol.PacketType;

public class Aura extends Check {
    MovingList<Boolean> landedHits = new MovingList<>(100);
    long lastHit = System.currentTimeMillis();
    long lastFlying = System.currentTimeMillis();
    long lastCheat = System.currentTimeMillis();

    @Override
    public void onMove(MoveEvent e) {
        lastFlying = System.currentTimeMillis();
    }

    @Override
    public void onHit(HitEvent e) {
        if (System.currentTimeMillis() - lastHit > 1000)
            landedHits.clear();
        boolean post = System.currentTimeMillis() - lastFlying < 5;
        lastHit = System.currentTimeMillis();
        landedHits.setLast(true);
        if (landedHits.isReady()) {
            int accuracy = 0;
            for (boolean hit: landedHits.getData())
                if (hit)
                    accuracy++;
            if (accuracy < 90)
                lastCheat = System.currentTimeMillis();
        }
        if (post)
            lastCheat = System.currentTimeMillis();
        if (user.isUsingItem())
            lastCheat = System.currentTimeMillis();
        if (System.currentTimeMillis() - lastCheat > 1000 && landedHits.isReady()) {
            flag("doesn't use killaura");
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent e) {
        if (e.packet.getType() == PacketType.Play.Client.ARM_ANIMATION)
            landedHits.add(false);
    }
}
