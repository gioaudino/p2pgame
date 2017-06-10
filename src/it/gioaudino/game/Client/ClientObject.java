package it.gioaudino.game.Client;

import com.mashape.unirest.http.exceptions.UnirestException;
import it.gioaudino.game.Entity.*;
import it.gioaudino.game.Exception.BadRequestException;
import it.gioaudino.game.Exception.HTTPException;
import it.gioaudino.game.Service.ClientRESTCommunicationService;
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
    private boolean haveIWon = false;
    private boolean killed = false;
    private boolean ready = false;
    private MovePerformerRunnable mover;

    public final Object token = new Object();


    public ClientObject() throws IOException {
        this.serverSocket = new ServerSocket(0);
    }

    public int getScore() {
        return score;
    }

    public void increaseScore(Peer dead) {
        this.score++;
        System.out.println("\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
        System.out.println("\t\tWell done! You scored one point killing " + dead.getUsername());
        System.out.println("\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
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
        this.next = connections.size() > 0 ? connections.get(0) : null;
    }

    public void setNext(Socket next) {
        this.next = null == next ? connections.get(0) : next;
    }

    public void clearNext() {
        this.next = null;
        synchronized (this.token) {
            this.token.notify();
        }
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

    public boolean isKilled() {
        return killed;
    }

    public void setKilled() {
        this.killed = true;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady() {
        setReady(true);
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public Peer buildPeer(String username) {
        this.user = new Peer(username, serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort());
        return this.user;
    }

    public void prepareForNewGame(Game game) {
        this.setGame(game);
        this.setStatus(ClientStatus.STATUS_PLAYING);
        this.setPosition(Position.getRandomPosition(game.getSize()));
        UserInteractionHandler.printPlayingHeader(this);
        this.mover = new MovePerformerRunnable(this);
        new Thread(this.mover).start();
    }

    public void prepareToJoinGame() {
        this.mover = new MovePerformerRunnable(this);
        P2PCommunicationService.generateConnections(this);
        this.setNext();
        new Thread(this.mover).start();
        P2PCommunicationService.newPlayer(this);

        this.position = P2PCommunicationService.findPosition(this);
        UserInteractionHandler.printPlayingHeader(this);

    }

    public boolean checkWonGame() {
        this.haveIWon = this.game != null && this.score >= this.game.getPoints();
        return haveIWon;
    }

    public void clearMove() {
        this.move = null;
    }

    public void quitGame() {
        this.setStatus(ClientStatus.STATUS_DEAD);
        if (exitFromGame()) return;

        P2PCommunicationService.quitGame(this);
        this.score = 0;
        this.connections = new ArrayList<>();
        this.game = null;
        this.next = null;
        this.move = null;
        this.status = ClientStatus.STATUS_NOT_PLAYING;
    }

    private boolean exitFromGame() {
        try {
            ClientRESTCommunicationService.quitGame(this);
        } catch (UnirestException | HTTPException e) {
            if (!(e instanceof BadRequestException)) {
                System.out.println("Something happened while quitting the game: " + e.getMessage());
                return true;
            }
        }
        return false;
    }

    public void die(Peer killer) {
        exitFromGame();
        P2PCommunicationService.die(this, killer);

        System.out.println("\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
        System.out.println("\t\t\t\tYou just got killed by " + killer.getUsername());
        System.out.println("\t\t\t\tPress enter to continue");
        System.out.println("\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");

        this.score = 0;
        this.connections = new ArrayList<>();
        this.game = null;
        this.next = null;
        this.move = null;
        this.status = ClientStatus.STATUS_NOT_PLAYING;
    }

    public void win() {

    }

    public void removeConnection(Socket socket) {
        this.connections.remove(socket);
    }


}
