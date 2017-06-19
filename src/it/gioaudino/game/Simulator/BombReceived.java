package it.gioaudino.game.Simulator;

import it.gioaudino.game.Client.Player;
import it.gioaudino.game.Entity.Bomb;

/**
 * Created by gioaudino on 15/06/17.
 * Package it.gioaudino.game.Simulator in game
 */
public class BombReceived implements Runnable {
    private Bomb bomb;
    private Player player;

    public BombReceived(Player player, Bomb bomb) {
        this.player = player;
        this.bomb = bomb;
    }

    @Override
    public void run() {
        for (int i = 0; i < Bomb.EXPLOSION_TIME; i++) {
            player.getOutputPrinter().println(bomb.getThrower().getUsername() + "'s " + bomb.getZone() + " bomb | " + (5 - i) + " seconds...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
