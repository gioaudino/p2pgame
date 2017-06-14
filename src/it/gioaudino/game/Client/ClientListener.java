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
    private ClientObject client;
    private List<InFromPeer> established;

    public ClientListener(ClientObject client) {
        this.serverSocket = client.getServerSocket();
        this.client = client;
        this.established = new ArrayList<>();
    }

    public void clearList(){
        for(InFromPeer runnable: established)
            runnable.stopMe();
    }

    @Override
    public void run() {
        while (true){
            try {
                Socket connectionSocket = serverSocket.accept();
                System.out.println("Received connection! -- " + connectionSocket.getRemoteSocketAddress());
                InFromPeer p = new InFromPeer(client, connectionSocket);
                new Thread(p).start();
                established.add(p);
            } catch (IOException | CannotSetCommunicationPipeException e) {
                e.printStackTrace();
            }

        }
    }
}
