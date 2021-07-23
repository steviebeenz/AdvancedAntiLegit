package com.aal.event.events;

import com.aal.event.Event;
import com.aal.util.Velo;
import org.bukkit.entity.Player;

public class VelocityEvent extends Event {
    private final Velo velo;

    public VelocityEvent(Player player, Velo velo) {
        super(player);
        this.velo = velo;
    }

    public Velo getVelocity() {
        return velo;
    }
}
