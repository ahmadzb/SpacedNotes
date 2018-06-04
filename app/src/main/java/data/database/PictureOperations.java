package data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

import data.model.note.Note;
import data.xml.port.IdProvider;

/**
 * Created by Ahmad on 02/04/18.
 * All rights reserved.
 */
public class PictureOperations {
    public static ArrayList<Long> getPictureIdsByNote(Note note, SQLiteDatabase readableDb) {
        String selection = Contract.Picture.noteId + " = " + note.getId();
        Cursor cursor = readableDb.query(Contract.Picture.table, null, selection, null, null, null, null);

        int pictureIdIndex = cursor.getColumnIndex(Contract.Picture.pictureId);
        ArrayList<Long> ids = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            ids.add(cursor.getLong(pictureIdIndex));
        }
        cursor.close();
        return ids;
    }

    /**
     * this method is used when picture is submitted from another agent and now it comes from xml
     * @param note
     * @param pictureId
     * @param writableDb
     */
    public static void submitPicture(Note note, Long pictureId, SQLiteDatabase writableDb) {
        ContentValues values = new ContentValues();
        values.put(Contract.Picture.noteId, note.getId());
        values.put(Contract.Picture.pictureId, pictureId);
        writableDb.replace(Contract.Picture.table, null, values);
    }

    public static long registerNewPicture(Note note, SQLiteDatabase writableDb, Context context) {
        ContentValues values = new ContentValues();
        values.put(Contract.Picture.noteId, note.getId());
        values.put(Contract.Picture.pictureId, IdProvider.nextPictureId(context));
        long pictureId = writableDb.insert(Contract.Picture.table, null, values);
        return pictureId;
    }

    public static void markAsDeleted(long pictureId, SQLiteDatabase writableDb) {
        String selection = Contract.Picture.pictureId + " = " + pictureId;
        ContentValues values = new ContentValues();
        values.putNull(Contract.Picture.noteId);
        writableDb.update(Contract.Picture.table, values, selection, null);
    }
}
