package com.aal;

import com.aal.check.CheckManager;
import com.aal.user.UserManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class AAL extends JavaPlugin {
    private static AAL          instance;
    private static UserManager  userManager;
    private static CheckManager checkManager;

    @Override
    public void onEnable() {
        instance     = this;
        userManager  = new UserManager();
        checkManager = new CheckManager();
    }

    @Override
    public void onDisable() { }

    public static AAL getInstance() {
        return instance;
    }

    public static UserManager getUserManager() {
        return userManager;
    }

    public static CheckManager getCheckManager() {
        return checkManager;
    }
}
