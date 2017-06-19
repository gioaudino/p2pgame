package it.gioaudino.game.Entity;

import it.gioaudino.game.Client.Player;
import it.gioaudino.game.Exception.IllegalMoveException;
import it.gioaudino.game.Service.P2PCommunicationService;
import it.gioaudino.game.Simulator.BombThrower;

/**
 * Created by gioaudino on 01/06/17.
 * Package it.gioaudino.game.Entity in game
 */
public class Move {
    private Position from;
    private Position to;
    private Bomb bomb;

    public Move(Position from, Position to) {
        this.from = from;
        this.to = to;
    }

    public Move(Bomb bomb) {
        this.bomb = bomb;
    }

    public Move(Position from, Direction direction) throws IllegalMoveException {
        Position to = null;
        switch (direction) {
            case LEFT:
                if (!this.checkNewPosition(from.getX() - 1, from.getY(), from.getGridSize()))
                    throw new IllegalMoveException();
                to = new Position(from.getX() - 1, from.getY(), from.getGridSize());
                break;
            case UP:
                if (!this.checkNewPosition(from.getX(), from.getY() - 1, from.getGridSize()))
                    throw new IllegalMoveException();
                to = new Position(from.getX(), from.getY() - 1, from.getGridSize());
                break;
            case RIGHT:
                if (!this.checkNewPosition(from.getX() + 1, from.getY(), from.getGridSize()))
                    throw new IllegalMoveException();
                to = new Position(from.getX() + 1, from.getY(), from.getGridSize());
                break;
            case DOWN:
                if (!this.checkNewPosition(from.getX(), from.getY() + 1, from.getGridSize()))
                    throw new IllegalMoveException();
                to = new Position(from.getX(), from.getY() + 1, from.getGridSize());
                break;
        }
        this.from = from;
        this.to = to;
    }

    public Position getFrom() {
        return from;
    }

    public void setFrom(Position from) {
        this.from = from;
    }

    public Position getTo() {
        return to;
    }

    public void setTo(Position to) {
        this.to = to;
    }

    public Bomb getBomb() {
        return bomb;
    }

    public void setBomb(Bomb bomb) {
        this.bomb = bomb;
    }

    private boolean checkNewPosition(int x, int y, int gridSize) {
        return x >= 0 && x < gridSize && y >= 0 && y < gridSize;
    }

    private static void perform(Player player, Move move) {
        if (player.getStatus() == ClientStatus.STATUS_PLAYING) {
            if (move.bomb != null) {
                P2PCommunicationService.bombThrown(player, move.bomb);
                new Thread(new BombThrower(player, move.bomb)).start();
            } else {
                player.setPosition(move.getTo());
                player.getOutputPrinter().printPlayingHeader(player);
                P2PCommunicationService.move(player);
            }
        }
        player.clearMove();
    }

    public static void perform(Player player) {
        perform(player, player.getMove());
    }

    @Override
    public String toString() {
        return "move: " + from + " -> " + to;
    }
}
