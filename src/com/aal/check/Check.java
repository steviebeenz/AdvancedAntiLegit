package com.aal.check;

import com.aal.AAL;
import com.aal.event.Event;
import com.aal.event.events.MoveEvent;
import com.aal.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Check {
    public User user = null;
    private final String name;
    private final String bancmd;
    private final boolean experimental;
    private final Punishment punishment;
    private final int max;
    protected double vl = 0;

    public Check() {
        this.name         = this.getClass().getSimpleName();
        this.bancmd       = AAL.getInstance().config.getString("bancmd");
        this.max          = AAL.getInstance().config.getInt("checks." + name.toLowerCase() + ".max");
        this.punishment   = Punishment.valueOf(AAL.getInstance().config.getString("checks." + name.toLowerCase() + ".punishment").toUpperCase());
        this.experimental = AAL.getInstance().config.getBoolean("checks." + name.toLowerCase() + ".max");
    }

    public void onMove(MoveEvent e) {

    }

    public void onEvent(Event e) {
        if (e instanceof MoveEvent) {
            onMove((MoveEvent) e);
        }
    }

    public double hypot(double x, double y) {
        return Math.hypot(x, y);
    }

    public void debug(String debug) {
        user.sendMessage("&8[&cAAL&8] &7" + debug.replace(" ", "&7 ").replace(",", "&7,").replace(":&7 ", ": &c").replace("=", "=&c"));
    }

    public void debug(String x, Object... y) {
        for (Object o: y) {
            String so = String.valueOf(o);
            x = x.replaceFirst("\\{}", so);
        }
        user.sendMessage("&8[&cAAL&8] &7" + x.replace(" ", "&7 ").replace(",", "&7,").replace(":&7 ", ": &c").replace("=", "=&c"));
    }

    public void verbose(String message) {
        for (Player staff: Bukkit.getOnlinePlayers()) {
            staff.sendMessage(format("&8[&cAAL&8] &c" + user.getName() + "&7 " + message));
        }
    }

    public void flag(String info) {
        vl++;
        for (Player staff: Bukkit.getOnlinePlayers()) {
            staff.sendMessage(format("&8[&cAAL&8] &c" + user.getName() + "&7 failed &c" + name + ": " + info + " &8(&cx" + Math.round(vl) + "&8)" + (experimental ? " &c&o(EXPERIMENTAL)" : "")));
        }
        if (Math.round(vl) >= max) {
            vl = 0;
            punish();
        }
    }

    public void flag(double multiplier, String info) {
        vl += multiplier;
        for (Player staff: Bukkit.getOnlinePlayers()) {
            staff.sendMessage(format("&8[&cAAL&8] &c" + user.getName() + "&7 failed &c" + name + ": " + info + " &8(&cx" + Math.round(vl) + "&8)" + (experimental ? " &c&o(EXPERIMENTAL)" : "")));
        }
        if (Math.round(vl) >= max) {
            vl = 0;
            punish();
        }
    }

    public void punish() {
        switch (punishment) {
            case KICK:
                user.kick(name);
                break;
            case BAN:
                AAL.getInstance().execute(bancmd.replace("%player", user.getName()).replace("%check", name));
                break;
        }
    }

    public String format(String format) {
        return ChatColor.translateAlternateColorCodes('&', format);
    }
}
