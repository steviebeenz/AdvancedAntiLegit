package com.aal.check;

import com.aal.check.impl.*;
import com.aal.user.User;

public class CheckManager {
    public void registerChecks(User u) {
        u.addCheck(new Aim());
        u.addCheck(new Aura());
        u.addCheck(new Clicker());
        u.addCheck(new LegitMove());
        u.addCheck(new Packets());
    }
}
