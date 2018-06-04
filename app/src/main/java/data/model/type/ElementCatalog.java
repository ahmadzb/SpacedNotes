package data.model.type;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.TreeMap;

import data.database.TypeElementOperations;

/**
 * Created by Ahmad on 01/09/18.
 * All rights reserved.
 */

public class ElementCatalog {
    //=========================================== Read =============================================
    public static int getCountAll(SQLiteDatabase readableDb) {
        return TypeElementOperations.getCountAll(readableDb);
    }

    public static int getCountAvailable(SQLiteDatabase readableDb) {
        return TypeElementOperations.getCountAvailable(readableDb);
    }

    public static Element getElementById(long id, SQLiteDatabase readableDb) {
        return TypeElementOperations.getElementById(id, readableDb);
    }

    public static ArrayList<Element> getElements(Type type, SQLiteDatabase readableDb) {
        return TypeElementOperations.getElements(type, readableDb);
    }

    public static TreeMap<Long, Element> getElementMap(Type type, SQLiteDatabase readableDb) {
        return TypeElementOperations.getElementMap(type, readableDb);
    }

    public static TreeMap<Long, Element> getElementMap(long typeId, SQLiteDatabase readableDb) {
        return TypeElementOperations.getElementMap(typeId, readableDb);
    }

    public static ArrayList<Element> getElementsArchived(Type type, SQLiteDatabase readableDb) {
        return TypeElementOperations.getElementsArchived(type, readableDb);
    }

    public static ArrayList<Element> getElementsAvailable(Type type, SQLiteDatabase readableDb) {
        return TypeElementOperations.getElementsAvailable(type, readableDb);
    }

    public static ArrayList<Element> getElements(String selection, SQLiteDatabase readableDb) {
        return TypeElementOperations.getElements(selection, readableDb);
    }

    public static boolean hasRelatedItems(Element element, SQLiteDatabase readableDb) {
        return TypeElementOperations.hasRelatedItems(element, readableDb);
    }

    //========================================== Write =============================================
    public static long addElement(Element element, SQLiteDatabase writableDb, Context context) {
        element.setId(TypeElementOperations.addElement(element, writableDb, context));
        data.xml.log.operations.TypeElementOperations.addElement(element, context);
        return element.getId();
    }

    public static int updateElement(Element element, SQLiteDatabase writableDb, Context context) {
        int count = TypeElementOperations.updateElement(element, writableDb);
        data.xml.log.operations.TypeElementOperations.updateElement(element, context);
        return count;
    }

    public static int updateElementPosition(Element element, int position, SQLiteDatabase writableDb, Context context) {
        int count = TypeElementOperations.updateElementPosition(element, position, writableDb);
        data.xml.log.operations.TypeElementOperations.updateElementPosition(element, position, context);
        return count;
    }

    public static int updateElementArchivedState(Element element, boolean isArchived, SQLiteDatabase writableDb, Context context) {
        int count = TypeElementOperations.updateElementArchivedState(element, isArchived, writableDb);
        data.xml.log.operations.TypeElementOperations.updateElementArchivedState(element, isArchived, context);
        return count;
    }

    public static int deleteElement(Element element, SQLiteDatabase writableDb, Context context) {
        int count = TypeElementOperations.deleteElement(element, writableDb);
        data.xml.log.operations.TypeElementOperations.deleteElement(element, context);
        return count;
    }
}
