package it.gioaudino.game.Service;

import it.gioaudino.game.Client.ClientObject;
import it.gioaudino.game.Entity.Message;
import it.gioaudino.game.Entity.MessageType;
import it.gioaudino.game.Entity.Peer;
import it.gioaudino.game.Exception.NotAcknowledgedException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by gioaudino on 19/05/17.
 * Package it.gioaudino.game.Service in game
 */
public class P2PCommunicationService {

    private static final int MAX_ATTEMPTS = 5;

    public static void generateConnections(ClientObject client) {
        Collection<Peer> peers = client.getGame().getPeers().values();
        for (Peer peer : peers) {
            try {
                System.out.println("Connecting to " + peer);
                Socket socket = new Socket(peer.getAddress(), peer.getPort());
                client.addConnection(socket);
            } catch (IOException e) {
//                System.err.println("Peer " + peer.getUsername() + " is unreachable.");
            }
        }
    }

    public static void newPlayer(ClientObject client) {
        Collection<Socket> sockets = client.getConnections();

        Message message = new Message();
        message.setSender(client.getUser());
        message.setType(MessageType.TYPE_NEW);

        fireToSocketsAndWait(sockets, message);
    }

    public static void move(ClientObject client){
        Collection<Socket> sockets = client.getConnections();

        Message message = new Message();
        message.setSender(client.getUser());
        message.setType(MessageType.TYPE_MOVE);
        message.setPosition(client.getPosition());

        //TODO actually send new position

    }

    public static void giveToken(ClientObject client){
        Socket recipient = client.getNext();
        Message message = new Message();
        message.setSender(client.getUser());
        message.setType(MessageType.TYPE_TOKEN);
        fireMessage(recipient, message);
    }

    private static void fireToSocketsAndWait(Collection<Socket> sockets, Message message) {
        ArrayList<Thread> threads = new ArrayList<>();
        for (Socket socket : sockets) {
            threads.add(new Thread(() -> P2PCommunicationService.fireMessage(socket, message)));
        }
        for (Thread thread : threads)
            thread.start();
        for (Thread thread : threads)
            try {
                thread.join(1000);
            } catch (InterruptedException ignored) {
            }
    }

    private static void fireMessage(Socket socket, Message message) {
        try {
            doFireMessage(socket, message, MAX_ATTEMPTS);
        } catch (NotAcknowledgedException e) {
            System.err.println("Peer @ " + socket.getRemoteSocketAddress().toString() + " not reachable.");
        }
    }

    private static void doFireMessage(Socket socket, Message message, int attempts) throws NotAcknowledgedException {
        try {
            String serializedMessage = GsonService.getSimpleInstance().toJson(message);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.writeBytes(serializedMessage + '\n');
            String response = in.readLine();
            Message responseMessage = GsonService.getSimpleInstance().fromJson(response, Message.class);
            if (responseMessage.getType() != MessageType.TYPE_ACK) {
                throw new NotAcknowledgedException(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotAcknowledgedException e) {
            if (attempts - 1 > 0) doFireMessage(socket, message, attempts - 1);
            throw e;
        }
    }
}

