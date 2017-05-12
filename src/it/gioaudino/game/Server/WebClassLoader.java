package it.gioaudino.game.Server;

import it.gioaudino.game.Service.ServerProvider;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/")
public class WebClassLoader extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        HashSet h = new HashSet<Class<?>>();
        h.add(ServerProvider.getLoggedInstance());
        return h;
    }
}