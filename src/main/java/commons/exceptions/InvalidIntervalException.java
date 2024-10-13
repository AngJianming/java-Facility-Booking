package commons.exceptions;

/**
 * Thrown when start time and end time does not form a valid interval
 */
public class InvalidIntervalException extends Exception {
    public InvalidIntervalException(){
        super("End datetime is not after start datetime");
    }
}
