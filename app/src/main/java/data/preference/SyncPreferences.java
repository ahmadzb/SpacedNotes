package data.preference;

import android.content.Context;

/**
 * Created by Ahmad on 05/24/18.
 * All rights reserved.
 */
public class SyncPreferences {
    private static final String KEY_CURRENT_SYNC_OPERATOR = "currentSyncOperator";
    private static final String KEY_DROPBOX_TOKEN = "dropboxToken";
    private static final String KEY_PCLOUD_TOKEN = "pCloudToken";

    private static final int SYNC_OPERATOR_DRIVE = 1;
    private static final int SYNC_OPERATOR_DROPBOX = 2;

    private static Integer currentSyncOperator;

    public static boolean isCurrentSyncOperatorDrive(Context context) {
        return getCurrentSyncOperator(context) == SYNC_OPERATOR_DRIVE;
    }

    public static boolean isCurrentSyncOperatorDropbox(Context context) {
        return getCurrentSyncOperator(context) == SYNC_OPERATOR_DROPBOX;
    }

    private static int getCurrentSyncOperator(Context context) {
        if (currentSyncOperator == null) {
            currentSyncOperator = Contract.getSyncPreferences(context).getInt(KEY_CURRENT_SYNC_OPERATOR, SYNC_OPERATOR_DRIVE);
        }
        return currentSyncOperator;
    }

    public static void setCurrentSyncOperatorDrive(Context context) {
        setCurrentSyncOperator(SYNC_OPERATOR_DRIVE, context);
    }

    public static void setCurrentSyncOperatorDropbox(Context context) {
        setCurrentSyncOperator(SYNC_OPERATOR_DROPBOX, context);
    }

    private static void setCurrentSyncOperator(int currentSyncOperator, Context context) {
        SyncPreferences.currentSyncOperator = currentSyncOperator;
        Contract.getSyncPreferences(context).edit().putInt(KEY_CURRENT_SYNC_OPERATOR, currentSyncOperator).apply();
    }

    public static class Dropbox {
        private static String token;

        public static String getToken(Context context) {
            if (token == null) {
                token = Contract.getSyncPreferences(context).getString(KEY_DROPBOX_TOKEN, null);
            }
            return token;
        }

        public static void setToken(String token, Context context) {
            Dropbox.token = token;
            Contract.getSyncPreferences(context).edit().putString(KEY_DROPBOX_TOKEN, token).apply();
        }
    }


    public static class PCloud {
        private static String token;

        public static String getToken(Context context) {
            if (token == null) {
                token = Contract.getSyncPreferences(context).getString(KEY_PCLOUD_TOKEN, null);
            }
            return token;
        }

        public static void setToken(String token, Context context) {
            Dropbox.token = token;
            Contract.getSyncPreferences(context).edit().putString(KEY_PCLOUD_TOKEN, token).apply();
        }
    }

}
