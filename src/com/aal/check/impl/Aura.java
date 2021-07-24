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

    @Override
    public void onMove(MoveEvent e) {

    }

    @Override
    public void onHit(HitEvent e) {
        if (System.currentTimeMillis() - lastHit > 1000)
            landedHits.clear();
        lastHit = System.currentTimeMillis();
        landedHits.setLast(true);
        if (landedHits.isReady()) {
            int accuracy = 0;
            for (boolean hit: landedHits.getData())
                if (hit)
                    accuracy++;
            if (accuracy < 95)
                flag("low accuracy: " + accuracy);
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent e) {
        if (e.packet.getType() == PacketType.Play.Client.ARM_ANIMATION)
            landedHits.add(false);
    }
}
