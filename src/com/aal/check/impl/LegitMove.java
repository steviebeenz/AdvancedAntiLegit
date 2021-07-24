package com.aal.check.impl;

import com.aal.check.Check;
import com.aal.event.events.MoveEvent;
import com.aal.util.Loc;
import com.aal.util.Util;

public class LegitMove extends Check {
    double deltaX, deltaY, deltaZ, delta, lastDeltaX, lastDeltaY, lastDeltaZ, lastDelta;
    int air, ground;
    boolean onGround, lastOnGround;
    double motionX, motionY, motionZ;
    double lastX, lastY, lastZ;
    float yaw;
    boolean resetY = false;
    long lastWaterBounce = System.currentTimeMillis();
    boolean legitJump = true;
    boolean badOnGround = false;

    long flying = System.currentTimeMillis(), lastFlying = System.currentTimeMillis();
    double balance = 0;

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
        lastFlying = flying;
        flying = System.currentTimeMillis();
        double delay = flying - lastFlying;


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

        if (Math.abs(motionX) < 0.005)
            motionX = 0;
        if (Math.abs(motionY) < 0.005)
            motionY = 0;
        if (Math.abs(motionZ) < 0.005)
            motionZ = 0;

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
        double jumpHeight = 0.42f + 0.1 * user.getJumpLevel();
        if (air == 1 && deltaY > 0)
            motionY = jumpHeight;
        if (System.currentTimeMillis() - lastWaterBounce < 5000 || !legitJump)
            legit = 0;
        if (!user.isInWater() || user.isFlying()) {
            if (!user.isInLava() || user.isFlying()) {
                float friction = 0.91f;

                if (lastOnGround)
                    friction = getFriction(e.getFrom());

                float f = 0.16277136F / (friction * friction * friction);
                float moveSpeed = onGround ? user.getLandMovementFactor(user.getSpeedLevel(), user.getSlowLevel()) * f : user.isFlying() ? user.isSprinting() ? user.getFlySpeed() : user.getFlySpeed() / 2f : user.isSprinting() ? 0.026f : (float)((double) airSpeed + (double) airSpeed * 0.3D);

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

                {
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

                {
                    double diff = Math.abs(deltaY - motionY);
                    boolean validUnderBlock = user.isUnderBlock() && ((deltaY == 0.20000004768371582 && air == 1) || (deltaY == -0.12160004615783748 && ground == 1));
                    if (diff > 1E-13 && !validUnderBlock) {
                        if (air == 1) legitJump = false;
                        legit = Math.max(legit - 3, 0);
                    } else if (deltaY != 0) {
                        if (air == 1) legitJump = !badOnGround;
                        if (++legit > 10) {
                            legit = Math.min(legit, 15);
                            flag("moved legitimately");
                        }
                    }
                }

                {
                    boolean serverGround = Util.isCollided(user.getPlayer(), e.getTo().getX(), e.getTo().getY() - 1, e.getTo().getZ());
                    if (!serverGround && onGround) {
                        badOnGround = true;
                        legit = 0;
                    } else {
                        badOnGround = false;
                    }
                }
            } else {
                strafe = forward = 0f;
                motionX = lastDeltaX * 0.5;
                motionY = lastDeltaY * 0.5 - 0.02;
                motionZ = lastDeltaZ * 0.5;

                if (Util.isCollided(user.getPlayer(), lastX + motionX, lastY, lastZ + motionZ))
                    motionY = 0.30000001192092896;

                if (deltaY > motionY + 0.001)
                    this.motionY += 0.03999999910593033D;

                moveFlying(strafe, forward, 0.02F);
                moveEntity();

                {
                    double diff = hypot(deltaX - motionX, deltaZ - motionZ);
                    if (diff > 0.05) {
                        legit = Math.max(legit - 2, 0);
                    } else if (deltaY != 0) {
                        if (++legit > 10) {
                            legit = Math.min(legit, 15);
                            flag("moved legitimately");
                        }
                    }
                }

                {
                    double diff = Math.abs(deltaY - motionY);
                    if (diff > 1E-9) {
                        legit = Math.max(legit - 2, 0);
                    } else if (deltaY != 0) {
                        if (++legit > 10) {
                            legit = Math.min(legit, 15);
                            flag("moved legitimately");
                        }
                    }
                }
            }
        } else {
            strafe = forward = 0f;
            float f1 = 0.8F;
            float f2 = 0.02F;
            float f3 = user.getDepthStrider();

            if (f3 > 3.0F)
            {
                f3 = 3.0F;
            }

            if (!lastOnGround)
                f3 *= 0.5F;

            if (f3 > 0.0F)
            {
                f1 += (0.54600006F - f1) * f3 / 3.0F;
                f2 += (user.getLandMovementFactor(user.getSpeedLevel(), user.getSlowLevel()) - f2) * f3 / 3.0F;
            }

            motionX = lastDeltaX * f1;
            motionY = lastDeltaY * 0.800000011920929D - 0.02D;
            motionZ = lastDeltaZ * f1;

            if (Util.isCollided(user.getPlayer(), lastX + motionX, lastY, lastZ + motionZ))
                this.motionY = 0.30000001192092896D;

            if (deltaY > motionY + 0.001)
                this.motionY += 0.03999999910593033D;

            moveFlying(strafe, forward, f2);
            moveEntity();

            {
                double diff = hypot(deltaX - motionX, deltaZ - motionZ);
                if (diff > 0.05) {
                    legit = Math.max(legit - 2, 0);
                } else if (deltaY != 0) {
                    if (++legit > 10) {
                        legit = Math.min(legit, 15);
                        flag("moved legitimately");
                    }
                }
            }

            {
                double diff = Math.abs(deltaY - motionY);
                if (diff > 1E-9) {
                    if (diff > 0.2)
                        lastWaterBounce = System.currentTimeMillis();
                    legit = Math.max(legit - 2, 0);
                } else if (deltaY != 0) {
                    if (++legit > 10) {
                        legit = Math.min(legit, 15);
                        flag("moved legitimately");
                    }
                }
            }
        }
        if (resetY) {
            resetY = false;
            deltaY = 0;
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
        if (Util.isCollided(user.getPlayer(), predictedX, lastY, lastZ)) {
            if (predictedX > lastZ)
                predictedX = Math.ceil(predictedX) - 0.03f;
            else
                predictedX = Math.floor(predictedX) + 0.03f;
        }
        if (Util.isCollided(user.getPlayer(), lastX, predictedY, lastZ)) {
            if (lastY > predictedY)
                predictedY = Math.ceil(predictedY);
            else
                predictedY = Math.ceil(predictedY) - 0.8f;
            resetY = true;
        }
        if (Util.isCollided(user.getPlayer(), lastX, lastY, predictedZ)) {
            if (predictedZ > lastZ)
                predictedZ = Math.ceil(predictedZ) - 0.03f;
            else
                predictedZ = Math.floor(predictedZ) + 0.03f;
        }
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
