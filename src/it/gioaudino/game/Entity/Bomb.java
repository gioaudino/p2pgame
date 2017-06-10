package it.gioaudino.game.Entity;

/**
 * Created by gioaudino on 10/06/17.
 * Package it.gioaudino.game.Entity in game
 */
public class Bomb {
    private Peer thrower;
    private long thrownTimestamp;
    private PositionZone zone;

    public Bomb(Peer thrower, PositionZone zone) {
        this.thrower = thrower;
        this.zone = zone;
    }

    public Peer getThrower() {
        return thrower;
    }

    public void setThrower(Peer thrower) {
        this.thrower = thrower;
    }

    public long getThrownTimestamp() {
        return thrownTimestamp;
    }

    public void setThrownTimestamp(long thrownTimestamp) {
        this.thrownTimestamp = thrownTimestamp;
    }

    public PositionZone getZone() {
        return zone;
    }

    public void setZone(PositionZone zone) {
        this.zone = zone;
    }
}
