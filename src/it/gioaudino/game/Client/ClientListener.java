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
public class ClientListener implements Runnable {

    private ServerSocket serverSocket;
    private Player player;
    private List<InFromPeer> established;

    public ClientListener(Player player) {
        this.serverSocket = player.getServerSocket();
        this.player = player;
        this.established = new ArrayList<>();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket connectionSocket = serverSocket.accept();
                InFromPeer p = new InFromPeer(player, connectionSocket);
                new Thread(p).start();
                established.add(p);
            } catch (IOException | CannotSetCommunicationPipeException e) {
                e.printStackTrace();
            }

        }
    }
}
