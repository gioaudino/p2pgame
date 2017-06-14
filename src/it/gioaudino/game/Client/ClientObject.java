package it.gioaudino.game.Client;

import com.mashape.unirest.http.exceptions.UnirestException;
import it.gioaudino.game.Entity.*;
import it.gioaudino.game.Exception.BadRequestException;
import it.gioaudino.game.Exception.HTTPException;
import it.gioaudino.game.Service.ClientRESTCommunicationService;
import it.gioaudino.game.Service.P2PCommunicationService;
import it.gioaudino.game.Simulator.BufferedMeasurements;
import it.gioaudino.game.Simulator.MeasurementAnalyser;
import it.unimi.Simulator.AccelerometerSimulator;
import it.unimi.Simulator.Buffer;
import it.unimi.Simulator.Measurement;

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
    private volatile int score = 0;
    private volatile Position position;
    private ServerSocket serverSocket;
    private volatile ClientStatus status = ClientStatus.STATUS_NEW;
    private volatile List<Socket> connections = new ArrayList<>();
    private volatile Socket next;
    private volatile Move move;
    private Bomb bomb;
    private volatile SimpleQueue<Bomb> bombQueue = new SimpleQueue<>();
    private MovePerformerRunnable mover;
    private AccelerometerSimulator accelerometer;
    private volatile Buffer<Measurement> buffer;
    private MeasurementAnalyser analyzer;
    private ClientListener listener;

    public final Token token = new Token();

    public ClientObject() throws IOException {
        this.serverSocket = new ServerSocket(0);
    }

    public int getScore() {
        return score;
    }

    public synchronized void increaseScore(Peer dead) {
        this.score++;
        if (checkWonGame()) win();
        else {
            System.out.println("\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");
            System.out.println("\t\tWell done! You scored one point killing " + dead.getUsername());
            System.out.println("\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");
        }
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
        token.unlock();
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

    public ClientListener getListener() {
        return listener;
    }

    public void setListener(ClientListener listener) {
        this.listener = listener;
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
        startBombMaker();
    }

    public void prepareToJoinGame() {
        this.mover = new MovePerformerRunnable(this);
        P2PCommunicationService.generateConnections(this);
        this.setNext();
        this.mover = new MovePerformerRunnable(this);

        P2PCommunicationService.newPlayer(this);
        this.token.lock();
        this.position = P2PCommunicationService.findPosition(this);

        new Thread(this.mover).start();
        System.out.println();
        UserInteractionHandler.printPlayingHeader(this);
        startBombMaker();

    }

    public boolean checkWonGame() {
        return this.game != null && this.score >= this.game.getPoints();
    }

    public void clearMove() {
        this.move = null;
    }

    public void quitGame() {
        this.mover.stopMe();
        this.setStatus(ClientStatus.STATUS_DEAD);
        System.out.println("My status says I'm dead");
        new Thread(this::exitFromGame).start();
        System.out.println("Even the server knows I'm dead");
        holdTokenAndQuit(null);

//        P2PCommunicationService.quitGame(this);
        clearGameValues();
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
        this.mover.stopMe();
        new Thread(this::exitFromGame).start();
        holdTokenAndQuit(killer);
//        P2PCommunicationService.die(this, killer);

        System.out.println("\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
        System.out.println("\t\t\tYou just got killed by " + killer.getUsername());
        System.out.println("\t\t\tPress enter to continue");
        System.out.println("\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");

        clearGameValues();
    }

    private void holdTokenAndQuit(Peer killer) {
        if (connections.size() > 0) {
            synchronized (token) {
                if (!token.getStatus())
                    this.token.lock();
            }
            if (null == killer)
                P2PCommunicationService.quitGame(this);
            else
                P2PCommunicationService.die(this, killer);

            P2PCommunicationService.giveToken(this, true);
        }
    }


    public void win() {
        exitFromGame();
        P2PCommunicationService.win(this);

        System.out.println("\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");
        System.out.println("\t\t\tYou won!! That was an astonishing game, congratulations!");
        System.out.println("\t\t\tPress enter to continue");
        System.out.println("\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");

        clearGameValues();
    }

    public void endGame(Peer winner) {
        exitFromGame();
        System.out.println("\n####################################################################################\n");
        System.out.println("\t\t\tThe game was won by " + winner.getUsername() + ". Thanks for playing!");
        System.out.println("\t\t\tPress enter to continue");
        System.out.println("\n####################################################################################\n");
        clearGameValues();
    }

    private void clearGameValues() {
        System.out.println("\n\nCLEARING GAME VALUES\n\n");
        this.score = 0;
        for (Socket socket : connections) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
        this.connections = new ArrayList<>();
        this.game = null;
        this.next = null;
        this.move = null;
        this.status = ClientStatus.STATUS_NOT_PLAYING;
        System.out.println("CLOSED LISTENING SOCKETS");
        this.accelerometer.stopMeGently();
        this.analyzer.setKilled();
        this.bombQueue.clearQueue();
    }

    private void createAndStartAccelerometer() {
        this.buffer = new BufferedMeasurements<>();
        this.accelerometer = new AccelerometerSimulator("", this.buffer);
        new Thread(this.accelerometer).start();
    }

    private void createAndStartAnalyzer() {
        this.analyzer = new MeasurementAnalyser(this.buffer);
        new Thread(this.analyzer).start();
    }

    private void startBombMaker() {
        this.createAndStartAccelerometer();
        this.createAndStartAnalyzer();
    }


    public void removeConnection(Socket socket) {
        this.connections.remove(socket);
    }


}
