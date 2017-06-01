package it.gioaudino.game.Client;

import java.net.Socket;

/**
 * Created by gioaudino on 20/05/17.
 * Package it.gioaudino.game.Client in game
 */
public class ReceiverRunnable implements Runnable {

    private Socket socket;
    private ClientObject client;

    public ReceiverRunnable(Socket socket, ClientObject client) {
        this.socket = socket;
        this.client = client;
    }

    @Override
    public void run() {

    }
}
