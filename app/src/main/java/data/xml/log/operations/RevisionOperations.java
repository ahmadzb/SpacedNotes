package data.xml.log.operations;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.jdom2.DataConversionException;
import org.jdom2.Element;

import data.model.note.Note;
import data.model.schedule.Schedule;
import data.model.scheduler.RevisionFuture;
import data.model.scheduler.RevisionPast;
import data.xml.log.operator.LogContract;
import data.xml.log.operator.LogOperations;

/**
 * Created by Ahmad on 02/04/18.
 * All rights reserved.
 */

public class RevisionOperations {
    //============================================ Read ============================================
    public static void performOperation(Element element, long time, SQLiteDatabase writableDb, Context context) {
        String name = element.getName();
        try {
            if (name.equals(LogContract.Revision.BatchConvertRevisionFutures.itemName)) {
                performBatchConvertRevisionFutures(element, writableDb);
            } else if (name.equals(LogContract.Revision.AddRevisionFuture.itemName)) {
                performAddRevisionFuture(element, writableDb);
            } else if (name.equals(LogContract.Revision.UpdateRevisionFuture.itemName)) {
                performUpdateRevisionFuture(element, writableDb);
            } else if (name.equals(LogContract.Revision.DeleteRevisionFuture.itemName)) {
                performDeleteRevisionFuture(element, writableDb);
            } else if (name.equals(LogContract.Revision.AddRevisionPast.itemName)) {
                performAddRevisionPast(element, writableDb, context);
            } else if (name.equals(LogContract.Revision.DeleteRevisionPast.itemName)) {
                performDeleteRevisionPast(element, writableDb);
            }
        } catch (DataConversionException e) {
            e.printStackTrace();
        }
    }

    private static void performBatchConvertRevisionFutures(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        Schedule oldSchedule = Schedule.newInstance();
        oldSchedule.setId(element.getAttribute(
                LogContract.Revision.BatchConvertRevisionFutures.scheduleId).getLongValue());
        Schedule newSchedule = Schedule.newInstance();
        newSchedule.setId(element.getAttribute(
                LogContract.Revision.BatchConvertRevisionFutures.newScheduleId).getLongValue());
        data.database.RevisionOperations.batchConvertRevisionFutures(oldSchedule, newSchedule, writableDb);
    }

    private static void performAddRevisionFuture(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        RevisionFuture revisionFuture = RevisionFuture.newInstance();
        revisionFuture.setNoteId(element.getAttribute(
                LogContract.Revision.AddRevisionFuture.noteId).getLongValue());
        revisionFuture.setDueDate(element.getAttribute(
                LogContract.Revision.AddRevisionFuture.dueDate).getIntValue());
        revisionFuture.setOccurrenceNumber(element.getAttribute(
                LogContract.Revision.AddRevisionFuture.occurrenceNumber).getIntValue());
        revisionFuture.setScheduleId(element.getAttribute(
                LogContract.Revision.AddRevisionFuture.scheduleId).getLongValue());
        revisionFuture.setRealized(true);
        revisionFuture.setInitialized(true);
        data.database.RevisionOperations.addRevisionFuture(revisionFuture, writableDb);
    }

    private static void performUpdateRevisionFuture(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        RevisionFuture revisionFuture = RevisionFuture.newInstance();
        revisionFuture.setNoteId(element.getAttribute(
                LogContract.Revision.UpdateRevisionFuture.noteId).getLongValue());
        revisionFuture.setDueDate(element.getAttribute(
                LogContract.Revision.UpdateRevisionFuture.dueDate).getIntValue());
        revisionFuture.setOccurrenceNumber(element.getAttribute(
                LogContract.Revision.UpdateRevisionFuture.occurrenceNumber).getIntValue());
        revisionFuture.setScheduleId(element.getAttribute(
                LogContract.Revision.UpdateRevisionFuture.scheduleId).getLongValue());
        revisionFuture.setRealized(true);
        revisionFuture.setInitialized(true);
        data.database.RevisionOperations.updateRevisionFuture(revisionFuture, writableDb);
    }

    private static void performDeleteRevisionFuture(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        Note note = Note.newInstance();
        note.setId(element.getAttribute(
                LogContract.Revision.DeleteRevisionFuture.noteId).getLongValue());
        data.database.RevisionOperations.deleteRevisionFuture(note, writableDb);
    }


    private static void performAddRevisionPast(Element element, SQLiteDatabase writableDb, Context context) throws DataConversionException {
        RevisionPast revisionPast = RevisionPast.newInstance();
        revisionPast.setDate(element.getAttribute(
                LogContract.Revision.AddRevisionPast.date).getIntValue());
        revisionPast.setNoteId(element.getAttribute(
                LogContract.Revision.AddRevisionPast.noteId).getLongValue());
        revisionPast.setRealized(true);
        revisionPast.setInitialized(true);
        data.database.RevisionOperations.addRevisionPast(revisionPast, writableDb, context);
    }

    private static void performDeleteRevisionPast(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        RevisionPast revisionPast = RevisionPast.newInstance();
        revisionPast.setDate(element.getAttribute(
                LogContract.Revision.DeleteRevisionPast.date).getIntValue());
        revisionPast.setNoteId(element.getAttribute(
                LogContract.Revision.DeleteRevisionPast.noteId).getLongValue());
        data.database.RevisionOperations.deleteRevisionPast(revisionPast, writableDb);
    }


    //=========================================== Write ============================================
    public static void batchConvertRevisionFutures(Schedule oldSchedule, Schedule newSchedule, Context context) {
        Element element = new Element(LogContract.Revision.BatchConvertRevisionFutures.itemName);
        element.setAttribute(LogContract.Revision.BatchConvertRevisionFutures.newScheduleId,
                String.valueOf(newSchedule.getId()));
        element.setAttribute(LogContract.Revision.BatchConvertRevisionFutures.scheduleId,
                String.valueOf(oldSchedule.getId()));
        LogOperations.addRevisionOperation(element, context);
    }

    public static void addRevisionFuture(RevisionFuture revisionFuture, Context context) {
        Element element = new Element(LogContract.Revision.AddRevisionFuture.itemName);
        element.setAttribute(LogContract.Revision.AddRevisionFuture.noteId,
                String.valueOf(revisionFuture.getNoteId()));
        element.setAttribute(LogContract.Revision.AddRevisionFuture.dueDate,
                String.valueOf(revisionFuture.getDueDate()));
        element.setAttribute(LogContract.Revision.AddRevisionFuture.occurrenceNumber,
                String.valueOf(revisionFuture.getOccurrenceNumber()));
        element.setAttribute(LogContract.Revision.AddRevisionFuture.scheduleId,
                String.valueOf(revisionFuture.getScheduleId()));
        LogOperations.addRevisionOperation(element, context);
    }

    public static void updateRevisionFuture(RevisionFuture revisionFuture, Context context) {
        Element element = new Element(LogContract.Revision.UpdateRevisionFuture.itemName);
        element.setAttribute(LogContract.Revision.UpdateRevisionFuture.noteId,
                String.valueOf(revisionFuture.getNoteId()));
        element.setAttribute(LogContract.Revision.UpdateRevisionFuture.dueDate,
                String.valueOf(revisionFuture.getDueDate()));
        element.setAttribute(LogContract.Revision.UpdateRevisionFuture.occurrenceNumber,
                String.valueOf(revisionFuture.getOccurrenceNumber()));
        element.setAttribute(LogContract.Revision.UpdateRevisionFuture.scheduleId,
                String.valueOf(revisionFuture.getScheduleId()));
        LogOperations.addRevisionOperation(element, context);
    }

    public static void deleteRevisionFuture(Note note, Context context) {
        Element element = new Element(LogContract.Revision.DeleteRevisionFuture.itemName);
        element.setAttribute(LogContract.Revision.DeleteRevisionFuture.noteId,
                String.valueOf(note.getId()));
        LogOperations.addRevisionOperation(element, context);
    }


    public static void addRevisionPast(RevisionPast revisionPast, Context context) {
        Element element = new Element(LogContract.Revision.AddRevisionPast.itemName);
        element.setAttribute(LogContract.Revision.AddRevisionPast.noteId,
                String.valueOf(revisionPast.getNoteId()));
        element.setAttribute(LogContract.Revision.AddRevisionPast.date,
                String.valueOf(revisionPast.getDate()));
        LogOperations.addRevisionOperation(element, context);
    }

    public static void deleteRevisionPast(RevisionPast revisionPast, Context context) {
        Element element = new Element(LogContract.Revision.DeleteRevisionPast.itemName);
        element.setAttribute(LogContract.Revision.DeleteRevisionPast.noteId,
                String.valueOf(revisionPast.getNoteId()));
        element.setAttribute(LogContract.Revision.DeleteRevisionPast.date,
                String.valueOf(revisionPast.getDate()));
        LogOperations.addRevisionOperation(element, context);
    }
}
