package it.gioaudino.game.Client;

import it.gioaudino.game.Entity.*;
import it.gioaudino.game.Exception.IWonException;
import it.gioaudino.game.Service.P2PCommunicationService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gioaudino on 16/05/17.
 */
public class ClientObject {

    private Peer user;
    private Game game;
    private int score = 0;
    private Position position;
    private ServerSocket serverSocket;
    private ClientStatus status = ClientStatus.STATUS_NEW;
    private List<Socket> connections = new ArrayList<>();
    private Socket next;
    private Move move;

    public final Object token = new Object();


    public ClientObject() throws IOException {
        this.serverSocket = new ServerSocket(0);
    }

    public int getScore() {
        return score;
    }

    public void increaseScore() throws IWonException {
        this.score++;
        if (checkWonGame())
            throw new IWonException();
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
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

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setStartingPosition() {
        Position pos = new Position(1, 1, game.getSize());
    }

    public Move getMove() {
        return move;
    }

    public void setMove(Move move) {
        this.move = move;
    }

    public void setNext() {
        this.next = connections.get(0);
    }

    public void setNext(Socket next) {
        this.next = null == next ? connections.get(0) : next;
    }

    public Socket getNext() {
        return next;
    }

    public void addConnection(Socket socket) {
        this.connections.add(socket);
    }

    public void clearConnections() {
        this.connections = new ArrayList<>();
    }

    public List<Socket> getConnections() {
        return connections;
    }

    public Peer buildPeer(String username) {
        this.user = new Peer(username, serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort());
        return this.user;
    }

    public void prepareToJoinGame() {
        System.out.println("I have " + game.getPeers().size() + " peers to call\n");
        if (game.getPeers().size() > 0) {
            P2PCommunicationService.generateConnections(this);
            P2PCommunicationService.newPlayer(this);
            this.setNext();
        }
        new Thread(new MovePerformerRunnable(this)).start();
    }


    private boolean checkWonGame() {
        return this.score >= this.game.getPoints();
    }

    public void clearMove() {
        this.move = null;
    }
}
