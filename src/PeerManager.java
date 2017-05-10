import Entity.Peer;

/**
 * Created by gioaudino on 10/05/17.
 */
public class PeerManager {
    public static Peer deserialize(String json){
        Peer peer = GsonService.getSimpleInstance().fromJson(json, Peer.class);
        return peer;
    }

}