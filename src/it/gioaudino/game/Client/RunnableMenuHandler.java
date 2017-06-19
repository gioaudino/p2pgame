package it.gioaudino.game.Client;

import it.gioaudino.game.Exception.ExitClientException;

/**
 * Created by gioaudino on 19/05/17.
 * Package it.gioaudino.game.Client in game
 */
public class RunnableMenuHandler implements Runnable {

    Player player;

    public RunnableMenuHandler(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        try {
            while (true)
                UserInteractionHandler.printMenu(player);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExitClientException ignored) {
        }
    }
}
