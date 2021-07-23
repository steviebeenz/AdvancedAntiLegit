package com.aal.event.events;

import com.aal.event.Event;
import com.aal.util.Loc;
import org.bukkit.entity.Player;

public class TeleportEvent extends Event {
    private final Loc loc;

    public TeleportEvent(Player player, Loc loc) {
        super(player);
        this.loc = loc;
    }

    public Loc getLoc() {
        return loc;
    }
}
