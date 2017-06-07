package it.gioaudino.game.Entity;

import it.gioaudino.game.Service.GsonService;

/**
 * Created by gioaudino on 20/05/17.
 * Package it.gioaudino.game.Entity in game
 */

public class Message {

    private Peer sender;
    private MessageType type;
    private Position position;
    private Peer killer;
    private PositionZone zone;


    public Peer getSender() {
        return sender;
    }

    public void setSender(Peer sender) {
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

    public Peer getKiller() {
        return killer;
    }

    public void setKiller(Peer killer) {
        this.killer = killer;
    }

    public PositionZone getZone() {
        return zone;
    }

    public void setZone(PositionZone zone) {
        this.zone = zone;
    }

    @Override
    public String toString() {
        return GsonService.getSimpleInstance().toJson(this);
    }
}
