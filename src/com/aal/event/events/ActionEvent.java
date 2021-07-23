package com.aal.event.events;

import com.aal.event.Event;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.entity.Player;

public class ActionEvent extends Event {
    private EnumWrappers.PlayerAction action;

    public ActionEvent(Player player, EnumWrappers.PlayerAction action) {
        super(player);
        this.action = action;
    }

    public EnumWrappers.PlayerAction getAction() {
        return action;
    }
}
