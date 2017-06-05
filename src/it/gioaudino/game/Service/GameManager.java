package it.gioaudino.game.Service;

import it.gioaudino.game.Entity.Game;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by gioaudino on 10/05/17.
 */
public class GameManager {
    private static GameManager ourInstance = new GameManager();

    public static GameManager getInstance() {
        return ourInstance;
    }

    private Map<String, Game> games;

    private GameManager() {
        this.reset();
    }

    public synchronized Game createGame(String json) {
        Game game = GsonService.getSimpleInstance().fromJson(json, Game.class);
        game.setCreatedTimestamp();
        if (game.getSize() <= 0 || game.getSize() % 2 != 0) throw new IllegalArgumentException("Size has to be a positive even number");
        if (this.games.containsKey(game.getName()))
            throw new IllegalArgumentException("A game with name '" + game.getName() + "' already exists");
        if (game.getPeers().size() < 1)
            throw new IllegalArgumentException("Cannot create a game without players");
        this.games.put(game.getName(), game);
        return game;

    }

//    public Game getGameWithoutPlayer(String gameName) {
//        Game game = this.games.get(gameName);
//        if (null == game) return null;
//        return new Game(gameName, game.getPoints(), game.getSize());
//    }
//
//    public Game getGameWithoutPlayer(String gameName, String player) {
//        Game game = this.games.get(gameName);
//        if (null == game) return null;
//        Game tbr = new Game(gameName, game.getPoints(), game.getSize());
//        Map<String, Peer> peers = game.getPeers(player);
//        tbr.setPeers(peers);
//        return tbr;
//    }

    public void removeGame(String name) {
        this.games.remove(name);
    }

    public boolean hasGame(String name) {
        return this.games.containsKey(name);
    }

    public Game getGame(String name) {
        return this.games.get(name);
    }

    public Map<String, Game> getGames() {
        return games;
    }

    public void reset() {
        this.games = new TreeMap<>();
    }

}
