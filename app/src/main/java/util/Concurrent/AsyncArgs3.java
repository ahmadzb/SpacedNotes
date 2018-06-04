package util.Concurrent;

/**
 * Created by Ahmad on 11/23/17.
 * All rights reserved.
 */

public class AsyncArgs3<T1, T2, T3> {
    T1 arg1;
    T2 arg2;
    T3 arg3;

    public AsyncArgs3(T1 arg1, T2 arg2, T3 arg3) {
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.arg3 = arg3;
    }

    public T1 getArg1() {
        return arg1;
    }

    public T2 getArg2() {
        return arg2;
    }

    public T3 getArg3() {
        return arg3;
    }
}
