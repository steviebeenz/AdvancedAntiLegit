package com.aal;

import com.aal.check.CheckManager;
import com.aal.event.PacketListener;
import com.aal.user.User;
import com.aal.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class AAL extends JavaPlugin {
    private static AAL          instance;
    private static UserManager  userManager;
    private static CheckManager checkManager;

    public FileConfiguration config;

    private static int version = 0;

    @Override
    public void onEnable() {
        version = Integer.parseInt(Bukkit.getServer().getClass().getPackage().getName().substring(Bukkit.getServer().getClass().getPackage().getName().lastIndexOf('.') + 1).replace("v", "").replaceFirst("_", "").split("_")[0]);

        instance     = this;
        userManager  = new UserManager();
        checkManager = new CheckManager();
        config       = this.getConfig();

        Bukkit.getPluginManager().registerEvents(new PacketListener(), instance);

        this.saveDefaultConfig();
    }

    @Override
    public void onDisable() { }

    @SuppressWarnings("deprecation")
    public void execute(String command) {
        Bukkit.getScheduler().runTask(instance, new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        });
    }

    public static AAL getInstance() {
        return instance;
    }

    public static UserManager getUserManager() {
        return userManager;
    }

    public static CheckManager getCheckManager() {
        return checkManager;
    }

    public static int getVersion() {
        return version;
    }
}
