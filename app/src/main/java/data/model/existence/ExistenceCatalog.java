package data.model.existence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import data.database.file.ExistenceOperations;
import data.model.profiles.Profile;
import data.sync.SyncOperator;
import data.xml.port.IdProvider;

/**
 * Created by Ahmad on 02/15/18.
 * All rights reserved.
 */

public class ExistenceCatalog {
    public static Existence getPictureExistence(Profile profile, long id, SQLiteDatabase fileReadableDb) {
        return getExistenceByPattern(Existence.Pattern.Picture.getPattern(profile, id), fileReadableDb);
    }

    public static Existence getCaptureExistence(long id, SQLiteDatabase fileReadableDb) {
        return getExistenceByPattern(Existence.Pattern.Capture.getPattern(id), fileReadableDb);
    }

    public static ArrayList<Existence> getPictureExistencesByState(int state, SQLiteDatabase fileReadableDb) {
        return ExistenceOperations.getPictureExistencesByState(state, fileReadableDb);
    }

    public static ArrayList<Existence> getCaptureExistencesByState(int state, SQLiteDatabase fileReadableDb) {
        return ExistenceOperations.getCaptureExistencesByState(state, fileReadableDb);
    }

    public static Existence getExistenceByPattern(long pattern, SQLiteDatabase fileReadableDb) {
        return ExistenceOperations.getExistenceByPattern(pattern, fileReadableDb);
    }

    public static ArrayList<Existence> getAllDownloadExistences(SyncOperator operator, SQLiteDatabase fileReadableDb, Context context) {
        return ExistenceOperations.getAllDownloadExistences(operator, fileReadableDb, context);
    }

    public static ArrayList<Existence> getAllUploadExistences(SyncOperator operator, SQLiteDatabase fileReadableDb) {
        return ExistenceOperations.getAllUploadExistences(operator, fileReadableDb);
    }

    public static ArrayList<Existence> getAllDeleteExistences(SyncOperator operator, SQLiteDatabase fileReadableDb) {
        return ExistenceOperations.getAllDeleteExistences(operator, fileReadableDb);
    }

    //============================================ Write ===========================================
    //========== Add
    public static long addPictureExistence(Profile profile, long id, SQLiteDatabase fileWritableDb, Context context) {
        Existence existence = Existence.newInstance();
        existence.setType(Existence.TYPE_PICTURE);
        existence.setPattern(Existence.Pattern.Picture.getPattern(profile, id));
        existence.setProfile(profile.getId());
        existence.setState(Existence.STATE_PRESENT);
        existence.setExistenceFlags(0);
        existence.setInitialized(true);
        return addExistence(existence, fileWritableDb, context);
    }

    public static long addCaptureExistence(long captureId, long cumulativeOperationCount,
                                           SQLiteDatabase fileWritableDb, Context context) {
        Existence existence = Existence.newInstance();
        existence.setType(Existence.TYPE_CAPTURE);
        existence.setPattern(Existence.Pattern.Capture.getPattern(captureId));
        existence.setState(Existence.STATE_PRESENT);
        existence.setExistenceFlags(0);
        existence.setInitialized(true);
        existence.setData1(cumulativeOperationCount);
        return addExistence(existence, fileWritableDb, context);
    }

    private static long addExistence(Existence existence, SQLiteDatabase writableDb, Context context) {
        long pattern = ExistenceOperations.addExistence(existence, writableDb);
        data.xml.log.operations.ExistenceOperations.addExistence(existence, context);
        return pattern;
    }

    //========== Edit
    public static Existence setExistenceFlag(Existence existence, int flag, SQLiteDatabase fileWritableDb, Context context) {
        if (!existence.isInitialized() || !existence.isRealized())
            throw new RuntimeException("Existence is not initialized or realized");
        Existence result = existence.clone().setExistenceFlags(existence.getExistenceFlags() | flag);
        updateExistence(result, fileWritableDb, context);
        return result;
    }

    public static Existence removeExistenceFlag(Existence existence, int flag, SQLiteDatabase fileWritableDb, Context context) {
        if (!existence.isInitialized() || !existence.isRealized())
            throw new RuntimeException("Existence is not initialized or realized");
        Existence result = existence.clone().setExistenceFlags((existence.getExistenceFlags() | flag) ^ flag);
        updateExistence(result, fileWritableDb, context);
        return result;
    }

    public static Existence setExistenceState(Existence existence, int state, SQLiteDatabase fileWritableDb, Context context) {
        if (!existence.isInitialized() || !existence.isRealized())
            throw new RuntimeException("Existence is not initialized or realized");
        Existence result = existence.clone().setState(state);
        updateExistence(result, fileWritableDb, context);
        return result;
    }

    public static int updateExistence(Existence existence, SQLiteDatabase fileWritableDb, Context context) {
        int count = ExistenceOperations.updateExistence(existence, fileWritableDb);
        data.xml.log.operations.ExistenceOperations.updateExistence(existence, context);
        return count;
    }

    //========== Delete
    public static int deleteCaptureExistence(long captureId, SQLiteDatabase fileWritableDb, Context context) {
        Existence existence = Existence.newInstance();
        existence.setPattern(Existence.Pattern.Capture.getPattern(captureId));
        return deleteExistence(existence, fileWritableDb, context);
    }

    public static int deletePictureExistence(Profile profile, long pictureId, SQLiteDatabase fileWritableDb, Context context) {
        Existence existence = Existence.newInstance();
        existence.setPattern(Existence.Pattern.Picture.getPattern(profile, pictureId));
        return deleteExistence(existence, fileWritableDb, context);
    }

    public static int deleteExistence(Existence existence, SQLiteDatabase fileWritableDb, Context context) {
        int count = ExistenceOperations.deleteExistence(existence, fileWritableDb);
        data.xml.log.operations.ExistenceOperations.deleteExistence(existence, context);
        return count;
    }

    //========== Existence Flag
    public static void clearExistenceFlag(int flag, SQLiteDatabase fileWritableDb, Context context) {
        ExistenceOperations.clearExistenceFlag(flag, fileWritableDb);
        data.xml.log.operations.ExistenceOperations.clearExistenceFlag(flag, context);
    }
}
