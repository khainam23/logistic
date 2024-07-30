package org.logistic.algorithms;

public class Location {
    final private boolean pickup;
    final private int x;
    final private int y;
    final private int load;
    private int LTW;// Lower time window
    private int UTW;// Upper time window
    private boolean serviced;
    private boolean serviceable;

    public Location(boolean pickup, int x, int y, int load) {
        this.pickup = pickup;
        this.x = x;
        this.y = y;
        this.load = load;
        this.serviced = false;
        this.serviceable = pickup;
    }

    public boolean isPickup() {
        return pickup;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getLoad() {
        return load;
    }

    public int getLTW() {
        return LTW;
    }

    public void setLTW(int LTW) {
        this.LTW = LTW;
    }

    public int getUTW() {
        return UTW;
    }

    public void setUTW(int UTW) {
        this.UTW = UTW;
    }

    public boolean isServiced() {
        return serviced;
    }

    public void setServiced(boolean serviced) {
        this.serviced = serviced;
    }

    public boolean isServiceable() {
        return serviceable;
    }

    public void setServiceable(boolean serviceable) {
        this.serviceable = serviceable;
    }

    public void resetServiced(){
        serviced = false;
        serviceable = pickup;
    }

    @Override
    public String toString() {
        return String.format("%8s %11s %6s %6s %6s %10s %13s", pickup, "(" + x + ", " + y + "):", load, LTW, UTW, serviced, serviceable);
    }
}
