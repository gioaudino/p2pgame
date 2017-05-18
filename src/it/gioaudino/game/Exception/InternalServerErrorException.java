package it.gioaudino.game.Exception;

/**
 * Created by gioaudino on 16/05/17.
 */
public class InternalServerErrorException extends HTTPException {
    public InternalServerErrorException() {
    }

    public InternalServerErrorException(String message) {
        super(message);
    }
}
