package util.Concurrent;

import java.util.ArrayList;

/**
 * Created by Ahmad on 02/15/18.
 * All rights reserved.
 */

public interface TaskProgress {
    void setStatus(String status);
    void setStatus(int statusResId);
    boolean isProgressCancelled();
}
