package exceptions;

import java.security.InvalidKeyException;

/**
 * Created by Ahmad on 01/04/18.
 * All rights reserved.
 */

public class InvalidCursorException extends RuntimeException {
    public InvalidCursorException() {
        super("Given cursor does not contain required columns");
    }
}
