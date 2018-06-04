package exceptions;

/**
 * Created by Ahmad on 01/02/18.
 * All rights reserved.
 */

public class InvalidItemException extends RuntimeException {
    public InvalidItemException() {
        super("Item in data.model is invalid (Validity check failure)");
    }
}
