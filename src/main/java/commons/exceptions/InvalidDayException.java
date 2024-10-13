package commons.exceptions;

/**
 * Thrown when day provided is not in the Day enum
 */
public class InvalidDayException extends Exception {
    public InvalidDayException() {
        super("Day of week should be one of the following: Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday");
    }
}
