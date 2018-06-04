package data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;

import data.model.label.Label;
import data.model.label.LabelList;
import data.xml.port.IdProvider;
import exceptions.InvalidCursorCountException;
import exceptions.InvalidCursorException;
import exceptions.NotRealizedException;
import exceptions.ObjectNotCompleteException;

/**
 * Created by Ahmad on 02/02/18.
 * All rights reserved.
 */

public class LabelListOperations {

    //=========================================== Read =============================================
    public static LabelList getLabelListByIdWithLabels(long id, SQLiteDatabase readableDb) {
        String selection = Contract.LabelList.id + " = " + id;
        Cursor cursor = readableDb.query(Contract.LabelList.table, null, selection, null, null, null, null);
        LabelList labelList = retrieveLabelList(cursor);
        cursor.close();
        labelList.setLabels(LabelOperations.getLabelsByLabelList(id, readableDb));
        return labelList;
    }

    public static LabelList getLabelListById(long id, SQLiteDatabase readableDb) {
        String selection = Contract.LabelList.id + " = " + id;
        Cursor cursor = readableDb.query(Contract.LabelList.table, null, selection, null, null, null, null);
        LabelList labelList = retrieveLabelList(cursor);
        cursor.close();
        return labelList;
    }

    public static TreeMap<Long, LabelList> getLabelListsMapWithLabels(SQLiteDatabase readableDb) {
        String sql = "SELECT * FROM " + Contract.LabelListLabel.table;
        Cursor cursor = readableDb.rawQuery(sql, null);
        int labelIndex = cursor.getColumnIndex(Contract.LabelListLabel.labelId);
        int labelListIndex = cursor.getColumnIndex(Contract.LabelListLabel.labelListId);
        int positionIndex = cursor.getColumnIndex(Contract.LabelListLabel.position);

        TreeMap<Long, Label> labelMap = LabelOperations.getLabelsMap(readableDb);
        TreeMap<Long, LabelList> labelListMap = getLabelListsMap(readableDb);

        while (cursor.moveToNext()) {
            long labelId = cursor.getLong(labelIndex);
            long labelListId = cursor.getLong(labelListIndex);
            int position = cursor.getInt(positionIndex);
            LabelList labelList = labelListMap.get(labelListId);
            ArrayList<Label> labels = labelList.getLabels();
            if (labels == null) {
                labels = new ArrayList<>(labelMap.size());
                labelList.setLabels(labels);
            }
            Label label = labelMap.get(labelId);
            if (label.getDeleted() == null) {
                labels.add(label.clone().setForeignPosition(position));
            }
        }
        return labelListMap;
    }

    public static TreeMap<Long, LabelList> getLabelListsMap(SQLiteDatabase readableDb) {
        TreeMap<Long, LabelList> map = new TreeMap<>();
        ArrayList<LabelList> list = getLabelLists(readableDb);
        for (LabelList labelList : list) {
            map.put(labelList.getId(), labelList);
        }
        return map;
    }

    public static ArrayList<LabelList> getLabelListsByLabelList(long labelListId, SQLiteDatabase readableDb) {
        String selection = Contract.LabelList.parentId + " = " + labelListId;
        return getLabelLists(selection, readableDb);
    }

    public static ArrayList<LabelList> getLabelListsRoot(SQLiteDatabase readableDb) {
        String selection = Contract.LabelList.parentId + " IS NULL";
        return getLabelLists(selection, readableDb);
    }

    public static ArrayList<LabelList> getLabelLists(SQLiteDatabase readableDb) {
        return getLabelLists(null, readableDb);
    }

    public static ArrayList<LabelList> getLabelLists(String selection, SQLiteDatabase readableDb) {
        Cursor cursor = readableDb.query(Contract.LabelList.table, null, selection, null, null, null, null);
        ArrayList<LabelList> labelLists = retrieveLabelLists(cursor);
        cursor.close();
        return labelLists;
    }

    public static LabelList retrieveLabelList(Cursor cursor) {
        ArrayList<LabelList> labelLists = retrieveLabelLists(cursor);
        if (labelLists.size() > 1)
            throw new InvalidCursorCountException(1, labelLists.size());
        else if (labelLists.size() == 1) {
            return labelLists.get(0);
        } else
            return null;
    }

    public static ArrayList<LabelList> retrieveLabelLists(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(Contract.LabelList.id);
        int titleIndex = cursor.getColumnIndex(Contract.LabelList.title);
        int colorIndex = cursor.getColumnIndex(Contract.LabelList.color);
        int parentIdIndex = cursor.getColumnIndex(Contract.LabelList.parentId);
        int positionIndex = cursor.getColumnIndex(Contract.LabelList.position);

        if (idIndex < 0)
            throw new InvalidCursorException();

        ArrayList<LabelList> labelLists = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            LabelList builder = LabelList.newInstance().setId(cursor.getLong(idIndex)).setRealized(true)
                    .setInitialized(true);
            if (titleIndex >= 0 && !cursor.isNull(titleIndex)) {
                builder.setTitle(cursor.getString(titleIndex));
            }
            if (colorIndex >= 0 && !cursor.isNull(colorIndex)) {
                builder.setColor(cursor.getInt(colorIndex));
            }
            if (parentIdIndex >= 0 && !cursor.isNull(parentIdIndex)) {
                builder.setParentId(cursor.getLong(parentIdIndex));
            }
            if (positionIndex >= 0 && !cursor.isNull(positionIndex)) {
                builder.setPosition(cursor.getInt(positionIndex));
            }
            labelLists.add(builder);
        }
        return labelLists;
    }


    //========================================== Write =============================================
    private static ContentValues getContentValues(LabelList labelList) {
        ContentValues values = new ContentValues();

        if (labelList.getTitle() != null) {
            values.put(Contract.LabelList.title, labelList.getTitle());
        } else {
            values.putNull(Contract.LabelList.title);
        }
        if (labelList.getColor() != null) {
            values.put(Contract.LabelList.color, labelList.getColor());
        } else {
            values.putNull(Contract.LabelList.color);
        }
        if (labelList.getParentId() != null) {
            values.put(Contract.LabelList.parentId, labelList.getParentId());
        } else {
            values.putNull(Contract.LabelList.parentId);
        }
        values.put(Contract.LabelList.position, labelList.getPosition());

        return values;
    }

    public static long addLabelList(LabelList labelList, SQLiteDatabase writableDb, Context context) {
        if (labelList.getTitle() == null)
            throw new ObjectNotCompleteException();
        ContentValues values = getContentValues(labelList);
        if (labelList.isRealized()) {
            values.put(Contract.LabelList.id, labelList.getId());
            return writableDb.replace(Contract.LabelList.table, null, values);
        } else {
            values.put(Contract.LabelList.id, IdProvider.nextLabelListId(context));
            return writableDb.insert(Contract.LabelList.table, null, values);
        }
    }

    public static int updateLabelList(LabelList labelList, SQLiteDatabase writableDb) {
        if (!labelList.isRealized() || !labelList.isInitialized())
            throw new NotRealizedException();
        if (labelList.getTitle() == null)
            throw new ObjectNotCompleteException();
        String where = Contract.LabelList.id + " = " + labelList.getId();
        int count = writableDb.update(Contract.LabelList.table, getContentValues(labelList), where, null);
        return count;
    }

    public static void updateLabelListLabels(ArrayList<Long> labelIds, LabelList labelList, SQLiteDatabase writableDb) {
        //Finding current labels positions
        TreeMap<Long, Integer> currentLabelPositions = new TreeMap<>();
        {
            String selection = Contract.LabelListLabel.labelListId + " = " + labelList.getId();
            Cursor cursor = writableDb.query(Contract.LabelListLabel.table, null, selection, null,
                    null, null, null);
            int labelIdIndex = cursor.getColumnIndex(Contract.LabelListLabel.labelId);
            int positionIndex = cursor.getColumnIndex(Contract.LabelListLabel.position);
            while (cursor.moveToNext()) {
                long labelId = cursor.getLong(labelIdIndex);
                int position = cursor.getInt(positionIndex);
                currentLabelPositions.put(labelId, position);
            }
            cursor.close();
        }
        //Deleting old items
        {
            String selection = Contract.LabelListLabel.labelListId + " = " + labelList.getId();
            writableDb.delete(Contract.LabelListLabel.table, selection, null);
        }
        //Inserting new items
        for (Long id : labelIds) {
            ContentValues values = new ContentValues();
            values.put(Contract.LabelListLabel.labelId, id);
            values.put(Contract.LabelListLabel.labelListId, labelList.getId());
            Integer position = currentLabelPositions.get(id);
            if (position == null) {
                position = 0;
            }
            values.put(Contract.LabelListLabel.position, position);
            writableDb.insert(Contract.LabelListLabel.table, null, values);
        }
    }

    public static void updateLabelListLabelPosition(long labelListId, long labelId, int position, SQLiteDatabase writableDb) {
        String selection = Contract.LabelListLabel.labelId + " = " + labelId +
                " AND " + Contract.LabelListLabel.labelListId + " = " + labelListId;
        ContentValues values = new ContentValues();
        values.put(Contract.LabelListLabel.position, position);
        writableDb.update(Contract.LabelListLabel.table, values, selection, null);
    }

    public static void updateLabelListPosition(long labelListId, int position, SQLiteDatabase writableDb) {
        String selection = Contract.LabelList.id + " = " + labelListId;
        ContentValues values = new ContentValues();
        values.put(Contract.LabelList.position, position);
        writableDb.update(Contract.LabelList.table, values, selection, null);
    }
    public static void deleteLabelList(LabelList labelList, SQLiteDatabase writableDb) {
        //LabelList
        {
            //Delete
            {
                String where = Contract.LabelList.id + " = " + labelList.getId();
                writableDb.delete(Contract.LabelList.table, where, null);
            }
            //Update Child Lists
            {
                String where = Contract.LabelList.parentId + " = " + labelList.getId();
                ContentValues values = new ContentValues();
                values.putNull(Contract.LabelList.parentId);
                writableDb.update(Contract.LabelList.table, values, where, null);
            }
        }
        //LabelListLabel
        {
            String where = Contract.LabelListLabel.labelListId + " = " + labelList.getId();
            writableDb.delete(Contract.LabelListLabel.table, where, null);
        }
    }
}
