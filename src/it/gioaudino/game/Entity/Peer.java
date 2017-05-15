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
    }

    public void setPort(int port) {
        this.port = port;
    }
}
