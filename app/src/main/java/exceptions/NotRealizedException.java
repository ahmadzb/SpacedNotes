package exceptions;

/**
 * Created by Ahmad on 01/04/18.
 * All rights reserved.
 */

public class NotRealizedException extends RuntimeException {
    public NotRealizedException() {
        super("Model object is not realized");
    }
}
