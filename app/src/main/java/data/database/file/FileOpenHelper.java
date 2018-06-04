package data.database.file;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Ahmad on 02/15/18.
 * All rights reserved.
 */

public class FileOpenHelper extends SQLiteOpenHelper {

    public static final int version = 1;
    public static final String nameDatabase = "fileDatabase.db";


    private static FileOpenHelper mInstance;
    private static SQLiteDatabase mDatabase;


    private FileOpenHelper(Context context) {
        super(context, nameDatabase, null, version);
    }

    public static FileOpenHelper getInstance(Context context) {
        if (mInstance == null)
            mInstance = new FileOpenHelper(context);
        return mInstance;
    }

    public static SQLiteDatabase getDatabase(Context context) {
        if (mDatabase == null)
            mDatabase = getInstance(context).getWritableDatabase();
        return mDatabase;
    }

    public static void closeInstance() {
        if (mInstance != null) {
            mDatabase.close();
            mInstance.close();
            mInstance = null;
            mDatabase = null;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Tables.existence);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private static class Tables {
        public static final String existence = "CREATE TABLE " + FileContract.Existence.table + "(" +
                FileContract.Existence.pattern + " INTEGER PRIMARY KEY," +
                FileContract.Existence.type + " INTEGER NOT NULL," +
                FileContract.Existence.profile + " INTEGER," +
                FileContract.Existence.existenceFlags + " INTEGER NOT NULL," +
                FileContract.Existence.state + " INTEGER NOT NULL," +
                FileContract.Existence.data1 + " INTEGER)";
    }
}
