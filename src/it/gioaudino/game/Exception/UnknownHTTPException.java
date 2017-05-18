package it.gioaudino.game.Exception;

/**
 * Created by gioaudino on 16/05/17.
 */
public class UnknownHTTPException extends HTTPException {
    public UnknownHTTPException() {
    }

    public UnknownHTTPException(String message) {
        super(message);
    }
}
