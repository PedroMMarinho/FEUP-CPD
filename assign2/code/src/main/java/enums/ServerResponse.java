// enums/ServerResponse.java
package enums;

public enum ServerResponse {
    REGISTER_SUCCESS("REGISTER_SUCCESS"),
    REGISTER_FAILED("REGISTER_FAILED"),
    LOGIN_SUCCESS("LOGIN_SUCCESS"),
    LOGIN_FAILED("LOGIN_FAILED"),
    JOINED_ROOM("JOINED_ROOM"),
    JOIN_FAILED("JOIN_FAILED"),
    ROOM_CREATED("ROOM_CREATED"),
    CREATE_FAILED("CREATE_FAILED"),
    AI_ROOM_CREATED("AI_ROOM_CREATED"),
    CREATE_AI_FAILED("CREATE_AI_FAILED"),
    SEND_SUCCESS("SEND_SUCCESS"),
    SEND_FAILED("SEND_FAILED"),
    LIST_ROOMS_RESPONSE("LIST_ROOMS_RESPONSE"),
    LOGOUT_SUCCESS("LOGOUT_SUCCESS"),
    LOGOUT_FAILED("LOGOUT_FAILED"),
    INVALID_REQUEST("INVALID_REQUEST"),
    UNKNOWN_COMMAND("UNKNOWN_COMMAND");

    private final String response;

    ServerResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    @Override
    public String toString() {
        return response;
    }
}