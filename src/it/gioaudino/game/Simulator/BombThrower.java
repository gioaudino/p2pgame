package it.gioaudino.game.Simulator;

import it.gioaudino.game.Client.ClientObject;
import it.gioaudino.game.Entity.Bomb;
import it.gioaudino.game.Service.P2PCommunicationService;

/**
 * Created by gioaudino on 15/06/17.
 * Package it.gioaudino.game.Simulator in game
 */
public class BombThrower implements Runnable{
    private ClientObject client;
    private Bomb bomb;

    public BombThrower(ClientObject client, Bomb bomb) {
        this.client = client;
        this.bomb = bomb;
    }

    @Override
    public void run() {
        System.out.println(this.bomb.getZone() + " bomb thrown!");
        for(int i = 0; i< 5; i++){
            System.out.println(this.bomb.getZone() + " | " + (5-i) + " seconds...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
        }
        if(client.getPosition().getZone().equals(this.bomb.getZone())){
            client.bombSuicide(this.bomb);
        } else {
            P2PCommunicationService.bombExploded(client, this.bomb);
        }
    }
}
