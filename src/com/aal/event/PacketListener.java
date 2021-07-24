package com.aal.event;

import com.aal.AAL;
import com.aal.event.events.*;
import com.aal.user.User;
import com.aal.util.Loc;
import com.aal.util.Velo;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.InvocationTargetException;

public class PacketListener implements Listener {
    public PacketListener() {
        for (PacketType packetType: PacketType.values()) {
            if (packetType.isSupported()) {
                if (packetType.isClient()) {
                    ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(AAL.getInstance(), ListenerPriority.NORMAL, packetType) {
                        @Override
                        public void onPacketReceiving(PacketEvent e) {
                            onPacketReceive(e.getPlayer(), e.getPacket());
                        }
                    });
                }
                if (packetType.isServer()) {
                    ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(AAL.getInstance(), ListenerPriority.NORMAL, packetType) {
                        @Override
                        public void onPacketSending(PacketEvent e) {
                            onPacketSend(e.getPlayer(), e.getPacket());
                        }
                    });
                }
            }
        }
    }

    public void onPacketReceive(Player player, PacketContainer packet) {
        User user = AAL.getUserManager().getUser(player);
        if (user == null)
            return;
        Loc location = user.getLocation();
        boolean onGround;
        boolean isFlyingPacket = packet.getType() == PacketType.Play.Client.FLYING || packet.getType() == PacketType.Play.Client.POSITION || packet.getType() == PacketType.Play.Client.LOOK || packet.getType() == PacketType.Play.Client.POSITION_LOOK;
        if (isFlyingPacket) {
            onGround = packet.getBooleans().read(0);
            if (packet.getType() == PacketType.Play.Client.POSITION) {
                location = new Loc(packet.getDoubles().read(0), packet.getDoubles().read(1), packet.getDoubles().read(2), location.getYaw(), location.getPitch());
            } else if (packet.getType() == PacketType.Play.Client.LOOK) {
                location = new Loc(location.getX(), location.getY(), location.getZ(), packet.getFloat().read(0), packet.getFloat().read(1));
            } else if (packet.getType() == PacketType.Play.Client.POSITION_LOOK) {
                location = new Loc(packet.getDoubles().read(0), packet.getDoubles().read(1), packet.getDoubles().read(2), packet.getFloat().read(0), packet.getFloat().read(1));
            }
            user.setLastLocation(user.getLocation());
            user.setLocation(location);
            user.setLastOnGround(user.isOnGround());
            user.setOnGround(onGround);
            MoveEvent e = new MoveEvent(player, user.getLocation(), user.getLastLocation(), user.isOnGround());
            user.onEvent(e);
        }
        if (packet.getType() == PacketType.Play.Client.TRANSACTION || packet.getType() == PacketType.Play.Client.PONG) {
            TransactionEvent e = new TransactionEvent(player, packet.getIntegers().read(0));
            user.onEvent(e);
        }
        if (packet.getType() == PacketType.Play.Client.ENTITY_ACTION) {
            ActionEvent e = new ActionEvent(player, packet.getPlayerActions().read(0));
            user.onEvent(e);
        }
        if (packet.getType() == PacketType.Play.Client.USE_ENTITY) {
            if (packet.getEntityUseActions().read(0) != EnumWrappers.EntityUseAction.ATTACK)
                return;
            HitEvent e = new HitEvent(player, packet.getIntegers().read(0));
            user.onEvent(e);
        }
        if (packet.getType() == PacketType.Play.Client.ABILITIES) {
            CAbilityEvent e = new CAbilityEvent(player, packet.getBooleans().read(2), packet.getBooleans().read(1));
            user.onEvent(e);
        }
        PacketReceiveEvent e = new PacketReceiveEvent(player, packet);
        user.onEvent(e);
    }

    public void onPacketSend(Player player, PacketContainer packet) {
        User user = AAL.getUserManager().getUser(player);
        if (user == null)
            return;
        if (packet.getType() == PacketType.Play.Server.ABILITIES) {
            SAbilityEvent e = new SAbilityEvent(player, packet.getBooleans().read(2), packet.getBooleans().read(1));
            user.onEvent(e);
        }
        if (packet.getType() == PacketType.Play.Server.ENTITY_VELOCITY) {
            if (packet.getIntegers().read(0) != player.getEntityId())
                return;
            Velo velo = new Velo(packet.getIntegers().read(1) / 8000D, packet.getIntegers().read(2) / 8000D, packet.getIntegers().read(3) / 8000D, user.transaction);
            sendTransaction(user);
            VelocityEvent e = new VelocityEvent(player, velo);
            user.onEvent(e);
        }
        if (packet.getType() == PacketType.Play.Server.POSITION) {
            Loc loc = new Loc(packet.getDoubles().read(0), packet.getDoubles().read(1), packet.getDoubles().read(2), packet.getFloat().read(0), packet.getFloat().read(1));
            TeleportEvent e = new TeleportEvent(player, loc);
            user.onEvent(e);
        }
    }

    public void sendTransaction(User user) {
        int version = Integer.parseInt(Bukkit.getServer().getClass().getPackage().getName().substring(Bukkit.getServer().getClass().getPackage().getName().lastIndexOf('.') + 1).replace("v", "").replaceFirst("_", "").split("_")[0]);
        PacketContainer packet;
        if (version > 116) {
            packet = new PacketContainer(PacketType.Play.Server.PING);
            packet.getIntegers().write(0, (int) user.transaction);
        } else {
            packet = new PacketContainer(PacketType.Play.Server.TRANSACTION);
            packet.getShorts().write(0, user.transaction);
        }
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(user.getPlayer(), packet);
        } catch (InvocationTargetException ignored) { }
        if (++user.transaction > 32500) {
            user.transaction = 0;
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        User user = new User(e.getPlayer());
        AAL.getUserManager().addUser(user);
        AAL.getCheckManager().registerChecks(user);
        user.onEvent(new SAbilityEvent(e.getPlayer(), e.getPlayer().getAllowFlight(), e.getPlayer().isFlying()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        AAL.getUserManager().delUser(e.getPlayer());
    }
}
