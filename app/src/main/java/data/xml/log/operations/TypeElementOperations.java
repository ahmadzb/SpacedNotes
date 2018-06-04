package data.xml.log.operations;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;

import data.xml.log.operator.LogContract;
import data.xml.log.operator.LogOperations;

/**
 * Created by Ahmad on 02/02/18.
 * All rights reserved.
 */

public class TypeElementOperations {
    //============================================ Read ============================================
    public static void performOperation(Element element, long time, SQLiteDatabase writableDb, Context context) {
        String name = element.getName();
        try {
            if (name.equals(LogContract.TypeElement.Add.itemName)) {
                performAddElement(element, writableDb, context);
            } else if (name.equals(LogContract.TypeElement.Update.itemName)) {
                performUpdateElement(element, writableDb);
            } else if (name.equals(LogContract.TypeElement.UpdatePosition.itemName)) {
                performUpdateElementPosition(element, writableDb);
            } else if (name.equals(LogContract.TypeElement.UpdateArchived.itemName)) {
                performUpdateElementArchivedState(element, writableDb);
            } else if (name.equals(LogContract.TypeElement.Delete.itemName)) {
                performDeleteElement(element, writableDb);
            }
        } catch (DataConversionException e) {
            e.printStackTrace();
        }
    }

    private static void performAddElement(Element element, SQLiteDatabase writableDb, Context context) throws DataConversionException {
        data.model.type.Element typeElement = data.model.type.Element.newInstance();
        Attribute attr;
        typeElement.setId(element.getAttribute(LogContract.TypeElement.Add.id).getLongValue());
        typeElement.setTypeId(element.getAttribute(LogContract.TypeElement.Add.typeId).getLongValue());
        typeElement.setPosition(element.getAttribute(LogContract.TypeElement.Add.position).getIntValue());
        if ((attr = element.getAttribute(LogContract.TypeElement.Add.title)) != null) {
            typeElement.setTitle(attr.getValue());
        }
        typeElement.setArchived(element.getAttribute(LogContract.TypeElement.Add.isArchived).getBooleanValue());
        typeElement.setSides(element.getAttribute(LogContract.TypeElement.Add.sides).getIntValue());
        typeElement.setPattern(element.getAttribute(LogContract.TypeElement.Add.pattern).getIntValue());
        typeElement.setInitialCopy(element.getAttribute(LogContract.TypeElement.Add.initialCopy).getBooleanValue());
        if ((attr = element.getAttribute(LogContract.TypeElement.Add.data1)) != null) {
            typeElement.setData1(attr.getLongValue());
        }
        if ((attr = element.getAttribute(LogContract.TypeElement.Add.data2)) != null) {
            typeElement.setData2(attr.getLongValue());
        }
        if ((attr = element.getAttribute(LogContract.TypeElement.Add.data3)) != null) {
            typeElement.setData3(attr.getValue());
        }
        if ((attr = element.getAttribute(LogContract.TypeElement.Add.data4)) != null) {
            typeElement.setData4(attr.getValue());
        }
        typeElement.setRealized(true);
        typeElement.setInitialized(true);
        data.database.TypeElementOperations.addElement(typeElement, writableDb, context);
    }

    private static void performUpdateElement(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        data.model.type.Element typeElement = data.model.type.Element.newInstance();
        Attribute attr;
        typeElement.setId(element.getAttribute(LogContract.TypeElement.Update.id).getLongValue());
        typeElement.setTypeId(element.getAttribute(LogContract.TypeElement.Update.typeId).getLongValue());
        typeElement.setPosition(element.getAttribute(LogContract.TypeElement.Update.position).getIntValue());
        if ((attr = element.getAttribute(LogContract.TypeElement.Update.title)) != null) {
            typeElement.setTitle(attr.getValue());
        }
        typeElement.setArchived(element.getAttribute(LogContract.TypeElement.Update.isArchived).getBooleanValue());
        typeElement.setSides(element.getAttribute(LogContract.TypeElement.Update.sides).getIntValue());
        typeElement.setPattern(element.getAttribute(LogContract.TypeElement.Update.pattern).getIntValue());
        typeElement.setInitialCopy(element.getAttribute(LogContract.TypeElement.Update.initialCopy).getBooleanValue());
        if ((attr = element.getAttribute(LogContract.TypeElement.Update.data1)) != null) {
            typeElement.setData1(attr.getLongValue());
        }
        if ((attr = element.getAttribute(LogContract.TypeElement.Update.data2)) != null) {
            typeElement.setData2(attr.getLongValue());
        }
        if ((attr = element.getAttribute(LogContract.TypeElement.Update.data3)) != null) {
            typeElement.setData3(attr.getValue());
        }
        if ((attr = element.getAttribute(LogContract.TypeElement.Update.data4)) != null) {
            typeElement.setData4(attr.getValue());
        }
        typeElement.setRealized(true);
        typeElement.setInitialized(true);
        data.database.TypeElementOperations.updateElement(typeElement, writableDb);
    }

    private static void performUpdateElementPosition(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        data.model.type.Element typeElement = data.model.type.Element.newInstance();
        typeElement.setId(element.getAttribute(LogContract.TypeElement.UpdatePosition.id).getLongValue());
        typeElement.setPosition(element.getAttribute(LogContract.TypeElement.UpdatePosition.position).getIntValue());
        data.database.TypeElementOperations.updateElementPosition(typeElement, typeElement.getPosition(), writableDb);
    }

    private static void performUpdateElementArchivedState(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        data.model.type.Element typeElement = data.model.type.Element.newInstance();
        typeElement.setId(element.getAttribute(LogContract.TypeElement.UpdateArchived.id).getLongValue());
        typeElement.setArchived(element.getAttribute(LogContract.TypeElement.UpdateArchived.isArchived).getBooleanValue());
        data.database.TypeElementOperations.updateElementArchivedState(typeElement, typeElement.isArchived(), writableDb);
    }

    private static void performDeleteElement(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        data.model.type.Element typeElement = data.model.type.Element.newInstance();
        typeElement.setId(element.getAttribute(LogContract.TypeElement.Delete.id).getLongValue());
        data.database.TypeElementOperations.deleteElement(typeElement, writableDb);
    }

    //=========================================== Write ============================================

    public static void addElement(data.model.type.Element typeElement, Context context) {
        Element element = new Element(LogContract.TypeElement.Add.itemName);
        element.setAttribute(LogContract.TypeElement.Add.id, String.valueOf(typeElement.getId()));
        element.setAttribute(LogContract.TypeElement.Add.typeId, String.valueOf(typeElement.getTypeId()));
        element.setAttribute(LogContract.TypeElement.Add.position, String.valueOf(typeElement.getPosition()));
        if (typeElement.getTitle() != null) {
            element.setAttribute(LogContract.TypeElement.Add.title, typeElement.getTitle());
        }
        element.setAttribute(LogContract.TypeElement.Add.isArchived, String.valueOf(typeElement.isArchived()));
        element.setAttribute(LogContract.TypeElement.Add.sides, String.valueOf(typeElement.getSides()));
        element.setAttribute(LogContract.TypeElement.Add.pattern, String.valueOf(typeElement.getPattern()));
        element.setAttribute(LogContract.TypeElement.Add.initialCopy, String.valueOf(typeElement.isInitialCopy()));
        if (typeElement.getData1() != null) {
            element.setAttribute(LogContract.TypeElement.Add.data1, String.valueOf(typeElement.getData1()));
        }
        if (typeElement.getData2() != null) {
            element.setAttribute(LogContract.TypeElement.Add.data2, String.valueOf(typeElement.getData2()));
        }
        if (typeElement.getData3() != null) {
            element.setAttribute(LogContract.TypeElement.Add.data3, typeElement.getData3());
        }
        if (typeElement.getData4() != null) {
            element.setAttribute(LogContract.TypeElement.Add.data4, typeElement.getData4());
        }
        LogOperations.addTypeElementOperation(element, context);
    }

    public static void updateElement(data.model.type.Element typeElement, Context context) {
        Element element = new Element(LogContract.TypeElement.Update.itemName);
        element.setAttribute(LogContract.TypeElement.Update.id, String.valueOf(typeElement.getId()));
        element.setAttribute(LogContract.TypeElement.Update.typeId, String.valueOf(typeElement.getTypeId()));
        element.setAttribute(LogContract.TypeElement.Update.position, String.valueOf(typeElement.getPosition()));
        if (typeElement.getTitle() != null) {
            element.setAttribute(LogContract.TypeElement.Update.title, typeElement.getTitle());
        }
        element.setAttribute(LogContract.TypeElement.Update.isArchived, String.valueOf(typeElement.isArchived()));
        element.setAttribute(LogContract.TypeElement.Update.sides, String.valueOf(typeElement.getSides()));
        element.setAttribute(LogContract.TypeElement.Update.pattern, String.valueOf(typeElement.getPattern()));
        element.setAttribute(LogContract.TypeElement.Update.initialCopy, String.valueOf(typeElement.isInitialCopy()));
        if (typeElement.getData1() != null) {
            element.setAttribute(LogContract.TypeElement.Update.data1, String.valueOf(typeElement.getData1()));
        }
        if (typeElement.getData2() != null) {
            element.setAttribute(LogContract.TypeElement.Update.data2, String.valueOf(typeElement.getData2()));
        }
        if (typeElement.getData3() != null) {
            element.setAttribute(LogContract.TypeElement.Update.data3, typeElement.getData3());
        }
        if (typeElement.getData4() != null) {
            element.setAttribute(LogContract.TypeElement.Update.data4, typeElement.getData4());
        }
        LogOperations.addTypeElementOperation(element, context);
    }

    public static void updateElementPosition(data.model.type.Element typeElement, int position, Context context) {
        Element element = new Element(LogContract.TypeElement.UpdatePosition.itemName);
        element.setAttribute(LogContract.TypeElement.UpdatePosition.id, String.valueOf(typeElement.getId()));
        element.setAttribute(LogContract.TypeElement.UpdatePosition.position, String.valueOf(position));
        LogOperations.addTypeElementOperation(element, context);
    }

    public static void updateElementArchivedState(data.model.type.Element typeElement, boolean isArchived, Context context) {
        Element element = new Element(LogContract.TypeElement.UpdateArchived.itemName);
        element.setAttribute(LogContract.TypeElement.UpdateArchived.id, String.valueOf(typeElement.getId()));
        element.setAttribute(LogContract.TypeElement.UpdateArchived.isArchived, String.valueOf(isArchived));
        LogOperations.addTypeElementOperation(element, context);
    }

    public static void deleteElement(data.model.type.Element typeElement, Context context) {
        Element element = new Element(LogContract.TypeElement.Delete.itemName);
        element.setAttribute(LogContract.TypeElement.Delete.id, String.valueOf(typeElement.getId()));
        LogOperations.addTypeElementOperation(element, context);
    }
}
