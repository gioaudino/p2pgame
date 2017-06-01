package it.gioaudino.game.Client;

import it.gioaudino.game.Exception.CannotSetCommunicationPipeException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by gioaudino on 20/05/17.
 * Package it.gioaudino.game.Client in game
 */
public class ClientListener implements Runnable {

    private ServerSocket serverSocket;
    private ClientObject client;

    public ClientListener(ClientObject client) {
        this.serverSocket = client.getServerSocket();
        this.client = client;
    }

    @Override
    public void run() {
        while (true){
            try {
                Socket connectionSocket = serverSocket.accept();
                System.out.println("Received connection! -- " + connectionSocket.getRemoteSocketAddress());
                Thread thread = new Thread(new InFromPeer(client, connectionSocket));
                thread.start();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CannotSetCommunicationPipeException e) {
                e.printStackTrace();
            }

        }
    }
}
