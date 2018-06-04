package data.model.pictures;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.io.File;
import java.util.TreeMap;

import data.database.PictureOperations;
import data.model.existence.Existence;
import data.model.existence.ExistenceCatalog;
import data.model.note.Note;
import data.model.profiles.ProfileCatalog;
import util.Concurrent.TaskResult;

/**
 * Created by Ahmad on 01/19/18.
 * All rights reserved.
 */

public class PictureCatalog {
    /**
     * @param note
     * @return
     */
    public static void cachePicturesToSelectDirectory(final Note note, final Context context,
            final SQLiteDatabase database, final TaskResult<TreeMap<Long, File>> taskResult) {
        AsyncTask<Void, Void, TreeMap<Long, File>> asyncTask = new AsyncTask<Void, Void, TreeMap<Long, File>>() {
            @Override
            protected TreeMap<Long, File> doInBackground(Void... params) {
                data.storage.PictureOperations.clearSelectDir(context);
                TreeMap<Long, File> map = data.storage.PictureOperations.copyToSelectDir(note, context, database);
                if (map == null) {
                    //TODO load from drive
                }
                return map;
            }

            @Override
            protected void onPostExecute(TreeMap<Long, File> map) {
                if (map != null) {
                    taskResult.onResultSuccess(map);
                } else {
                    taskResult.onResultFailure();
                }
            }
        };
        asyncTask.execute();
    }

    public static long submitPicture(File picture, Note note,
                                     SQLiteDatabase writableDb, SQLiteDatabase fileWritableDb, Context context) {
        //Database
        long pictureId = PictureOperations.registerNewPicture(note, writableDb, context);
        //XML
        data.xml.log.operations.PictureOperations.submitPicture(note, pictureId, context);
        //File
        data.storage.PictureOperations.submitPicture(picture, pictureId, context);
        //Existence
        ExistenceCatalog.addPictureExistence(ProfileCatalog.getCurrentProfile(context), pictureId, fileWritableDb, context);
        return pictureId;
    }

    public static void deletePicture(long pictureId, Context context, SQLiteDatabase writableDb, SQLiteDatabase fileWritableDb) {
        //Database
        PictureOperations.markAsDeleted(pictureId, writableDb);
        //XML
        data.xml.log.operations.PictureOperations.deletePicture(pictureId, context);
        //File
        data.storage.PictureOperations.deletePicture(pictureId, context);
        //Existence
        Existence existence = ExistenceCatalog.getPictureExistence(
                ProfileCatalog.getCurrentProfile(context), pictureId, fileWritableDb);
        ExistenceCatalog.setExistenceState(existence, Existence.STATE_DELETE, fileWritableDb, context);
    }
}
