package com.aal.user;

import com.aal.AAL;
import com.aal.check.Check;
import com.aal.event.Event;
import com.aal.event.events.ActionEvent;
import com.aal.event.events.CAbilityEvent;
import com.aal.event.events.HitEvent;
import com.aal.event.events.SAbilityEvent;
import com.aal.util.Loc;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.UUID;

public class User {
    private final ArrayList<Check> checks = new ArrayList<>();
    private final Player player;
    private final String name;
    private final UUID uid;
    private Loc location = new Loc(), lastLocation = new Loc();
    private boolean onGround = false, lastOnGround = false;
    public short transaction = 0;
    public boolean flying = false, allowFlight = false;
    public boolean sprinting = false, hitUnSprint = false;
    public boolean sneaking = false;

    public User(Player player) {
        this.player = player;
        this.name = player.getName();
        this.uid = player.getUniqueId();
    }

    public void onEvent(Event e) {
        if (e instanceof SAbilityEvent) {
            flying = ((SAbilityEvent) e).isFlying();
            allowFlight = ((SAbilityEvent) e).isAllowFlight();
        }

        if (e instanceof CAbilityEvent) {
            flying = ((CAbilityEvent) e).isFlying() && allowFlight;
        }

        if (e instanceof ActionEvent) {
            EnumWrappers.PlayerAction action = ((ActionEvent) e).getAction();
            if (action == EnumWrappers.PlayerAction.START_SPRINTING) {
                sprinting = true;
            } else if (action == EnumWrappers.PlayerAction.STOP_SPRINTING) {
                if (!hitUnSprint) {
                    sprinting = false;
                }
                hitUnSprint = false;
            } else if (action == EnumWrappers.PlayerAction.START_SNEAKING) {
                sneaking = true;
            } else if (action == EnumWrappers.PlayerAction.STOP_SNEAKING) {
                sneaking = false;
            }
        }

        if (e instanceof HitEvent) {
            boolean found = false;
            for (Entity entity: player.getWorld().getEntities()) {
                if (entity.getEntityId() == ((HitEvent) e).getEntityId()) {
                    if (entity instanceof Player)
                        hitUnSprint = true;
                    found = true;
                    break;
                }
            }
        }

        for (Check c: checks)
            c.onEvent(e);
    }

    public void addCheck(Check c) {
        c.user = this;
        checks.add(c);
    }

    public void sendMessage(String sendMessage) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', sendMessage));
    }

    @SuppressWarnings("deprecation")
    public void kick(String reason) {
        Bukkit.getScheduler().runTask(AAL.getInstance(), new BukkitRunnable() {
            @Override
            public void run() {
                player.kickPlayer(ChatColor.translateAlternateColorCodes('&', "&8[&cAAL&8] &c" + reason));
                Bukkit.broadcastMessage("&8[&cAAL&8] &c" + player.getName() + "&7 was kicked for &c" + reason);
            }
        });
    }

    /*
      utils
           */



    public float getLandMovementFactor(float speed) {
        return (player.getWalkSpeed() / 2) * (1.0f + speed * 0.2f) * (sprinting ? 1.3f : 1.0f);
    }

    public float getFlySpeed() {
        return player.getFlySpeed();
    }

    public float getSlowFallLevel() {
        for (PotionEffect e: player.getActivePotionEffects()) {
            if (e.getType().getId() == 28) {
                return e.getAmplifier() + 1;
            }
        }
        return 0;
    }

    public float getConduitLevel() {
        for (PotionEffect e: player.getActivePotionEffects()) {
            if (e.getType().getId() == 29) {
                return e.getAmplifier() + 1;
            }
        }
        return 0;
    }

    public float getDGLevel() {
        for (PotionEffect e: player.getActivePotionEffects()) {
            if (e.getType().getId() == 30) {
                return e.getAmplifier() + 1;
            }
        }
        return 0;
    }

    public float getFatigueLevel() {
        for (PotionEffect e: player.getActivePotionEffects()) {
            if (e.getType().getId() == PotionEffectType.SLOW_DIGGING.getId()) {
                return e.getAmplifier() + 1;
            }
        }
        return 0;
    }

    public float getHasteLevel() {
        for (PotionEffect e: player.getActivePotionEffects()) {
            if (e.getType().getId() == PotionEffectType.FAST_DIGGING.getId()) {
                return e.getAmplifier() + 1;
            }
        }
        return 0;
    }

    public float getLeviLevel() {
        for (PotionEffect e: player.getActivePotionEffects()) {
            if (e.getType().getId() == 25) {
                return e.getAmplifier() + 1;
            }
        }
        return 0;
    }

    public float getJumpLevel() {
        for (PotionEffect e: player.getActivePotionEffects()) {
            if (e.getType().getId() == PotionEffectType.JUMP.getId()) {
                return e.getAmplifier() + 1;
            }
        }
        return 0;
    }

    public float getSpeedLevel() {
        for (PotionEffect e: player.getActivePotionEffects()) {
            if (e.getType().getId() == PotionEffectType.SPEED.getId()) {
                return e.getAmplifier() + 1;
            }
        }
        return 0;
    }

    public float getSlowLevel() {
        for (PotionEffect e: player.getActivePotionEffects()) {
            if (e.getType().getId() == PotionEffectType.SLOW.getId()) {
                return e.getAmplifier() + 1;
            }
        }
        return 0;
    }

    /*
      setters
             */

    public void setLastOnGround(boolean lastOnGround) {
        this.lastOnGround = lastOnGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public void setLastLocation(Loc lastLocation) {
        this.lastLocation = lastLocation;
    }

    public void setLocation(Loc location) {
        this.location = location;
    }

    /*
      getters
             */

    public boolean wasOnGround() {
        return lastOnGround;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public Loc getLastLocation() {
        return lastLocation;
    }

    public Loc getLocation() {
        return location;
    }

    public ArrayList<Check> getChecks() {
        return checks;
    }

    public Player getPlayer() {
        return player;
    }

    public String getName() {
        return name;
    }

    public UUID getUid() {
        return uid;
    }
}
