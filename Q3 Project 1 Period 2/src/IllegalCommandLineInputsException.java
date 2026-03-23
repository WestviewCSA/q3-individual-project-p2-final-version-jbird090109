/**
 * Thrown when required or valid command line arguments are missing or conflicting.
 */
public class IllegalCommandLineInputsException extends Exception {
    public IllegalCommandLineInputsException(String message) {
        super(message);
    }
}
