package com.aal.user;

import org.bukkit.entity.Player;

import java.util.ArrayList;

public class UserManager {
    public final ArrayList<User> users = new ArrayList<>();

    public void addUser(User u) {
        users.add(u);
    }

    public void delUser(Player player) {
        for (User u: users) {
            if (u.getPlayer() == player) {
                users.remove(u);
                break;
            }
        }
    }

    public User getUser(Player player) {
        for (User u: users)
            if (u.getPlayer() == player)
                return u;
        return null;
    }
}
