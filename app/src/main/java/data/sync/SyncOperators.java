package data.sync;

import android.content.Context;

import data.drive.DriveOperator;
import data.dropbox.DropboxOperator;
import data.model.existence.Existence;
import data.preference.SyncPreferences;

/**
 * Created by Ahmad on 02/16/18.
 * All rights reserved.
 */

public class SyncOperators {
    private static SyncOperator currentOperator;

    public static SyncOperator getCurrentOperator(Context context) {
        if (SyncPreferences.isCurrentSyncOperatorDrive(context) &&
                !(currentOperator instanceof DriveOperator)) {
            currentOperator = new DriveOperator();
        } else if (SyncPreferences.isCurrentSyncOperatorDropbox(context) &&
                !(currentOperator instanceof DropboxOperator)) {
            currentOperator = new DropboxOperator();
        } else if (SyncPreferences.isCurrentSyncOperatorDropbox(context) &&
                !(currentOperator instanceof DropboxOperator)) {
            currentOperator = new DropboxOperator();
        }

        return currentOperator;
    }

    public static int getCurrentOperatorExistenceFlag(Context context) {
        return getOperatorExistenceFlag(getCurrentOperator(context));
    }

    public static int getOperatorExistenceFlag(SyncOperator operator) {
        if (operator instanceof DriveOperator)
            return Existence.EXISTENCE_FLAG_DRIVE;
        else if (operator instanceof DropboxOperator) {
            return Existence.EXISTENCE_FLAG_DROP_BOX;
        } else
            throw new RuntimeException("Operator is not recognized");
    }
}
