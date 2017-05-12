package it.gioaudino.game.Entity;

/**
 * Created by gioaudino on 10/05/17.
 */
public class Peer {
    private String username;
    private String address;
    private String port;
    private String fullAddress;

    public Peer(String address, String port) {
        this.address = address;
        this.port = port;
        this.fullAddress = address + ":" + port;
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

    public String getPort() {
        return port;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }
}
