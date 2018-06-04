package data.preference;

import android.content.Context;

import org.joda.time.DateTimeZone;

/**
 * Created by Ahmad on 03/29/18.
 * All rights reserved.
 */

public class ChronologyCatalog {
    public static void setCurrentChronologyPersian(Context context) {
        ContentPreferences.Chronology.setCurrentChronologyPersian(context);
    }

    public static void setCurrentChronologyGregorian(Context context) {
        ContentPreferences.Chronology.setCurrentChronologyGregorian(context);
    }
    public static org.joda.time.Chronology getCurrentChronology(Context context) {
        return ContentPreferences.Chronology.getCurrentChronology(context);
    }

    public static org.joda.time.Chronology getCurrentChronology(Context context, DateTimeZone timeZone) {
        return ContentPreferences.Chronology.getCurrentChronology(context, timeZone);
    }
}
