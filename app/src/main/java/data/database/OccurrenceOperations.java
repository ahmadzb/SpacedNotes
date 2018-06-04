package data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeMap;

import data.model.schedule.Occurrence;
import data.model.schedule.ScheduleConversion;
import data.xml.port.IdProvider;
import exceptions.InvalidCursorException;

/**
 * Created by Ahmad on 02/02/18.
 * All rights reserved.
 */

public class OccurrenceOperations {

    //=========================================== Read =============================================
    public static int getCountAll(SQLiteDatabase readableDb) {
        Cursor cursor = readableDb.query(Contract.Occurrence.table, null, null, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public static int getCountAllByScheduleId(long scheduleId, SQLiteDatabase readableDb) {
        String selection = Contract.Occurrence.scheduleId + " = " + scheduleId;
        Cursor cursor = readableDb.query(Contract.Occurrence.table, null, selection, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public static Occurrence getOccurrenceById(long id, boolean conversions, SQLiteDatabase readableDb) {
        String selection = Contract.Occurrence.id + " = " + id;
        ArrayList<Occurrence> occurrences = getOccurrences(selection, conversions, readableDb);
        if (occurrences.size() == 0)
            return null;
        else
            return occurrences.get(0);
    }

    public static ArrayList<Occurrence> getOccurrencesAll(boolean conversions, SQLiteDatabase readableDb) {
        return getOccurrences(null, conversions, readableDb);
    }

    public static ArrayList<Occurrence> getOccurrences(String selection, boolean conversions, SQLiteDatabase readableDb) {
        Cursor cursor = readableDb.query(Contract.Occurrence.table, null, selection, null, null, null, null);
        TreeMap<Long, TreeMap<Long, ScheduleConversion>> conversionMap = null;
        if (conversions) {
            conversionMap = retrieveConversionMap(null, readableDb);
        }
        ArrayList<Occurrence> occurrences = retrieveOccurrences(cursor, conversionMap);
        cursor.close();

        return occurrences;
    }

    /**
     * @param conversions
     * @param readableDb
     * @return key is scheduleId and value is the set of occurrences for that schedule
     */
    public static TreeMap<Long, ? extends Collection<Occurrence>> getOccurrencesAllMap(boolean conversions, SQLiteDatabase readableDb) {
        Cursor cursor = readableDb.query(Contract.Occurrence.table, null, null, null, null, null, null);
        TreeMap<Long, TreeMap<Long, ScheduleConversion>> conversionMap = null;
        if (conversions) {
            conversionMap = retrieveConversionMap(null, readableDb);
        }
        ArrayList<Occurrence> occurrences = retrieveOccurrences(cursor, conversionMap);
        cursor.close();

        TreeMap<Long, LinkedList<Occurrence>> occurrenceMap = new TreeMap<>();
        for (Occurrence occurrence : occurrences) {
            LinkedList<Occurrence> scheduleOccurrences = occurrenceMap.get(occurrence.getScheduleId());
            if (scheduleOccurrences == null) {
                scheduleOccurrences = new LinkedList<>();
                occurrenceMap.put(occurrence.getScheduleId(), scheduleOccurrences);
            }
            scheduleOccurrences.add(occurrence);
        }
        return occurrenceMap;
    }

    public static ArrayList<Occurrence> getOccurrencesByScheduleId(long scheduleId, boolean conversions,
                                                                   SQLiteDatabase readableDb) {
        String selection = Contract.Occurrence.scheduleId + " = " + scheduleId;
        Cursor cursor = readableDb.query(Contract.Occurrence.table, null, selection, null, null, null, null);
        TreeMap<Long, TreeMap<Long, ScheduleConversion>> conversionMap = null;
        if (conversions) {
            conversionMap = retrieveConversionMap(null, readableDb);
        }
        ArrayList<Occurrence> occurrences = retrieveOccurrences(cursor, conversionMap);
        cursor.close();
        return occurrences;
    }

    public static ArrayList<Occurrence> retrieveOccurrences(Cursor cursor, @Nullable TreeMap<Long, TreeMap<Long, ScheduleConversion>> conversionMap) {
        int idIndex = cursor.getColumnIndex(Contract.Occurrence.id);
        int numberIndex = cursor.getColumnIndex(Contract.Occurrence.number);
        int plusDaysIndex = cursor.getColumnIndex(Contract.Occurrence.plusDays);
        int scheduleIdIndex = cursor.getColumnIndex(Contract.Occurrence.scheduleId);

        if (idIndex < 0)
            throw new InvalidCursorException();

        ArrayList<Occurrence> occurrences = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            long occurrenceId = cursor.getLong(idIndex);
            Occurrence occurrence = Occurrence.newInstance().setId(occurrenceId).setRealized(true);
            if (numberIndex >= 0 && !cursor.isNull(numberIndex)) {
                occurrence.setNumber(cursor.getInt(numberIndex));
            }
            if (plusDaysIndex >= 0 && !cursor.isNull(plusDaysIndex)) {
                occurrence.setPlusDays(cursor.getInt(plusDaysIndex));
            }
            if (scheduleIdIndex >= 0 && !cursor.isNull(scheduleIdIndex)) {
                occurrence.setScheduleId(cursor.getLong(scheduleIdIndex));
            }
            if (conversionMap != null) {
                occurrence.setConversions(conversionMap.get(occurrenceId));
            }
            occurrence.setInitialized(true);
            occurrences.add(occurrence);
        }
        return occurrences;
    }

    private static TreeMap<Long, TreeMap<Long, ScheduleConversion>> retrieveConversionMap(@Nullable Long forOccurrenceId, SQLiteDatabase readableDb) {
        String selection = null;
        if (forOccurrenceId != null) {
            selection = Contract.ScheduleConversion.fromOccurrenceId + " = " + forOccurrenceId;
        }
        Cursor cursor = readableDb.query(Contract.ScheduleConversion.table, null, selection, null, null, null, null);
        int fromOccurrenceIdIndex = cursor.getColumnIndex(Contract.ScheduleConversion.fromOccurrenceId);
        int toScheduleIdIndex = cursor.getColumnIndex(Contract.ScheduleConversion.toScheduleId);
        int toOccurrenceNumberIndex = cursor.getColumnIndex(Contract.ScheduleConversion.toOccurrenceNumber);

        TreeMap<Long, TreeMap<Long, ScheduleConversion>> conversionMap = new TreeMap<>();
        while (cursor.moveToNext()) {
            long fromOccurrenceId = cursor.getLong(fromOccurrenceIdIndex);
            long toScheduleId = cursor.getLong(toScheduleIdIndex);
            int toOccurrenceNumber = cursor.getInt(toOccurrenceNumberIndex);

            TreeMap<Long, ScheduleConversion> conversions = conversionMap.get(fromOccurrenceId);
            if (conversions == null) {
                conversions = new TreeMap<>();
                conversionMap.put(fromOccurrenceId, conversions);
            }
            ScheduleConversion scheduleConversion = ScheduleConversion.newInstance();
            scheduleConversion.setFromOccurrenceId(fromOccurrenceId);
            scheduleConversion.setToScheduleId(toScheduleId);
            scheduleConversion.setToOccurrenceNumber(toOccurrenceNumber);
            conversions.put(toScheduleId, scheduleConversion);
        }
        cursor.close();
        return conversionMap;
    }

    //========================================== Write =============================================
    private static ContentValues getContentValues(Occurrence occurrence) {
        ContentValues values = new ContentValues();

        values.put(Contract.Occurrence.number, occurrence.getNumber());
        values.put(Contract.Occurrence.plusDays, occurrence.getPlusDays());
        values.put(Contract.Occurrence.scheduleId, occurrence.getScheduleId());

        return values;
    }

    public static long addOccurrence(Occurrence occurrence, SQLiteDatabase writableDb, Context context) {
        occurrence = occurrence.clone();
        ContentValues values = getContentValues(occurrence);
        long id;
        if (occurrence.isRealized()) {
            values.put(Contract.Occurrence.id, occurrence.getId());
            id = writableDb.replace(Contract.Occurrence.table, null, values);
        } else {
            values.put(Contract.Occurrence.id, IdProvider.nextOccurrenceId(context));
            id = writableDb.insert(Contract.Occurrence.table, null, values);
            occurrence.setId(id);
        }
        updateScheduleConversions(occurrence, writableDb);
        return id;
    }

    public static void updateOccurrence(Occurrence occurrence, SQLiteDatabase writableDb) {
        //update Occurrence
        {
            String selection = Contract.Occurrence.id + " = " + occurrence.getId();
            writableDb.update(Contract.Occurrence.table, getContentValues(occurrence), selection, null);
        }
        //update ScheduleConversions
        {
            updateScheduleConversions(occurrence, writableDb);
        }
    }

    private static void updateScheduleConversions(Occurrence occurrence, SQLiteDatabase writableDb) {
        //Delete old ScheduleConversions
        {
            String sql = "DELETE FROM " + Contract.ScheduleConversion.table +
                    " WHERE " + Contract.ScheduleConversion.fromOccurrenceId + " = " + occurrence.getId();
            writableDb.execSQL(sql);
        }
        //Add new ScheduleConversions
        {
            if (occurrence.getConversions() != null) {
                Collection<ScheduleConversion> conversions = occurrence.getConversions();
                for (ScheduleConversion conversion : conversions) {
                    ContentValues values = new ContentValues();
                    values.put(Contract.ScheduleConversion.fromOccurrenceId, occurrence.getId());
                    values.put(Contract.ScheduleConversion.toScheduleId, conversion.getToScheduleId());
                    values.put(Contract.ScheduleConversion.toOccurrenceNumber, conversion.getToOccurrenceNumber());
                    writableDb.insert(Contract.ScheduleConversion.table, null, values);
                }
            }
        }
    }

    public static void deleteOccurrence(Occurrence occurrence, SQLiteDatabase writableDb) {
        //Delete ScheduleConversions
        {
            String sql = "DELETE FROM " + Contract.ScheduleConversion.table +
                    " WHERE " + Contract.ScheduleConversion.fromOccurrenceId + " = " + occurrence.getId();
            writableDb.execSQL(sql);
        }
        //Delete Occurrence
        {
            String where = Contract.Occurrence.id + " = " + occurrence.getId();
            writableDb.delete(Contract.Occurrence.table, where, null);
        }
    }

    public static void deleteOccurrencesForSchedule(long scheduleId, SQLiteDatabase writableDb) {
        //ScheduleConversion
        {
            String sql = "DELETE FROM " + Contract.ScheduleConversion.table +
                    " WHERE " + Contract.ScheduleConversion.fromOccurrenceId +
                    " IN " +
                    "(SELECT " + Contract.Occurrence.id +
                    " FROM " + Contract.Occurrence.table +
                    " WHERE " + Contract.Occurrence.scheduleId + " = " + scheduleId + ")";
            writableDb.execSQL(sql);
        }
        //Occurrence
        {
            String where = Contract.Occurrence.scheduleId + " = " + scheduleId;
            writableDb.delete(Contract.Occurrence.table, where, null);
        }
    }
}
