package com.aal.event.events;

import com.aal.event.Event;
import org.bukkit.entity.Player;

public class HitEvent extends Event {
    private final int entityId;

    public HitEvent(Player player, int entityId) {
        super(player);
        this.entityId = entityId;
    }

    public int getEntityId() {
        return entityId;
    }
}
