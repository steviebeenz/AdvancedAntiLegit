package com.aal.event;

import org.bukkit.entity.Player;

public class Event {
    private final Player player;

    public Event(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}
