package it.gioaudino.game.Client;

import it.gioaudino.game.Exception.CannotSetCommunicationPipeException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gioaudino on 20/05/17.
 * Package it.gioaudino.game.Client in game
 */
public class ClientListener extends Thread {

    private ServerSocket serverSocket;
    private Player player;
    private List<InFromPeer> establishedThreads;

    public ClientListener(Player player) {
        this.serverSocket = player.getServerSocket();
        this.player = player;
        this.establishedThreads = new ArrayList<>();

    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket connectionSocket = serverSocket.accept();
                InFromPeer p = new InFromPeer(player, connectionSocket);
                p.start();
                establishedThreads.add(p);
            } catch (IOException | CannotSetCommunicationPipeException e) {
                e.printStackTrace();
            }

        }
    }

    public void killAllListeningThreads(){
        for(InFromPeer in: establishedThreads){
            in.interrupt();
        }
    }
}
