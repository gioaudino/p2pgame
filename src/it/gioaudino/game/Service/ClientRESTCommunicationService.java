package it.gioaudino.game.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import it.gioaudino.game.Entity.Game;
import it.gioaudino.game.Entity.Peer;
import it.gioaudino.game.Exception.BadRequestException;
import it.gioaudino.game.Exception.HTTPException;
import it.gioaudino.game.Exception.InternalServerErrorException;
import it.gioaudino.game.Exception.UnknownHTTPException;
import it.gioaudino.game.Server.Routes;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gioaudino on 15/05/17.
 */
public class ClientRESTCommunicationService {

    private static final String INSTANCE_SERVER_ADDRESS;

    static {
        INSTANCE_SERVER_ADDRESS = Routes.SERVER_ADDRESS;
        Unirest.setDefaultHeader("accept", MediaType.APPLICATION_JSON);
        Unirest.setDefaultHeader("Content-Type", MediaType.APPLICATION_JSON);
    }

    public static List<Game> getExistingGames() throws UnirestException, HTTPException {

        String url = INSTANCE_SERVER_ADDRESS + Routes.GAMES_GET;

        HttpResponse<String> jsonResponse = Unirest.get(url).asString();

        throwExceptionIfNotOk(jsonResponse);

        Type gameListType = new TypeToken<ArrayList<Game>>() {
        }.getType();
        List<Game> gamesList = GsonService.getSimpleInstance().fromJson(jsonResponse.getBody().toString(), gameListType);

        return gamesList;
    }

    public static Game createNewGame(Game game) throws UnirestException, HTTPException {
        String url = INSTANCE_SERVER_ADDRESS + Routes.GAME_POST;
        Gson gson = GsonService.getSimpleInstance();
        HttpResponse<String> jsonResponse = Unirest.post(url)
                .body(gson.toJson(game)).asString();

        throwExceptionIfNotOk(jsonResponse);

        Game responseGame = gson.fromJson(jsonResponse.getBody().toString(), Game.class);

        return responseGame;
    }

    public static Game fetchGame(String gameName) throws UnirestException, HTTPException {
        String url = INSTANCE_SERVER_ADDRESS + Routes.GAME_GET + gameName;

        HttpResponse<String> jsonResponse = Unirest.get(url).asString();

        throwExceptionIfNotOk(jsonResponse);

        Game game = GsonService.getSimpleInstance().fromJson(jsonResponse.getBody(), Game.class);

        return game;
    }

    public static Game joinExistingGame(String gameName, Peer peer) throws UnirestException, HTTPException {
        String url = INSTANCE_SERVER_ADDRESS + Routes.GAME_PUT + gameName;

        HttpResponse<String> jsonResponse = Unirest.put(url).body(GsonService.getSimpleInstance().toJson(peer)).asString();

        throwExceptionIfNotOk(jsonResponse);

        Game game = GsonService.getSimpleInstance().fromJson(jsonResponse.getBody(), Game.class);

        return game;
    }

    public static boolean tryGameName(String gameName) throws UnirestException {
        String url = INSTANCE_SERVER_ADDRESS + Routes.GAME_HEAD + gameName;
        HttpResponse<String> jsonResponse = Unirest.head(url).asString();

        return Response.Status.OK.getStatusCode() == jsonResponse.getStatus();
    }


    private static void throwExceptionIfNotOk(HttpResponse<String> response) throws HTTPException {

        if (response.getStatus() == 400)
            throw new BadRequestException(response.getBody().toString());

        if (response.getStatus() == 500)
            throw new InternalServerErrorException(response.getBody().toString());

        if (response.getStatus() != 200)
            throw new UnknownHTTPException(response.getBody().toString());

    }

}
