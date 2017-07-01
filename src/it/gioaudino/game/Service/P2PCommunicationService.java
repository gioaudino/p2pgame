package it.gioaudino.game.Service;

import com.google.gson.JsonSyntaxException;
import it.gioaudino.game.Client.Player;
import it.gioaudino.game.Entity.*;
import it.gioaudino.game.Exception.NegativeResponseException;
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

    public static void generateConnections(Player player) {
        if (player.getGame().getUsers().size() > 0) {
            Collection<User> users = player.getGame().getUsers().values();
            for (User user : users) {
                if (!user.equals(player.getUser())) {
                    try {
                        Socket socket = new Socket(user.getAddress(), user.getPort());
                        player.addConnection(socket);
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    public static void newPlayer(Player player) {
        if (player.getConnections().size() > 0) {
            Message message = new Message();
            message.setSender(player.getUser());
            message.setType(MessageType.TYPE_NEW);

            fireToSocketsAndWait(player.getConnections(), message, MAX_ATTEMPTS);
        }
    }

    public static void move(Player player) {
        Collection<Socket> sockets = player.getConnections();
        Message message = new Message();
        message.setSender(player.getUser());
        message.setType(MessageType.TYPE_MOVE);
        message.setPosition(player.getPosition());
        fireToSocketsAndWait(sockets, message, MAX_ATTEMPTS);
    }

    public static Position findPosition(Player player) {
        class AccessibleThread extends Thread {
            private MessageType responseType;
            private Socket socket;
            private Message message;

            private AccessibleThread(Socket socket, Message message) {
                this.socket = socket;
                this.message = message;
            }

            @Override
            public void run() {
                try {
                    P2PCommunicationService.fireMessage(socket, message, true, MAX_ATTEMPTS);
                } catch (NegativeResponseException e) {
                    responseType = MessageType.TYPE_NACK;
                }
            }
        }
        Position position = Position.getRandomPosition(player.getGame().getSize());
        Message message = new Message();
        message.setType(MessageType.TYPE_FIND_POSITION);
        message.setPosition(position);
        message.setSender(player.getUser());

        ArrayList<AccessibleThread> threads = new ArrayList<>();
        for (Socket socket : player.getConnections()) {
            threads.add(new AccessibleThread(socket, message));
        }
        for (Thread thread : threads)
            thread.start();
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
        }

        threads.removeIf(el -> el.responseType != MessageType.TYPE_NACK);

        return threads.size() > 0 ? findPosition(player) : position;
    }

    public static void giveToken(Player player) {
        Socket recipient = player.getNext();
        Message message = new Message();
        message.setSender(player.getUser());
        message.setType(MessageType.TYPE_TOKEN);
        String serializedMessage = GsonService.getSimpleInstance().toJson(message);
        synchronized (player.getNext()) {
            Message responseMsg = null;
            do {

                try {
                    DataOutputStream out = new DataOutputStream(recipient.getOutputStream());
                    BufferedReader in = new BufferedReader(new InputStreamReader(recipient.getInputStream()));
                    out.writeBytes(serializedMessage + '\n');
                    String response = in.readLine();
                    try {
                        responseMsg = GsonService.getSimpleInstance().fromJson(response, Message.class);
                    } catch (JsonSyntaxException ignored) {
                        ignored.printStackTrace();
                    }
                } catch (IOException ignored) {
                }
            } while (responseMsg != null && responseMsg.getType() != MessageType.TYPE_ACK);
        }
    }

    public static void quitGame(Player player) {
        if (player.getConnections().size() > 0) {
            Message message = new Message();
            message.setSender(player.getUser());
            message.setType(MessageType.TYPE_QUIT);
            fireToSocketsAndWait(player.getConnections(), message, 2 * MAX_ATTEMPTS);
        }
    }

    public static void die(Player player, User killer, Bomb bomb) {
        Message message = new Message();
        message.setSender(player.getUser());
        if (null == bomb)
            message.setType(MessageType.TYPE_DEAD);
        else {
            message.setType(MessageType.TYPE_BOMB_DEAD);
            message.setBomb(bomb);
        }
        message.setKiller(killer);
        fireToSocketsAndWait(player.getConnections(), message, 2 * MAX_ATTEMPTS);
    }

    public static void win(Player player) {
        if (player.getConnections().size() > 0) {
            Message message = new Message();
            message.setSender(player.getUser());
            message.setType(MessageType.TYPE_WIN);
            fireToSocketsAndWait(player.getConnections(), message, MAX_ATTEMPTS);
        }
    }

    public static void bombThrown(Player player, Bomb bomb) {
        bomb(player, bomb, MessageType.TYPE_BOMB_THROWN);
    }

    public static void bombExploded(Player player, Bomb bomb) {
        bomb(player, bomb, MessageType.TYPE_BOMB_EXPLODED);
    }

    private static void bomb(Player player, Bomb bomb, MessageType messageType) {
        if (player.getConnections().size() > 0) {
            Message message = new Message();
            message.setSender(player.getUser());
            message.setBomb(bomb);
            message.setType(messageType);
            fireToSocketsAndWait(player.getConnections(), message, MAX_ATTEMPTS);
        }
    }

    private static void fireToSocketsAndWait(Collection<Socket> sockets, Message message, int attempts) {
        ArrayList<Thread> threads = new ArrayList<>();
        for (Socket socket : sockets) {
            threads.add(new Thread(() -> P2PCommunicationService.fireMessage(socket, message, false, attempts)));
        }
        for (Thread thread : threads)
            thread.start();
        for (Thread thread : threads)
            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
    }

    private static void fireMessage(Socket socket, Message message, boolean ackAndNo, int attempts) throws NegativeResponseException {
        try {
            if (ackAndNo)
                doFireAckNoMessage(socket, message, attempts);
            else
                doFireAckMessage(socket, message, attempts);
        } catch (NotAcknowledgedException ignored) {
        }
    }

    private static void doFireAckMessage(Socket socket, Message message, int attempts) throws NotAcknowledgedException {
        try {
            Message responseMessage = sendMessageAndGetResponse(socket, message);
            if (responseMessage.getType() != MessageType.TYPE_ACK) {
                throw new NotAcknowledgedException();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotAcknowledgedException e) {
            if (attempts - 1 > 0) {
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                doFireAckMessage(socket, message, attempts - 1);
            } else
                throw e;
        }
    }

    private static void doFireAckNoMessage(Socket socket, Message message, int attempts) throws NotAcknowledgedException, NegativeResponseException {
        try {
            Message responseMessage = sendMessageAndGetResponse(socket, message);
            switch (responseMessage.getType()) {
                case TYPE_ACK:
                    break;
                case TYPE_NACK:
                    throw new NegativeResponseException();
                default:
                    throw new NotAcknowledgedException();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotAcknowledgedException e) {
            if (attempts - 1 > 0) {
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                doFireAckNoMessage(socket, message, attempts - 1);
            } else
                throw e;
        }
    }

    private static Message sendMessageAndGetResponse(Socket socket, Message message) throws IOException {
        synchronized (socket) {
            String serializedMessage = GsonService.getSimpleInstance().toJson(message);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.writeBytes(serializedMessage + '\n');
            String response = in.readLine();
            Message m;
            try {
                m = GsonService.getSimpleInstance().fromJson(response, Message.class);
            } catch (JsonSyntaxException e) {
                m = new Message();
                m.setType(MessageType.TYPE_PROBLEM);
            }
            return m;
        }
    }

}