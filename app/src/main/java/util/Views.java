package util;

import android.widget.TextView;

/**
 * Created by Ahmad on 11/05/17.
 * All rights reserved.
 */

public class Views {
    public static void setTextIfNotEqual(TextView textView, String text) {
        String oldText = textView.getText().toString();
        boolean equal = false;
        if (oldText != null && text != null)
            equal = oldText.equals(text);
        else if (oldText == null && text != null)
            equal = text.length() == 0;
        else if (oldText != null && text == null)
            equal = oldText.length() == 0;
        else
            equal = true;
        if (!equal) {
            textView.setText(text);
        }
    }
}
