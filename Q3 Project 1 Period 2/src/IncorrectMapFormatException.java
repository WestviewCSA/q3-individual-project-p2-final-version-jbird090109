/**
 * Thrown when the map file is incorrectly formatted,
 * such as not having three positive integers on the first line.
 */
public class IncorrectMapFormatException extends Exception {
    public IncorrectMapFormatException(String message) {
        super(message);
    }
}
