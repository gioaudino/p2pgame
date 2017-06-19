package it.gioaudino.game.Client;

import it.gioaudino.game.Entity.ClientStatus;
import it.gioaudino.game.Entity.Move;
import it.gioaudino.game.Service.P2PCommunicationService;

/**
 * Created by gioaudino on 01/06/17.
 * Package it.gioaudino.game.Client in game
 */
public class MovePerformerRunnable implements Runnable {

    private Player player;
    private boolean stopped;

    public MovePerformerRunnable(Player player) {
        this.player = player;
    }

    public void stopMe() {
        this.stopped = true;
    }

    @Override
    public void run() {

        while (!stopped && player.getStatus() == ClientStatus.STATUS_PLAYING) {
            if (null != player.getMove()) {
                Move.perform(player);
            }
            synchronized (player.token) {
                if (null != player.getNext()) {
                    P2PCommunicationService.giveToken(player, false);
                    player.token.lock();
                }
            }
        }
    }
}
