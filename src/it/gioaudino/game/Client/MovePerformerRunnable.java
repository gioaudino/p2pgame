package it.gioaudino.game.Client;

import it.gioaudino.game.Entity.Move;

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
            if (null == client.getNext()) {
                try {
                    client.token.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (true) {
                if(null != client.getMove())
                    Move.perform(client);
                if(null != client.getNext())
                    try {
                        client.token.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }
        }
    }
}
