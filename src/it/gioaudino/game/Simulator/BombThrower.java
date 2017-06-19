package it.gioaudino.game.Simulator;

import it.gioaudino.game.Client.Player;
import it.gioaudino.game.Entity.Bomb;
import it.gioaudino.game.Service.P2PCommunicationService;

/**
 * Created by gioaudino on 15/06/17.
 * Package it.gioaudino.game.Simulator in game
 */
public class BombThrower implements Runnable {
    private Player player;
    private Bomb bomb;

    public BombThrower(Player player, Bomb bomb) {
        this.player = player;
        this.bomb = bomb;
    }

    @Override
    public void run() {
        player.getOutputPrinter().println(this.bomb.getZone() + " bomb thrown!");
        for (int i = 0; i < Bomb.EXPLOSION_TIME; i++) {
            player.getOutputPrinter().println(this.bomb.getZone() + " | " + (5 - i) + " seconds...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
        if (player.getPosition().getZone().equals(this.bomb.getZone()))
            new Thread(() -> player.die(player.getUser(), this.bomb)).start();

        P2PCommunicationService.bombExploded(player, this.bomb);
    }
}