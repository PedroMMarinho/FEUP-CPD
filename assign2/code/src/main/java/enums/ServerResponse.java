// enums/ServerResponse.java
package enums;

public enum ServerResponse {
    OK,
    ERROR,
    LOGOUT_USER,
    LISTING_ROOMS,
    CREATED_ROOM,
    LEAVING_ROOM,
    JOIN_ROOM;

    public static ServerResponse fromString(String response) {
        if (response != null) {
            try {
                return valueOf(response.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ERROR;
            }
        }
        return ERROR;
    }
}