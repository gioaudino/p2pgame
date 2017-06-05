package it.gioaudino.game.Entity;

/**
 * Created by gioaudino on 10/05/17.
 */
public class Peer {
    private String username;
    private String address;
    private int port;
    private String fullAddress;

    public Peer(String username, String address, int port) {
        this.username = username;
        this.address = address.matches("0.0.0.0") ? "localhost" : address;
        this.port = port;
        this.fullAddress = this.address + ":" + port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setAddress(String address) {
        this.address = address.matches("0.0.0.0") ? "localhost" : address;
        updateFullAddress();
    }

    public void setPort(int port) {
        this.port = port;
        updateFullAddress();
    }

    private void updateFullAddress() {
        this.fullAddress = this.address + ":" + this.port;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Peer && username.equals(((Peer) obj).getUsername()) && fullAddress.equals(((Peer) obj).fullAddress);
    }

    @Override
    public String toString(){
        return this.fullAddress;
    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + address.hashCode();
        result = 31 * result + port;
        result = 31 * result + (fullAddress != null ? fullAddress.hashCode() : 0);
        return result;
    }
}
