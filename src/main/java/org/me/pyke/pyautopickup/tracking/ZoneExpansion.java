package org.me.pyke.pyautopickup.tracking;

public class ZoneExpansion {

    private final int verticalReach;
    private final int extraTicks;

    public ZoneExpansion(int verticalReach, int extraTicks) {
        this.verticalReach = verticalReach;
        this.extraTicks = extraTicks;
    }

    public int getVerticalReach() {
        return verticalReach;
    }

    public int getExtraTicks() {
        return extraTicks;
    }
}
