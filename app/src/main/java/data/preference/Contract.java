package data.preference;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.prefs.Preferences;

/**
 * Created by Ahmad on 02/01/18.
 * All rights reserved.
 */

public class Contract {
    private static final String contentPreferences = "content";
    private static final String syncPreferences = "sync";

    static SharedPreferences getContentPreferences(Context context) {
        return context.getSharedPreferences(contentPreferences, Context.MODE_PRIVATE);
    }

    static SharedPreferences getSyncPreferences(Context context) {
        return context.getSharedPreferences(syncPreferences, Context.MODE_PRIVATE);
    }
}
