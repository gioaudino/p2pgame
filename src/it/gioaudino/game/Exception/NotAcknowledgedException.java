package it.gioaudino.game.Exception;

/**
 * Created by gioaudino on 20/05/17.
 * Package it.gioaudino.game.Exception in game
 */
public class NotAcknowledgedException extends Exception {
    public NotAcknowledgedException() {
    }

    public NotAcknowledgedException(String message) {
        super(message);
    }
}
