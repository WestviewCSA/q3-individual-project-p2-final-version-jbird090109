/**
 * Thrown when the map file does not contain enough characters or rows.
 */
public class IncompleteMapException extends Exception {
    public IncompleteMapException(String message) {
        super(message);
    }
}
