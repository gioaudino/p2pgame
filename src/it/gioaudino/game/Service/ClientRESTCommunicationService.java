package it.gioaudino.game.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import it.gioaudino.game.Entity.Game;
import it.gioaudino.game.Entity.Peer;
import it.gioaudino.game.Exception.BadRequestException;
import it.gioaudino.game.Server.Routes;

import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gioaudino on 15/05/17.
 */
public class ClientRESTCommunicationService {

    private Peer peer;

    public ClientRESTCommunicationService(Peer peer) {
        this.peer = peer;
    }

    public void getGames() throws Exception {
        String url = Routes.SERVER_ADDRESS + Routes.GAMES_GET;
        HttpResponse<JsonNode> jsonResponse = Unirest.get(url).asJson();
        System.out.println(jsonResponse.getStatus());
        Type listType = new TypeToken<ArrayList<Game>>() {
        }.getType();

        List<Game> yourClassList = new Gson().fromJson(jsonResponse.getBody().toString(), listType);
        for (Game g : yourClassList)
            System.out.println(g.getName());
    }

    public ArrayList<Game> getExistingGames() throws BadRequestException {
        String url = Routes.SERVER_ADDRESS + Routes.GAMES_GET;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            int responseCode = connection.getResponseCode();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            if (responseCode != Response.Status.OK.getStatusCode()) {
                throw new BadRequestException(response.toString());
            }
            System.out.println("RESPONSE  " + responseCode);

            Type listType = new TypeToken<ArrayList<Game>>() {
            }.getType();

            List<Game> yourClassList = new Gson().fromJson(response.toString(), listType);
            for (Game g : yourClassList)
                System.out.println(GsonService.getSimpleInstance().toJson(g));

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
