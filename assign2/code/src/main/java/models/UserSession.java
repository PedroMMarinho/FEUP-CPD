package models;
import java.time.Instant;

public class UserSession {
    private final String token;
    private final Instant expiresAt;
    private final String username;
    private Room currentRoom;
    private boolean active;

    public UserSession(String token, String username) {
        this.token = token;
        this.expiresAt = Instant.now().plusSeconds(3600);
        this.username = username;
        this.active = true;
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public Room getRoom(){
        return currentRoom;
    }

    public boolean isActive(){
        return active;
    }

    public void setRoom(Room room){
        this.currentRoom = room;
    }

    public void removeRoom(){
        this.currentRoom = null;
    }

    public boolean isExpired(){
        return Instant.now().isAfter(this.expiresAt);
    }

    public void closeSession(){
        this.active = false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserSession session = (UserSession) obj;
        return token.equals(session.token);
    }

    @Override
    public int hashCode() {
        return token.hashCode();
    }

    @Override
    public String toString() {
        return "UserSession{token='" + token + "' expires=" + expiresAt.toString() + "user=" + username + "}";
    }
}