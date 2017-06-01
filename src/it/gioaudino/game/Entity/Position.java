package it.gioaudino.game.Entity;

/**
 * Created by gioaudino on 12/05/17.
 */
public class Position {
    public static final String ZONE_GREEN = "GREEN";
    public static final String ZONE_RED = "RED";
    public static final String ZONE_BLUE = "BLUE";
    public static final String ZONE_YELLOW = "YELLOW";

    private int x;
    private int y;
    private int gridSize;

    public Position(int gridSize) {
        this.gridSize = gridSize;
    }

    public Position(int x, int y, int gridSize) {
        this.x = x;
        this.y = y;
        this.gridSize = gridSize;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    private String getZone() {
        if (x < gridSize / 2) {
            if (y < gridSize / 2) return ZONE_GREEN;
            return ZONE_BLUE;
        }

        if (y < gridSize / 2) return ZONE_RED;
        return ZONE_YELLOW;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ") - " + this.getZone();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Position && x == ((Position) obj).getX() && y == ((Position) obj).getY();
    }
}
