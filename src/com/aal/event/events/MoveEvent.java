package com.aal.event.events;

import com.aal.event.Event;
import com.aal.util.Loc;
import org.bukkit.entity.Player;

public class MoveEvent extends Event {
    private final Loc to, from;
    private final boolean onGround;

    public MoveEvent(Player player, Loc to, Loc from, boolean onGround) {
        super(player);
        this.to = to;
        this.from = from;
        this.onGround = onGround;
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
