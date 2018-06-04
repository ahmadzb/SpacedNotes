package data.xml.log.operations;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.Collection;

import data.model.label.LabelList;
import data.xml.log.operator.LogContract;
import data.xml.log.operator.LogOperations;

/**
 * Created by Ahmad on 02/02/18.
 * All rights reserved.
 */

public class LabelListOperations {
    //============================================ Read ============================================
    public static void performOperation(Element element, long time, SQLiteDatabase writableDb, Context context) {
        String name = element.getName();
        try {
            if (name.equals(LogContract.LabelList.Add.itemName)) {
                performAdd(element, writableDb, context);
            } else if (name.equals(LogContract.LabelList.Update.itemName)) {
                performUpdate(element, writableDb);
            } else if (name.equals(LogContract.LabelList.UpdateLabels.itemName)) {
                performUpdateLabels(element, writableDb);
            } else if (name.equals(LogContract.LabelList.UpdateLabelPosition.itemName)) {
                performUpdateLabelListLabelPosition(element, writableDb);
            } else if (name.equals(LogContract.LabelList.UpdateLabelListPosition.itemName)) {
                performUpdateLabelListPosition(element, writableDb);
            } else if (name.equals(LogContract.LabelList.Delete.itemName)) {
                performDelete(element, writableDb);
            }
        } catch (DataConversionException e) {
            e.printStackTrace();
        }
    }

    private static void performAdd(Element element, SQLiteDatabase writableDb, Context context) throws DataConversionException {
        LabelList labelList = LabelList.newInstance();
        labelList.setId(element.getAttribute(LogContract.LabelList.Add.id).getLongValue());
        {
            Attribute attr = element.getAttribute(LogContract.LabelList.Add.title);
            if (attr != null) {
                labelList.setTitle(attr.getValue());
            }
        }
        {
            Attribute attr = element.getAttribute(LogContract.LabelList.Add.color);
            if (attr != null) {
                labelList.setColor(attr.getIntValue());
            }
        }
        {
            Attribute attr = element.getAttribute(LogContract.LabelList.Add.parentId);
            if (attr != null) {
                labelList.setParentId(attr.getLongValue());
            }
        }
        {
            Attribute attr = element.getAttribute(LogContract.LabelList.Add.position);
            if (attr != null) {
                labelList.setPosition(attr.getIntValue());
            }
        }
        labelList.setRealized(true);
        labelList.setInitialized(true);
        data.database.LabelListOperations.addLabelList(labelList, writableDb, context);
    }

    private static void performUpdate(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        LabelList labelList = LabelList.newInstance();
        labelList.setId(element.getAttribute(LogContract.LabelList.Update.id).getLongValue());
        {
            Attribute attr = element.getAttribute(LogContract.LabelList.Update.title);
            if (attr != null) {
                labelList.setTitle(attr.getValue());
            }
        }
        {
            Attribute attr = element.getAttribute(LogContract.LabelList.Update.color);
            if (attr != null) {
                labelList.setColor(attr.getIntValue());
            }
        }
        {
            Attribute attr = element.getAttribute(LogContract.LabelList.Update.parentId);
            if (attr != null) {
                labelList.setParentId(attr.getLongValue());
            }
        }
        {
            Attribute attr = element.getAttribute(LogContract.LabelList.Update.position);
            if (attr != null) {
                labelList.setPosition(attr.getIntValue());
            }
        }
        labelList.setRealized(true);
        labelList.setInitialized(true);
        data.database.LabelListOperations.updateLabelList(labelList, writableDb);
    }

    private static void performUpdateLabels(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        LabelList labelList = LabelList.newInstance().setId(element.getAttribute(
                LogContract.LabelList.UpdateLabels.labelListId).getLongValue());
        Collection<Element> children = element.getChildren();
        ArrayList<Long> labelIds = new ArrayList<>(children.size());
        for (Element child : children) {
            labelIds.add(child.getAttribute(LogContract.LabelList.UpdateLabels.Label.id).getLongValue());
        }
        data.database.LabelListOperations.updateLabelListLabels(labelIds, labelList, writableDb);
    }

    private static void performUpdateLabelListLabelPosition(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        long labelListId = element.getAttribute(LogContract.LabelList.UpdateLabelPosition.labelListId).getLongValue();
        long labelId = element.getAttribute(LogContract.LabelList.UpdateLabelPosition.labelId).getLongValue();
        int position = element.getAttribute(LogContract.LabelList.UpdateLabelPosition.position).getIntValue();
        data.database.LabelListOperations.updateLabelListLabelPosition(labelListId, labelId, position, writableDb);
    }

    private static void performUpdateLabelListPosition(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        long labelListId = element.getAttribute(LogContract.LabelList.UpdateLabelListPosition.labelListId).getLongValue();
        int position = element.getAttribute(LogContract.LabelList.UpdateLabelListPosition.position).getIntValue();
        data.database.LabelListOperations.updateLabelListPosition(labelListId, position, writableDb);
    }

    private static void performDelete(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        LabelList labelList = LabelList.newInstance().setId(element.getAttribute(
                LogContract.LabelList.Delete.id).getLongValue());
        data.database.LabelListOperations.deleteLabelList(labelList, writableDb);
    }

    //=========================================== Write ============================================
    public static void addLabelList(LabelList labelList, Context context) {
        Element add = new Element(LogContract.LabelList.Add.itemName);
        add.setAttribute(LogContract.LabelList.Add.id, String.valueOf(labelList.getId()));
        if (labelList.getTitle() != null) {
            add.setAttribute(LogContract.LabelList.Add.title, labelList.getTitle());
        }
        if (labelList.getColor() != null) {
            add.setAttribute(LogContract.LabelList.Add.color, String.valueOf(labelList.getColor()));
        }
        if (labelList.getParentId() != null) {
            add.setAttribute(LogContract.LabelList.Add.parentId, String.valueOf(labelList.getParentId()));
        }
        add.setAttribute(LogContract.LabelList.Add.position, String.valueOf(labelList.getPosition()));
        LogOperations.addLabelListOperation(add, context);
    }

    public static void updateLabelList(LabelList labelList, Context context) {
        Element update = new Element(LogContract.LabelList.Update.itemName);
        update.setAttribute(LogContract.LabelList.Update.id, String.valueOf(labelList.getId()));
        if (labelList.getTitle() != null) {
            update.setAttribute(LogContract.LabelList.Update.title, labelList.getTitle());
        }
        if (labelList.getColor() != null) {
            update.setAttribute(LogContract.LabelList.Update.color, String.valueOf(labelList.getColor()));
        }
        if (labelList.getParentId() != null) {
            update.setAttribute(LogContract.LabelList.Update.parentId, String.valueOf(labelList.getParentId()));
        }
        update.setAttribute(LogContract.LabelList.Update.position, String.valueOf(labelList.getPosition()));
        LogOperations.addLabelListOperation(update, context);
    }

    public static void updateLabelListLabels(ArrayList<Long> labelIds, LabelList labelList, Context context) {
        Element updateLabels = new Element(LogContract.LabelList.UpdateLabels.itemName);
        updateLabels.setAttribute(LogContract.LabelList.UpdateLabels.labelListId, String.valueOf(labelList.getId()));
        for (Long labelId : labelIds) {
            Element label = new Element(LogContract.LabelList.UpdateLabels.Label.itemName);
            label.setAttribute(LogContract.LabelList.UpdateLabels.Label.id, String.valueOf(labelId));
            updateLabels.addContent(label);
        }
        LogOperations.addLabelListOperation(updateLabels, context);
    }

    public static void updateLabelListLabelPosition(long labelListId, long labelId, int position, Context context) {
        Element update = new Element(LogContract.LabelList.UpdateLabelPosition.itemName);
        update.setAttribute(LogContract.LabelList.UpdateLabelPosition.labelListId, String.valueOf(labelListId));
        update.setAttribute(LogContract.LabelList.UpdateLabelPosition.labelId, String.valueOf(labelId));
        update.setAttribute(LogContract.LabelList.UpdateLabelPosition.position, String.valueOf(position));
        LogOperations.addLabelListOperation(update, context);
    }

    public static void updateLabelListPosition(long labelListId, int position, Context context) {
        Element update = new Element(LogContract.LabelList.UpdateLabelListPosition.itemName);
        update.setAttribute(LogContract.LabelList.UpdateLabelListPosition.labelListId, String.valueOf(labelListId));
        update.setAttribute(LogContract.LabelList.UpdateLabelListPosition.position, String.valueOf(position));
        LogOperations.addLabelListOperation(update, context);
    }


    public static void deleteLabelList(LabelList labelList, Context context) {
        Element delete = new Element(LogContract.LabelList.Delete.itemName);
        delete.setAttribute(LogContract.LabelList.Delete.id, String.valueOf(labelList.getId()));
        LogOperations.addLabelListOperation(delete, context);
    }
}
