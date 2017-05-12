package it.gioaudino.game.Entity;

import com.google.gson.annotations.Expose;

import java.sql.Timestamp;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by gioaudino on 10/05/17.
 */
public class Game {
    @Expose(serialize = false)
    private Map<String, Peer> peers;
    @Expose
    private final String name;
    @Expose
    private final int points;
    @Expose
    private final int size;
    @Expose
    private Timestamp createdAt;


    public Game(String name, int size, int pointsToWin, Peer creator) {
        if (size % 2 != 0) throw new IllegalArgumentException("Battlefield size has to be an even number");
        this.size = size;
        this.name = name;
        this.points = pointsToWin;
        this.peers = new TreeMap<>();
        this.peers.put(creator.getUsername(), creator);
    }

    public synchronized Game addPeer(Peer peer){
        this.peers.put(peer.getUsername(), peer);
        return this;
    }

    public Peer removePeer(String name){
        return this.peers.remove(name);
    }

    public Map<String, Peer> getPeers() {
        return peers;
    }

    public String getName() {
        return name;
    }

    public int getPoints() {
        return points;
    }

    public int getSize() {
        return size;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedTimestamp() {
         this.createdAt = new Timestamp(System.currentTimeMillis());
    }
}
