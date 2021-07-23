package com.aal.user;

import com.aal.check.Check;
import com.aal.event.Event;
import com.aal.util.Loc;
import org.bukkit.entity.Player;

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

    public User(Player player) {
        this.player = player;
        this.name = player.getName();
        this.uid = player.getUniqueId();
    }

    public void onEvent(Event e) {

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
