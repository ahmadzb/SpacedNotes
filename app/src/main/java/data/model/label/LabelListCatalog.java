package data.model.label;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.TreeMap;

import data.database.LabelListOperations;

/**
 * Created by Ahmad on 01/01/18.
 * All rights reserved.
 */

public class LabelListCatalog {

    //=========================================== Read =============================================
    public static LabelList getLabelListByIdWithLabels(long id, SQLiteDatabase readableDb) {
        return LabelListOperations.getLabelListByIdWithLabels(id, readableDb);
    }

    public static LabelList getLabelListById(long id, SQLiteDatabase readableDb) {
        return LabelListOperations.getLabelListById(id, readableDb);
    }

    public static TreeMap<Long, LabelList> getLabelListsMapWithLabels(SQLiteDatabase readableDb) {
        return LabelListOperations.getLabelListsMapWithLabels(readableDb);
    }

    public static TreeMap<Long, LabelList> getLabelListsMap(SQLiteDatabase readableDb) {
        return LabelListOperations.getLabelListsMap(readableDb);
    }

    public static ArrayList<LabelList> getLabelListsByLabelList(long labelListId, SQLiteDatabase readableDb) {
        return LabelListOperations.getLabelListsByLabelList(labelListId, readableDb);
    }

    public static ArrayList<LabelList> getLabelListsRoot(SQLiteDatabase readableDb) {
        return LabelListOperations.getLabelListsRoot(readableDb);
    }

    public static ArrayList<LabelList> getLabelLists(SQLiteDatabase readableDb) {
        return LabelListOperations.getLabelLists(readableDb);
    }

    public static ArrayList<LabelList> getLabelLists(String selection, SQLiteDatabase readableDb) {
        return LabelListOperations.getLabelLists(selection, readableDb);
    }

    public static LabelList retrieveLabelList(Cursor cursor) {
        return LabelListOperations.retrieveLabelList(cursor);
    }

    public static ArrayList<LabelList> retrieveLabelLists(Cursor cursor) {
        return LabelListOperations.retrieveLabelLists(cursor);
    }


    //========================================== Write =============================================
    public static long addLabelList(LabelList labelList, SQLiteDatabase writableDb, Context context) {
        labelList.setId(LabelListOperations.addLabelList(labelList, writableDb, context));
        data.xml.log.operations.LabelListOperations.addLabelList(labelList, context);
        return labelList.getId();
    }

    public static int updateLabelList(LabelList labelList, SQLiteDatabase writableDb, Context context) {
        int count = LabelListOperations.updateLabelList(labelList, writableDb);
        data.xml.log.operations.LabelListOperations.updateLabelList(labelList, context);
        return count;
    }

    public static void updateLabelListLabels(ArrayList<Long> labelIds, LabelList labelList,
                                             SQLiteDatabase writableDb, Context context) {
        LabelListOperations.updateLabelListLabels(labelIds, labelList, writableDb);
        data.xml.log.operations.LabelListOperations.updateLabelListLabels(labelIds, labelList, context);
    }

    public static void updateLabelListLabelPosition(long labelListId, long labelId, int position, SQLiteDatabase writableDb, Context context) {
        LabelListOperations.updateLabelListLabelPosition(labelListId, labelId, position, writableDb);
        data.xml.log.operations.LabelListOperations.updateLabelListLabelPosition(labelListId, labelId, position, context);
    }

    public static void updateLabelListPosition(long labelListId, int position, SQLiteDatabase writableDb, Context context) {
        LabelListOperations.updateLabelListPosition(labelListId, position, writableDb);
        data.xml.log.operations.LabelListOperations.updateLabelListPosition(labelListId, position, context);
    }

    public static void deleteLabelList(LabelList labelList, SQLiteDatabase writableDb, Context context) {
        LabelListOperations.deleteLabelList(labelList, writableDb);
        data.xml.log.operations.LabelListOperations.deleteLabelList(labelList, context);
    }
}
