package it.gioaudino.game.Service;

import it.gioaudino.game.Client.ClientObject;
import it.gioaudino.game.Entity.Message;
import it.gioaudino.game.Entity.MessageType;
import it.gioaudino.game.Entity.Peer;
import it.gioaudino.game.Exception.CannotSetCommunicationPipeException;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

/**
 * Created by gioaudino on 01/06/17.
 * Package it.gioaudino.game.Service in game
 */
public class MessageHandler {

    public static Message handleMessage(ClientObject client, Message message) {
        switch (message.getType()) {
            case TYPE_NEW:
                return newPlayer(client, message);
            case TYPE_TOKEN:
                return token(client, message);
        }
        return null;
    }

    private static Message newPlayer(ClientObject client, Message message) {
        Peer sender = message.getSender();
        try {
            Socket socket = new Socket(sender.getAddress(), sender.getPort());
            client.addConnection(socket);
            Map.Entry<String, Peer> fst = client.getGame().getPeers().entrySet().iterator().next();
            client.setNext(sender == fst.getValue() ? socket : null);
            return buildAckMessage(client);
        } catch (IOException ignored) {
        }
        return null;
    }

    private static Message token(ClientObject client, Message message){
        client.token.notify();
        System.out.println("RECEIVED TOKEN from " + message.getSender());
        return buildAckMessage(client);
    }

    private static Message buildAckMessage(ClientObject client) {
        Message message = new Message();
        message.setSender(client.getUser());
        message.setType(MessageType.TYPE_ACK);
        return message;
    }

}
