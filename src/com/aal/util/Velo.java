package com.aal.util;

public class Velo {
    private double x, y, z;
    private int id;
    private int received = 2;

    public Velo(double x, double y, double z, int id) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public int getId() {
        return id;
    }

    public void setReceived(int received) {
        this.received = received;
    }

    public int getReceived() {
        return received;
    }
}
