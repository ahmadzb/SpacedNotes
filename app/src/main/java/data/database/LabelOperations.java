package data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.TreeMap;

import data.model.label.Label;
import data.xml.port.IdProvider;
import exceptions.InvalidCursorCountException;
import exceptions.InvalidCursorException;
import exceptions.NotRealizedException;
import exceptions.ObjectNotCompleteException;

/**
 * Created by Ahmad on 02/02/18.
 * All rights reserved.
 */

public class LabelOperations {
    //=========================================== Read =============================================
    private static String selectionNotDeleted = Contract.Label.deleted + " IS NULL";
    private static String selectionDeleted = Contract.Label.deleted + " IS NOT NULL";

    public static Label getLabelById(long id, SQLiteDatabase readableDb) {
        String selection = Contract.Label.id + " = " + id;
        Cursor cursor = readableDb.query(Contract.Label.table, null, selection, null, null, null, null);
        Label label = retrieveLabel(cursor);
        cursor.close();
        return label;
    }

    public static ArrayList<Label> getLabelsByNote(long noteId, SQLiteDatabase readableDb) {
        String sql = "SELECT " + Contract.Label.table + ".*" +
                " FROM " + Contract.Label.table + " INNER JOIN " + Contract.LabelNote.table +
                " ON " + Contract.Label.idFull + " = " + Contract.LabelNote.labelIdFull +
                " WHERE " + Contract.LabelNote.noteId + " = " + noteId;
        Cursor cursor = readableDb.rawQuery(sql, null);
        ArrayList<Label> labels = retrieveLabels(cursor);
        cursor.close();
        return labels;
    }

    public static ArrayList<Label> getLabelsByLabelList(long labelListId, SQLiteDatabase readableDb) {
        String sql = "SELECT " + Contract.Label.table + ".*" +
                " FROM " + Contract.Label.table + " INNER JOIN " + Contract.LabelListLabel.table +
                " ON " + Contract.Label.idFull + " = " + Contract.LabelListLabel.labelIdFull +
                " WHERE " + Contract.LabelListLabel.labelListIdFull + " = " + labelListId +
                " AND " + selectionNotDeleted;
        Cursor cursor = readableDb.rawQuery(sql, null);
        ArrayList<Label> labels = retrieveLabels(cursor);
        cursor.close();
        return labels;
    }

    public static TreeMap<Long, Label> getLabelsMap(SQLiteDatabase readableDb) {
        ArrayList<Label> labels = getLabels(null, readableDb);
        TreeMap<Long, Label> map = new TreeMap<>();
        for (Label label : labels) {
            map.put(label.getId(), label);
        }
        return map;
    }

    public static ArrayList<Label> getLabelsAll(SQLiteDatabase readableDb) {
        return getLabels(null, readableDb);
    }

    public static ArrayList<Label> getLabels(SQLiteDatabase readableDb) {
        return getLabels(selectionNotDeleted, readableDb);
    }

    public static ArrayList<Label> getLabelsDeleted(SQLiteDatabase readableDb) {
        return getLabels(selectionDeleted, readableDb);
    }

    public static ArrayList<Label> getLabels(String selection, SQLiteDatabase readableDb) {
        Cursor cursor = readableDb.query(Contract.Label.table, null, selection, null, null, null, null);
        ArrayList<Label> labels = retrieveLabels(cursor);
        cursor.close();
        return labels;
    }

    public static Label retrieveLabel(Cursor cursor) {
        ArrayList<Label> labels = retrieveLabels(cursor);
        if (labels.size() > 1)
            throw new InvalidCursorCountException(1, labels.size());
        else if (labels.size() == 1) {
            return labels.get(0);
        } else
            return null;
    }

    public static ArrayList<Label> retrieveLabels(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(Contract.Label.id);
        int titleIndex = cursor.getColumnIndex(Contract.Label.title);
        int deletedIndex = cursor.getColumnIndex(Contract.Label.deleted);

        if (idIndex < 0)
            throw new InvalidCursorException();

        ArrayList<Label> labels = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            Label builder = Label.newInstance().setId(cursor.getLong(idIndex)).setRealized(true)
                    .setInitialized(true);
            if (titleIndex >= 0 && !cursor.isNull(titleIndex)) {
                builder.setTitle(cursor.getString(titleIndex));
            }
            if (deletedIndex >= 0 && !cursor.isNull(deletedIndex)) {
                builder.setDeleted(cursor.getLong(deletedIndex));
            }
            labels.add(builder);
        }
        return labels;
    }

    //========================================== Write =============================================
    private static ContentValues getContentValues(Label label) {
        ContentValues values = new ContentValues();

        if (label.getTitle() != null) {
            values.put(Contract.Label.title, label.getTitle());
        } else {
            values.putNull(Contract.Label.title);
        }
        if (label.getDeleted() != null) {
            values.put(Contract.Label.deleted, label.getDeleted());
        } else {
            values.putNull(Contract.Label.deleted);
        }

        return values;
    }

    public static long addLabel(Label label, SQLiteDatabase writableDb, Context context) {
        if (label.getTitle() == null)
            throw new ObjectNotCompleteException();
        ContentValues values = getContentValues(label);
        if (label.isRealized()) {
            values.put(Contract.Label.id, label.getId());
            return writableDb.replace(Contract.Label.table, null, values);
        } else {
            values.put(Contract.Label.id, IdProvider.nextLabelId(context));
            return writableDb.insert(Contract.Label.table, null, values);
        }
    }

    public static int updateLabel(Label label, SQLiteDatabase writableDb) {
        if (!label.isRealized())
            throw new NotRealizedException();
        if (label.getTitle() == null)
            throw new ObjectNotCompleteException();
        String where = Contract.Label.id + " = " + label.getId();
        int count = writableDb.update(Contract.Label.table, getContentValues(label), where, null);
        return count;
    }

    public static void deleteLabel(Label label, SQLiteDatabase writableDb) {
        //Label
        {
            String where = Contract.Label.id + " = " + label.getId();
            writableDb.delete(Contract.Label.table, where, null);
        }
        //LabelListLabel
        {
            String where = Contract.LabelListLabel.labelId + " = " + label.getId();
            writableDb.delete(Contract.LabelListLabel.table, where, null);
        }
        //LabelNote
        {
            String where = Contract.LabelNote.labelId + " = " + label.getId();
            writableDb.delete(Contract.LabelNote.table, where, null);
        }
    }

    public static void markLabelAsDeleted(Label label, SQLiteDatabase writableDb) {
        if (label.getDeleted() == null)
            throw new RuntimeException("deleted date is null");

        String where = Contract.Label.id + " = " + label.getId();
        ContentValues values = new ContentValues();
        values.put(Contract.Label.deleted, label.getDeleted());
        writableDb.update(Contract.Label.table, values, where, null);
    }

    public static void markLabelAsNotDeleted(Label label, SQLiteDatabase writableDb) {
        String where = Contract.Label.id + " = " + label.getId();
        ContentValues values = new ContentValues();
        values.putNull(Contract.Label.deleted);
        writableDb.update(Contract.Label.table, values, where, null);
    }
}
