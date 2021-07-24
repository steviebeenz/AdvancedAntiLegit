package com.aal.user;

import com.aal.AAL;
import com.aal.check.Check;
import com.aal.check.impl.LegitMove;
import com.aal.event.Event;
import com.aal.event.events.*;
import com.aal.util.Loc;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
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
    private Loc location = new Loc(), lastLocation = new Loc(), l3 = new Loc(), l4 = new Loc(), l5 = new Loc();
    private boolean onGround = false, lastOnGround = false;
    public short transaction = 0;
    private boolean flying = false, allowFlight = false;
    private boolean sprinting = false, hitUnSprint = false;
    private boolean sneaking = false, realSneaking = false;
    private boolean onSoulSand = false, inWeb = false, inWater = false, inLava = false, underBlock = false, onLadder = false;
    private int tickstp;
    private Loc tploc = new Loc();
    private boolean cantp = false;
    private double dDeltaX = 0, dDeltaZ = 0;
    private boolean tickPassed = false;
    private long lastFlying = System.currentTimeMillis();

    public double getdDeltaX() {
        return dDeltaX;
    }

    public double getdDeltaZ() {
        return dDeltaZ;
    }

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
                if (System.currentTimeMillis() - lastFlying < 5)
                    for (Check c: checks)
                        if (c.getName().equals("LegitMove"))
                            ((LegitMove)c).legit = 0;
            } else if (action == EnumWrappers.PlayerAction.STOP_SNEAKING) {
                sneaking = false;
                if (System.currentTimeMillis() - lastFlying < 5)
                    for (Check c: checks)
                        if (c.getName().equals("LegitMove"))
                            ((LegitMove)c).legit = 0;
            }
        }

        if (e instanceof HitEvent) {
            for (Entity entity: player.getWorld().getEntities()) {
                if (entity.getEntityId() == ((HitEvent) e).getEntityId()) {
                    if (entity instanceof Player)
                        hitUnSprint = true;
                    break;
                }
            }
        }

        if (e instanceof MoveEvent) {
            lastFlying = System.currentTimeMillis();
            Loc to = ((MoveEvent) e).getTo();
            Loc from = ((MoveEvent) e).getFrom();
            dDeltaX = (to.getX() - from.getX()) * (inLava ? 0.5 : onGround ? getFriction(from) : 0.91f);
            dDeltaZ = (to.getZ() - from.getZ()) * (inLava ? 0.5 : onGround ? getFriction(from) : 0.91f);
            if (onSoulSand) {
                dDeltaX *= 0.4;
                dDeltaZ *= 0.4;
            }
            if (inWeb) {
                dDeltaX = 0.0D;
                dDeltaZ = 0.0D;
            }
            try {
                {
                    String block = from.blockAt(player, 0, 0, 0).getType().name().toLowerCase();
                    onLadder = block.contains("vine") || block.contains("ladder");
                }
                {
                    String block = from.blockAt(player, 0, 0, 0).getType().name().toLowerCase();
                    onSoulSand = block.contains("soul");
                }
                inWater = inLava = inWeb = false;
                for (double x = -0.3; x <= 0.3; x += 0.3) {
                    for (double y = 0; y <= 1; y++) {
                        for (double z = -0.3; z <= 0.3; z += 0.3) {
                            String block = from.blockAt(player, x, y, z).getType().name().toLowerCase();
                            inWater = inWater || block.contains("water");
                            inLava = inLava || block.contains("lava");
                            inWeb = inWeb || block.contains("web");
                        }
                    }
                }
                underBlock = false;
                for (double x = -0.3; x <= 0.3; x += 0.3) {
                    for (double y = 1; y <= 2; y++) {
                        for (double z = -0.3; z <= 0.3; z += 0.3) {
                            underBlock = underBlock || from.blockAt(player, x, y, z).getType().isSolid();
                        }
                    }
                }
            } catch (Exception ignored) { }

            if (to.distance(tploc) > 0.5 && tickstp > 0 && !cantp) {
                tickstp = 0;
            }

            if (cantp) {
                tickstp++;
                if (to.distance(tploc) < 0.1) {
                    cantp = false;
                }
            }
        }

        if (e instanceof TeleportEvent) {
            tickstp = Math.max(tickstp, 2);
            cantp = true;
            tploc = ((TeleportEvent) e).getLoc();
            if (tploc.getYaw() == location.getYaw()) {
                tploc.setYaw(-69);
            }
        }

        for (Check c: checks)
            c.onEvent(e);
    }

    private float getFriction(Loc loc) {
        try {
            String block = loc.blockAt(player, 0, -1, 0).getType().name().toLowerCase();
            return 0.91f * (block.equals("blue_ice") ? 0.989f : block.contains("ice") ? 0.98f : block.contains("slime") ? 0.8f : 0.6f);
        } catch (Exception ignored) {
            return 0.91f * 0.6f;
        }
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
                //player.kickPlayer(ChatColor.translateAlternateColorCodes('&', "&8[&cAAL&8] &c" + reason));
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&8[&cAAL&8] &c" + player.getName() + "&7 was kicked for &c" + reason));
            }
        });
    }

    /*
      utils
           */

    public boolean isInsideVehicle() {
        return player.isInsideVehicle();
    }

    public float getLandMovementFactor(float speed, float slow) {
        double gay = 0.10000000149011612D;
        gay += gay * 0.20000000298023224D * speed;
        gay += gay * -0.15000000596046448D * slow;
        if (isSprinting())
            gay += gay * 0.30000001192092896D;
        return (float)gay;
    }

    public float getFlySpeed() {
        return player.getFlySpeed() / 2f;
    }

    public float getDepthStrider() {
        return player.getInventory().getBoots() == null ? 0f : player.getInventory().getBoots().getEnchantmentLevel(Enchantment.DEPTH_STRIDER);
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

    public Loc getTploc() {
        return tploc;
    }

    public boolean isInWeb() {
        return inWeb;
    }

    public boolean isInWater() {
        return inWater;
    }

    public boolean isInLava() {
        return inLava;
    }

    public boolean isOnLadder() {
        return onLadder;
    }

    public boolean isUnderBlock() {
        return underBlock;
    }

    public boolean hasTeleported() {
        return tickstp > 0;
    }

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

    public boolean isOnSoulSand() {
        return onSoulSand;
    }

    public boolean isUsingItem() {
        return player.isBlocking();
    }

    public boolean isSneaking() {
        return sneaking;
    }

    public boolean isSprinting() {
        return sprinting;
    }

    public boolean isFlying() {
        return flying;
    }

    public boolean isAllowFlight() {
        return allowFlight;
    }

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
