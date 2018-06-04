package data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import data.model.schedule.Occurrence;
import data.model.schedule.Schedule;
import data.xml.port.IdProvider;
import exceptions.InvalidCursorCountException;
import exceptions.InvalidCursorException;
import exceptions.NotRealizedException;
import exceptions.ObjectNotCompleteException;

/**
 * Created by Ahmad on 02/02/18.
 * All rights reserved.
 */

public class ScheduleOperations {

    //=========================================== Read =============================================
    public static int getCountAll(SQLiteDatabase readableDb) {
        Cursor cursor = readableDb.query(Contract.Schedule.table, null, null, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public static Schedule getScheduleById(long id, SQLiteDatabase readableDb) {
        String selection = Contract.Schedule.id + " = " + id;
        Cursor cursor = readableDb.query(Contract.Schedule.table, null, selection, null, null, null, null);
        Schedule schedule = retrieveSchedule(
                cursor,
                OccurrenceOperations.getOccurrencesByScheduleId(id, true, readableDb));
        cursor.close();
        return schedule;
    }

    public static TreeMap<Long, Schedule> getSchedulesMapWithOccurrences(SQLiteDatabase readableDb) {
        ArrayList<Schedule> schedules = getSchedulesWithOccurrences(null, readableDb);
        TreeMap<Long, Schedule> scheduleTreeMap = new TreeMap<>();
        for (Schedule schedule : schedules) {
            scheduleTreeMap.put(schedule.getId(), schedule);
        }
        return scheduleTreeMap;
    }

    public static ArrayList<Schedule> getSchedules(SQLiteDatabase readableDb) {
        return getSchedules(null, readableDb);
    }

    public static ArrayList<Schedule> getSchedules(String selection, SQLiteDatabase readableDb) {
        Cursor cursor = readableDb.query(Contract.Schedule.table, null, selection, null, null, null, null);
        ArrayList<Schedule> schedules = retrieveSchedules(cursor, null);
        cursor.close();
        return schedules;
    }

    public static ArrayList<Schedule> getSchedulesWithOccurrences(SQLiteDatabase readableDb) {
        return getSchedulesWithOccurrences(null, readableDb);
    }
    public static ArrayList<Schedule> getSchedulesWithOccurrences(String selection, SQLiteDatabase readableDb) {
        Cursor cursor = readableDb.query(Contract.Schedule.table, null, selection, null, null, null, null);
        TreeMap<Long, ? extends Collection<Occurrence>> occurrences =
                OccurrenceOperations.getOccurrencesAllMap(false, readableDb);
        ArrayList<Schedule> schedules = retrieveSchedules(cursor, occurrences);
        cursor.close();
        return schedules;
    }

    public static Schedule retrieveSchedule(Cursor cursor, @Nullable Collection<Occurrence> occurrences) {
        TreeMap<Long, Collection<Occurrence>> occurrencesMap = null;
        if (occurrences != null && occurrences.size() > 0) {
            long scheduleId = occurrences.iterator().next().getScheduleId();
            occurrencesMap = new TreeMap<>();
            occurrencesMap.put(scheduleId, occurrences);
        }
        ArrayList<Schedule> schedules = retrieveSchedules(cursor, occurrencesMap);
        if (schedules.size() > 1)
            throw new InvalidCursorCountException(1, schedules.size());
        else if (schedules.size() == 1) {
            return schedules.get(0);
        } else
            return null;
    }

    /**
     * @param cursor         a cursor of Schedule entries
     * @param occurrencesMap key is scheduleId and value is the set of occurrences for that schedule
     * @return
     */
    public static ArrayList<Schedule> retrieveSchedules(
            Cursor cursor, @Nullable TreeMap<Long, ? extends Collection<Occurrence>> occurrencesMap) {
        int idIndex = cursor.getColumnIndex(Contract.Schedule.id);
        int titleIndex = cursor.getColumnIndex(Contract.Schedule.title);
        int colorIndex = cursor.getColumnIndex(Contract.Schedule.color);
        int positionIndex = cursor.getColumnIndex(Contract.Schedule.position);

        if (idIndex < 0)
            throw new InvalidCursorException();

        ArrayList<Schedule> schedules = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            long scheduleId = cursor.getLong(idIndex);
            Schedule schedule = Schedule.newInstance().setId(scheduleId).setRealized(true);
            if (titleIndex >= 0 && !cursor.isNull(titleIndex)) {
                schedule.setTitle(cursor.getString(titleIndex));
            }
            if (colorIndex >= 0 && !cursor.isNull(colorIndex)) {
                schedule.setColor(cursor.getInt(colorIndex));
            }
            if (positionIndex >= 0 && !cursor.isNull(positionIndex)) {
                schedule.setPosition(cursor.getInt(positionIndex));
            }
            if (occurrencesMap != null) {
                schedule.setOccurrenceSet(
                        Schedule.OccurrenceSet.newInstance(occurrencesMap.get(scheduleId), scheduleId));
            }
            schedule.setInitialized(true);
            schedules.add(schedule);
        }
        return schedules;
    }

    //========================================== Write =============================================
    private static ContentValues getContentValues(Schedule schedule) {
        ContentValues values = new ContentValues();

        if (schedule.getTitle() != null) {
            values.put(Contract.Schedule.title, schedule.getTitle());
        } else {
            values.putNull(Contract.Schedule.title);
        }
        if (schedule.getColor() != null) {
            values.put(Contract.Schedule.color, schedule.getColor());
        } else {
            values.putNull(Contract.Schedule.color);
        }
        values.put(Contract.Schedule.position, schedule.getPosition());

        return values;
    }

    public static long addSchedule(Schedule schedule, SQLiteDatabase writableDb, Context context) {
        if (!schedule.isInitialized())
            throw new RuntimeException("schedule not initialized");
        if (schedule.getTitle() == null)
            throw new ObjectNotCompleteException();
        ContentValues values = getContentValues(schedule);
        long id;
        if (schedule.isRealized()) {
            values.put(Contract.Schedule.id, schedule.getId());
            id = writableDb.replace(Contract.Schedule.table, null, values);
        } else {
            values.put(Contract.Schedule.id, IdProvider.nextScheduleId(context));
            id = writableDb.insert(Contract.Schedule.table, null, values);
        }
        return id;
    }

    public static int updateSchedulePosition(Schedule schedule, int position, SQLiteDatabase writableDb) {
        ContentValues values = new ContentValues();
        values.put(Contract.Schedule.position, position);
        String selection = Contract.Schedule.id + " = " + schedule.getId();
        int count = writableDb.update(Contract.Schedule.table, values, selection, null);
        return count;
    }

    public static int updateSchedule(Schedule schedule, SQLiteDatabase writableDb) {
        if (!schedule.isRealized())
            throw new NotRealizedException();
        if (!schedule.isInitialized())
            throw new RuntimeException("schedule not initialized");
        if (schedule.getTitle() == null)
            throw new ObjectNotCompleteException();
        String where = Contract.Schedule.id + " = " + schedule.getId();
        int count = writableDb.update(Contract.Schedule.table, getContentValues(schedule), where, null);
        return count;
    }

    public static void mergeThenDeleteSchedule(Schedule schedule, Schedule newSchedule, SQLiteDatabase writableDb) {
        if (!schedule.isRealized() || !schedule.isInitialized() || !newSchedule.isRealized()
                || !newSchedule.isInitialized())
            throw new NotRealizedException();
        //Convert
        {
            RevisionOperations.batchConvertRevisionFutures(schedule, newSchedule, writableDb);
        }
        //Delete
        {
            deleteSchedule(schedule, writableDb);
        }
    }

    public static void deleteSchedule(Schedule schedule, SQLiteDatabase writableDb) {
        if (RevisionOperations.hasFutureRevision(schedule, writableDb)) {
            throw new RuntimeException("schedule has related future revisions, cannot delete");
        }
        //Occurrence
        {
            OccurrenceOperations.deleteOccurrencesForSchedule(schedule.getId(), writableDb);
        }
        //Schedule
        {
            String where = Contract.Schedule.id + " = " + schedule.getId();
            writableDb.delete(Contract.Schedule.table, where, null);
        }
    }
}
