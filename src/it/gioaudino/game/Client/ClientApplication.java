package it.gioaudino.game.Client;

import com.mashape.unirest.http.exceptions.UnirestException;
import it.gioaudino.game.Exception.HTTPException;

import java.io.IOException;


/**
 * Created by gioaudino on 15/05/17.
 * Package gioaudino.game.Client in game
 */
public class ClientApplication {
    public static final boolean DEBUG = false;

    public static void main(String[] args) throws IOException, HTTPException, UnirestException, InterruptedException {
        Player player = new Player();

        Thread menu = new Thread(new RunnableMenuHandler(player));
        player.startListener();

        menu.start();
        menu.join();
        System.exit(0);
    }
}
