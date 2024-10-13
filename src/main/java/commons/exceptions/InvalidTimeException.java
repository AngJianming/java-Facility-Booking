package commons.exceptions;

/**
 * Thrown when time provided does not conform to a 24h clock format in 30-min intervals
 */
public class InvalidTimeException extends Exception {
    public InvalidTimeException(){
        super("Date is invalid. HH should be between 00 and 23 inclusive and MM should be either 00 or 30");
    }
}
