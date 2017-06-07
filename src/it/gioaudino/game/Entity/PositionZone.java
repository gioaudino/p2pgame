package it.gioaudino.game.Entity;

/**
 * Created by gioaudino on 07/06/17.
 * Package it.gioaudino.game.Entity in game
 */
public enum PositionZone {
    ZONE_GREEN, ZONE_RED, ZONE_BLUE, ZONE_YELLOW;

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

}
