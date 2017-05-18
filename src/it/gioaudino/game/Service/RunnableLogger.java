package it.gioaudino.game.Service;

import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * Created by gioaudino on 18/05/17.
 * Package it.gioaudino.game.Service in game
 */
public class RunnableLogger implements Runnable {

    Map<String, Object> requestPayload;
    Response response;
    String methodName;

    public RunnableLogger(String methodName, Map<String, Object> requestPayload, Response response) {
        this.methodName = methodName;
        this.requestPayload = requestPayload;
        this.response = response;
    }

    @Override
    public void run() {
        MongoDBLogger.log(methodName, requestPayload, response);
    }
}
