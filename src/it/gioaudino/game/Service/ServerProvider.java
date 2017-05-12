package it.gioaudino.game.Service;

import it.gioaudino.game.Server.GameServer;
import it.gioaudino.game.Server.LoggedGameServer;

/**
 * Created by gioaudino on 11/05/17.
 */
public class ServerProvider {
    public static Class getSimpleInstance(){
        return GameServer.class;
    }

    public static Class getLoggedInstance(){
        return LoggedGameServer.class;
    }

}
