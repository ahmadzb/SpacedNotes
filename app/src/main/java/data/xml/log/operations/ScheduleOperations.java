package data.xml.log.operations;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;

import data.model.schedule.Schedule;
import data.xml.log.operator.LogContract;
import data.xml.log.operator.LogOperations;

/**
 * Created by Ahmad on 02/02/18.
 * All rights reserved.
 */

public class ScheduleOperations {
    //============================================ Read ============================================
    public static void performOperation(Element element, long time, SQLiteDatabase writableDb, Context context) {
        String name = element.getName();
        try {
            if (name.equals(LogContract.Schedule.Add.itemName)) {
                performAddSchedule(element, writableDb, context);
            } else if (name.equals(LogContract.Schedule.UpdatePosition.itemName)) {
                performUpdateSchedulePosition(element, writableDb);
            } else if (name.equals(LogContract.Schedule.Update.itemName)) {
                performUpdateSchedule(element, writableDb);
            } else if (name.equals(LogContract.Schedule.MergeThenDelete.itemName)) {
                performMergeThenDeleteSchedule(element, writableDb);
            } else if (name.equals(LogContract.Schedule.Delete.itemName)) {
                performDeleteSchedule(element, writableDb);
            }
        } catch (DataConversionException e) {
            e.printStackTrace();
        }
    }

    private static void performAddSchedule(Element element, SQLiteDatabase writableDb, Context context) throws DataConversionException {
        Schedule schedule = Schedule.newInstance();
        Attribute attr;
        schedule.setId(element.getAttribute(LogContract.Schedule.Add.id).getLongValue());
        if ((attr = element.getAttribute(LogContract.Schedule.Add.title)) != null) {
            schedule.setTitle(attr.getValue());
        }
        if ((attr = element.getAttribute(LogContract.Schedule.Add.color)) != null) {
            schedule.setColor(attr.getIntValue());
        }
        schedule.setPosition(element.getAttribute(LogContract.Schedule.Add.position).getIntValue());
        schedule.setRealized(true);
        schedule.setInitialized(true);
        data.database.ScheduleOperations.addSchedule(schedule, writableDb, context);
    }

    private static void performUpdateSchedulePosition(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        Schedule schedule = Schedule.newInstance();
        schedule.setId(element.getAttribute(LogContract.Schedule.UpdatePosition.id).getLongValue());
        schedule.setPosition(element.getAttribute(LogContract.Schedule.UpdatePosition.position).getIntValue());
        schedule.setRealized(true);
        data.database.ScheduleOperations.updateSchedulePosition(schedule, schedule.getPosition(), writableDb);
    }

    private static void performUpdateSchedule(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        Schedule schedule = Schedule.newInstance();
        Attribute attr;
        schedule.setId(element.getAttribute(LogContract.Schedule.Update.id).getLongValue());
        if ((attr = element.getAttribute(LogContract.Schedule.Update.title)) != null) {
            schedule.setTitle(attr.getValue());
        }
        if ((attr = element.getAttribute(LogContract.Schedule.Update.color)) != null) {
            schedule.setColor(attr.getIntValue());
        }
        schedule.setPosition(element.getAttribute(LogContract.Schedule.Update.position).getIntValue());
        schedule.setRealized(true);
        schedule.setInitialized(true);
        data.database.ScheduleOperations.updateSchedule(schedule, writableDb);
    }

    private static void performMergeThenDeleteSchedule(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        Schedule oldSchedule = Schedule.newInstance();
        oldSchedule.setId(element.getAttribute(
                LogContract.Schedule.MergeThenDelete.scheduleId).getLongValue());
        Schedule newSchedule = Schedule.newInstance();
        newSchedule.setId(element.getAttribute(
                LogContract.Schedule.MergeThenDelete.newScheduleId).getLongValue());
        data.database.ScheduleOperations.mergeThenDeleteSchedule(oldSchedule, newSchedule, writableDb);
    }

    private static void performDeleteSchedule(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        Schedule schedule = Schedule.newInstance();
        schedule.setId(element.getAttribute(LogContract.Schedule.Delete.id).getLongValue());
        data.database.ScheduleOperations.deleteSchedule(schedule, writableDb);
    }

    //=========================================== Write ============================================
    public static void addSchedule(Schedule schedule, Context context) {
        Element element = new Element(LogContract.Schedule.Add.itemName);
        element.setAttribute(LogContract.Schedule.Add.id, String.valueOf(schedule.getId()));
        if (schedule.getTitle() != null) {
            element.setAttribute(LogContract.Schedule.Add.title, schedule.getTitle());
        }
        if (schedule.getColor() != null) {
            element.setAttribute(LogContract.Schedule.Add.color, String.valueOf(schedule.getColor()));
        }
        element.setAttribute(LogContract.Schedule.Add.position, String.valueOf(schedule.getPosition()));
        LogOperations.addScheduleOperation(element, context);
    }

    public static void updateSchedulePosition(Schedule schedule, int position, Context context) {
        Element element = new Element(LogContract.Schedule.UpdatePosition.itemName);
        element.setAttribute(LogContract.Schedule.UpdatePosition.id, String.valueOf(schedule.getId()));
        element.setAttribute(LogContract.Schedule.UpdatePosition.position, String.valueOf(position));
        LogOperations.addScheduleOperation(element, context);
    }

    public static void updateSchedule(Schedule schedule, Context context) {
        Element element = new Element(LogContract.Schedule.Update.itemName);
        element.setAttribute(LogContract.Schedule.Update.id, String.valueOf(schedule.getId()));
        if (schedule.getTitle() != null) {
            element.setAttribute(LogContract.Schedule.Update.title, schedule.getTitle());
        }
        if (schedule.getColor() != null) {
            element.setAttribute(LogContract.Schedule.Update.color, String.valueOf(schedule.getColor()));
        }
        element.setAttribute(LogContract.Schedule.Update.position, String.valueOf(schedule.getPosition()));
        LogOperations.addScheduleOperation(element, context);
    }

    public static void mergeThenDeleteSchedule(Schedule schedule, Schedule newSchedule, Context context) {
        Element element = new Element(LogContract.Schedule.MergeThenDelete.itemName);
        element.setAttribute(LogContract.Schedule.MergeThenDelete.scheduleId, String.valueOf(schedule.getId()));
        element.setAttribute(LogContract.Schedule.MergeThenDelete.newScheduleId, String.valueOf(newSchedule.getId()));
        LogOperations.addScheduleOperation(element, context);
    }

    public static void deleteSchedule(Schedule schedule, Context context) {
        Element element = new Element(LogContract.Schedule.Delete.itemName);
        element.setAttribute(LogContract.Schedule.Delete.id, String.valueOf(schedule.getId()));
        LogOperations.addScheduleOperation(element, context);
    }
}