package it.gioaudino.game.Client;

import com.mashape.unirest.http.exceptions.UnirestException;
import it.gioaudino.game.Entity.*;
import it.gioaudino.game.Exception.BadRequestException;
import it.gioaudino.game.Exception.HTTPException;
import it.gioaudino.game.Service.ClientRESTCommunicationService;
import it.gioaudino.game.Service.P2PCommunicationService;
import it.gioaudino.game.Simulator.BombThrower;
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
public class ClientObject {
    private Peer user;
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

    private volatile Buffer<Measurement> buffer;
    private AccelerometerSimulator accelerometer;
    private MeasurementAnalyser analyzer;

    public final Token token = new Token();

    public ClientObject() throws IOException {
        this.serverSocket = new ServerSocket(0);
    }

    public int getScore() {
        return score;
    }

    public void increaseScore(Peer dead) {
        addToScore();
        if (checkWonGame()) win();
        else {
            System.out.println("\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");
            System.out.println("\t\tWell done! You scored one point killing " + dead.getUsername());
            System.out.println("\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");
        }
    }

    private synchronized void addToScore() {
        this.score++;
    }

    public void increaseBombScore(Bomb bomb, Peer dead) {
        System.out.println("I killed someone with my bomb");
        int currentBombScore = this.bombScore.getOrDefault(bomb, 0);
        System.out.println("CURRENT BOMB SCORE IS " + currentBombScore + " -- Bomb was: " + bomb);
        if (currentBombScore < 3) {
            System.out.println(currentBombScore + " is < 3");
            addToScore();
            this.bombScore.put(bomb, currentBombScore + 1);
            System.out.println("SAVED BOMBS:");
            for(Map.Entry e: bombScore.entrySet())
                System.out.println(e.getKey() + " ---> " + e.getValue());
            if (checkWonGame()) win();
            else {
                System.out.println("\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");
                System.out.println("\t\tWell done! Your bomb killed " + dead.getUsername());
                System.out.println("\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");
            }
        } else {
            System.out.println("\n====================================================================================\n");
            System.out.println("\t\tYour bomb killed " + dead.getUsername() + " but this bomb gave you enough points");
            System.out.println("\n====================================================================================\n");
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

    public SimpleQueue<Bomb> getBombs() {
        return bombQueue;
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
        exitFromGame();
        System.out.println("Even the server knows I'm dead");
        holdTokenAndQuit(null, null);

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

    public void die(Peer killer, Bomb bomb) {
        this.status = ClientStatus.STATUS_DEAD;
        System.out.println("DYING!");
        this.mover.stopMe();
        exitFromGame();
        holdTokenAndQuit(killer, bomb);

        System.out.println("\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
        System.out.println("\t\t\tYou just got killed by " + killer.getUsername() + (null == bomb ? "" : "'s bomb"));
        System.out.println("\t\t\tPress enter to continue");
        System.out.println("\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");

        clearGameValues();
    }

    private void holdTokenAndQuit(Peer killer, Bomb bomb) {
        if (connections.size() > 0) {
            synchronized (token) {
                if (!token.getStatus())
                    this.token.lock();
            }
            System.out.println("Holding token!");
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
            System.out.println("••••• You have a bomb now! It's a " + PositionZone.getZoneAsString(this.bombQueue.peek().getZone()).toLowerCase() + " bomb •••••");
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
        System.out.println("BOMB EXPLODED! " + bomb);
        if (bomb.getZone().equals(this.position.getZone())) {
            System.out.println("I got killed!");
            this.die(bomb.getThrower(), bomb);
        } else {
            System.out.println(bomb.getThrower().getUsername() + "'s bomb exploded! You made it, keep going!");
        }
    }

    public void bombSuicide(Bomb bomb) {
        this.mover.stopMe();
        exitFromGame();
        System.out.println("\n====================================================================================\n");
        System.out.println("\t\t\tYou just committed suicide!");
        System.out.println("\t\t\tPress enter to continue");
        System.out.println("\n====================================================================================\n");

        if (connections.size() > 0) {
            synchronized (token) {
                if (!token.getStatus())
                    this.token.lock();
            }
            P2PCommunicationService.die(this, this.user, bomb);
            P2PCommunicationService.bombExploded(this, bomb);
            P2PCommunicationService.giveToken(this, true);
        }
        clearGameValues();
    }

    public void throwNext() {
        Bomb bomb = this.bombQueue.pop();
        Move move = new Move(bomb);
        this.setMove(move);
    }
}
