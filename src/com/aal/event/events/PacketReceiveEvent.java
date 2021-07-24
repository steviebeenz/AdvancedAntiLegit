package com.aal.event.events;

import com.aal.event.Event;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;

public class PacketReceiveEvent extends Event {
    public PacketContainer packet;

    public PacketReceiveEvent(Player player, PacketContainer packet) {
        super(player);
        this.packet = packet;
    }
}
