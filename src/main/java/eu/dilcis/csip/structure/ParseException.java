package eu.dilcis.csip.structure;

public class ParseException extends Exception {
    public ParseException(final String message) {
        super(message);
    }
    public ParseException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
