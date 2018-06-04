package util;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Ahmad on 10/22/17.
 * All rights reserved.
 */

public class Keyboard {

    public static boolean hide(Activity activity, Fragment fragment) {
        boolean result = false;
        if (activity != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

            //Find the currently focused view, so we can grab the correct window token from it.
            View view = activity.getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(activity);
            }
            result = imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            if (!result && fragment != null) {
                View v = fragment.getView();
                if (v != null) {
                    result = imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return result;
    }

    public static boolean show(Activity activity, Fragment fragment) {
        boolean result = false;
        if (activity != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = activity.getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(activity);
            }

            result = imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
            if (!result && fragment != null) {
                View v = fragment.getView();
                if (v != null) {
                    result = imm.showSoftInput(v, InputMethodManager.SHOW_FORCED);
                }
            }
        }
        return result;
    }
}
