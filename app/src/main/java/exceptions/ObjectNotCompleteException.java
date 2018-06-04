package exceptions;

/**
 * Created by Ahmad on 01/04/18.
 * All rights reserved.
 */

public class ObjectNotCompleteException extends RuntimeException {
    public ObjectNotCompleteException() {
        super("Object is not complete, some fields need to have value assigned");
    }
}
