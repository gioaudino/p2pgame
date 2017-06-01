package it.gioaudino.game.Entity;

import com.google.gson.Gson;
import it.gioaudino.game.Service.GsonService;

/**
 * Created by gioaudino on 20/05/17.
 * Package it.gioaudino.game.Entity in game
 */

public class Message {

    private Peer sender;
    private MessageType type;
    private Position position;
    private String jsonParameters;

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

    public String getJsonParameters() {
        return jsonParameters;
    }

    public void setJsonParameters(String jsonParameters) {
        this.jsonParameters = jsonParameters;
    }

    @Override
    public String toString() {
        return GsonService.getSimpleInstance().toJson(this);
    }
}
