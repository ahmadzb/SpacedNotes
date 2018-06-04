package com.diplinkblaze.spacednote.contract;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

/**
 * Created by Ahmad on 10/30/17.
 * All rights reserved.
 */

public class ContentUpdateUtil {
    public static void updateContentChildren(AppCompatActivity activity) {
        updateContentChildren(activity.getSupportFragmentManager().getFragments());
    }

    public static void updateContentChildren(Fragment fragment) {
        updateContentChildren(fragment.getChildFragmentManager().getFragments());
    }

    private static void updateContentChildren(List<Fragment> fragments) {
        if (fragments != null)
            for (Fragment fragment : fragments)
                if (fragment instanceof ContentUpdateListener)
                    ((ContentUpdateListener) fragment).updateContent();
    }
}
