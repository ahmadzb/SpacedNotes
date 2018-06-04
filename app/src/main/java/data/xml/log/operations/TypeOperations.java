package data.xml.log.operations;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.jdom2.DataConversionException;
import org.jdom2.Element;

import data.model.type.Type;
import data.xml.log.operator.LogContract;
import data.xml.log.operator.LogOperations;

/**
 * Created by Ahmad on 02/02/18.
 * All rights reserved.
 */

public class TypeOperations {
    //============================================ Read ============================================
    public static void performOperation(Element element, long time, SQLiteDatabase writableDb, Context context) {
        String name = element.getName();
        try {
            if (name.equals(LogContract.Type.Add.itemName)) {
                performAddType(element, writableDb, context);
            } else if (name.equals(LogContract.Type.Update.itemName)) {
                performUpdateType(element, writableDb);
            } else if (name.equals(LogContract.Type.UpdatePosition.itemName)) {
                performUpdateTypePosition(element, writableDb);
            } else if (name.equals(LogContract.Type.UpdateArchived.itemName)) {
                performUpdateTypeArchivedState(element, writableDb);
            } else if (name.equals(LogContract.Type.Delete.itemName)) {
                performDeleteType(element, writableDb);
            }
        } catch (DataConversionException e) {
            e.printStackTrace();
        }
    }

    private static void performAddType(Element element, SQLiteDatabase writableDb, Context context) throws DataConversionException {
        Type type = Type.newInstance();
        type.setId(element.getAttribute(LogContract.Type.Add.id).getLongValue());
        type.setTitle(element.getAttribute(LogContract.Type.Add.title).getValue());
        type.setColor(element.getAttribute(LogContract.Type.Add.color).getIntValue());
        type.setPosition(element.getAttribute(LogContract.Type.Add.position).getIntValue());
        type.setArchived(element.getAttribute(LogContract.Type.Add.isArchived).getBooleanValue());
        type.setRealized(true);
        type.setInitialized(true);
        data.database.TypeOperations.addType(type, writableDb, context);
    }

    private static void performUpdateType(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        Type type = Type.newInstance();
        type.setId(element.getAttribute(LogContract.Type.Update.id).getLongValue());
        type.setTitle(element.getAttribute(LogContract.Type.Update.title).getValue());
        type.setColor(element.getAttribute(LogContract.Type.Update.color).getIntValue());
        type.setPosition(element.getAttribute(LogContract.Type.Update.position).getIntValue());
        type.setArchived(element.getAttribute(LogContract.Type.Update.isArchived).getBooleanValue());
        type.setRealized(true);
        type.setInitialized(true);
        data.database.TypeOperations.updateType(type, writableDb);
    }

    private static void performUpdateTypePosition(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        Type type = Type.newInstance();
        type.setId(element.getAttribute(LogContract.Type.UpdatePosition.id).getLongValue());
        type.setPosition(element.getAttribute(LogContract.Type.UpdatePosition.position).getIntValue());
        type.setRealized(true);
        data.database.TypeOperations.updateTypePosition(type, type.getPosition(), writableDb);
    }

    private static void performUpdateTypeArchivedState(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        Type type = Type.newInstance();
        type.setId(element.getAttribute(LogContract.Type.UpdateArchived.id).getLongValue());
        type.setArchived(element.getAttribute(LogContract.Type.UpdateArchived.isArchived).getBooleanValue());
        type.setRealized(true);
        data.database.TypeOperations.updateTypeArchivedState(type, type.isArchived(), writableDb);
    }

    private static void performDeleteType(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        Type type = Type.newInstance();
        type.setId(element.getAttribute(LogContract.Type.Delete.id).getLongValue());
        type.setRealized(true);
        data.database.TypeOperations.deleteType(type, writableDb);
    }

    //=========================================== Write ============================================

    public static void addType(Type type, Context context) {
        Element element = new Element(LogContract.Type.Add.itemName);
        element.setAttribute(LogContract.Type.Add.id, String.valueOf(type.getId()));
        element.setAttribute(LogContract.Type.Add.title, String.valueOf(type.getTitle()));
        element.setAttribute(LogContract.Type.Add.color, String.valueOf(type.getColor()));
        element.setAttribute(LogContract.Type.Add.position, String.valueOf(type.getPosition()));
        element.setAttribute(LogContract.Type.Add.isArchived, String.valueOf(type.isArchived()));
        LogOperations.addTypeOperation(element, context);
    }

    public static void updateType(Type type, Context context) {
        Element element = new Element(LogContract.Type.Update.itemName);
        element.setAttribute(LogContract.Type.Update.id, String.valueOf(type.getId()));
        element.setAttribute(LogContract.Type.Update.title, String.valueOf(type.getTitle()));
        element.setAttribute(LogContract.Type.Update.color, String.valueOf(type.getColor()));
        element.setAttribute(LogContract.Type.Update.position, String.valueOf(type.getPosition()));
        element.setAttribute(LogContract.Type.Update.isArchived, String.valueOf(type.isArchived()));
        LogOperations.addTypeOperation(element, context);
    }

    public static void updateTypePosition(Type type, int position, Context context) {
        Element element = new Element(LogContract.Type.UpdatePosition.itemName);
        element.setAttribute(LogContract.Type.UpdatePosition.id, String.valueOf(type.getId()));
        element.setAttribute(LogContract.Type.UpdatePosition.position, String.valueOf(position));
        LogOperations.addTypeOperation(element, context);
    }

    public static void updateTypeArchivedState(Type type, boolean isArchived, Context context) {
        Element element = new Element(LogContract.Type.UpdateArchived.itemName);
        element.setAttribute(LogContract.Type.UpdateArchived.id, String.valueOf(type.getId()));
        element.setAttribute(LogContract.Type.UpdateArchived.isArchived, String.valueOf(isArchived));
        LogOperations.addTypeOperation(element, context);
    }

    public static void deleteType(Type type, Context context) {
        Element element = new Element(LogContract.Type.Delete.itemName);
        element.setAttribute(LogContract.Type.Delete.id, String.valueOf(type.getId()));
        LogOperations.addTypeOperation(element, context);
    }
}
