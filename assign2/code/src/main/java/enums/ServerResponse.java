// enums/ServerResponse.java
package enums;

public enum ServerResponse {
    REGISTER_SUCCESS,
    REGISTER_FAILED,
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    JOINED_ROOM,
    JOIN_FAILED,
    ROOM_CREATED,
    AI_ROOM_CREATED,
    LIST_ROOMS_RESPONSE,
    LOGOUT_SUCCESS,
    INVALID_REQUEST,
    LOGIN_FAILED_ALREADY_LOGGED_IN,
    UNKNOWN_COMMAND;

    public static ServerResponse fromString(String response) {
        if (response != null) {
            try {
                return valueOf(response.toUpperCase());
            } catch (IllegalArgumentException e) {
                return UNKNOWN_COMMAND;
            }
        }
        return UNKNOWN_COMMAND;
    }
}