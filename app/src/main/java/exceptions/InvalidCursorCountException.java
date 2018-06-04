package exceptions;

/**
 * Created by Ahmad on 01/04/18.
 * All rights reserved.
 */

public class InvalidCursorCountException extends RuntimeException {
    public InvalidCursorCountException(int expected, int actual) {
        super("Cursor retrieved " + actual + " items, " + expected + " was expected");
    }
}
