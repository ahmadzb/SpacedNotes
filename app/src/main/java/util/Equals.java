package util;

import java.util.ArrayList;

/**
 * Created by Ahmad on 11/20/17.
 * All rights reserved.
 */

public class Equals {
    public static <T> boolean ArrayList(ArrayList<T> a, ArrayList<T> b) {
        if (a == b)
            return true;
        else if (a == null && b == null)
            return true;
        else if (a == null || b == null)
            return false;
        else if (a.size() != b.size())
            return false;
        else {
            for (int i = 0; i < a.size(); i++) {
                if (!a.get(i).equals(b.get(i)))
                    return false;
            }
        }
        return true;
    }
}
