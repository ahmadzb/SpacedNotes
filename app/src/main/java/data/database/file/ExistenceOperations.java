package data.database.file;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import data.model.existence.Existence;
import data.model.profiles.Profile;
import data.sync.SyncOperator;
import data.sync.SyncOperators;
import data.xml.profiles.ProfilesOperations;

/**
 * Created by Ahmad on 02/15/18.
 * All rights reserved.
 */

public class ExistenceOperations {
    public static Existence getExistenceByPattern(long pattern, SQLiteDatabase fileReadableDb) {
        String selection = FileContract.Existence.pattern + " = " + pattern;
        Cursor cursor = fileReadableDb.query(FileContract.Existence.table, null, selection,
                null, null, null, null);
        ArrayList<Existence> existences = retrieveExistences(cursor);
        cursor.close();
        if (existences.size() == 0) {
            return null;
        } else {
            return existences.get(0);
        }
    }


    public static ArrayList<Existence> getPictureExistencesByState(int state, SQLiteDatabase fileReadableDb) {
        String selection = FileContract.Existence.state + " = " + state +
                " AND " + FileContract.Existence.type + " = " + Existence.TYPE_PICTURE;
        Cursor cursor = fileReadableDb.query(FileContract.Existence.table, null, selection,
                null, null, null, null);
        ArrayList<Existence> existences = retrieveExistences(cursor);
        cursor.close();
        return existences;
    }

    public static ArrayList<Existence> getCaptureExistencesByState(int state, SQLiteDatabase fileReadableDb) {
        String selection = FileContract.Existence.state + " = " + state +
                " AND " + FileContract.Existence.type + " = " + Existence.TYPE_CAPTURE;
        Cursor cursor = fileReadableDb.query(FileContract.Existence.table, null, selection,
                null, null, null, null);
        ArrayList<Existence> existences = retrieveExistences(cursor);
        cursor.close();
        return existences;
    }

    public static ArrayList<Existence> getAllDownloadExistences(SyncOperator operator, SQLiteDatabase fileReadableDb, Context context) {
        int existenceFlag = SyncOperators.getOperatorExistenceFlag(operator);
        ArrayList<Existence> existences;
        //Retrieve all possible download existences
        {
            ArrayList<Profile> notOffline = ProfilesOperations.getProfilesNotOffline(context);
            String notOfflineList = "";
            for (int i = 0; i < notOffline.size(); i++) {
                notOfflineList = notOfflineList + (i == 0 ? "" : ",") + notOffline.get(i).getId();
            }
            String sql = "SELECT * FROM " + FileContract.Existence.table +
                    " WHERE " + "(" + FileContract.Existence.existenceFlags + " & " + existenceFlag + ") = " + existenceFlag +
                    " AND " + FileContract.Existence.state + " = " + FileContract.Existence.STATE_PRESENT +
                    " AND " + FileContract.Existence.profile + " NOT IN(" + notOfflineList + ")";
            Cursor cursor = fileReadableDb.rawQuery(sql, null);
            existences = retrieveExistences(cursor);
            cursor.close();
        }
        ArrayList<Existence> result = new ArrayList<>(existences.size());
        //Select download existences
        {
            TreeMap<File, TreeSet<File>> dirLists = new TreeMap<>();
            for (Existence existence : existences) {
                File dir = existence.getDirectory();
                TreeSet<File> set = dirLists.get(dir);
                if (set == null) {
                    File[] list = dir.listFiles();
                    ArrayList<File> files = new ArrayList<>(list.length);
                    for (File file : list)
                        files.add(file);
                    set = new TreeSet<>(files);
                    dirLists.put(dir, set);
                }
                if (!set.contains(existence.getFile())) {
                    result.add(existence);
                }
            }
        }
        return result;
    }

    public static ArrayList<Existence> getAllUploadExistences(SyncOperator operator, SQLiteDatabase fileReadableDb) {
        int existenceFlag = SyncOperators.getOperatorExistenceFlag(operator);
        String selection = "(" + FileContract.Existence.existenceFlags + " & " + existenceFlag + ") != " + existenceFlag +
                " AND " + FileContract.Existence.state + " = " + FileContract.Existence.STATE_PRESENT;
        Cursor cursor = fileReadableDb.query(FileContract.Existence.table, null, selection,
                null, null, null, null);
        ArrayList<Existence> existences = retrieveExistences(cursor);
        cursor.close();
        return existences;
    }

    public static ArrayList<Existence> getAllDeleteExistences(SyncOperator operator, SQLiteDatabase fileReadableDb) {
        int existenceFlag = SyncOperators.getOperatorExistenceFlag(operator);
        String selection = "(" + FileContract.Existence.existenceFlags + " & " + existenceFlag + ") = " + existenceFlag +
                " AND " + FileContract.Existence.state + " = " + FileContract.Existence.STATE_DELETE;
        Cursor cursor = fileReadableDb.query(FileContract.Existence.table, null, selection,
                null, null, null, null);
        ArrayList<Existence> existences = retrieveExistences(cursor);
        cursor.close();
        return existences;
    }

    private static ArrayList<Existence> retrieveExistences(Cursor cursor) {
        int patternIndex = cursor.getColumnIndex(FileContract.Existence.pattern);
        int typeIndex = cursor.getColumnIndex(FileContract.Existence.type);
        int existenceFlagsIndex = cursor.getColumnIndex(FileContract.Existence.existenceFlags);
        int stateIndex = cursor.getColumnIndex(FileContract.Existence.state);
        int profileIndex = cursor.getColumnIndex(FileContract.Existence.profile);
        int data1Index = cursor.getColumnIndex(FileContract.Existence.data1);

        ArrayList<Existence> existences = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            Existence existence = Existence.newInstance();
            existence.setPattern(cursor.getLong(patternIndex));
            existence.setType(cursor.getInt(typeIndex));
            existence.setExistenceFlags(cursor.getInt(existenceFlagsIndex));
            existence.setState(cursor.getInt(stateIndex));
            if (!cursor.isNull(profileIndex)) {
                existence.setProfile(cursor.getLong(profileIndex));
            }
            if (!cursor.isNull(data1Index)) {
                existence.setData1(cursor.getLong(data1Index));
            }
            existence.setInitialized(true);
            existence.setRealized(true);
            existences.add(existence);
        }
        return existences;
    }

    //============================================ Write ===========================================
    private static ContentValues getContentValues(Existence existence) {
        ContentValues values = new ContentValues();
        values.put(FileContract.Existence.pattern, existence.getPattern());
        values.put(FileContract.Existence.type, existence.getType());
        if (existence.getProfile() != null) {
            values.put(FileContract.Existence.profile, existence.getProfile());
        }
        values.put(FileContract.Existence.existenceFlags, existence.getExistenceFlags());
        values.put(FileContract.Existence.state, existence.getState());
        if (existence.getData1() != null) {
            values.put(FileContract.Existence.data1, existence.getData1());
        }
        return values;
    }

    public static long addExistence(Existence existence, SQLiteDatabase fileWritableDb) {
        if (!existence.isInitialized())
            throw new RuntimeException("Existence is not initialized");
        ContentValues values = getContentValues(existence);
        if (existence.isRealized()) {
            return fileWritableDb.replace(FileContract.Existence.table, null, values);
        } else {
            return fileWritableDb.insert(FileContract.Existence.table, null, values);
        }
    }

    public static int updateExistence(Existence existence, SQLiteDatabase fileWritableDb) {
        if (!existence.isInitialized() || !existence.isRealized())
            throw new RuntimeException("Existence is not initialized or realized");
        String selection = FileContract.Existence.pattern + " = " + existence.getPattern();
        return fileWritableDb.update(FileContract.Existence.table,
                getContentValues(existence), selection, null);
    }

    public static int setExistenceStateByPattern(long pattern, int state, SQLiteDatabase fileWritableDb) {
        String selection = FileContract.Existence.pattern + " = " + pattern;
        ContentValues contentValues = new ContentValues();
        contentValues.put(FileContract.Existence.state, state);
        return fileWritableDb.update(FileContract.Existence.table, contentValues, selection, null);
    }

    public static int deleteExistence(Existence existence, SQLiteDatabase fileWritableDb) {
        String selection = FileContract.Existence.pattern + " = " + existence.getPattern();
        return fileWritableDb.delete(FileContract.Existence.table, selection, null);
    }


    public static int setAllExistencesStateByProfile(Profile profile, int state, SQLiteDatabase fileWritableDb) {
        String selection = FileContract.Existence.profile + " = " + profile.getId();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FileContract.Existence.state, state);
        return fileWritableDb.update(FileContract.Existence.table, contentValues, selection, null);
    }

    public static void clearExistenceFlag(int flag, SQLiteDatabase fileWritableDb) {
        String sql = "UPDATE " + FileContract.Existence.table +
                " SET " + FileContract.Existence.existenceFlags + " = " +
                "(" + FileContract.Existence.existenceFlags + " & (~" + flag + "))";
        fileWritableDb.execSQL(sql);
    }
}
