// enums/Command.java
package enums;

public enum Command {
    REGISTER,
    LOGIN,
    EXIT,
    REFRESH,
    JOIN,
    LOGOUT,
    LEAVE_ROOM,
    CHANGE_STATE,
    JOIN_AI,
    MESSAGE,
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