package it.gioaudino.game.Service;

import com.mongodb.MongoClient;
import com.mongodb.player.MongoCollection;
import com.mongodb.player.MongoDatabase;
import com.mongodb.util.JSON;
import org.bson.Document;

import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.Map;

/**
 * Created by gioaudino on 11/05/17.
 */
public class MongoDBLogger {
    private static final String DATABASE = "sdp";
    private static final String COLLECTION = "gameLog";
    private static final String TEMP_COLLECTION = "temp";
    private static final MongoClient mongoClient = new MongoClient();
    private static final MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE);
    private static final MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(COLLECTION);

    private static final MongoCollection<Document> tempMongoCollection = mongoDatabase.getCollection(TEMP_COLLECTION);

    public static void log(String controller, Map<String, Object> requestPayload, Response response) {
        Document document = new Document();
        Document responseValues = new Document();
        Document requestValues = new Document();

        requestValues.put("method", controller.split("(?=\\p{Upper})")[0].toUpperCase());
        requestValues.put("controller", controller);
        requestValues.put("body", requestPayload);


        responseValues.put("status", String.valueOf(response.getStatus()));
        Document doc = null;
        try {
            doc = Document.parse(response.getEntity().toString());
        } catch (NullPointerException e) {
        } catch (Exception e) {
            doc = new Document();
            doc.append("values", JSON.parse(response.getEntity().toString()));
        }
        responseValues.put("payload", doc);


        document
                .append("request", requestValues)
                .append("response", responseValues)
                .append("timestamp", new Date(System.currentTimeMillis()));
        mongoCollection.insertOne(document);

    }

    public static void logTemp(String toLog) {
        Document document = new Document();
        document.put("str", toLog);
        tempMongoCollection.insertOne(document);
    }

    public static void logTemp(Object obj) {
        logTemp(GsonService.getSimpleInstance().toJson(obj));
    }

}
