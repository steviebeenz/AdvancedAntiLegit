package com.aal.check.impl;

import com.aal.check.Check;
import com.aal.event.events.MoveEvent;
import com.aal.util.Loc;

public class LegitMove extends Check {
    double deltaX, deltaY, deltaZ, delta, lastDeltaX, lastDeltaY, lastDeltaZ, lastDelta;
    int air, ground;
    boolean onGround, lastOnGround;
    double motionX, motionY, motionZ;
    double lastX, lastY, lastZ;
    float yaw;

    float radToIndex = roundToFloat(651.8986469044033D);
    boolean fastMath = false;
    static final float[] SIN_TABLE_FAST = new float[4096];
    static final float[] SIN_TABLE = new float[65536];

    int legit = 0;

    public LegitMove() {
        for (int i = 0; i < 65536; ++i)
            SIN_TABLE[i] = (float)Math.sin((double)i * Math.PI * 2.0D / 65536.0D);
        for (int j = 0; j < SIN_TABLE_FAST.length; ++j)
            SIN_TABLE_FAST[j] = roundToFloat(Math.sin((double)j * Math.PI * 2.0D / 4096.0D));
    }

    @Override
    public void onMove(MoveEvent e) {
        lastX = e.getFrom().getX();
        lastY = e.getFrom().getY();
        lastZ = e.getFrom().getZ();
        yaw = e.getTo().getYaw();

        lastOnGround = onGround;
        onGround     = e.isOnGround();

        ground = onGround ? Math.min(ground + 1, 10) : 0;
        air    = onGround ? 0 : Math.min(air + 1, 10);

        lastDeltaX = deltaX;
        lastDeltaY = deltaY;
        lastDeltaZ = deltaZ;
        lastDelta  = delta;

        deltaX = e.getTo().getX() - e.getFrom().getX();
        deltaY = e.getTo().getY() - e.getFrom().getY();
        deltaZ = e.getTo().getZ() - e.getFrom().getZ();
        delta  = hypot(deltaX, deltaZ);

        motionX = lastDeltaX * (lastOnGround ? getFriction(e.getFrom()) : 0.91f);
        motionY = (lastDeltaY - 0.08) * 0.9800000190734863;
        motionZ = lastDeltaZ * (lastOnGround ? getFriction(e.getFrom()) : 0.91f);

        int roundDelta = (int) Math.round(delta * 1000000);
        if (roundDelta % 10 == 0 && delta > 0.28) {
            return;
        }

        float strafe = e.getStrafe();
        float forward = e.getForward();

        if (user.isUsingItem()) {
            strafe *= 0.2f;
            forward *= 0.2f;
        }

        strafe *= 0.98f;
        forward *= 0.98f;

        float airSpeed = 0.02f;
        if (!user.isInWater() || user.isFlying()) {
            if (!user.isInLava() || user.isFlying()) {
                float friction = 0.91f;

                if (lastOnGround)
                    friction = getFriction(e.getFrom());

                float f = 0.16277136F / (friction * friction * friction);
                float moveSpeed = onGround ? user.getLandMovementFactor(user.getSpeedLevel(), user.getSlowLevel()) * f : user.isSprinting() ? 0.026f : (float)((double) airSpeed + (double) airSpeed * 0.3D);

                moveFlying(strafe, forward, moveSpeed);

                if (user.isOnLadder()) {
                    double limit = 0.15F;

                    motionX = clamp(motionX, -limit, limit);
                    motionZ = clamp(motionZ, -limit, limit);

                    if (motionY < -0.15D)
                        motionY = -0.15D;

                    if (user.isSneaking())
                        motionY = 0;
                }

                if (user.isOnLadder() && Math.abs(deltaY - ((0.2 - 0.08) * 0.9800000190734863)) < 0.001) {
                    motionY = (0.2 - 0.08) * 0.9800000190734863;
                }

                moveEntity();

                double diff = hypot(deltaX - motionX, deltaZ - motionZ);
                if (diff < 1E-8 && (strafe != 0 || forward != 0)) {
                    if (++legit > 10) {
                        legit = Math.min(legit, 15);
                        flag("moved legitimately");
                    }
                } else {
                    if (legit > 10)
                        flag("moved legitimately");
                    fastMath = !fastMath;
                    legit = Math.max(legit - 1, 0);
                }
            }
        }

    }

    public void moveFlying(float strafe, float forward, float moveSpeed) {
        float f = strafe * strafe + forward * forward;

        if (f >= 1.0E-4F) {
            f = (float) Math.sqrt(f);

            if (f < 1.0F)
                f = 1.0F;

            f = moveSpeed / f;

            strafe = strafe * f;
            forward = forward * f;

            float dir1 = sin(yaw * (float)Math.PI / 180.0F);
            float dir2 = cos(yaw * (float)Math.PI / 180.0F);

            this.motionX += strafe * dir2 - forward * dir1;
            this.motionZ += forward * dir2 + strafe * dir1;
        }
    }

    public void moveEntity() {
        double predictedX = lastX + motionX;
        double predictedY = lastY + motionY;
        double predictedZ = lastZ + motionZ;
        motionX = predictedX - lastX;
        motionY = predictedY - lastY;
        motionZ = predictedZ - lastZ;
    }

    public float sin(float p_76126_0_) {
        return fastMath ? SIN_TABLE_FAST[(int)(p_76126_0_ * radToIndex) & 4095] : SIN_TABLE[(int)(p_76126_0_ * 10430.378F) & 65535];
    }

    public float cos(float value) {
        return fastMath ? SIN_TABLE_FAST[(int)(value *  + 1024.0F) & 4095] : SIN_TABLE[(int)(value * 10430.378F + 16384.0F) & 65535];
    }

    public float roundToFloat(double d) {
        return (float)((double)Math.round(d * 1.0E8D) / 1.0E8D);
    }

    public double clamp(double x, double y, double z) {
        return x < y ? y : Math.min(x, z);
    }

    public float getFriction(Loc loc) {
        try {
            String block = loc.blockAt(user.getPlayer(), 0, -1, 0).getType().name().toLowerCase();
            return 0.91f * (block.equals("blue_ice") ? 0.989f : block.contains("ice") ? 0.98f : block.contains("slime") ? 0.8f : 0.6f);
        } catch (Exception ignored) {
            return 0.91f * 0.6f;
        }
    }
}
