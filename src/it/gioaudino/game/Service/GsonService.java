package it.gioaudino.game.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by gioaudino on 10/05/17.
 */
public class GsonService {
    private static Gson gsonExclusionInstance = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
    private static Gson gsonSimpleInstance = new Gson();

    public static Gson getExclusionInstance() {
        return gsonExclusionInstance;
    }

    public static Gson getSimpleInstance() {
        return gsonSimpleInstance;
    }

}
