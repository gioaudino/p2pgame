package it.gioaudino.game.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by gioaudino on 10/05/17.
 */
public class GsonService {
    private static Gson gsonExclusionInstance = null;
    private static Gson gsonSimpleInstance = null;

    public static Gson getExclusionInstance() {
        if (gsonExclusionInstance == null) {
            gsonExclusionInstance = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
        }
        return gsonExclusionInstance;
    }

    public static Gson getSimpleInstance() {
        if (gsonSimpleInstance == null) {
            gsonSimpleInstance = new Gson();
        }
        return gsonSimpleInstance;
    }

}
