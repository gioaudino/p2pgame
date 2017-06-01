package it.gioaudino.game.Client;

import com.mashape.unirest.http.exceptions.UnirestException;
import it.gioaudino.game.Exception.HTTPException;

import java.io.IOException;

/**
 * Created by gioaudino on 15/05/17.
 * Package gioaudino.game.Client in game
 */
public class Client {
    public static void main(String[] args) throws IOException, HTTPException, UnirestException, InterruptedException {
        ClientObject client = new ClientObject();

        Thread menu = new Thread(new RunnableMenuHandler(client));
        Thread listener = new Thread(new ClientListener(client));


        listener.start();
        menu.start();
        menu.join();

    }
}
