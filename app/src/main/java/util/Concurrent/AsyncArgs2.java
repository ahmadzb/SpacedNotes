package util.Concurrent;

/**
 * Created by Ahmad on 11/23/17.
 * All rights reserved.
 */

public class AsyncArgs2<T1, T2> {
    T1 arg1;
    T2 arg2;

    public AsyncArgs2(T1 arg1, T2 arg2) {
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    public T1 getArg1() {
        return arg1;
    }

    public T2 getArg2() {
        return arg2;
    }
}
