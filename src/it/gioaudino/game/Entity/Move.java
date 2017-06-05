package it.gioaudino.game.Entity;

import it.gioaudino.game.Client.ClientObject;
import it.gioaudino.game.Client.UserInteractionHandler;
import it.gioaudino.game.Exception.IllegalMoveException;
import it.gioaudino.game.Service.P2PCommunicationService;

import static it.gioaudino.game.Client.UserInteractionHandler.printPlayingHeader;

/**
 * Created by gioaudino on 01/06/17.
 * Package it.gioaudino.game.Entity in game
 */
public class Move {
    private Position from;
    private Position to;

    public Move(Position from, Position to) {
        this.from = from;
        this.to = to;
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

    private boolean checkNewPosition(int x, int y, int gridSize) {
        return x >= 0 && x < gridSize && y >= 0 && y < gridSize;
    }

    public static void perform(ClientObject client, Move move){
        client.setPosition(move.getTo());
        P2PCommunicationService.move(client);
        client.clearMove();
        UserInteractionHandler.printPlayingHeader(client);

    }

    public static void perform(ClientObject client) {
        perform(client, client.getMove());
    }

    @Override
    public String toString() {
        return "move: " + from + " -> " + to;
    }
}
