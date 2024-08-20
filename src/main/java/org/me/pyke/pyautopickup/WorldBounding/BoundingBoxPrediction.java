package org.me.pyke.pyautopickup.WorldBounding;

public class BoundingBoxPrediction {

    private final int heightNeeded;
    private final int lifetimeNeeded;

    public BoundingBoxPrediction(int heightNeeded, int lifetimeNeeded) {
        this.heightNeeded = heightNeeded;
        this.lifetimeNeeded = lifetimeNeeded;
    }

    public int getHeightNeeded() {
        return heightNeeded;
    }

    public int getLifetimeNeeded() {
        return lifetimeNeeded;
    }
}
