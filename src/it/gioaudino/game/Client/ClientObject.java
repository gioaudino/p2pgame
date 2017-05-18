package it.gioaudino.game.Client;

import it.gioaudino.game.Entity.ClientStatus;
import it.gioaudino.game.Entity.Game;
import it.gioaudino.game.Entity.Peer;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by gioaudino on 16/05/17.
 */
public class ClientObject {

    private Peer user;
    private Game game;
    private ServerSocket serverSocket;
    private ClientStatus status = ClientStatus.STATUS_NEW;

    public ClientObject() throws IOException {
        this.serverSocket = new ServerSocket(0);
    }

    public ClientStatus getStatus() {
        return status;
    }

    public void setStatus(ClientStatus status) {
        this.status = status;
    }

    public Peer getUser() {
        return user;
    }

    public void setUser(Peer user) {
        this.user = user;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Peer buildPeer(String username) {
        this.user = new Peer(username, serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort());
        return this.user;
    }
}
