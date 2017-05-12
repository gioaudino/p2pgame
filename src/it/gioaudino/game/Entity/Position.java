package it.gioaudino.game.Entity;

import it.gioaudino.game.Exception.IllegalMoveException;

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
        if(x < gridSize/2){
            if(y<gridSize/2) return ZONE_GREEN;
            return ZONE_BLUE;
        }

        if(y<gridSize/2) return ZONE_RED;
        return ZONE_YELLOW;
    }

    public Position move(Directions direction) throws IllegalMoveException {
        switch (direction) {
            case LEFT:
                if(!this.checkNewPosition(x-1, y))
                    throw new IllegalMoveException();
                this.x--;
                break;
            case UP:
                if(!this.checkNewPosition(x, y-1))
                    throw new IllegalMoveException();
                this.y--;
                break;
            case RIGHT:
                if(!this.checkNewPosition(x+1, y))
                    throw new IllegalMoveException();
                this.x++;
                break;
            case DOWN:
                if(!this.checkNewPosition(x, y+1))
                    throw new IllegalMoveException();
                this.y++;
                break;
        }
        return this;
    }

    private boolean checkNewPosition(int x, int y) {
        return x >= 0 && x < gridSize && y >= 0 && y < gridSize;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ") - " + this.getZone();
    }
}
