package de.ugdata.mousesharett.model;

public class HeartbeatMessage {
    private String id;
    private String hostname;

    public HeartbeatMessage() {}

    public HeartbeatMessage(String id, String hostname) {
        this.id = id;
        this.hostname = hostname;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }
}
