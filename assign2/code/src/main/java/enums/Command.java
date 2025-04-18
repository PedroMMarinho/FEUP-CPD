// enums/Command.java
package enums;

public enum Command {
    REGISTER,
    LOGIN,
    REFRESH,
    JOIN,
    NEXT_PAGE,
    PREVIOUS_PAGE,
    LOGOUT,
    LEAVE_ROOM,
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