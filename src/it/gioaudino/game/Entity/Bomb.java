package it.gioaudino.game.Entity;

/**
 * Created by gioaudino on 10/06/17.
 * Package it.gioaudino.game.Entity in game
 */
public class Bomb {
    public static final int EXPLOSION_TIME = 5;
    private User thrower;
    private long thrownTimestamp;
    private PositionZone zone;

    public Bomb(User thrower, PositionZone zone) {
        this.thrower = thrower;
        this.zone = zone;
        this.thrownTimestamp = System.currentTimeMillis();
    }

    public User getThrower() {
        return thrower;
    }

    public void setThrower(User thrower) {
        this.thrower = thrower;
    }

    public long getThrownTimestamp() {
        return thrownTimestamp;
    }

    public void setThrownTimestamp() {
        this.thrownTimestamp = System.currentTimeMillis();
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
