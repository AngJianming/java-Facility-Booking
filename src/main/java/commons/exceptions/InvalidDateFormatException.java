package commons.exceptions;

/**
 * Thrown when date format provided is invalid
 */
public class InvalidDateFormatException extends Exception {
    public InvalidDateFormatException() {
        super("Date format provided is invalid. Please provide date in the following format: [D/HH/MM] where D = day of the week");
    }
}
