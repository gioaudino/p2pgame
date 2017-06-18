package it.gioaudino.game.Entity;

/**
 * Created by gioaudino on 07/06/17.
 * Package it.gioaudino.game.Entity in game
 */
public enum PositionZone {
    ZONE_GREEN,
    ZONE_RED,
    ZONE_BLUE,
    ZONE_YELLOW;

    public static String getZoneAsString(PositionZone zone) {
        switch (zone) {
            case ZONE_GREEN:
                return "GREEN";
            case ZONE_RED:
                return "RED";
            case ZONE_BLUE:
                return "BLUE";
            case ZONE_YELLOW:
                return "YELLOW";
            default:
                return "UNKNOWN";
        }
    }

    public static PositionZone findZoneFromOutlier(double outlier) {
        int value = (int) Math.ceil(outlier);
        switch (value % 4) {
            case 0:
                return ZONE_GREEN;
            case 1:
                return ZONE_RED;
            case 2:
                return ZONE_BLUE;
            case 3:
                return ZONE_YELLOW;
        }
        return null;
    }

    @Override
    public String toString() {
        return getZoneAsString(this);
    }
}
