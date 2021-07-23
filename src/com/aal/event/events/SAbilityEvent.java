package com.aal.event.events;

import com.aal.event.Event;
import org.bukkit.entity.Player;

public class SAbilityEvent extends Event {
    private final boolean allowFlight, flying;

    public SAbilityEvent(Player player, boolean allowFlight, boolean flying) {
        super(player);
        this.allowFlight = allowFlight;
        this.flying = flying;
    }

    public boolean isAllowFlight() {
        return allowFlight;
    }

    public boolean isFlying() {
        return flying;
    }
}
