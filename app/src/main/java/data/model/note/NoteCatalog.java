package data.model.note;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.TreeMap;

import javax.annotation.Nullable;

import data.database.NoteOperations;
import data.model.existence.Existence;
import data.model.existence.ExistenceCatalog;
import data.model.label.LabelList;
import data.model.profiles.ProfileCatalog;

/**
 * Created by Ahmad on 01/09/18.
 * All rights reserved.
 */

public class NoteCatalog {
    //=========================================== Read =============================================
    public static Note getNoteById(long id, SQLiteDatabase readableDb) {
        return NoteOperations.getNoteById(id, readableDb);
    }

    public static ArrayList<Note> getNotesNotDeleted(SQLiteDatabase readableDb) {
        return NoteOperations.getNotesNotDeleted(readableDb);
    }

    public static ArrayList<Note> getNotesDeleted(SQLiteDatabase readableDb) {
        return NoteOperations.getNotesDeleted(readableDb);
    }

    public static ArrayList<Note> getNotesByRevisionPastDate(LocalDate date, SQLiteDatabase readableDb) {
        return NoteOperations.getNotesByRevisionPastDate(date, readableDb);
    }

    public static ArrayList<Note> getNotesByRevisionFutureDate(LocalDate date, SQLiteDatabase readableDb,
                                                               boolean withRevisionFuture) {
        return NoteOperations.getNotesByRevisionFutureDate(date, readableDb, withRevisionFuture);
    }

    public static ArrayList<Note> getNotesByRevisionFutureRange(
            @Nullable LocalDate from, @Nullable LocalDate to, SQLiteDatabase readableDb, boolean withRevisionFuture) {
        return NoteOperations.getNotesByRevisionFutureRange(from, to, readableDb, withRevisionFuture);
    }

    public static ArrayList<Note> getNotesByLabel(long labelId, SQLiteDatabase readableDb, boolean withRevisionFuture) {
        return NoteOperations.getNotesByLabel(labelId, readableDb, withRevisionFuture);
    }

    public static TreeMap<Long, Integer> getNotesCountByLabelMap(SQLiteDatabase readableDb) {
        return NoteOperations.getNotesCountByLabelMap(readableDb);
    }

    public static TreeMap<Long, Integer> getNotesCountByLabelMap(LabelList labelList, SQLiteDatabase readableDb) {
        return NoteOperations.getNotesCountByLabelMap(labelList, readableDb);
    }

    public static ArrayList<Note> getNotes(String selection, SQLiteDatabase readableDb) {
        return NoteOperations.getNotes(selection, readableDb);
    }

    public static boolean hasRelatedNotes(data.model.type.Type type, SQLiteDatabase readableDb) {
        return NoteOperations.hasRelatedNotes(type, readableDb);
    }

    //========================================== Write =============================================
    public static long addNote(Note note, SQLiteDatabase writableDb, Context context) {
        note.setId(NoteOperations.addNote(note, writableDb, context));
        data.xml.log.operations.NoteOperations.addNote(note, context);
        return note.getId();
    }

    public static int updateNote(Note note, SQLiteDatabase writableDb, Context context) {
        int count = NoteOperations.updateNote(note, writableDb);
        data.xml.log.operations.NoteOperations.updateNote(note, context);
        return count;
    }

    public static int markAsDeleted(Note note, SQLiteDatabase writableDb, Context context) {
        int count = NoteOperations.markAsDeleted(note, writableDb);
        data.xml.log.operations.NoteOperations.markAsDeleted(note, context);
        return count;
    }

    public static int markAsNotDeleted(Note note, SQLiteDatabase writableDb, Context context) {
        int count = NoteOperations.markAsNotDeleted(note, writableDb);
        data.xml.log.operations.NoteOperations.markAsNotDeleted(note, context);
        return count;
    }

    public static int deleteNote(Note note, SQLiteDatabase writableDb, SQLiteDatabase fileWritableDb, Context context) {
        int count = NoteOperations.deleteNoteWithRelatedContent(note, writableDb, fileWritableDb,
                ProfileCatalog.getCurrentProfile(context).getId());
        data.xml.log.operations.NoteOperations.deleteNote(note, context);
        return count;
    }

    //========================================= Labels =============================================

    public static void setLabelToNote(Note note, long labelId, SQLiteDatabase writableDb, Context context) {
        NoteOperations.setLabelToNote(note, labelId, writableDb);
        data.xml.log.operations.NoteOperations.setLabelToNote(note, labelId, context);
    }

    public static void unsetLabelFromNote(Note note, long labelId, SQLiteDatabase writableDb, Context context) {
        NoteOperations.unsetLabelFromNote(note, labelId, writableDb);
        data.xml.log.operations.NoteOperations.unsetLabelFromNote(note, labelId, context);
    }

    public static void unsetAllLabelsFromNote(Note note, SQLiteDatabase writableDb, Context context) {
        NoteOperations.unsetAllLabelsFromNote(note, writableDb);
        data.xml.log.operations.NoteOperations.unsetAllLabelsFromNote(note, context);
    }
}
