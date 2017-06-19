package it.gioaudino.game.Entity;

import it.gioaudino.game.Service.GsonService;

/**
 * Created by gioaudino on 20/05/17.
 * Package it.gioaudino.game.Entity in game
 */

public class Message {

    private User sender;
    private MessageType type;
    private Position position;
    private User killer;
    private Bomb bomb;
    private long timestamp = System.currentTimeMillis();

    
    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public User getKiller() {
        return killer;
    }

    public void setKiller(User killer) {
        this.killer = killer;
    }

    public Bomb getBomb() {
        return bomb;
    }

    public void setBomb(Bomb bomb) {
        this.bomb = bomb;
    }

    @Override
    public String toString() {
        return GsonService.getSimpleInstance().toJson(this);
    }
}
