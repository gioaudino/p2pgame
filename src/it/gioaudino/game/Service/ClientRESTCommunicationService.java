package it.gioaudino.game.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import it.gioaudino.game.Client.Player;
import it.gioaudino.game.Entity.Game;
import it.gioaudino.game.Entity.User;
import it.gioaudino.game.Exception.BadRequestException;
import it.gioaudino.game.Exception.HTTPException;
import it.gioaudino.game.Exception.InternalServerErrorException;
import it.gioaudino.game.Exception.UnknownHTTPException;
import it.gioaudino.game.Server.Routes;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gioaudino on 15/05/17.
 */
public class ClientRESTCommunicationService {

    private static final String DEFAULT_ENCODING = "UTF-8";
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
        return GsonService.getSimpleInstance().fromJson(jsonResponse.getBody(), gameListType);

    }

    public static Game createNewGame(Game game) throws UnirestException, HTTPException {
        String url = INSTANCE_SERVER_ADDRESS + Routes.GAME_POST;
        Gson gson = GsonService.getSimpleInstance();
        HttpResponse<String> jsonResponse = Unirest.post(url)
                .body(gson.toJson(game)).asString();

        throwExceptionIfNotOk(jsonResponse);

        return gson.fromJson(jsonResponse.getBody(), Game.class);

    }


    public static Game joinExistingGame(String gameName, User user) throws UnirestException, HTTPException {
        String url = INSTANCE_SERVER_ADDRESS + Routes.GAME_PUT + urlEncode(gameName);

        HttpResponse<String> jsonResponse = Unirest.put(url).body(GsonService.getSimpleInstance().toJson(user)).asString();

        throwExceptionIfNotOk(jsonResponse);

        return GsonService.getSimpleInstance().fromJson(jsonResponse.getBody(), Game.class);

    }

    public static boolean tryGameName(String gameName) throws UnirestException {
        String url = INSTANCE_SERVER_ADDRESS + Routes.GAME_HEAD + urlEncode(gameName);
        HttpResponse<String> jsonResponse = Unirest.head(url).asString();

        return Response.Status.OK.getStatusCode() == jsonResponse.getStatus();
    }

    public static void quitGame(Player player) throws UnirestException, HTTPException {
        String url = INSTANCE_SERVER_ADDRESS + Routes.GAME_USER_DELETE + urlEncode(player.getGame().getName()) + "/" + urlEncode(player.getUser().getUsername());
        HttpResponse<String> jsonResponse = Unirest.delete(url).asString();
        throwExceptionIfNotOk(jsonResponse);
    }

    private static void throwExceptionIfNotOk(HttpResponse<String> response) throws HTTPException {

        if (response.getStatus() == 400)
            throw new BadRequestException(response.getBody());

        if (response.getStatus() == 500)
            throw new InternalServerErrorException(response.getBody());

        if (response.getStatus() != 200)
            throw new UnknownHTTPException(response.getBody());

    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }


}
