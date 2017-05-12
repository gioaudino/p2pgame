package it.gioaudino.game.Server;

import it.gioaudino.game.Service.MongoDBLogger;
import org.bson.Document;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gioaudino on 11/05/17.
 */
public class LoggedGameServer extends GameServer {
    @Override
    public Response getGames() {
        Response response = super.getGames();
        callLogger(null, response);
        return response;
    }

    @Override
    public Response getSingleGameInfo(String gameName) {
        Response response = super.getSingleGameInfo(gameName);
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("gameName", gameName);
        callLogger(requestPayload, response);
        return response;
    }

    @Override
    public Response postNewGame(String json) {
        Response response = super.postNewGame(json);
        Map<String, Object> requestPayload = new HashMap<>();
        Document doc = Document.parse(json.toString());
        requestPayload.put("payload", doc);
        callLogger(requestPayload, response);
        return response;
    }

    @Override
    public Response putUserToGame(String gameName, String json) {
        Response response = super.putUserToGame(gameName, json);
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("gameName", gameName);
        Document doc = Document.parse(json.toString());
        requestPayload.put("payload", doc);
        callLogger(requestPayload, response);
        return response;
    }

    @Override
    public Response deleteUserFromGame(String gameName, String username) {
        Response response = super.deleteUserFromGame(gameName, username);
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("gameName", gameName);
        requestPayload.put("username", username);
        callLogger(requestPayload, response);
        return response;
    }

    @Override
    public Response deleteSingleGame(String gameName) {
        Response response = super.deleteSingleGame(gameName);
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("gameName", gameName);
        callLogger(requestPayload, response);
        return response;
    }

    protected void callLogger(Map<String, Object> requestPayload, Response response) {
        MongoDBLogger.log(Thread.currentThread().getStackTrace()[2].getMethodName(), requestPayload, response);
    }
}