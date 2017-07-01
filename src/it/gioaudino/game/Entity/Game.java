package it.gioaudino.game.Entity;

import com.google.gson.annotations.Expose;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by gioaudino on 10/05/17.
 */
public class Game {
    @Expose(serialize = false)
    private Map<String, User> users;
    @Expose
    private final String name;
    @Expose
    private final int points;
    @Expose
    private final int size;
    @Expose
    private Timestamp createdAt;
    private User creator;

    public Game(String name, int points, int size) {
        this.name = name;
        this.points = points;
        this.size = size;
        this.users = new LinkedHashMap<>();
    }

    public Game(String name, int size, int pointsToWin, User creator) {
        if (size % 2 != 0) throw new IllegalArgumentException("Battlefield size has to be an even number");
        this.size = size;
        this.name = name;
        this.points = pointsToWin;
        this.users = new LinkedHashMap<>();
        this.users.put(creator.getUsername(), creator);
        this.creator = creator;
    }

    public synchronized Game addUser(User user) {
        if (this.users.containsKey(user.getUsername()))
            throw new IllegalArgumentException("A player with name \'" + user.getUsername() + "\' is already playing in the game '" + this.name + "'");
        if (this.users.size() >= this.size * this.size)
            throw new IllegalArgumentException("There is no more room in the field. Try another match");
        this.users.put(user.getUsername(), user);
        return this;
    }

    public User removeUser(String name) {
        return this.users.remove(name);
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public void setUsers(Map<String, User> users) {
        this.users = users;
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

    public User getCreator() {
        return creator;
    }
}
