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
    CREATE_FAILED,
    AI_ROOM_CREATED,
    CREATE_AI_FAILED,
    SEND_SUCCESS,
    SEND_FAILED,
    LIST_ROOMS_RESPONSE,
    LOGOUT_SUCCESS,
    LOGOUT_FAILED,
    INVALID_REQUEST,
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