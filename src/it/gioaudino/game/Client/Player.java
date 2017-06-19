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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gioaudino on 16/05/17.
 */
public class Player {
    private User user;
    private Game game;
    private volatile Integer score = 0;
    private volatile Position position;
    private ServerSocket serverSocket;
    private volatile ClientStatus status = ClientStatus.STATUS_NEW;
    private volatile List<Socket> connections = new ArrayList<>();
    private volatile Socket next;
    private volatile Move move;
    private Map<Bomb, Integer> bombScore = new HashMap<>();
    private volatile SimpleQueue<Bomb> bombQueue = new SimpleQueue<>();
    private MovePerformerRunnable mover;
    private OutputPrinter outputPrinter = new OutputPrinter(System.out);

    private volatile Buffer<Measurement> buffer;
    private AccelerometerSimulator accelerometer;
    private MeasurementAnalyser analyzer;

    public final Token token = new Token();

    public Player() throws IOException {
        this.serverSocket = new ServerSocket(0);
    }

    public int getScore() {
        return score;
    }

    public void increaseScore(User dead) {
        addToScore();
        if (checkWonGame()) win();
        else {
            outputPrinter.pointScored(new Object[]{dead.getUsername()});
        }
    }

    private synchronized void addToScore() {
        this.score++;
    }

    public void increaseBombScore(Bomb bomb, User dead) {
        int currentBombScore = this.bombScore.getOrDefault(bomb, 0);
        if (currentBombScore < 3) {
            addToScore();
            this.bombScore.put(bomb, currentBombScore + 1);
            if (checkWonGame()) win();
            else {
                outputPrinter.bombPointScored(new Object[]{dead.getUsername()});
            }
        } else {
            outputPrinter.bombNoPointsScored(new Object[]{dead.getUsername()});
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
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

    public SimpleQueue<Bomb> getBombs() {
        return bombQueue;
    }

    public OutputPrinter getOutputPrinter() {
        return outputPrinter;
    }

    public User buildPeer(String username) {
        this.user = new User(username, serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort());
        return this.user;
    }

    public void prepareForNewGame(Game game) {
        this.bombQueue.clearQueue();
        this.setGame(game);
        this.setStatus(ClientStatus.STATUS_PLAYING);
        this.setPosition(Position.getRandomPosition(game.getSize()));
        outputPrinter.printPlayingHeader(this);
        this.mover = new MovePerformerRunnable(this);
        new Thread(this.mover).start();
        startBombMaker();
    }

    public void prepareToJoinGame() {
        this.bombQueue.clearQueue();
        this.mover = new MovePerformerRunnable(this);
        P2PCommunicationService.generateConnections(this);
        this.setNext();
        this.mover = new MovePerformerRunnable(this);

        P2PCommunicationService.newPlayer(this);
        this.token.lock();
        this.position = P2PCommunicationService.findPosition(this);

        new Thread(this.mover).start();
        outputPrinter.println();
        outputPrinter.printPlayingHeader(this);
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
        exitFromGame();
        holdTokenAndQuit(null, null);

        clearGameValues();
    }

    private void exitFromGame() {

        try {
            ClientRESTCommunicationService.quitGame(this);
        } catch (UnirestException | HTTPException e) {
            if (!(e instanceof BadRequestException)) {
                outputPrinter.println("Something happened while quitting the game: " + e.getMessage());
            }
        }
    }

    public void die(User killer, Bomb bomb) {
        this.status = ClientStatus.STATUS_DEAD;
        this.mover.stopMe();
        exitFromGame();
        holdTokenAndQuit(killer, bomb);

        if (killer.equals(this.user))
            outputPrinter.suicide();
        else
            outputPrinter.dead(new Object[]{killer.getUsername(), (null == bomb ? "" : "'s bomb")});

        clearGameValues();
    }

    private void holdTokenAndQuit(User killer, Bomb bomb) {
        if (connections.size() > 0) {
            synchronized (token) {
                if (!token.getStatus())
                    this.token.lock();
            }
            if (null == killer)
                P2PCommunicationService.quitGame(this);
            else
                P2PCommunicationService.die(this, killer, bomb);
            P2PCommunicationService.giveToken(this, true);
        }
    }


    public void win() {
        exitFromGame();
        P2PCommunicationService.win(this);

        outputPrinter.win();

        clearGameValues();
    }

    public void endGame(User winner) {
        exitFromGame();
        outputPrinter.endGame(new Object[]{winner.getUsername()});
        clearGameValues();
    }

    private void clearGameValues() {
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
        this.accelerometer.stopMeGently();
        this.analyzer.setKilled();
        try {
            this.serverSocket = new ServerSocket(0);
            new Thread(new ClientListener(this)).start();
        } catch (IOException ignored) {

        }
        this.bombQueue.clearQueue();
    }

    private void createAndStartAccelerometer() {
        this.buffer = new BufferedMeasurements<>();
        this.accelerometer = new AccelerometerSimulator("", this.buffer);
        new Thread(this.accelerometer).start();
    }

    private void createAndStartAnalyzer() {
        this.analyzer = new MeasurementAnalyser(this, this.buffer);
        new Thread(this.analyzer).start();
    }

    private void startBombMaker() {
        this.createAndStartAccelerometer();
        this.createAndStartAnalyzer();
    }

    public void addBomb(Bomb bomb) {
        this.bombQueue.push(bomb);
        if (this.bombQueue.size() == 1) {
            outputPrinter.println("••••• You have a bomb now! It's a " + PositionZone.getZoneAsString(this.bombQueue.peek().getZone()).toLowerCase() + " bomb •••••");
        }
    }

    public void addBomb(Double outlier) {
        Bomb bomb = new Bomb(this.user, PositionZone.findZoneFromOutlier(outlier));
        this.addBomb(bomb);
    }


    public void removeConnection(Socket socket) {
        this.connections.remove(socket);
    }


    public void bombExploded(Bomb bomb) {
        if (bomb.getZone().equals(this.position.getZone())) {
            this.die(bomb.getThrower(), bomb);
        } else {
            outputPrinter.println(bomb.getThrower().getUsername() + "'s bomb exploded! You made it, keep going!");
        }
    }

    public void throwNext() {
        Bomb bomb = this.bombQueue.pop();
        Move move = new Move(bomb);
        this.setMove(move);
    }
}
