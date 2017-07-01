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
public class InFromPeer extends Thread{

    private Player player;
    private DataOutputStream out;
    private BufferedReader in;

    public InFromPeer(Player player, Socket socket) throws CannotSetCommunicationPipeException {
        this.player = player;
        try {
            out = new DataOutputStream(socket.getOutputStream());
            InputStreamReader isr = new InputStreamReader(socket.getInputStream());
            in = new BufferedReader(isr);
        } catch (IOException e) {
            throw new CannotSetCommunicationPipeException();
        }
    }

    @Override
    public void run() {
        String input;

        while (player.getStatus() == ClientStatus.STATUS_DEAD || player.getStatus() == ClientStatus.STATUS_PLAYING) {
            try {
                input = in.readLine();
                Message message = GsonService.getSimpleInstance().fromJson(input, Message.class);
                Message response;

                if (message.getType() != MessageType.TYPE_TOKEN && player.getStatus() == ClientStatus.STATUS_DEAD) {
                    if (message.getType() == MessageType.TYPE_BOMB_DEAD || message.getType() == MessageType.TYPE_DEAD)
                        MessageHandler.removeSocketAndSetNext(player, message);
                    response = new Message();
                    response.setSender(player.getUser());
                    response.setType(MessageType.TYPE_ACK);
                } else {
                    response = MessageHandler.handleMessage(player, message);
                }

                String serializedResponse = GsonService.getSimpleInstance().toJson(response);
                out.writeBytes(serializedResponse + "\n");
            } catch (IOException | NullPointerException ex) {
                return;
            } catch (JsonSyntaxException e) {
                Message response = new Message();
                response.setSender(player.getUser());
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
        }
    }
    public void closeReader(){
        try {
            in.close();
        } catch (IOException ignored) {
        }
    }
}
