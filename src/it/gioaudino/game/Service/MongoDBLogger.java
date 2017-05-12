package it.gioaudino.game.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import org.bson.Document;

import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gioaudino on 11/05/17.
 */
public class MongoDBLogger {
    private static final String DATABASE = "sdp";
    private static final String COLLECTION = "gameLog";
    private static final MongoClient mongoClient = new MongoClient();
    private static final MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE);
    private static final MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(COLLECTION);

    public static void log(String controller, Map<String, Object> requestPayload, Response response) {
        Document document = new Document();
        Document responseValues = new Document();
        Document requestValues = new Document();

        requestValues.put("method", controller.split("(?=\\p{Upper})")[0]);
        requestValues.put("controller", controller);
        requestValues.put("body", requestPayload);


        responseValues.put("status", String.valueOf(response.getStatus()));
        Document doc = Document.parse(response.getEntity().toString());
        responseValues.put("payload", doc);


        document
                .append("request", requestValues)
                .append("response", responseValues)
                .append("timestamp", new Date());
        mongoCollection.insertOne(document);

    }

}
