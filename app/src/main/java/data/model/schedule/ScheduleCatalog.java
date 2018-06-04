package data.model.schedule;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.TreeMap;

import data.database.ScheduleOperations;

/**
 * Created by Ahmad on 01/01/18.
 * All rights reserved.
 */

public class ScheduleCatalog {

    //=========================================== Read =============================================
    public static int getCountAll(SQLiteDatabase readableDb) {
        return ScheduleOperations.getCountAll(readableDb);
    }

    public static Schedule getScheduleById(long id, SQLiteDatabase readableDb) {
        return ScheduleOperations.getScheduleById(id, readableDb);
    }

    public static TreeMap<Long, Schedule> getSchedulesMapWithOccurrences(SQLiteDatabase readableDb) {
        return ScheduleOperations.getSchedulesMapWithOccurrences(readableDb);
    }

    public static ArrayList<Schedule> getSchedules(SQLiteDatabase readableDb) {
        return ScheduleOperations.getSchedules(readableDb);
    }

    public static ArrayList<Schedule> getSchedules(String selection, SQLiteDatabase readableDb) {
        return ScheduleOperations.getSchedules(selection, readableDb);
    }

    public static ArrayList<Schedule> getSchedulesWithOccurrences(SQLiteDatabase readableDb) {
        return ScheduleOperations.getSchedulesWithOccurrences(readableDb);
    }
    public static ArrayList<Schedule> getSchedulesWithOccurrences(String selection, SQLiteDatabase readableDb) {
        return ScheduleOperations.getSchedulesWithOccurrences(selection, readableDb);
    }

    //========================================== Write =============================================
    public static long addSchedule(Schedule schedule, SQLiteDatabase writableDb, Context context) {
        schedule.setId(ScheduleOperations.addSchedule(schedule, writableDb, context));
        data.xml.log.operations.ScheduleOperations.addSchedule(schedule, context);
        return schedule.getId();
    }

    public static int updateSchedulePosition(Schedule schedule, int position, SQLiteDatabase writableDb, Context context) {
        int count = ScheduleOperations.updateSchedulePosition(schedule, position, writableDb);
        data.xml.log.operations.ScheduleOperations.updateSchedulePosition(schedule, position, context);
        return count;
    }

    public static int updateSchedule(Schedule schedule, SQLiteDatabase writableDb, Context context) {
        int count = ScheduleOperations.updateSchedule(schedule, writableDb);
        data.xml.log.operations.ScheduleOperations.updateSchedule(schedule, context);
        return count;
    }

    public static void mergeThenDeleteSchedule(Schedule schedule, Schedule newSchedule, SQLiteDatabase writableDb, Context context) {
        ScheduleOperations.mergeThenDeleteSchedule(schedule, newSchedule, writableDb);
        data.xml.log.operations.ScheduleOperations.mergeThenDeleteSchedule(schedule, newSchedule, context);
    }

    public static void deleteSchedule(Schedule schedule, SQLiteDatabase writableDb, Context context) {
        ScheduleOperations.deleteSchedule(schedule, writableDb);
        data.xml.log.operations.ScheduleOperations.deleteSchedule(schedule, context);
    }
}
