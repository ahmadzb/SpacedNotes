package data.model.scheduler;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.joda.time.LocalDate;

import java.util.ArrayList;

import data.database.RevisionOperations;
import data.model.note.Note;
import data.model.schedule.Schedule;

/**
 * Created by Ahmad on 02/04/18.
 * All rights reserved.
 */

public class RevisionCatalog {
    //===================================== Revision Future ========================================
    public static boolean hasFutureRevision(Schedule schedule, SQLiteDatabase readableDb) {
        return RevisionOperations.hasFutureRevision(schedule, readableDb);
    }

    public static ArrayList<RevisionFuture> getRevisionFuturesUntilDate(LocalDate date, SQLiteDatabase readableDb) {
        return RevisionOperations.getRevisionFuturesUntilDate(date, readableDb);
    }

    public static ArrayList<RevisionFuture> getRevisionFuturesForDate(LocalDate date, SQLiteDatabase readableDb) {
        return RevisionOperations.getRevisionFuturesForDate(date, readableDb);
    }

    public static RevisionFuture getRevisionFutureForNote(Note note, SQLiteDatabase readableDb) {
        return RevisionOperations.getRevisionFutureForNote(note, readableDb);
    }

    public static ArrayList<RevisionFuture> getRevisionFutures(String selection, SQLiteDatabase readableDb) {
        return RevisionOperations.getRevisionFutures(selection, readableDb);
    }

    public static void batchConvertRevisionFutures(Schedule oldSchedule, Schedule newSchedule,
                                                   SQLiteDatabase writableDb, Context context) {
        RevisionOperations.batchConvertRevisionFutures(oldSchedule, newSchedule, writableDb);
        data.xml.log.operations.RevisionOperations.batchConvertRevisionFutures(oldSchedule, newSchedule, context);
    }

    public static void addRevisionFuture(RevisionFuture revisionFuture, SQLiteDatabase writableDb, Context context) {
        RevisionOperations.addRevisionFuture(revisionFuture, writableDb);
        data.xml.log.operations.RevisionOperations.addRevisionFuture(revisionFuture, context);
    }

    public static int updateRevisionFuture(RevisionFuture revisionFuture, SQLiteDatabase writableDb, Context context) {
        int count = RevisionOperations.updateRevisionFuture(revisionFuture, writableDb);
        data.xml.log.operations.RevisionOperations.updateRevisionFuture(revisionFuture, context);
        return count;
    }

    public static void deleteRevisionFuture(Note note, SQLiteDatabase writableDb, Context context) {
        RevisionOperations.deleteRevisionFuture(note, writableDb);
        data.xml.log.operations.RevisionOperations.deleteRevisionFuture(note, context);
    }

    //====================================== Revision Past =========================================
    public static ArrayList<RevisionPast> getRevisionPastsForDate(LocalDate date, SQLiteDatabase readableDb) {
        return RevisionOperations.getRevisionPastsForDate(date, readableDb);
    }

    public static RevisionPast getLastRevisionPastForNote(Note note, SQLiteDatabase readableDb) {
        return RevisionOperations.getLastRevisionPastForNote(note, readableDb);
    }

    public static ArrayList<RevisionPast> getRevisionPastsForNote(Note note, SQLiteDatabase readableDb) {
        return RevisionOperations.getRevisionPastsForNote(note, readableDb);
    }

    public static ArrayList<RevisionPast> getRevisionPasts(String selection, SQLiteDatabase readableDb) {
        return RevisionOperations.getRevisionPasts(selection, readableDb);
    }

    public static void addRevisionPast(RevisionPast revisionPast, SQLiteDatabase writableDb, Context context) {
        RevisionOperations.addRevisionPast(revisionPast, writableDb, context);
        data.xml.log.operations.RevisionOperations.addRevisionPast(revisionPast, context);
    }

    public static void deleteRevisionPast(RevisionPast revisionPast, SQLiteDatabase writableDb, Context context) {
        RevisionOperations.deleteRevisionPast(revisionPast, writableDb);
        data.xml.log.operations.RevisionOperations.deleteRevisionPast(revisionPast, context);
    }
}
