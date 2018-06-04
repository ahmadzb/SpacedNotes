package data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.TreeMap;

import javax.annotation.Nullable;

import data.database.file.ExistenceOperations;
import data.database.file.FileOpenHelper;
import data.model.existence.Existence;
import data.model.existence.ExistenceCatalog;
import data.model.label.LabelList;
import data.model.note.Element;
import data.model.note.ElementCatalog;
import data.model.note.ElementPicture;
import data.model.note.Note;
import data.model.profiles.Profile;
import data.model.profiles.ProfileCatalog;
import data.xml.port.IdProvider;
import data.xml.profiles.ProfilesOperations;
import exceptions.InvalidCursorException;
import exceptions.NotRealizedException;
import util.datetime.primitive.Representation;

/**
 * Created by Ahmad on 02/02/18.
 * All rights reserved.
 */

public class NoteOperations {
    //=========================================== Read =============================================
    public static Note getNoteById(long id, SQLiteDatabase readableDb) {
        String selection = Contract.Note.id + " = " + id;
        ArrayList<Note> notes = getNotes(selection, readableDb);
        if (notes.size() == 0)
            return null;
        else
            return notes.get(0);
    }

    public static ArrayList<Note> getNotesNotDeleted(SQLiteDatabase readableDb) {
        String selection = Contract.Note.deleted + " IS NULL";
        return getNotes(selection, readableDb);
    }

    public static ArrayList<Note> getNotesDeleted(SQLiteDatabase readableDb) {
        String selection = Contract.Note.deleted + " IS NOT NULL";
        return getNotes(selection, readableDb);
    }

    public static ArrayList<Note> getNotesByRevisionPastDate(LocalDate date, SQLiteDatabase readableDb) {
        int datePrimitive = Representation.fromLocalDate(date);
        String sql = "SELECT " + Contract.Note.table + ".*" +
                " FROM " + Contract.Note.table + " INNER JOIN " + Contract.RevisionPast.table +
                " ON " + Contract.Note.idFull + " = " + Contract.RevisionPast.noteId +
                " WHERE " + Contract.Note.deleted + " IS NULL" +
                " AND " + Contract.RevisionPast.date + " = " + datePrimitive;
        Cursor cursor = readableDb.rawQuery(sql, null);
        ArrayList<Note> notes = retrieveNotes(cursor);
        cursor.close();
        return notes;
    }

    public static ArrayList<Note> getNotesByRevisionFutureDate(LocalDate date, SQLiteDatabase readableDb,
                                                               boolean withRevisionFuture) {
        int datePrimitive = Representation.fromLocalDate(date);
        String sql = "SELECT " + Contract.Note.table + ".*" +
                (withRevisionFuture? ", " + Contract.RevisionFuture.table + ".*" : "") +
                " FROM " + Contract.Note.table + " INNER JOIN " + Contract.RevisionFuture.table +
                " ON " + Contract.Note.id + " = " + Contract.RevisionFuture.noteId +
                " WHERE " + Contract.Note.deleted + " IS NULL" +
                " AND " + Contract.RevisionFuture.dueDate + " = " + datePrimitive;
        Cursor cursor = readableDb.rawQuery(sql, null);
        ArrayList<Note> notes = retrieveNotes(cursor, withRevisionFuture);
        cursor.close();
        return notes;
    }

    public static ArrayList<Note> getNotesByRevisionFutureRange(
            @Nullable LocalDate from, @Nullable LocalDate to, SQLiteDatabase readableDb, boolean withRevisionFuture) {
        String sql = "SELECT " + Contract.Note.table + ".*" +
                (withRevisionFuture? ", " + Contract.RevisionFuture.table + ".*" : "") +
                " FROM " + Contract.Note.table + " INNER JOIN " + Contract.RevisionFuture.table +
                " ON " + Contract.Note.id + " = " + Contract.RevisionFuture.noteId +
                " WHERE " + Contract.Note.deleted + " IS NULL";
        if (from != null) {
            sql = sql + " AND " + Contract.RevisionFuture.dueDate + " >= " + Representation.fromLocalDate(from);
        }
        if (to != null) {
            sql = sql + " AND " + Contract.RevisionFuture.dueDate + " <= " + Representation.fromLocalDate(to);
        }
        Cursor cursor = readableDb.rawQuery(sql, null);
        ArrayList<Note> notes = retrieveNotes(cursor, withRevisionFuture);
        cursor.close();
        return notes;
    }

    public static ArrayList<Note> getNotesByLabel(long labelId, SQLiteDatabase readableDb, boolean withRevisionFuture) {
        String sql = "SELECT " + Contract.Note.table + ".*" +
                (withRevisionFuture? ", " + Contract.RevisionFuture.table + ".*" : "") +
                " FROM " + Contract.Note.table + " INNER JOIN " + Contract.LabelNote.table +
                " ON " + Contract.Note.id + " = " + Contract.LabelNote.noteIdFull +
                (withRevisionFuture? " LEFT JOIN " + Contract.RevisionFuture.table +
                        " ON " +  Contract.Note.id + " = " + Contract.RevisionFuture.noteIdFull : "") +
                " WHERE " + Contract.Note.deletedFull + " IS NULL" +
                " AND " + Contract.LabelNote.labelId + " = " + labelId;
        Cursor cursor = readableDb.rawQuery(sql, null);
        ArrayList<Note> notes = retrieveNotes(cursor, withRevisionFuture);
        cursor.close();
        return notes;
    }

    public static TreeMap<Long, Integer> getNotesCountByLabelMap(SQLiteDatabase readableDb) {
        String countColumn = "notesCount";
        String sql = "SELECT " + Contract.LabelNote.labelId + "," +
                " COUNT(" + Contract.LabelNote.noteId + ") AS " + countColumn +
                " FROM " + Contract.LabelNote.table + " INNER JOIN " + Contract.Note.table +
                " ON " + Contract.LabelNote.noteId + " = " + Contract.Note.id +
                " WHERE " + Contract.Note.deleted + " IS NULL" +
                " GROUP BY " + Contract.LabelNote.labelId;
        Cursor cursor = readableDb.rawQuery(sql, null);
        TreeMap<Long, Integer> map = new TreeMap<>();
        int labelIndex = cursor.getColumnIndex(Contract.LabelNote.labelId);
        int countIndex = cursor.getColumnIndex(countColumn);
        while (cursor.moveToNext()) {
            long labelId = cursor.getLong(labelIndex);
            int count = cursor.getInt(countIndex);
            map.put(labelId, count);
        }
        cursor.close();
        return map;
    }


    public static TreeMap<Long, Integer> getNotesCountByLabelMap(LabelList labelList, SQLiteDatabase readableDb) {
        String countColumn = "notesCount";
        String sql = "SELECT " + Contract.LabelNote.labelId + "," +
                " COUNT(" + Contract.LabelNote.noteId + ") AS " + countColumn +
                " FROM " + Contract.LabelNote.table + " INNER JOIN " + Contract.Note.table +
                " ON " + Contract.LabelNote.noteId + " = " + Contract.Note.id +
                " WHERE " + Contract.Note.deleted + " IS NULL" +
                " AND " + Contract.LabelNote.labelId + " IN" +
                "(SELECT " + Contract.LabelListLabel.labelId +
                " FROM " + Contract.LabelListLabel.table +
                " WHERE " + Contract.LabelListLabel.labelListId + " = " + labelList.getId() +
                ") GROUP BY " + Contract.LabelNote.labelId;
        Cursor cursor = readableDb.rawQuery(sql, null);
        TreeMap<Long, Integer> map = new TreeMap<>();
        int labelIndex = cursor.getColumnIndex(Contract.LabelNote.labelId);
        int countIndex = cursor.getColumnIndex(countColumn);
        while (cursor.moveToNext()) {
            long labelId = cursor.getLong(labelIndex);
            int count = cursor.getInt(countIndex);
            map.put(labelId, count);
        }
        cursor.close();
        return map;
    }

    public static ArrayList<Note> getNotes(String selection, SQLiteDatabase readableDb) {
        Cursor cursor = readableDb.query(Contract.Note.table, null, selection, null, null, null, null);
        ArrayList<Note> notes = retrieveNotes(cursor);
        cursor.close();
        return notes;
    }

    public static boolean hasRelatedNotes(data.model.type.Type type, SQLiteDatabase readableDb) {
        String countColumn = "countColumn";
        String sql = "SELECT COUNT(*) AS " + countColumn +
                " FROM " + Contract.Note.table +
                " WHERE " + Contract.Note.typeId + " = " + type.getId();
        Cursor cursor = readableDb.rawQuery(sql, null);
        cursor.moveToFirst();
        int count = cursor.getInt(cursor.getColumnIndex(countColumn));
        cursor.close();
        return count != 0;
    }

    public static ArrayList<Note> retrieveNotes(Cursor cursor) {
        return retrieveNotes(cursor, false);
    }

    public static ArrayList<Note> retrieveNotes(Cursor cursor, boolean withRevisionFuture) {
        int indexId = cursor.getColumnIndex(Contract.Note.id);
        int indexTypeId = cursor.getColumnIndex(Contract.Note.typeId);
        int indexCreateDate = cursor.getColumnIndex(Contract.Note.createDate);
        int indexModifyDate = cursor.getColumnIndex(Contract.Note.modifyDate);
        int indexDisplayTitleFront = cursor.getColumnIndex(Contract.Note.displayTitleFront);
        int indexDisplayDetailsFront = cursor.getColumnIndex(Contract.Note.displayDetailsFront);
        int indexDisplayTitleBack = cursor.getColumnIndex(Contract.Note.displayTitleBack);
        int indexDisplayDetailsBack = cursor.getColumnIndex(Contract.Note.displayDetailsBack);
        int indexDeleted = cursor.getColumnIndex(Contract.Note.deleted);

        RevisionOperations.RevisionFutureIndexCache revisionFutureIndexCache = null;
        if (withRevisionFuture) {
            revisionFutureIndexCache = RevisionOperations.RevisionFutureIndexCache.newInstance(cursor);
        }
        if (indexId < 0)
            throw new InvalidCursorException();
        ArrayList<Note> notes = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            Note note = Note.newInstance();
            note.setRealized(true);
            note.setInitialized(true);
            note.setId(cursor.getLong(indexId));
            if (indexTypeId >= 0) {
                note.setTypeId(cursor.getLong(indexTypeId));
            }
            if (indexCreateDate >= 0) {
                note.setCreateDate(cursor.getLong(indexCreateDate));
            }
            if (indexModifyDate >= 0) {
                note.setModifyDate(cursor.getLong(indexModifyDate));
            }
            if (indexDisplayTitleFront >= 0 && !cursor.isNull(indexDisplayTitleFront)) {
                note.setDisplayTitleFront(cursor.getString(indexDisplayTitleFront));
            }
            if (indexDisplayTitleBack >= 0 && !cursor.isNull(indexDisplayTitleBack)) {
                note.setDisplayTitleBack(cursor.getString(indexDisplayTitleBack));
            }
            if (indexDisplayDetailsFront >= 0 && !cursor.isNull(indexDisplayDetailsFront)) {
                note.setDisplayDetailsFront(cursor.getString(indexDisplayDetailsFront));
            }
            if (indexDisplayDetailsBack >= 0 && !cursor.isNull(indexDisplayDetailsBack)) {
                note.setDisplayDetailsBack(cursor.getString(indexDisplayDetailsBack));
            }
            if (indexDeleted >= 0 && !cursor.isNull(indexDeleted)) {
                note.setDeleted(cursor.getLong(indexDeleted));
            }
            if (withRevisionFuture) {
                note.setRevisionFuture(RevisionOperations.retrieveRevisionFuture(cursor, revisionFutureIndexCache));
            }
            notes.add(note);
        }
        return notes;
    }

    //========================================== Write =============================================
    private static ContentValues getContentValues(Note note) {
        ContentValues values = new ContentValues();

        values.put(Contract.Note.typeId, note.getTypeId());
        values.put(Contract.Note.createDate, note.getCreateDate());
        values.put(Contract.Note.modifyDate, note.getModifyDate());
        values.put(Contract.Note.displayTitleFront, note.getDisplayTitleFront());
        values.put(Contract.Note.displayDetailsFront, note.getDisplayDetailsFront());
        values.put(Contract.Note.displayTitleBack, note.getDisplayTitleBack());
        values.put(Contract.Note.displayDetailsBack, note.getDisplayDetailsBack());
        values.put(Contract.Note.deleted, note.getDeleted());

        return values;
    }

    public static long addNote(Note note, SQLiteDatabase writableDb, Context context) {
        ContentValues values = getContentValues(note);
        if (note.isRealized()) {
            values.put(Contract.Note.id, note.getId());
            return writableDb.replace(Contract.Note.table, null, values);
        } else {
            values.put(Contract.Note.id, IdProvider.nextNoteId(context));
            return writableDb.insert(Contract.Note.table, null, values);
        }
    }

    public static int updateNote(Note note, SQLiteDatabase writableDb) {
        if (!note.isRealized() || !note.isInitialized())
            throw new NotRealizedException();
        String selection = Contract.Note.id + " = " + note.getId();
        return writableDb.update(Contract.Note.table, getContentValues(note), selection, null);
    }

    public static int markAsDeleted(Note note, SQLiteDatabase writableDb) {
        if (note.getDeleted() == null) {
            note.setDeleted(System.currentTimeMillis());
        }
        String selection = Contract.Note.id + " = " + note.getId();
        ContentValues values = new ContentValues();
        values.put(Contract.Note.deleted, note.getDeleted());
        return writableDb.update(Contract.Note.table, values, selection, null);
    }

    public static int markAsNotDeleted(Note note, SQLiteDatabase writableDb) {
        String selection = Contract.Note.id + " = " + note.getId();
        ContentValues values = new ContentValues();
        values.putNull(Contract.Note.deleted);
        return writableDb.update(Contract.Note.table, values, selection, null);
    }

    public static int deleteNote(Note note, SQLiteDatabase writableDb) {
        String selection = Contract.Note.id + " = " + note.getId();
        return writableDb.delete(Contract.Note.table, selection, null);
    }

    public static int deleteNoteWithRelatedContent(Note note, SQLiteDatabase writableDb,
                                                   SQLiteDatabase fileWritableDb, long profileId) {
        ArrayList<Element> elements = ElementCatalog.getNoteElements(note, writableDb);
        for (Element element : elements) {
            if (element instanceof ElementPicture) {
                ElementPicture elementPicture = (ElementPicture) element;
                for (int i = 0; i < elementPicture.getItemCount(); i++) {
                    long pattern = Existence.Pattern.Picture.getPattern(profileId, elementPicture.getItemAt(i).getPictureId());
                    ExistenceOperations.setExistenceStateByPattern(pattern, Existence.STATE_DELETE, fileWritableDb);
                }
            }
        }
        NoteElementOperations.deleteAllElementsByNote(note, writableDb);
        return deleteNote(note, writableDb);
    }

    //========================================= Labels =============================================

    public static void setLabelToNote(Note note, long labelId, SQLiteDatabase writableDb) {
        ContentValues values = new ContentValues();
        values.put(Contract.LabelNote.noteId, note.getId());
        values.put(Contract.LabelNote.labelId, labelId);
        writableDb.replace(Contract.LabelNote.table, null, values);
    }

    public static void unsetLabelFromNote(Note note, long labelId, SQLiteDatabase writableDb) {
        String selection = Contract.LabelNote.noteId + " = " + note.getId() +
                " AND " + Contract.LabelNote.labelId + " = " + labelId;
        writableDb.delete(Contract.LabelNote.table, selection, null);
    }

    public static void unsetAllLabelsFromNote(Note note, SQLiteDatabase writableDb) {
        String selection = Contract.LabelNote.noteId + " = " + note.getId();
        writableDb.delete(Contract.LabelNote.table, selection, null);
    }
}
