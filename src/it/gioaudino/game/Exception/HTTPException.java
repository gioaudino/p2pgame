package it.gioaudino.game.Exception;

/**
 * Created by gioaudino on 16/05/17.
 */
public abstract class HTTPException extends Exception {
    public HTTPException() {
    }

    public HTTPException(String message) {
        super(message);
    }
}
