package util.Concurrent;

/**
 * Created by Ahmad on 01/30/18.
 * All rights reserved.
 */

public interface TaskResult<T> {
    void onResultSuccess(T result);
    void onResultFailure();
}
