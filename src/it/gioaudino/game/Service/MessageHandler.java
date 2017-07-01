package it.gioaudino.game.Service;

import it.gioaudino.game.Client.Player;
import it.gioaudino.game.Entity.*;
import it.gioaudino.game.Simulator.BombReceived;

import java.io.IOException;
import java.net.Socket;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Created by gioaudino on 01/06/17.
 * Package it.gioaudino.game.Service in game
 */
public class MessageHandler {

    public static Message handleMessage(Player player, Message message) {
        if (player.getStatus() == ClientStatus.STATUS_NOT_PLAYING)
            return buildResponseMessage(player, MessageType.TYPE_ACK);
        switch (message.getType()) {
            case TYPE_NEW:
                return newPlayer(player, message);
            case TYPE_TOKEN:
                return token(player);
            case TYPE_FIND_POSITION:
                return findPosition(player, message);
            case TYPE_QUIT:
                return quit(player, message);
            case TYPE_BOMB_THROWN:
                return bombThrown(player, message);
            case TYPE_BOMB_EXPLODED:
                return bombExploded(player, message);
            case TYPE_BOMB_DEAD:
                return bombDead(player, message);
            case TYPE_DEAD:
                return dead(player, message);
            case TYPE_WIN:
                return win(player, message);
            case TYPE_MOVE:
                return move(player, message);
            default:
                return buildResponseMessage(player, MessageType.TYPE_PROBLEM);
        }
    }

    private static Message bombDead(Player player, Message message) {
        if (player.getUser().equals(message.getKiller())) {
            new Thread(() -> player.increaseBombScore(message.getBomb(), message.getSender())).start();
        }
        removeSocketAndSetNext(player, message);
        return buildResponseMessage(player, MessageType.TYPE_ACK);
    }

    private static Message move(Player player, Message message) {
        if (player.getPosition().equals(message.getPosition())) {
            player.setStatus(ClientStatus.STATUS_DEAD);
            new Thread(() -> player.die(message.getSender(), null)).start();
        }
        return buildResponseMessage(player, MessageType.TYPE_ACK);
    }

    private static Message dead(Player player, Message message) {
        if (player.getUser().equals(message.getKiller())) {
            new Thread(() -> player.increaseScore(message.getSender())).start();
        }
        removeSocketAndSetNext(player, message);
        return buildResponseMessage(player, MessageType.TYPE_ACK);
    }

    private static Message quit(Player player, Message message) {
        removeSocketAndSetNext(player, message);
        return buildResponseMessage(player, MessageType.TYPE_ACK);
    }

    public static void removeSocketAndSetNext(Player player, Message message) {
        if (player.getConnections().size() == 1 && getCanonicalRemoteAddress(player.getConnections().get(0)).equals(message.getSender().toString())) {
            player.clearConnections();
            player.clearNext();
            return;
        }
        Socket newNext = null;
        Socket toBeRemoved = null;
        ListIterator<Socket> sockets = player.getConnections().listIterator();

        while (sockets.hasNext()) {
            Socket s = sockets.next();
            String canonicalAddress = getCanonicalRemoteAddress(s);
            if (canonicalAddress.equals(message.getSender().toString())) {
                toBeRemoved = s;
                newNext = sockets.hasNext() ? sockets.next() : player.getConnections().get(0);
                break;
            }
        }

        player.removeConnection(toBeRemoved);
        try {
            toBeRemoved.close();
        } catch (IOException | NullPointerException ignored) {

        }
        if (newNext != null)
            player.setNext(newNext);
    }

    private static Message findPosition(Player player, Message message) {
        Position position = message.getPosition();
        if (player.getPosition() != null && player.getPosition().equals(position))
            return buildResponseMessage(player, MessageType.TYPE_NACK);
        return buildResponseMessage(player, MessageType.TYPE_ACK);
    }

    private static Message newPlayer(Player player, Message message) {
        User sender = message.getSender();
        try {
            Socket socket = new Socket(sender.getAddress(), sender.getPort());
            player.addConnection(socket);
            try {
                if (null == player.getNext()) {
                    player.setNext();
                } else {
                    Map.Entry<String, User> fst = player.getGame().getUsers().entrySet().iterator().next();
                    if (getCanonicalRemoteAddress(player.getNext()).equals(fst.getValue().toString())) {
                        player.setNext(socket);
                    }
                }
            } catch (NoSuchElementException e) {
                player.setNext();
            }
            return buildResponseMessage(player, MessageType.TYPE_ACK);
        } catch (IOException ignored) {
        }
        return null;
    }

    private static Message token(Player player) {
        while (player.getNext() != null && player.token.getStatus()) /* NO-OP */ ;
        player.token.unlock();
        return buildResponseMessage(player, MessageType.TYPE_ACK);
    }

    private static Message win(Player player, Message message) {
        new Thread(() -> player.endGame(message.getSender())).start();
        return buildResponseMessage(player, MessageType.TYPE_ACK);
    }

    private static Message bombThrown(Player player, Message message) {
        player.getOutputPrinter().println("•*•*•*• A bomb was thrown! •*•*•*•");
        player.getOutputPrinter().println(message.getSender().getUsername() + " has thrown a " + message.getBomb().getZone().toString().toLowerCase() + " bomb");

        new Thread(new BombReceived(player, message.getBomb())).start();
        return buildResponseMessage(player, MessageType.TYPE_ACK);
    }

    private static Message bombExploded(Player player, Message message) {
        new Thread(() -> player.bombExploded(message.getBomb())).start();
        return buildResponseMessage(player, MessageType.TYPE_ACK);
    }

    private static Message buildResponseMessage(Player player, MessageType messageType) {
        Message message = new Message();
        message.setSender(player.getUser());
        message.setType(messageType);
        return message;
    }

    private static String getCanonicalRemoteAddress(Socket socket) {
        return socket.getInetAddress().getCanonicalHostName() + ":" + socket.getPort();
    }
}
