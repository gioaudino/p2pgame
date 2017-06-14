package it.gioaudino.game.Client;

import com.mashape.unirest.http.exceptions.UnirestException;
import it.gioaudino.game.Exception.HTTPException;
import it.gioaudino.game.Service.MessageHandler;

import java.io.IOException;


/**
 * Created by gioaudino on 15/05/17.
 * Package gioaudino.game.Client in game
 */
public class Client {
    public static void main(String[] args) throws IOException, HTTPException, UnirestException, InterruptedException {
        ClientObject client = new ClientObject();

        Thread menu = new Thread(new RunnableMenuHandler(client));
        client.setListener(new ClientListener(client));
        new Thread(client.getListener()).start();

        menu.start();
        if (MessageHandler.DEBUG) {
            Thread.sleep(5000);
            try {
                if (client.getUser().getUsername().equals("morto")) {
                    Thread.sleep(15000);
                    client.quitGame();
                }
            } catch (NullPointerException e) {
            }
        }
        menu.join();
        System.exit(0);

    }
}
