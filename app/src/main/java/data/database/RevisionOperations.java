package data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.joda.time.LocalDate;

import java.util.ArrayList;

import data.model.note.Note;
import data.model.schedule.Occurrence;
import data.model.schedule.Schedule;
import data.model.scheduler.RevisionFuture;
import data.model.scheduler.RevisionPast;
import data.model.scheduler.Scheduler;
import data.xml.port.IdProvider;
import data.xml.profiles.ProfilesContract;
import util.datetime.primitive.Representation;

/**
 * Created by Ahmad on 02/04/18.
 * All rights reserved.
 */

public class RevisionOperations {
    //===================================== Revision Future ========================================
    public static boolean hasFutureRevision(Schedule schedule, SQLiteDatabase readableDb) {
        String sql = "SELECT COUNT(*) AS countColumn FROM " + Contract.RevisionFuture.table +
                " WHERE " + Contract.RevisionFuture.scheduleId + " = " + schedule.getId();
        Cursor cursor = readableDb.rawQuery(sql, null);
        cursor.moveToFirst();
        int count = cursor.getInt(cursor.getColumnIndex("countColumn"));
        cursor.close();
        return count != 0;
    }

    public static ArrayList<RevisionFuture> getRevisionFuturesUntilDate(LocalDate date, SQLiteDatabase readableDb) {
        String selection = Contract.RevisionFuture.dueDate + " <= " + Representation.fromLocalDate(date);
        return getRevisionFutures(selection, readableDb);
    }

    public static ArrayList<RevisionFuture> getRevisionFuturesForDate(LocalDate date, SQLiteDatabase readableDb) {
        String selection = Contract.RevisionFuture.dueDate + " = " + Representation.fromLocalDate(date);
        return getRevisionFutures(selection, readableDb);
    }

    public static RevisionFuture getRevisionFutureForNote(Note note, SQLiteDatabase readableDb) {
        String selection = Contract.RevisionFuture.noteId + "=" + note.getId();
        ArrayList<RevisionFuture> revisionFutures = getRevisionFutures(selection, readableDb);
        if (revisionFutures.size() == 0)
            return null;
        else
            return revisionFutures.get(0);
    }

    public static ArrayList<RevisionFuture> getRevisionFutures(String selection, SQLiteDatabase readableDb) {
        Cursor cursor = readableDb.query(Contract.RevisionFuture.table, null, selection, null, null, null, null);
        RevisionFutureIndexCache indexCache = RevisionFutureIndexCache.newInstance(cursor);

        ArrayList<RevisionFuture> revisionFutures = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            revisionFutures.add(retrieveRevisionFuture(cursor, indexCache));
        }
        cursor.close();
        return revisionFutures;
    }

    public static RevisionFuture retrieveRevisionFuture(Cursor cursor, RevisionFutureIndexCache indexCache) {
        RevisionFuture revisionFuture = RevisionFuture.newInstance();
        if (!cursor.isNull(indexCache.noteIdIndex)) {
            revisionFuture.setNoteId(cursor.getLong(indexCache.noteIdIndex));
        } else {
            return null;
        }

        if (!cursor.isNull(indexCache.dueDateIndex)) {
            revisionFuture.setDueDate(cursor.getInt(indexCache.dueDateIndex));
        } else {
            return null;
        }

        if (!cursor.isNull(indexCache.scheduleIdIndex)) {
            revisionFuture.setScheduleId(cursor.getLong(indexCache.scheduleIdIndex));
        } else {
            return null;
        }

        if (!cursor.isNull(indexCache.occurrenceNumberIndex)) {
            revisionFuture.setOccurrenceNumber(cursor.getInt(indexCache.occurrenceNumberIndex));
        } else {
            return null;
        }

        revisionFuture.setInitialized(true);
        revisionFuture.setRealized(true);

        return revisionFuture;
    }

    public static void batchConvertRevisionFutures(Schedule oldSchedule, Schedule newSchedule, SQLiteDatabase writableDb) {
        if (!newSchedule.isRealized() || !newSchedule.isInitialized() || !oldSchedule.isRealized() ||
                !oldSchedule.isInitialized()) {
            throw new RuntimeException("Some items aren't realized or initialized");
        }

        if (oldSchedule.getId() == newSchedule.getId())
            return;

        for (int oldNumber = 0; oldNumber < oldSchedule.getOccurrencesCount(); oldNumber++) {
            Occurrence newOccurrence = Scheduler.getConvertedOccurrence(oldNumber, oldSchedule, newSchedule);
            if (newOccurrence == null) {
                throw new RuntimeException("New schedule does not have any occurrences, cannot merge");
            }
            ContentValues values = new ContentValues();
            values.put(Contract.RevisionFuture.scheduleId, newSchedule.getId());
            values.put(Contract.RevisionFuture.occurrenceNumber, newOccurrence.getNumber());
            String selection;
            if (oldNumber == oldSchedule.getOccurrencesCount() - 1) {
                selection = Contract.RevisionFuture.scheduleId + " = " + oldSchedule.getId() + " AND " +
                        Contract.RevisionFuture.occurrenceNumber + " >= " + oldNumber;
            } else {
                selection = Contract.RevisionFuture.scheduleId + " = " + oldSchedule.getId() + " AND " +
                        Contract.RevisionFuture.occurrenceNumber + " = " + oldNumber;
            }
            writableDb.update(Contract.RevisionFuture.table, values, selection, null);
        }

        //Sanity check:
        {
            if (hasFutureRevision(oldSchedule, writableDb)) {
                throw new RuntimeException("There is still something left of old schedule, something most have" +
                        "gone wrong");
            }
        }
    }

    private static ContentValues makeContentValues(RevisionFuture revisionFuture) {
        ContentValues values = new ContentValues();
        values.put(Contract.RevisionFuture.noteId, revisionFuture.getNoteId());
        values.put(Contract.RevisionFuture.scheduleId, revisionFuture.getScheduleId());
        values.put(Contract.RevisionFuture.dueDate, revisionFuture.getDueDate());
        values.put(Contract.RevisionFuture.occurrenceNumber, revisionFuture.getOccurrenceNumber());
        return values;
    }

    public static void addRevisionFuture(RevisionFuture revisionFuture, SQLiteDatabase writableDb) {
        if (!revisionFuture.isInitialized())
            throw new RuntimeException("com.diplinkblaze.spacednote.revision is not initialized");
        writableDb.insert(Contract.RevisionFuture.table, null, makeContentValues(revisionFuture));
    }

    public static int updateRevisionFuture(RevisionFuture revisionFuture, SQLiteDatabase writableDb) {
        if (!revisionFuture.isInitialized() || !revisionFuture.isRealized())
            throw new RuntimeException("com.diplinkblaze.spacednote.revision is not initialized or realized");
        String selection = Contract.RevisionFuture.noteId + " = " + revisionFuture.getNoteId();
        return writableDb.update(Contract.RevisionFuture.table, makeContentValues(revisionFuture), selection, null);
    }

    public static void deleteRevisionFuture(Note note, SQLiteDatabase writableDb) {
        String selection = Contract.RevisionFuture.noteId + " = " + note.getId();
        writableDb.delete(Contract.RevisionFuture.table, selection, null);
    }

    //====================================== Revision Past =========================================
    public static ArrayList<RevisionPast> getRevisionPastsForDate(LocalDate date, SQLiteDatabase readableDb) {
        String selection = Contract.RevisionPast.date + " = " + Representation.fromLocalDate(date);
        return getRevisionPasts(selection, readableDb);
    }

    public static RevisionPast getLastRevisionPastForNote(Note note, SQLiteDatabase readableDb) {
        String sql = "SELECT * FROM " + Contract.RevisionPast.table +
                " WHERE " + Contract.RevisionPast.noteId + " = " + note.getId() +
                " AND " + Contract.RevisionPast.date + " IN (" +
                "SELECT MAX(" + Contract.RevisionPast.date + ")" +
                " FROM " + Contract.RevisionPast.table +
                " WHERE " + Contract.RevisionPast.noteId + " = " + note.getId() + ")";
        Cursor cursor = readableDb.rawQuery(sql, null);
        ArrayList<RevisionPast> revisionPasts = retrieveRevisionPasts(cursor);
        cursor.close();
        if (revisionPasts.size() == 0)
            return null;
        else
            return revisionPasts.get(0);
    }

    public static ArrayList<RevisionPast> getRevisionPastsForNote(Note note, SQLiteDatabase readableDb) {
        String selection = Contract.RevisionPast.noteId + " = " + note.getId();
        return getRevisionPasts(selection, readableDb);
    }

    public static ArrayList<RevisionPast> getRevisionPasts(String selection, SQLiteDatabase readableDb) {
        Cursor cursor = readableDb.query(Contract.RevisionPast.table, null, selection, null, null, null, null);
        ArrayList<RevisionPast> revisionPasts = retrieveRevisionPasts(cursor);
        cursor.close();
        return revisionPasts;
    }

    private static ArrayList<RevisionPast> retrieveRevisionPasts(Cursor cursor) {
        int noteIdIndex = cursor.getColumnIndex(Contract.RevisionPast.noteId);
        int dateIndex = cursor.getColumnIndex(Contract.RevisionPast.date);

        ArrayList<RevisionPast> revisionPasts = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            RevisionPast revisionPast = RevisionPast.newInstance();
            revisionPast.setNoteId(cursor.getLong(noteIdIndex));
            revisionPast.setDate(cursor.getInt(dateIndex));

            revisionPast.setInitialized(true);
            revisionPast.setRealized(true);

            revisionPasts.add(revisionPast);
        }
        return revisionPasts;
    }

    private static ContentValues makeContentValues(RevisionPast revisionPast) {
        ContentValues values = new ContentValues();
        values.put(Contract.RevisionPast.noteId, revisionPast.getNoteId());
        values.put(Contract.RevisionPast.date, revisionPast.getDate());
        return values;
    }

    public static long addRevisionPast(RevisionPast revisionPast, SQLiteDatabase writableDb, Context context) {
        if (!revisionPast.isInitialized())
            throw new RuntimeException("com.diplinkblaze.spacednote.revision is not initialized");
        ContentValues values = makeContentValues(revisionPast);
        if (revisionPast.isRealized()) {
            return writableDb.replace(Contract.RevisionPast.table, null, values);
        } else {
            return writableDb.insert(Contract.RevisionPast.table, null, values);
        }
    }


    public static void deleteRevisionPast(RevisionPast revisionPast, SQLiteDatabase writableDb) {
        String selection = Contract.RevisionPast.noteId + " = " + revisionPast.getNoteId() +
                " AND " + Contract.RevisionPast.date + " = " + revisionPast.getDate();
        writableDb.delete(Contract.RevisionPast.table, selection, null);
    }

    //==============================================================================================
    public static class RevisionFutureIndexCache {
        int noteIdIndex;
        int dueDateIndex;
        int scheduleIdIndex;
        int occurrenceNumberIndex;

        private RevisionFutureIndexCache() {

        }

        public static RevisionFutureIndexCache newInstance(Cursor cursor) {
            RevisionFutureIndexCache instance = new RevisionFutureIndexCache();
            instance.noteIdIndex = cursor.getColumnIndex(Contract.RevisionFuture.noteId);
            instance.dueDateIndex = cursor.getColumnIndex(Contract.RevisionFuture.dueDate);
            instance.scheduleIdIndex = cursor.getColumnIndex(Contract.RevisionFuture.scheduleId);
            instance.occurrenceNumberIndex = cursor.getColumnIndex(Contract.RevisionFuture.occurrenceNumber);
            return instance;
        }

        public boolean isValid() {
            return noteIdIndex >= 0 && dueDateIndex >= 0 && scheduleIdIndex >= 0 && occurrenceNumberIndex >= 0;
        }
    }
}
