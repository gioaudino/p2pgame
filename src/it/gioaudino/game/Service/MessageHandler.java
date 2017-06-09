package it.gioaudino.game.Service;

import it.gioaudino.game.Client.ClientObject;
import it.gioaudino.game.Entity.*;

import java.io.IOException;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;

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
            case TYPE_FIND_POSITION:
                return findPosition(client, message);
            case TYPE_QUIT:
                return quit(client, message);
            case TYPE_DEAD:
                return dead(client, message);
            case TYPE_MOVE:
                return move(client, message);
            default:
                return buildResponseMessage(client, MessageType.TYPE_PROBLEM);
        }
    }

    private static Message move(ClientObject client, Message message) {
        if (client.getPosition().equals(message.getPosition())) {
            client.setStatus(ClientStatus.STATUS_DEAD);
            new Thread(() -> client.die(message.getSender())).start();
        }
        return buildResponseMessage(client, MessageType.TYPE_ACK);
    }

    private static Message dead(ClientObject client, Message message) {
        if (client.getUser().equals(message.getKiller())) {
            client.increaseScore(message.getSender());
        }
        removeSocketAndSetNext(client, message);
        return buildResponseMessage(client, MessageType.TYPE_ACK);
    }

    private static Message quit(ClientObject client, Message message) {
        removeSocketAndSetNext(client, message);
        return buildResponseMessage(client, MessageType.TYPE_ACK);
    }

    private static void removeSocketAndSetNext(ClientObject client, Message message) {
        MessageFormat format = new MessageFormat("Next: {0} -- Sender: {1} -- Connections.size(): {2}");
        System.out.println(format.format(new Object[]{client.getNext(), message.getSender(), client.getConnections().size()}));

        if (client.getConnections().size() == 1 && getCanonicalRemoteAddress(client.getConnections().get(0)).equals(message.getSender().toString())) {
            System.out.println("I'm all alone!");
            client.clearConnections();
            client.clearNext();
            System.out.println(format.format(new Object[]{client.getNext(), message.getSender(), client.getConnections().size()}));
            return;
        }

        Socket newNext = null;
        Socket toBeRemoved = null;
        ListIterator<Socket> sockets = client.getConnections().listIterator();

        while (sockets.hasNext()) {
            Socket s = sockets.next();
            String canonicalAddress = getCanonicalRemoteAddress(s);
            if (canonicalAddress.equals(message.getSender().toString())) {
                System.out.println("Found socket to be removed");
                toBeRemoved = s;
                if (canonicalAddress.equals(getCanonicalRemoteAddress(client.getNext())))
                    System.out.println("I'm removing my next. Have to update it");
                newNext = sockets.hasNext() ? sockets.next() : client.getConnections().get(0);
                break;
            }
        }

        System.out.println(format.format(new Object[]{client.getNext(), message.getSender(), client.getConnections().size()}));

        client.removeConnection(toBeRemoved);
        if (newNext != null) {
            System.out.println("Updating next");
            client.setNext(newNext);
        }
        System.out.println(format.format(new Object[]{client.getNext(), message.getSender(), client.getConnections().size()}));


//        for (Socket socket : client.getConnections()) {
//            if (next && getCanonicalRemoteAddress(client.getNext()).equals(message.getSender().getFullAddress())) {
//                client.setNext(socket);
//                break;
//            }
//            if (getCanonicalRemoteAddress(socket).equals(message.getSender().toString())) {
//                s = socket;
//                if (getCanonicalRemoteAddress(client.getNext()).equals(message.getSender().getFullAddress()) && client.getConnections().indexOf(s) == client.getConnections().size() - 1) {
//                    if (client.getConnections().size() > 1)
//                        client.setNext();
//                    else
//                        client.clearNext();
//                }
//                next = true;
//            }
//        }
//        client.removeConnection(s);
    }

    private static Message findPosition(ClientObject client, Message message) {
        Position position = message.getPosition();
        if (client.getPosition().equals(position))
            return buildResponseMessage(client, MessageType.TYPE_NACK);
        return buildResponseMessage(client, MessageType.TYPE_ACK);
    }

    private static Message newPlayer(ClientObject client, Message message) {
        Peer sender = message.getSender();
        try {
            Socket socket = new Socket(sender.getAddress(), sender.getPort());
            client.addConnection(socket);
            try {
                if (null == client.getNext()) {
                    client.setNext();
                } else {
                    Map.Entry<String, Peer> fst = client.getGame().getPeers().entrySet().iterator().next();
                    if (getCanonicalRemoteAddress(client.getNext()).equals(fst.getValue().toString())) {
                        client.setNext(socket);
                    }
                }
            } catch (NoSuchElementException e) {
                client.setNext();
            }
            return buildResponseMessage(client, MessageType.TYPE_ACK);
        } catch (IOException ignored) {
        }
        return null;
    }

    private static Message token(ClientObject client, Message message) {
//        System.out.println("+-+-+- RECEIVED TOKEN FROM " + message.getSender() + " -+-+-+");
        synchronized (client.token) {
            client.token.notify();
        }
        return buildResponseMessage(client, MessageType.TYPE_ACK);
    }

    private static Message buildResponseMessage(ClientObject client, MessageType messageType) {
        Message message = new Message();
        message.setSender(client.getUser());
        message.setType(messageType);
        return message;
    }

    private static String getCanonicalRemoteAddress(Socket socket) {
        return socket.getInetAddress().getCanonicalHostName() + ":" + socket.getPort();
    }
}
