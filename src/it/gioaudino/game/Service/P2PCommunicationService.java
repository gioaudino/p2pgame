package it.gioaudino.game.Service;

import com.google.gson.JsonSyntaxException;
import it.gioaudino.game.Client.ClientObject;
import it.gioaudino.game.Entity.Message;
import it.gioaudino.game.Entity.MessageType;
import it.gioaudino.game.Entity.Peer;
import it.gioaudino.game.Entity.Position;
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

    public static void generateConnections(ClientObject client) {
        if (client.getGame().getPeers().size() > 0) {
            Collection<Peer> peers = client.getGame().getPeers().values();
            for (Peer peer : peers) {
                if (!peer.equals(client.getUser())) {
                    try {
                        System.out.println("Connecting to " + peer);
                        Socket socket = new Socket(peer.getAddress(), peer.getPort());
                        client.addConnection(socket);
                    } catch (IOException e) {
//                System.err.println("Peer " + peer.getUsername() + " is unreachable.");
                    }
                }
            }
        }
    }

    public static void newPlayer(ClientObject client) {
        if (client.getConnections().size() > 0) {
            Message message = new Message();
            message.setSender(client.getUser());
            message.setType(MessageType.TYPE_NEW);

            fireToSocketsAndWait(client.getConnections(), message);
        }
    }

    public static void move(ClientObject client) {
        Collection<Socket> sockets = client.getConnections();
        Message message = new Message();
        message.setSender(client.getUser());
        message.setType(MessageType.TYPE_MOVE);
        message.setPosition(client.getPosition());
        fireToSocketsAndWait(sockets, message);
    }

    public static Position findPosition(ClientObject client) {
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
                    P2PCommunicationService.fireMessage(socket, message, true);
                } catch (NegativeResponseException e) {
                    responseType = MessageType.TYPE_NACK;
                }
            }
        }
        Position position = Position.getRandomPosition(client.getGame().getSize());
        Message message = new Message();
        message.setType(MessageType.TYPE_FIND_POSITION);
        message.setPosition(position);
        message.setSender(client.getUser());

        ArrayList<AccessibleThread> threads = new ArrayList<>();
        for (Socket socket : client.getConnections()) {
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

        return threads.size() > 0 ? findPosition(client) : position;
    }

    public static void giveToken(ClientObject client) {
        Socket recipient = client.getNext();
        Message message = new Message();
        message.setSender(client.getUser());
        message.setType(MessageType.TYPE_TOKEN);
//        System.out.println("+-+-+- SENDING TOKEN TO " + recipient.getInetAddress().getCanonicalHostName() + ":" +recipient.getPort() + " -+-+-+");
        fireMessage(recipient, message, false);
    }

    public static void quitGame(ClientObject client) {
        if (client.getConnections().size() > 0) {
            Message message = new Message();
            message.setSender(client.getUser());
            message.setType(MessageType.TYPE_QUIT);
            fireToSocketsAndWait(client.getConnections(), message);
        }
    }

    public static void die(ClientObject client, Peer killer) {
        Message message = new Message();
        message.setSender(client.getUser());
        message.setType(MessageType.TYPE_DEAD);
        message.setKiller(killer);
        fireToSocketsAndWait(client.getConnections(), message);
    }

    public static void win(ClientObject client){
        if (client.getConnections().size() > 0) {
            Message message = new Message();
            message.setSender(client.getUser());
            message.setType(MessageType.TYPE_WIN);
            fireToSocketsAndWait(client.getConnections(), message);
        }
    }

    private static void fireToSocketsAndWait(Collection<Socket> sockets, Message message) {
        ArrayList<Thread> threads = new ArrayList<>();
        for (Socket socket : sockets) {
            threads.add(new Thread(() -> P2PCommunicationService.fireMessage(socket, message, false)));
        }
        for (Thread thread : threads)
            thread.start();
        for (Thread thread : threads)
            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
    }

    private static void fireMessage(Socket socket, Message message, boolean ackAndNo) throws NegativeResponseException {
        try {
            if (!ackAndNo)
                doFireAckMessage(socket, message, MAX_ATTEMPTS);
            else
                doFireAckNoMessage(socket, message, MAX_ATTEMPTS);
        } catch (NotAcknowledgedException e) {
            System.err.println("Peer @ " + socket.getRemoteSocketAddress().toString() + " not reachable.");
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
                    Thread.sleep(50);
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
                    Thread.sleep(50);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                doFireAckNoMessage(socket, message, attempts - 1);}
            else
                throw e;
        }
    }

    private static synchronized Message sendMessageAndGetResponse(Socket socket, Message message) throws IOException {
        if (message.getType() != MessageType.TYPE_TOKEN) System.out.println("SENDING MESSAGE " + message.getType());
        String serializedMessage = GsonService.getSimpleInstance().toJson(message);
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out.writeBytes(serializedMessage + '\n');
        String response = in.readLine();
        try {
            return GsonService.getSimpleInstance().fromJson(response, Message.class);
        } catch (JsonSyntaxException e) {
            System.err.println(response);
            Message resp = new Message();
            resp.setType(MessageType.TYPE_PROBLEM);
            return resp;
        }

    }


}

