package com.aal.event.events;

import com.aal.event.Event;
import org.bukkit.entity.Player;

public class TransactionEvent extends Event {
    private final int id;

    public TransactionEvent(Player player, int id) {
        super(player);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
