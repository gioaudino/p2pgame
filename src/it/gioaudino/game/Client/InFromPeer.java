package it.gioaudino.game.Client;

import it.gioaudino.game.Entity.Message;
import it.gioaudino.game.Exception.CannotSetCommunicationPipeException;
import it.gioaudino.game.Exception.DyingThreadException;
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
    private Socket socket;
    private DataOutputStream out;
    private BufferedReader in;

    public InFromPeer(ClientObject client, Socket socket) throws CannotSetCommunicationPipeException {
        this.client = client;
        this.socket = socket;
        try {
            out = new DataOutputStream(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            throw new CannotSetCommunicationPipeException();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                String input = in.readLine();
                Message message = GsonService.getSimpleInstance().fromJson(input, Message.class);
                System.out.println("RECEIVED MESSAGE: " + message.getType());
                Message response = MessageHandler.handleMessage(client, message);
                out.writeBytes(response + "\n");
            } catch (Exception e) {
                return;
            }
        }
    }
}
