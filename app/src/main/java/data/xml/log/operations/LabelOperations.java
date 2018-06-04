package data.xml.log.operations;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;

import data.model.label.Label;
import data.xml.log.operator.LogContract;
import data.xml.log.operator.LogOperations;

/**
 * Created by Ahmad on 02/02/18.
 * All rights reserved.
 */

public class LabelOperations {
    //============================================ Read ============================================
    public static void performOperation(Element element, long time, SQLiteDatabase writableDb, Context context) {
        String name = element.getName();
        try {
            if (name.equals(LogContract.Label.Add.itemName)) {
                performAdd(element, writableDb, context);
            } else if (name.equals(LogContract.Label.Update.itemName)) {
                performUpdate(element, writableDb);
            } else if (name.equals(LogContract.Label.MarkAsDeleted.itemName)) {
                performMarkAsDeleted(element, writableDb);
            } else if (name.equals(LogContract.Label.MarkAsNotDeleted.itemName)) {
                performMarkAsNotDeleted(element, writableDb);
            } else if (name.equals(LogContract.Label.Delete.itemName)) {
                performDelete(element, writableDb);
            }
        } catch (DataConversionException e) {
            e.printStackTrace();
        }
    }

    private static void performAdd(Element element, SQLiteDatabase writableDb, Context context) throws DataConversionException {
        Label label = Label.newInstance();
        label.setId(element.getAttribute(LogContract.Label.Add.id).getLongValue());
        {
            Attribute attr = element.getAttribute(LogContract.Label.Add.title);
            if (attr != null) {
                label.setTitle(attr.getValue());
            }
        }
        {
            Attribute attr = element.getAttribute(LogContract.Label.Add.deleted);
            if (attr != null) {
                label.setDeleted(attr.getLongValue());
            }
        }
        label.setRealized(true);
        label.setInitialized(true);
        data.database.LabelOperations.addLabel(label, writableDb, context);
    }

    private static void performUpdate(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        Label label = Label.newInstance();
        label.setId(element.getAttribute(LogContract.Label.Update.id).getLongValue());
        {
            Attribute attr = element.getAttribute(LogContract.Label.Update.title);
            if (attr != null) {
                label.setTitle(attr.getValue());
            }
        }
        {
            Attribute attr = element.getAttribute(LogContract.Label.Update.deleted);
            if (attr != null) {
                label.setDeleted(attr.getLongValue());
            }
        }
        label.setRealized(true);
        label.setInitialized(true);
        data.database.LabelOperations.updateLabel(label, writableDb);
    }

    private static void performMarkAsDeleted(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        Label label = Label.newInstance();
        label.setId(element.getAttribute(LogContract.Label.MarkAsDeleted.id).getLongValue());
        label.setDeleted(element.getAttribute(LogContract.Label.MarkAsDeleted.deletedDate).getLongValue());
        label.setRealized(true);
        label.setInitialized(true);
        data.database.LabelOperations.markLabelAsDeleted(label, writableDb);
    }

    private static void performMarkAsNotDeleted(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        Label label = Label.newInstance();
        label.setId(element.getAttribute(LogContract.Label.MarkAsNotDeleted.id).getLongValue());
        label.setRealized(true);
        data.database.LabelOperations.markLabelAsNotDeleted(label, writableDb);
    }

    private static void performDelete(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        Label label = Label.newInstance();
        label.setId(element.getAttribute(LogContract.Label.Delete.id).getLongValue());
        label.setRealized(true);
        data.database.LabelOperations.deleteLabel(label, writableDb);
    }

    //=========================================== Write ============================================
    public static void addLabel(Label label, Context context) {
        Element add = new Element(LogContract.Label.Add.itemName);
        add.setAttribute(LogContract.Label.Add.id, String.valueOf(label.getId()));
        if (label.getTitle() != null) {
            add.setAttribute(LogContract.Label.Add.title, label.getTitle());
        }
        if (label.getDeleted() != null) {
            add.setAttribute(LogContract.Label.Add.deleted, String.valueOf(label.getDeleted()));
        }
        LogOperations.addLabelOperation(add, context);
    }

    public static void updateLabel(Label label, Context context) {
        Element add = new Element(LogContract.Label.Update.itemName);
        add.setAttribute(LogContract.Label.Update.id, String.valueOf(label.getId()));
        if (label.getTitle() != null) {
            add.setAttribute(LogContract.Label.Update.title, label.getTitle());
        }
        if (label.getDeleted() != null) {
            add.setAttribute(LogContract.Label.Update.deleted, String.valueOf(label.getDeleted()));
        }
        LogOperations.addLabelOperation(add, context);
    }

    public static void deleteLabel(Label label, Context context) {
        Element delete = new Element(LogContract.Label.Delete.itemName);
        delete.setAttribute(LogContract.Label.Delete.id, String.valueOf(label.getId()));
        LogOperations.addLabelOperation(delete, context);
    }

    public static void markLabelAsDeleted(Label label, Context context) {
        Element markAsDeleted = new Element(LogContract.Label.MarkAsDeleted.itemName);
        markAsDeleted.setAttribute(LogContract.Label.MarkAsDeleted.id, String.valueOf(label.getId()));
        markAsDeleted.setAttribute(LogContract.Label.MarkAsDeleted.deletedDate, String.valueOf(label.getDeleted()));
        LogOperations.addLabelOperation(markAsDeleted, context);
    }

    public static void markLabelAsNotDeleted(Label label, Context context) {
        Element markAsNotDeleted = new Element(LogContract.Label.MarkAsNotDeleted.itemName);
        markAsNotDeleted.setAttribute(LogContract.Label.MarkAsNotDeleted.id, String.valueOf(label.getId()));
        LogOperations.addLabelOperation(markAsNotDeleted, context);
    }
}
