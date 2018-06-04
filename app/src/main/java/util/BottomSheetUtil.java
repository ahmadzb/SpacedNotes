package util;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.widget.FrameLayout;

/**
 * Created by Ahmad on 11/02/17.
 * All rights reserved.
 */

public class BottomSheetUtil {
    public static void expand(@Nullable DialogInterface dialog) {
        if (dialog != null && dialog instanceof BottomSheetDialog) {
            BottomSheetDialog d = (BottomSheetDialog) dialog;

            FrameLayout bottomSheet = (FrameLayout) d.findViewById(android.support.design.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    public static void expandOnShow(Dialog dialog) {
        BottomSheetDialog d = (BottomSheetDialog) dialog;

        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                expand(dialog);
            }
        });
    }
}
