package util.Concurrent;

/**
 * Created by Ahmad on 11/23/17.
 * All rights reserved.
 */

public class AsyncArgs1<T1> {
    T1 arg1;

    public AsyncArgs1(T1 arg1) {
        this.arg1 = arg1;
    }

    public T1 getArg1() {
        return arg1;
    }
}
