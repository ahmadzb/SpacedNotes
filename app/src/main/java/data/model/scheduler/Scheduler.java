package data.model.scheduler;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.joda.time.LocalDate;

import util.datetime.primitive.Representation;

import data.model.note.Note;
import data.model.schedule.Occurrence;
import data.model.schedule.Schedule;
import data.model.schedule.ScheduleCatalog;
import data.model.schedule.ScheduleConversion;

/**
 * Created by Ahmad on 01/02/18.
 * All rights reserved.
 */

public class Scheduler {
    public static void carryOnWithSchedule(Note note, Schedule schedule, SQLiteDatabase writableDb, Context context) {
        if (RevisionCatalog.getRevisionFutureForNote(note, writableDb) != null) {
            throw new RuntimeException("current RevisionFuture is not null, use changeSchedule() instead");
        }
        RevisionPast lastRevisionPast = RevisionCatalog.getLastRevisionPastForNote(note, writableDb);
        Occurrence newOccurrence = schedule.getFirstOccurrence();
        if (newOccurrence != null) {
            RevisionFuture calculatedRevision = RevisionFuture.newInstance();
            calculatedRevision.setNoteId(note.getId());
            calculatedRevision.setScheduleId(schedule.getId());
            LocalDate lastRevisionDate = Representation.toLocalDate(lastRevisionPast.getDate());
            LocalDate nextRevisionDate = lastRevisionDate.plusDays(newOccurrence.getPlusDays());
            calculatedRevision.setDueDate(Representation.fromLocalDate(nextRevisionDate));
            calculatedRevision.setOccurrenceNumber(newOccurrence.getNumber());
            calculatedRevision.setInitialized(true);
            RevisionCatalog.addRevisionFuture(calculatedRevision, writableDb, context);
        }
    }

    public static void startSchedule(Note note, Schedule schedule, SQLiteDatabase writableDb, Context context) {
        RevisionFuture currentRevisionFuture = RevisionCatalog.getRevisionFutureForNote(note, writableDb);
        if (currentRevisionFuture != null) {
            RevisionCatalog.deleteRevisionFuture(note, writableDb, context);
        }

        RevisionPast newRevisionPast = RevisionCatalog.getLastRevisionPastForNote(note, writableDb);
        if (newRevisionPast == null ||
                Representation.toLocalDate(newRevisionPast.getDate()).isBefore(LocalDate.now())) {
            newRevisionPast = RevisionPast.newInstance()
                    .setDate(Representation.fromLocalDate(LocalDate.now()))
                    .setNoteId(note.getId())
                    .setInitialized(true);
            RevisionCatalog.addRevisionPast(newRevisionPast, writableDb, context);
        }

        Occurrence newOccurrence = schedule.getFirstOccurrence();
        if (newOccurrence != null) {
            RevisionFuture calculatedRevision = RevisionFuture.newInstance();
            calculatedRevision.setNoteId(note.getId());
            calculatedRevision.setScheduleId(schedule.getId());
            LocalDate lastRevisionDate = Representation.toLocalDate(newRevisionPast.getDate());
            LocalDate nextRevisionDate = lastRevisionDate.plusDays(newOccurrence.getPlusDays());
            calculatedRevision.setDueDate(Representation.fromLocalDate(nextRevisionDate));
            calculatedRevision.setOccurrenceNumber(newOccurrence.getNumber());
            calculatedRevision.setInitialized(true);
            RevisionCatalog.addRevisionFuture(calculatedRevision, writableDb, context);
        }
    }

    public static void changeSchedule(Note note, Schedule newSchedule, SQLiteDatabase writableDb, Context context) {
        RevisionFuture revisionFuture = forChangeSchedule(note, newSchedule, writableDb);
        RevisionCatalog.updateRevisionFuture(revisionFuture, writableDb, context);
    }

    public static void changeOccurrence(Note note, int newOccurrenceNumber, SQLiteDatabase writableDb, Context context) {
        RevisionFuture revisionFuture = forChangeOccurrence(note, newOccurrenceNumber, writableDb);
        RevisionCatalog.updateRevisionFuture(revisionFuture, writableDb, context);
    }

    public static void submitCurrentOccurrence(Note note, SQLiteDatabase writableDb, Context context) {
        RevisionFuture currentRevisionFuture = RevisionCatalog.getRevisionFutureForNote(note, writableDb);
        if (currentRevisionFuture == null) {
            throw new RuntimeException("Given note does not have revisionFuture");
        }
        LocalDate currentDueDate = Representation.toLocalDate(currentRevisionFuture.getDueDate());
        LocalDate now = LocalDate.now();
        if (currentDueDate.isAfter(now)) {
            throw new RuntimeException("Due date is not reached, cannot insert revisionPast");
        }

        RevisionPast revisionPast = RevisionPast.newInstance()
                .setDate(Representation.fromLocalDate(now))
                .setNoteId(note.getId())
                .setInitialized(true);
        RevisionCatalog.addRevisionPast(revisionPast, writableDb, context);
        RevisionFuture newRevisionFuture = forChangeOccurrence(note,
                currentRevisionFuture.getOccurrenceNumber() + 1, writableDb);
        if (newRevisionFuture != null) {
            RevisionCatalog.updateRevisionFuture(newRevisionFuture, writableDb, context);
        } else {
            RevisionCatalog.deleteRevisionFuture(note, writableDb, context);
        }
    }

    public static boolean hasNextOccurrence(Note note, SQLiteDatabase readableDb, Context context) {
        RevisionFuture currentRevisionFuture = RevisionCatalog.getRevisionFutureForNote(note, readableDb);
        if (currentRevisionFuture == null)
            return false;
        Schedule schedule = ScheduleCatalog.getScheduleById(currentRevisionFuture.getScheduleId(), readableDb);
        Occurrence nextOccurrence = schedule.getOccurrenceByNumber(currentRevisionFuture.getOccurrenceNumber() + 1);
        return nextOccurrence != null;
    }

    public static void reappointFutureRevision(Note note, SQLiteDatabase writableDb, Context context) {
        RevisionFuture currentRevisionFuture = RevisionCatalog.getRevisionFutureForNote(note, writableDb);
        if (currentRevisionFuture != null) {
            Schedule schedule = ScheduleCatalog.getScheduleById(currentRevisionFuture.getScheduleId(), writableDb);
            Occurrence currentOccurrence = schedule.getOccurrenceByNumber(currentRevisionFuture.getOccurrenceNumber());
            RevisionPast lastRevisionPast = RevisionCatalog.getLastRevisionPastForNote(note, writableDb);
            if (currentOccurrence == null || lastRevisionPast == null) {
                RevisionCatalog.deleteRevisionFuture(note, writableDb, context);
            } else {
                LocalDate lastDueDate = Representation.toLocalDate(lastRevisionPast.getDate());
                LocalDate newCurrentDueDate = lastDueDate.plusDays(currentOccurrence.getPlusDays());
                currentRevisionFuture.setDueDate(Representation.fromLocalDate(newCurrentDueDate));
                RevisionCatalog.updateRevisionFuture(currentRevisionFuture, writableDb, context);
            }
        }
    }

    private static RevisionFuture forChangeSchedule(Note note, Schedule newSchedule, SQLiteDatabase readableDb) {
        if (note == null || !note.isRealized() || newSchedule == null || !newSchedule.isRealized() ||
                !newSchedule.isInitialized()) {
            throw new RuntimeException("some of parameters are not realized/initialized");
        }

        RevisionFuture oldRevisionFuture = RevisionCatalog.getRevisionFutureForNote(note, readableDb);
        RevisionPast lastRevisionPast = RevisionCatalog.getLastRevisionPastForNote(note, readableDb);
        if (oldRevisionFuture == null || lastRevisionPast == null) {
            throw new RuntimeException("there exists no revisionFuture to change or revisionPast to pivot");
        }
        Schedule oldSchedule = ScheduleCatalog.getScheduleById(oldRevisionFuture.getScheduleId(), readableDb);


        RevisionFuture calculatedRevision = RevisionFuture.newInstance().setRealized(true);
        calculatedRevision.setNoteId(note.getId());
        calculatedRevision.setScheduleId(newSchedule.getId());
        Occurrence newOccurrence = getConvertedOccurrence(
                oldRevisionFuture.getOccurrenceNumber(), oldSchedule, newSchedule);
        LocalDate lastRevisionDate = Representation.toLocalDate(lastRevisionPast.getDate());
        LocalDate nextRevisionDate = lastRevisionDate.plusDays(newOccurrence.getPlusDays());
        calculatedRevision.setDueDate(Representation.fromLocalDate(nextRevisionDate));
        calculatedRevision.setOccurrenceNumber(newOccurrence.getNumber());
        calculatedRevision.setInitialized(true);

        return calculatedRevision;
    }

    private static RevisionFuture forChangeOccurrence(Note note, int newOccurrenceNumber, SQLiteDatabase readableDb) {
        if (note == null || !note.isRealized()) {
            throw new RuntimeException("note is not realized");
        }

        RevisionFuture oldRevisionFuture = RevisionCatalog.getRevisionFutureForNote(note, readableDb);
        RevisionPast lastRevisionPast = RevisionCatalog.getLastRevisionPastForNote(note, readableDb);
        if (oldRevisionFuture == null || lastRevisionPast == null) {
            throw new RuntimeException("there exists no revisionFuture to change or revisionPast to pivot");
        }
        Schedule schedule = ScheduleCatalog.getScheduleById(oldRevisionFuture.getScheduleId(), readableDb);

        RevisionFuture calculatedRevision = RevisionFuture.newInstance().setRealized(true);

        calculatedRevision.setNoteId(note.getId());
        calculatedRevision.setScheduleId(schedule.getId());

        Occurrence newOccurrence = schedule.getOccurrenceByNumber(newOccurrenceNumber);
        if (newOccurrence != null) {
            LocalDate lastRevisionDate = Representation.toLocalDate(lastRevisionPast.getDate());
            LocalDate nextRevisionDate = lastRevisionDate.plusDays(newOccurrence.getPlusDays());
            calculatedRevision.setDueDate(Representation.fromLocalDate(nextRevisionDate));
            calculatedRevision.setOccurrenceNumber(newOccurrence.getNumber());
            calculatedRevision.setInitialized(true);
            return calculatedRevision;
        } else {
            return null;
        }
    }

    public static Occurrence getConvertedOccurrence(int oldNumber, Schedule oldSchedule, Schedule newSchedule) {
        if (!newSchedule.isRealized() || !newSchedule.isInitialized() || !oldSchedule.isRealized() ||
                !oldSchedule.isInitialized()) {
            throw new RuntimeException("Some items aren't realized or initialized");
        }
        int newNumber;
        if (oldSchedule.getId() == newSchedule.getId()) {
            newNumber = Math.min(oldNumber, newSchedule.getOccurrencesCount() - 1);
        } else {
            int number = Math.min(oldNumber, oldSchedule.getOccurrencesCount() - 1);
            Occurrence oldOccurrence = oldSchedule.getOccurrenceByNumber(number);
            ScheduleConversion conversion = oldOccurrence.getConversionForSchedule(newSchedule);
            if (conversion == null) {
                newNumber = -1;
            } else {
                newNumber = Math.min(conversion.getToOccurrenceNumber(), newSchedule.getOccurrencesCount() - 1);
            }
        }
        if (newNumber <= 0)
            return newSchedule.getFirstOccurrence();
        return newSchedule.getOccurrenceByNumber(newNumber);
    }
}
