package util;

/**
 * Created by Ahmad on 11/01/17.
 * All rights reserved.
 */

public class Numbers {
    public static boolean isSmall(double amount) {
        return amount < 0.01 && amount > -0.01;
    }

    public static boolean isPreciseSmall(double amount) {
        return amount < 0.000001 && amount > -0.000001;
    }
}
