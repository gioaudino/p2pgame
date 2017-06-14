package it.gioaudino.game.Client;

import it.gioaudino.game.Entity.ClientStatus;
import it.gioaudino.game.Entity.Move;
import it.gioaudino.game.Service.P2PCommunicationService;

/**
 * Created by gioaudino on 01/06/17.
 * Package it.gioaudino.game.Client in game
 */
public class MovePerformerRunnable implements Runnable {

    private ClientObject client;
    private boolean stopped;

    public MovePerformerRunnable(ClientObject client) {
        this.client = client;
    }

    public void stopMe() {
        this.stopped = true;
    }

    @Override
    public void run() {

        while (!stopped && client.getStatus() == ClientStatus.STATUS_PLAYING) {
            if (null != client.getMove()) {
                Move.perform(client);
            }
            synchronized (client.token) {
                if (null != client.getNext()) {
                    P2PCommunicationService.giveToken(client, false);
                    client.token.lock();
                }
            }
        }
    }
}
