package util;

/**
 * Created by Ahmad on 11/15/17.
 * All rights reserved.
 */

public class Flags {
    public static boolean hasFlags(int allFlags, int flags) {
        return (allFlags & flags) == flags;
    }

    public static boolean hasFlags(long allFlags, int flags) {
        return (allFlags & flags) == flags;
    }
}
