package it.gioaudino.game.Service;

import it.gioaudino.game.Entity.Game;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by gioaudino on 10/05/17.
 */
public class GameManager {
    private static GameManager instance = new GameManager();

    public static GameManager getInstance() {
        return instance;
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
        if (game.getUsers().size() < 1)
            throw new IllegalArgumentException("Cannot create a game without players");
        this.games.put(game.getName(), game);
        return game;

    }

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
