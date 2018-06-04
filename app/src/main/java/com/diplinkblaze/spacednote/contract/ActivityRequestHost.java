package com.diplinkblaze.spacednote.contract;

import android.support.v4.app.Fragment;

/**
 * Created by Ahmad on 01/11/18.
 * All rights reserved.
 */

public interface ActivityRequestHost {
    int getRequestPrefix(Fragment child);
    int getRequestShift();
}
