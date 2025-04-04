// enums/Command.java
package enums;

public enum Command {
    REGISTER,
    LOGIN,
    LIST_ROOMS,
    JOIN_ROOM,
    CREATE_ROOM,
    CREATE_AI_ROOM,
    SEND,
    LOGOUT,
    UNKNOWN;

    public static Command fromString(String text) {
        if (text != null) {
            try {
                return valueOf(text.toUpperCase());
            } catch (IllegalArgumentException e) {
                return UNKNOWN;
            }
        }
        return UNKNOWN;
    }
}