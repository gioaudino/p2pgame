
import Entity.Game;
import Entity.Peer;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by gioaudino on 10/05/17.
 */

@Path("/games")
public class GameServer {

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
        Game game = gameManager.getGame(gameName);
        if (null == game) {
            return buildResponse("Entity.Game " + gameName + " does not exist (anymore).", Response.Status.BAD_REQUEST);
        }
        return Response.ok(GsonService.getSimpleInstance().toJson(game)).build();
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postNewGame(String json) {

        Game game = null;

        try {
            game = this.gameManager.createGame(json);
        } catch (IllegalArgumentException e) {
            return buildResponse(e.getMessage(), Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            return buildResponse("", Response.Status.INTERNAL_SERVER_ERROR);
        } finally {
            if (null == game) {
                return buildResponse("", Response.Status.INTERNAL_SERVER_ERROR);
            }
        }

        return Response.ok(GsonService.getSimpleInstance().toJson(game)).build();
    }

    @PUT
    @Path("/{gameName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response putUserToGame(@PathParam("gameName") String gameName, String json) {
        Game game = this.gameManager.getGame(gameName);
        if (null == game) {
            return buildResponse("Entity.Game " + gameName + " does not exist (anymore).", Response.Status.BAD_REQUEST);
        }
        Peer peer = PeerManager.deserialize(json);
        if (game.getPeers().containsKey(peer.getUsername())) {
            return buildResponse("Player " + peer.getUsername() + " already exists in game " + gameName, Response.Status.BAD_REQUEST);
        }
        game.addPeer(peer);
        return Response.ok(game).build();
    }

    @DELETE
    @Path("/{gameName}/{username}")
    public Response deleteUserFromGame(@PathParam("gameName") String gameName, @PathParam("username") String username) {
        if (!gameManager.hasGame(gameName))
            return buildResponse("Entity.Game " + gameName + " does not exist (anymore).", Response.Status.BAD_REQUEST);
        Game game = gameManager.getGame(gameName);
        if (!game.getPeers().containsKey(username))
            return buildResponse("Player " + username + " does not exist in game " + gameName, Response.Status.BAD_REQUEST);
        Peer peer = game.removePeer(username);
        if(null == peer)
            return buildResponse("Something happened while deleting player", Response.Status.INTERNAL_SERVER_ERROR);

        return Response.ok().build();
    }

    @DELETE
    @Path("/game/{gameName}")
    public Response deleteSingleGame(@PathParam("gameName") String gameName) {
        if (!gameManager.hasGame(gameName))
            return buildResponse("Entity.Game " + gameName + " does not exist (anymore).", Response.Status.BAD_REQUEST);
        gameManager.removeGame(gameName);
        return Response.ok("Games deleted").build();
    }

    private Response buildResponse(String message, Response.Status statusCode) {
        return Response.status(statusCode).entity(GsonService.getSimpleInstance().toJson(message)).build();
    }
}
