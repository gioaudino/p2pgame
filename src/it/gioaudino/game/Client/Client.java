package it.gioaudino.game.Client;

import com.mashape.unirest.http.exceptions.UnirestException;
import it.gioaudino.game.Entity.Bomb;
import it.gioaudino.game.Entity.Position;
import it.gioaudino.game.Entity.PositionZone;
import it.gioaudino.game.Exception.HTTPException;
import it.gioaudino.game.Service.P2PCommunicationService;
import it.gioaudino.game.Simulator.BombThrower;

import java.io.IOException;


/**
 * Created by gioaudino on 15/05/17.
 * Package gioaudino.game.Client in game
 */
public class Client {
    public static final boolean DEBUG = false;

    public static void main(String[] args) throws IOException, HTTPException, UnirestException, InterruptedException {
        ClientObject client = new ClientObject();

        Thread menu = new Thread(new RunnableMenuHandler(client));
        new Thread(new ClientListener(client)).start();

        menu.start();
        if (DEBUG) {
            Thread.sleep(5000);
            try {
                if (client.getUser().getUsername().equals("p1")) {
                    Thread.sleep(15000);
                    client.setPosition(new Position(0, 0, client.getGame().getSize()));
                    UserInteractionHandler.printPlayingHeader(client);
                }
                if (client.getUser().getUsername().equals("p2")) {
                    Thread.sleep(15000);
                    client.setPosition(new Position(0, 1, client.getGame().getSize()));
                    UserInteractionHandler.printPlayingHeader(client);
                }
                if (client.getUser().getUsername().equals("p3")) {
                    Thread.sleep(15000);
                    client.setPosition(new Position(3, 3, client.getGame().getSize()));
                    UserInteractionHandler.printPlayingHeader(client);
                    Bomb b = new Bomb(client.getUser(), PositionZone.ZONE_GREEN);
                    P2PCommunicationService.bombThrown(client, b);
                    new Thread(new BombThrower(client, b)).start();

                }
                if (client.getUser().getUsername().equals("p4")) {
                    Thread.sleep(15000);
                    client.setPosition(new Position(1, 0, client.getGame().getSize()));
                    UserInteractionHandler.printPlayingHeader(client);
                }
                if (client.getUser().getUsername().equals("p5")) {
                    Thread.sleep(15000);
                    client.setPosition(new Position(1, 1, client.getGame().getSize()));
                    UserInteractionHandler.printPlayingHeader(client);
                }
                Thread.sleep(10000);
                System.out.println(client.getStatus());
            } catch (NullPointerException e) {
            }
        }
        menu.join();
        System.exit(0);

    }
}
