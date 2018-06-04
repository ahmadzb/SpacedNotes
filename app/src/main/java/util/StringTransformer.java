package util;

import java.io.Serializable;

/**
 * Created by Ahmad on 12/04/17.
 * All rights reserved.
 */

public interface StringTransformer extends Serializable{
    String transform(String input);
}
