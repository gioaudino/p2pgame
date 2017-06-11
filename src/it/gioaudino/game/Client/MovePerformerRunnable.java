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

    public MovePerformerRunnable(ClientObject client) {
        this.client = client;
    }

    @Override
    public void run() {
        synchronized (client.token) {
            if (null != client.getNext()) {
                try {
                    client.token.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            long last = System.currentTimeMillis() / 1000;
            while (client.getStatus() == ClientStatus.STATUS_PLAYING) {
                if (System.currentTimeMillis() / 1000 - last >= 5) {
                    last = System.currentTimeMillis() / 1000;
                    System.out.print("I'm still trying to perform moves! Next exists? ");
                    System.out.println(client.getNext() != null);
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                }
                if (null != client.getMove()) {
                    Move.perform(client);
                }
                if (null != client.getNext()) {
                    new Thread(() -> P2PCommunicationService.giveToken(client)).start();
                    try {
                        client.token.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
