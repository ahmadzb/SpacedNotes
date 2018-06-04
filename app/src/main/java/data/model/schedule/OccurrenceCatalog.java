package data.model.schedule;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import data.database.OccurrenceOperations;

/**
 * Created by Ahmad on 01/04/18.
 * All rights reserved.
 */

public class OccurrenceCatalog {

    //=========================================== Read =============================================
    public static int getCountAll(SQLiteDatabase readableDb) {
        return OccurrenceOperations.getCountAll(readableDb);
    }

    public static int getCountAllByScheduleId(long scheduleId, SQLiteDatabase readableDb) {
        return OccurrenceOperations.getCountAllByScheduleId(scheduleId, readableDb);
    }

    public static Occurrence getOccurrenceById(long id, boolean conversions, SQLiteDatabase readableDb) {
        return OccurrenceOperations.getOccurrenceById(id, conversions, readableDb);
    }

    public static ArrayList<Occurrence> getOccurrencesAll(boolean conversions, SQLiteDatabase readableDb) {
        return OccurrenceOperations.getOccurrencesAll(conversions, readableDb);
    }

    public static ArrayList<Occurrence> getOccurrences(String selection, boolean conversions, SQLiteDatabase readableDb) {
        return OccurrenceOperations.getOccurrences(selection, conversions, readableDb);
    }

    /**
     * @param conversions
     * @param readableDb
     * @return key is scheduleId and value is the set of occurrences for that schedule
     */
    public static TreeMap<Long, ? extends Collection<Occurrence>> getOccurrencesAllMap(boolean conversions, SQLiteDatabase readableDb) {
        return OccurrenceOperations.getOccurrencesAllMap(conversions, readableDb);
    }

    public static ArrayList<Occurrence> getOccurrencesByScheduleId(long scheduleId, boolean conversions,
                                                                   SQLiteDatabase readableDb) {
        return OccurrenceOperations.getOccurrencesByScheduleId(scheduleId, conversions, readableDb);
    }

    //========================================== Write =============================================
    public static long addOccurrence(Occurrence occurrence, SQLiteDatabase writableDb, Context context) {
        occurrence.setId(OccurrenceOperations.addOccurrence(occurrence, writableDb, context));
        data.xml.log.operations.OccurrenceOperations.addOccurrence(occurrence, context);
        return occurrence.getId();
    }

    public static void updateOccurrence(Occurrence occurrence, SQLiteDatabase writableDb, Context context) {
        OccurrenceOperations.updateOccurrence(occurrence, writableDb);
        data.xml.log.operations.OccurrenceOperations.updateOccurrence(occurrence, context);
    }

    public static void deleteOccurrence(Occurrence occurrence, SQLiteDatabase writableDb, Context context) {
        OccurrenceOperations.deleteOccurrence(occurrence, writableDb);
        data.xml.log.operations.OccurrenceOperations.deleteOccurrence(occurrence, context);
    }
}
