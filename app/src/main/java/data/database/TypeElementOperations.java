package data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.TransitionRes;

import java.util.ArrayList;
import java.util.TreeMap;

import data.model.type.Element;
import data.model.type.Type;
import data.xml.port.IdProvider;
import exceptions.InvalidCursorException;
import exceptions.NotRealizedException;

/**
 * Created by Ahmad on 02/02/18.
 * All rights reserved.
 */

public class TypeElementOperations {
    //=========================================== Read =============================================
    public static int getCountAll(SQLiteDatabase readableDb) {
        Cursor cursor = readableDb.query(Contract.TypeElement.table, null, null, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public static int getCountAvailable(SQLiteDatabase readableDb) {
        String selection = Contract.TypeElement.isArchived + " = 0";
        Cursor cursor = readableDb.query(Contract.TypeElement.table, null, selection, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public static Element getElementById(long id, SQLiteDatabase readableDb) {
        String selection = Contract.TypeElement.id + " = " + id;
        ArrayList<Element> elements = getElements(selection, readableDb);
        if (elements.size() == 0)
            return null;
        else
            return elements.get(0);
    }

    public static ArrayList<Element> getElements(Type type, SQLiteDatabase readableDb) {
        String selection = Contract.TypeElement.typeId + " = " + type.getId();
        return getElements(selection, readableDb);
    }

    public static TreeMap<Long, Element> getElementMap(Type type, SQLiteDatabase readableDb) {
        return getElementMap(type.getId(), readableDb);
    }

    public static TreeMap<Long, Element> getElementMap(long typeId, SQLiteDatabase readableDb) {
        String selection = Contract.TypeElement.typeId + " = " + typeId;
        ArrayList<Element> elements = getElements(selection, readableDb);
        TreeMap<Long, Element> elementMap = new TreeMap<>();
        for (Element element : elements) {
            elementMap.put(element.getId(), element);
        }
        return elementMap;
    }


    public static ArrayList<Element> getElementsArchived(Type type, SQLiteDatabase readableDb) {
        String selection = Contract.TypeElement.typeId + " = " + type.getId() + " AND " +
                Contract.TypeElement.isArchived + " = 1";
        return getElements(selection, readableDb);
    }

    public static ArrayList<Element> getElementsAvailable(Type type, SQLiteDatabase readableDb) {
        String selection = Contract.TypeElement.typeId + " = " + type.getId() + " AND " +
                Contract.TypeElement.isArchived + " = 0";
        return getElements(selection, readableDb);
    }

    public static ArrayList<Element> getElements(String selection, SQLiteDatabase readableDb) {
        Cursor cursor = readableDb.query(Contract.TypeElement.table, null, selection, null, null, null, null);
        ArrayList<Element> elements = retrieveElements(cursor);
        cursor.close();
        return elements;
    }

    public static boolean hasRelatedItems(Element element, SQLiteDatabase readableDb) {
        boolean hasRelatedItems = false;
        hasRelatedItems = hasRelatedItems || NoteElementOperations.hasRelatedElements(element, readableDb);
        return hasRelatedItems;
    }

    public static ArrayList<Element> retrieveElements(Cursor cursor) {
        int indexId = cursor.getColumnIndex(Contract.TypeElement.id);
        int indexTypeId = cursor.getColumnIndex(Contract.TypeElement.typeId);
        int indexTitle = cursor.getColumnIndex(Contract.TypeElement.title);
        int indexPosition = cursor.getColumnIndex(Contract.TypeElement.position);
        int indexIsArchived = cursor.getColumnIndex(Contract.TypeElement.isArchived);
        int indexSides = cursor.getColumnIndex(Contract.TypeElement.sides);
        int indexPattern = cursor.getColumnIndex(Contract.TypeElement.pattern);
        int indexInitialCopy = cursor.getColumnIndex(Contract.TypeElement.initialCopy);
        int indexIsData1 = cursor.getColumnIndex(Contract.TypeElement.data1);
        int indexIsData2 = cursor.getColumnIndex(Contract.TypeElement.data2);
        int indexIsData3 = cursor.getColumnIndex(Contract.TypeElement.data3);
        int indexIsData4 = cursor.getColumnIndex(Contract.TypeElement.data4);

        if (indexId < 0)
            throw new InvalidCursorException();
        ArrayList<Element> elements = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            Element element = Element.newInstance();
            element.setRealized(true);
            element.setInitialized(true);
            element.setId(cursor.getLong(indexId));
            if (indexTypeId >= 0) {
                element.setTypeId(cursor.getLong(indexTypeId));
            }
            if (indexTitle >= 0) {
                element.setTitle(cursor.getString(indexTitle));
            }
            if (indexPosition >= 0) {
                element.setPosition(cursor.getInt(indexPosition));
            }
            if (indexIsArchived >= 0) {
                element.setArchived(cursor.getInt(indexIsArchived) == 1);
            }
            if (indexSides >= 0) {
                element.setSides(cursor.getInt(indexSides));
            }
            if (indexPattern >= 0) {
                element.setPattern(cursor.getInt(indexPattern));
            }
            if (indexInitialCopy >= 0) {
                element.setInitialCopy(cursor.getInt(indexInitialCopy) == 1);
            }
            if (indexIsData1 >= 0) {
                element.setData1(cursor.getLong(indexIsData1));
            }
            if (indexIsData2 >= 0) {
                element.setData2(cursor.getLong(indexIsData2));
            }
            if (indexIsData3 >= 0) {
                element.setData3(cursor.getString(indexIsData3));
            }
            if (indexIsData4 >= 0) {
                element.setData4(cursor.getString(indexIsData4));
            }
            elements.add(element);
        }
        return elements;
    }

    //========================================== Write =============================================
    private static ContentValues getContentValues(Element element) {
        ContentValues values = new ContentValues();

        values.put(Contract.TypeElement.typeId, element.getTypeId());
        values.put(Contract.TypeElement.position, element.getPosition());
        values.put(Contract.TypeElement.title, element.getTitle());
        values.put(Contract.TypeElement.isArchived, element.isArchived() ? 1 : 0);
        values.put(Contract.TypeElement.sides, element.getSides());
        values.put(Contract.TypeElement.pattern, element.getPattern());
        values.put(Contract.TypeElement.initialCopy, element.isInitialCopy() ? 1 : 0);
        values.put(Contract.TypeElement.data1, element.getData1());
        values.put(Contract.TypeElement.data2, element.getData2());
        values.put(Contract.TypeElement.data3, element.getData3());
        values.put(Contract.TypeElement.data4, element.getData4());

        return values;
    }

    public static long addElement(Element element, SQLiteDatabase writableDb, Context context) {
        if (!element.isInitialized())
            throw new RuntimeException("not initialized");
        ContentValues values = getContentValues(element);
        long id;
        if (element.isRealized()) {
            values.put(Contract.TypeElement.id, element.getId());
            id = writableDb.replace(Contract.TypeElement.table, null, values);
        } else {
            values.put(Contract.TypeElement.id, IdProvider.nextTypeElementId(context));
            id = writableDb.insert(Contract.TypeElement.table, null, values);
        }
        return id;
    }

    public static int updateElement(Element element, SQLiteDatabase writableDb) {
        if (!element.isRealized() || !element.isInitialized())
            throw new NotRealizedException();
        String selection = Contract.TypeElement.id + " = " + element.getId();
        return writableDb.update(Contract.TypeElement.table, getContentValues(element), selection, null);
    }

    public static int updateElementPosition(Element element, int position, SQLiteDatabase writableDb) {
        ContentValues values = new ContentValues();
        values.put(Contract.TypeElement.position, position);
        String selection = Contract.TypeElement.id + " = " + element.getId();
        int count = writableDb.update(Contract.TypeElement.table, values, selection, null);
        return count;
    }

    public static int updateElementArchivedState(Element element, boolean isArchived, SQLiteDatabase writableDb) {
        ContentValues values = new ContentValues();
        values.put(Contract.TypeElement.isArchived, isArchived);
        String selection = Contract.TypeElement.id + " = " + element.getId();
        int count = writableDb.update(Contract.TypeElement.table, values, selection, null);
        return count;
    }

    public static int deleteElement(Element element, SQLiteDatabase writableDb) {
        String selection = Contract.TypeElement.id + " = " + element.getId();
        NoteElementOperations.deleteAllElementsByTypeElement(element, writableDb);
        return writableDb.delete(Contract.TypeElement.table, selection, null);
    }
}
