package it.gioaudino.game.Entity;

import com.google.gson.annotations.Expose;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
    private Peer creator;

    public Game(String name, int points, int size) {
        this.name = name;
        this.points = points;
        this.size = size;
        this.peers = new HashMap<>();
    }

    public Game(String name, int size, int pointsToWin, Peer creator) {
        if (size % 2 != 0) throw new IllegalArgumentException("Battlefield size has to be an even number");
        this.size = size;
        this.name = name;
        this.points = pointsToWin;
        this.peers = new LinkedHashMap<>();
        this.peers.put(creator.getUsername(), creator);
        this.creator = creator;
    }

    public synchronized Game addPeer(Peer peer) {
        if (this.peers.containsKey(peer.getUsername()))
            throw new IllegalArgumentException("A player with name '" + peer.getUsername() + "' is already playing in the game '" + this.name + "'");
        if (this.peers.size() >= this.size * this.size)
            throw new IllegalArgumentException("There is no more room in the field. Try another match");
        this.peers.put(peer.getUsername(), peer);
        return this;
    }

    public Peer removePeer(String name) {
        return this.peers.remove(name);
    }

    public Map<String, Peer> getPeers() {
        return peers;
    }

//    public Map<String, Peer> getPeers(String exclude) {
//        return peers.entrySet().stream().filter(p -> !p.getKey().equals(exclude)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//    }

    public void setPeers(Map<String, Peer> peers) {
        this.peers = peers;
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

    public Peer getCreator() {
        return creator;
    }
}
