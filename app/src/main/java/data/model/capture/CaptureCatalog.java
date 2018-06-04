package data.model.capture;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

import data.database.file.FileOpenHelper;
import data.model.existence.Existence;
import data.model.existence.ExistenceCatalog;
import data.storage.CaptureOperations;
import data.xml.port.IdProvider;
import data.xml.port.PortOperations;
import data.xml.profiles.ProfilesOperations;
import data.xml.progress.ProgressOperations;

/**
 * Created by Ahmad on 03/22/18.
 * All rights reserved.
 */

public class CaptureCatalog {
    public static final int CAPTURE_RETRIEVAL_THRESHOLD = 6000;
    private static final int CAPTURE_OPERATION_COUNT_THRESHOLD = 6000;
    private static final double CAPTURE_EXPONENT = 2.0;

    public static void updateCaptures(Context context) {
        if (PortOperations.getCurrentPort(context) == null) return;
        SQLiteDatabase fileWritableDb = FileOpenHelper.getDatabase(context);
        createNewCaptureIfNeeded(fileWritableDb, context);//Do not put this line into transaction, created capture should copy the effected database
        fileWritableDb.beginTransaction();
        deleteRedundantCaptures(fileWritableDb, context);
        fileWritableDb.setTransactionSuccessful();
        fileWritableDb.endTransaction();
    }

    private static void createNewCaptureIfNeeded(SQLiteDatabase fileWritableDb, Context context) {
        ArrayList<Existence> captureExistences =
                ExistenceCatalog.getCaptureExistencesByState(Existence.STATE_PRESENT, fileWritableDb);
        long cumulativeOperationCount = IdProvider.progressedCumulativeOperationCount(context);
        long distance = cumulativeOperationCount;
        for (Existence existence : captureExistences) {
            if (distance > cumulativeOperationCount - existence.getData1()) {
                distance = cumulativeOperationCount - existence.getData1();
            }
        }
        if (distance > CAPTURE_OPERATION_COUNT_THRESHOLD) {
            CaptureOperations.makeCapture(context);
        }
    }

    private static void deleteRedundantCaptures(SQLiteDatabase fileWritableDb, Context context) {
        ArrayList<Existence> captureExistences = ExistenceCatalog.getCaptureExistencesByState(
                Existence.STATE_PRESENT, fileWritableDb);
        long cumulativeOperationCount = IdProvider.progressedCumulativeOperationCount(context);
        TreeMap<Integer, Existence> spacedExistences = new TreeMap<>();
        ArrayList<Existence> deleteList = new ArrayList<>(captureExistences.size());
        for (Existence existence : captureExistences) {
            int exp = calculateCaptureOperationExponent(cumulativeOperationCount, existence.getData1());
            Existence compare = spacedExistences.get(exp);
            if (compare == null) {
                spacedExistences.put(exp, existence);
            } else if (compare.getData1() > existence.getData1()) {
                spacedExistences.put(exp, existence);
                deleteList.add(compare);
            } else {
                deleteList.add(existence);
            }
        }
        for (Existence existence : deleteList) {
            long captureId = Existence.Pattern.Capture.getCaptureId(existence.getPattern());
            CaptureOperations.deleteCapture(captureId, context);
        }
    }

    private static int calculateCaptureOperationExponent(long currentCOC, long captureCOC) {
        if (captureCOC > currentCOC)
            throw new RuntimeException("captureCOC should not be greater than currentCOC");
        long distance = currentCOC - captureCOC;
        double exp = Math.log(distance / CAPTURE_OPERATION_COUNT_THRESHOLD + 1) / Math.log(CAPTURE_EXPONENT);
        return Math.max(0, (int) exp);
    }


    public static File makeCapture(Context context) {
        if (PortOperations.getCurrentPort(context) == null)
            throw new RuntimeException("Current Port is null, cannot create capture");
        return CaptureOperations.makeCapture(context);
    }

    public static void replaceFromCapture(File capture, Context context) {
        Log.i("Capture Load", capture.getName());
        CaptureOperations.replaceFromCapture(capture, context);
        ProgressOperations.reloadProgress();
        ProfilesOperations.reloadProfiles();
        PortOperations.reloadAll();
    }

    public static void deleteCapture(long captureId, Context context) {
        CaptureOperations.deleteCapture(captureId, context);
    }
}
