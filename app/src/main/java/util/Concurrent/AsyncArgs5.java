package util.Concurrent;

/**
 * Created by Ahmad on 11/23/17.
 * All rights reserved.
 */

public class AsyncArgs5<T1, T2, T3, T4, T5> {
    T1 arg1;
    T2 arg2;
    T3 arg3;
    T4 arg4;
    T5 arg5;

    public AsyncArgs5(T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5) {
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.arg3 = arg3;
        this.arg4 = arg4;
        this.arg5 = arg5;
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

    public T4 getArg4() {
        return arg4;
    }

    public T5 getArg5() {
        return arg5;
    }
}
