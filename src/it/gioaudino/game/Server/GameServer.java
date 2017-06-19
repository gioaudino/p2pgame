package it.gioaudino.game.Server;

import it.gioaudino.game.Entity.Game;
import it.gioaudino.game.Entity.User;
import it.gioaudino.game.Service.GameManager;
import it.gioaudino.game.Service.GsonService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by gioaudino on 10/05/17.
 */

@Path("/games")
public class GameServer {
    private static final String DEFAULT_ENCODING = "UTF-8";
    private GameManager gameManager = GameManager.getInstance();

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGames() {
        return Response.ok(GsonService.getExclusionInstance().toJson(gameManager.getGames().values())).build();
    }


    @GET
    @Path("/{gameName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSingleGameInfo(@PathParam("gameName") String gameName) {
        gameName = urlDecode(gameName);
        Game game = gameManager.getGame(gameName);
        if (null == game) {
            return buildResponse("Game " + gameName + " does not exist (anymore).", Response.Status.BAD_REQUEST);
        }
        return Response.ok(GsonService.getSimpleInstance().toJson(game)).build();
    }

    @HEAD
    @Path("/{gameName}")
    public Response headSingleGame(@PathParam("gameName") String gameName) {
        gameName = urlDecode(gameName);
        return buildResponse(null, gameManager.hasGame(gameName) ? Response.Status.OK : Response.Status.NOT_FOUND);
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postNewGame(String json) {
        Game game;
        try {
            game = this.gameManager.createGame(json);
        } catch (IllegalArgumentException e) {
            return buildResponse(e.getMessage(), Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            return buildResponse(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
        if (null == game) {
            return buildResponse("Something somewhere went terribly wrong", Response.Status.INTERNAL_SERVER_ERROR);
        }
        return Response.ok(GsonService.getSimpleInstance().toJson(game)).build();
    }


    @PUT
    @Path("/{gameName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response putUserToGame(@PathParam("gameName") String gameName, String json) {
        gameName = urlDecode(gameName);
        Game game = this.gameManager.getGame(gameName);
        if (null == game) {
            return buildResponse("Game " + gameName + " does not exist (anymore).", Response.Status.NOT_FOUND);
        }
        User user = GsonService.getSimpleInstance().fromJson(json, User.class);
        try {
            game.addUser(user);
        } catch (IllegalArgumentException e) {
            return buildResponse(e.getMessage(), Response.Status.BAD_REQUEST);
        }
        return Response.ok(GsonService.getSimpleInstance().toJson(game)).build();
    }

    @DELETE
    @Path("/{gameName}/{username}")
    public Response deleteUserFromGame(@PathParam("gameName") String gameName, @PathParam("username") String username) {
        gameName = urlDecode(gameName);
        username = urlDecode(username);

        if (!gameManager.hasGame(gameName))
            return buildResponse("Game " + gameName + " does not exist (anymore?).", Response.Status.NOT_FOUND);
        Game game = gameManager.getGame(gameName);
        if (!game.getUsers().containsKey(username))
            return buildResponse("Player " + username + " does not exist in game " + gameName, Response.Status.NOT_FOUND);
        User user = game.removeUser(username);
        if (null == user)
            return buildResponse("Something happened while deleting player", Response.Status.INTERNAL_SERVER_ERROR);
        if(game.getUsers().size() == 0)
            gameManager.removeGame(game.getName());
        return Response.ok().build();
    }


    @DELETE
    @Path("/game/{gameName}")
    public Response deleteSingleGame(@PathParam("gameName") String gameName) {
        if (!gameManager.hasGame(gameName))
            return buildResponse("Game " + gameName + " does not exist (anymore?).", Response.Status.BAD_REQUEST);
        gameManager.removeGame(gameName);
        return Response.ok("Games deleted").build();
    }

    @DELETE
    @Path("/deleteAll")
    public Response deleteAll(){
        gameManager.reset();
        return Response.ok().build();
    }

    private static String urlDecode(String gameName) {
        try {
            gameName = URLDecoder.decode(gameName, DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException ignored) {}
        return gameName;
    }

    private Response buildResponse(String message, Response.Status statusCode) {
        if (null != message)
            return Response.status(statusCode).entity(GsonService.getSimpleInstance().toJson(message)).build();
        return Response.status(statusCode).build();
    }
}
