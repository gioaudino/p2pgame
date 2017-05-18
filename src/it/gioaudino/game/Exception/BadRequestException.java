package it.gioaudino.game.Exception;

/**
 * Created by gioaudino on 15/05/17.
 */
public class BadRequestException extends HTTPException {
    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException() {
    }
}
