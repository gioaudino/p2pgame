package it.gioaudino.game.Client;

import it.gioaudino.game.Exception.ExitClientException;

/**
 * Created by gioaudino on 19/05/17.
 * Package it.gioaudino.game.Client in game
 */
public class RunnableMenuHandler implements Runnable {

    ClientObject client;

    public RunnableMenuHandler(ClientObject client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            while (true)
                UserInteractionHandler.printMenu(client);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExitClientException ignored) {
        }
    }
}
