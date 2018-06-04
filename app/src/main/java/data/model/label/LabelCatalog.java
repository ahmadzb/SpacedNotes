package data.model.label;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.TreeMap;

import data.database.LabelOperations;

/**
 * Created by Ahmad on 01/01/18.
 * All rights reserved.
 */

public class LabelCatalog {
    //=========================================== Read =============================================
    public static Label getLabelById(long id, SQLiteDatabase readableDb) {
        return LabelOperations.getLabelById(id, readableDb);
    }

    public static ArrayList<Label> getLabelsByNote(long noteId, SQLiteDatabase readableDb) {
        return LabelOperations.getLabelsByNote(noteId, readableDb);
    }

    public static ArrayList<Label> getLabelsByLabelList(long labelListId, SQLiteDatabase readableDb) {
        return LabelOperations.getLabelsByLabelList(labelListId, readableDb);
    }

    public static TreeMap<Long, Label> getLabelsMap(SQLiteDatabase readableDb) {
        return LabelOperations.getLabelsMap(readableDb);
    }

    public static ArrayList<Label> getLabelsAll(SQLiteDatabase readableDb) {
        return LabelOperations.getLabelsAll(readableDb);
    }

    public static ArrayList<Label> getLabels(SQLiteDatabase readableDb) {
        return LabelOperations.getLabels(readableDb);
    }

    public static ArrayList<Label> getLabelsDeleted(SQLiteDatabase readableDb) {
        return LabelOperations.getLabelsDeleted(readableDb);
    }

    public static ArrayList<Label> getLabels(String selection, SQLiteDatabase readableDb) {
        return LabelOperations.getLabels(selection, readableDb);
    }

    public static Label retrieveLabel(Cursor cursor) {
        return LabelOperations.retrieveLabel(cursor);
    }

    public static ArrayList<Label> retrieveLabels(Cursor cursor) {
        return LabelOperations.retrieveLabels(cursor);
    }

    //========================================== Write =============================================
    public static long addLabel(Label label, SQLiteDatabase writableDb, Context context) {
        label.setId(LabelOperations.addLabel(label, writableDb, context));
        data.xml.log.operations.LabelOperations.addLabel(label, context);
        return label.getId();
    }

    public static int updateLabel(Label label, SQLiteDatabase writableDb, Context context) {
        int count = LabelOperations.updateLabel(label, writableDb);
        data.xml.log.operations.LabelOperations.updateLabel(label, context);
        return count;
    }

    public static void deleteLabel(Label label, SQLiteDatabase writableDb, Context context) {
        LabelOperations.deleteLabel(label, writableDb);
        data.xml.log.operations.LabelOperations.deleteLabel(label, context);
    }

    public static void markLabelAsDeleted(Label label, SQLiteDatabase writableDb, Context context) {
        if (label.getDeleted() == null) {
            label.setDeleted(System.currentTimeMillis());
        }
        LabelOperations.markLabelAsDeleted(label, writableDb);
        data.xml.log.operations.LabelOperations.markLabelAsDeleted(label, context);
    }

    public static void markLabelAsNotDeleted(Label label, SQLiteDatabase writableDb, Context context) {
        LabelOperations.markLabelAsNotDeleted(label, writableDb);
        data.xml.log.operations.LabelOperations.markLabelAsNotDeleted(label, context);
    }
}
