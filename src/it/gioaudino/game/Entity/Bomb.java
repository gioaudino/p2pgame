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

    @Override
    public String toString() {
        return "Bomb{" +
                "thrower=" + thrower.getUsername() +
                ", zone=" + zone +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bomb bomb = (Bomb) o;

        return thrownTimestamp == bomb.thrownTimestamp && thrower.equals(bomb.thrower) && zone == bomb.zone;
    }

    @Override
    public int hashCode() {
        int result = thrower.hashCode();
        result = 31 * result + (int) (thrownTimestamp ^ (thrownTimestamp >>> 32));
        result = 31 * result + zone.hashCode();
        return result;
    }
}
