package it.gioaudino.game.Client;

import com.google.gson.JsonSyntaxException;
import it.gioaudino.game.Entity.ClientStatus;
import it.gioaudino.game.Entity.Message;
import it.gioaudino.game.Entity.MessageType;
import it.gioaudino.game.Exception.CannotSetCommunicationPipeException;
import it.gioaudino.game.Service.GsonService;
import it.gioaudino.game.Service.MessageHandler;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;


/**
 * Created by gioaudino on 31/05/17.
 * Package it.gioaudino.game.Client in game
 */
public class InFromPeer implements Runnable {

    private ClientObject client;
    private DataOutputStream out;
    private BufferedReader in;

    public InFromPeer(ClientObject client, Socket socket) throws CannotSetCommunicationPipeException {
        this.client = client;
        try {
            out = new DataOutputStream(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            throw new CannotSetCommunicationPipeException();
        }
    }

    @Override
    public void run() {
        String input;

        while (client.getStatus() == ClientStatus.STATUS_DEAD || client.getStatus() == ClientStatus.STATUS_PLAYING) {
            System.err.println("STATUS IS " + client.getStatus());
            try {
                input = in.readLine();
                Message message = GsonService.getSimpleInstance().fromJson(input, Message.class);
                Message response;

                if (message.getType() != MessageType.TYPE_TOKEN && client.getStatus() == ClientStatus.STATUS_DEAD) {
                    if (message.getType() == MessageType.TYPE_BOMB_DEAD || message.getType() == MessageType.TYPE_DEAD)
                        MessageHandler.removeSocketAndSetNext(client, message);
                    response = new Message();
                    response.setSender(client.getUser());
                    response.setType(MessageType.TYPE_ACK);
                } else {
                    response = MessageHandler.handleMessage(client, message);
                }

                String serializedResponse = GsonService.getSimpleInstance().toJson(response);
                out.writeBytes(serializedResponse + "\n");
            } catch (IOException | NullPointerException ex) {
                return;
            } catch (JsonSyntaxException e) {
                Message response = new Message();
                response.setSender(client.getUser());
                response.setType(MessageType.TYPE_PROBLEM);
                String serializedResponse = GsonService.getSimpleInstance().toJson(response);
                try {
                    out.writeBytes(serializedResponse + "\n");
                } catch (IOException e1) {
                    break;
                }
                try {
                    Thread.sleep(30);
                } catch (InterruptedException ignored) {
                }
            }
            System.err.println("STATUS IS " + client.getStatus());
        }
    }
}
