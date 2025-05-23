// enums/ServerResponse.java
package enums;

public enum ServerResponse {
    OK,
    ERROR,
    LOGOUT_USER,
    EXIT_USER,
    LISTING_ROOMS,
    CREATED_ROOM,
    LEAVING_ROOM,
    CHAT_COMMAND,
    CHAT_MESSAGE,
    JOIN_ROOM,
    NEW_TOKEN,
    VALID_TOKEN,
    INVALID_TOKEN;

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