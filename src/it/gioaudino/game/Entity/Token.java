package it.gioaudino.game.Entity;

/**
 * Created by gioaudino on 13/06/17.
 * Package it.gioaudino.game.Entity in game
 */
public class Token {
    private boolean status = true;

    public boolean getStatus() {
        return status;
    }

    public void lock() {
        synchronized (this) {
            this.status = false;
            try {
                this.wait();
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void unlock() {
        synchronized (this) {
            this.status = true;
            this.notifyAll();
        }
    }

}
