package com.diplinkblaze.spacednote.contract;

import android.support.v4.app.Fragment;

/**
 * Created by Ahmad on 02/16/18.
 * All rights reserved.
 */

public class ActivityRequestHostUtils {
    public static int toGlobalRequest(int localRequest, ActivityRequestHost host, Fragment child) {
        return (localRequest << host.getRequestShift()) | host.getRequestPrefix(child);
    }
}
