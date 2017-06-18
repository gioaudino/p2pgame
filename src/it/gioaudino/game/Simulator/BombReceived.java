package it.gioaudino.game.Simulator;

import it.gioaudino.game.Entity.Bomb;

/**
 * Created by gioaudino on 15/06/17.
 * Package it.gioaudino.game.Simulator in game
 */
public class BombReceived implements Runnable {
    private Bomb bomb;

    public BombReceived(Bomb bomb) {
        this.bomb = bomb;
    }

    @Override
    public void run() {
        for (int i = 0; i < 5; i++) {
            System.out.println(bomb.getThrower().getUsername() + "'s " + bomb.getZone() + " bomb | " + (5 - i) + " seconds...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
