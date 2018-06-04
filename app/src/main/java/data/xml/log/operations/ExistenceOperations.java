package data.xml.log.operations;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;

import data.model.existence.Existence;
import data.xml.log.operator.LogContract;
import data.xml.log.operator.LogOperations;

/**
 * Created by Ahmad on 02/15/18.
 * All rights reserved.
 */

public class ExistenceOperations {

    //============================================ Read ============================================
    public static void performOperation(Element element, long time, SQLiteDatabase fileWritableDb) {
        String name = element.getName();
        try {
            if (name.equals(LogContract.Existence.Add.itemName)) {
                performAddExistence(element, fileWritableDb);
            } else if (name.equals(LogContract.Existence.Update.itemName)) {
                performUpdateExistence(element, fileWritableDb);
            } else if (name.equals(LogContract.Existence.Delete.itemName)) {
                performDeleteExistence(element, fileWritableDb);
            } else if (name.equals(LogContract.Existence.ClearExistenceFlag.itemName)) {
                performClearExistenceFlag(element, fileWritableDb);
            }
        } catch (DataConversionException e) {
            e.printStackTrace();
        }
    }

    public static void performAddExistence(Element element, SQLiteDatabase fileWritableDb) throws DataConversionException {
        Existence existence = Existence.newInstance();
        existence.setPattern(element.getAttribute(LogContract.Existence.Add.pattern).getLongValue());
        existence.setType(element.getAttribute(LogContract.Existence.Add.type).getIntValue());
        existence.setExistenceFlags(element.getAttribute(LogContract.Existence.Add.existenceFlags).getIntValue());
        existence.setState(element.getAttribute(LogContract.Existence.Add.state).getIntValue());
        Attribute attr;
        if ((attr = element.getAttribute(LogContract.Existence.Add.profile)) != null) {
            existence.setProfile(attr.getLongValue());
        }
        if ((attr = element.getAttribute(LogContract.Existence.Add.data1)) != null) {
            existence.setData1(attr.getLongValue());
        }
        existence.setInitialized(true);
        existence.setRealized(true);
        data.database.file.ExistenceOperations.addExistence(existence, fileWritableDb);
    }

    public static void performUpdateExistence(Element element, SQLiteDatabase fileWritableDb) throws DataConversionException {
        Existence existence = Existence.newInstance();
        existence.setPattern(element.getAttribute(LogContract.Existence.Update.pattern).getLongValue());
        existence.setType(element.getAttribute(LogContract.Existence.Update.type).getIntValue());
        existence.setExistenceFlags(element.getAttribute(LogContract.Existence.Update.existenceFlags).getIntValue());
        existence.setState(element.getAttribute(LogContract.Existence.Update.state).getIntValue());
        Attribute attr;
        if ((attr = element.getAttribute(LogContract.Existence.Update.profile)) != null) {
            existence.setProfile(attr.getLongValue());
        }
        if ((attr = element.getAttribute(LogContract.Existence.Update.data1)) != null) {
            existence.setData1(attr.getLongValue());
        }
        existence.setInitialized(true);
        existence.setRealized(true);
        data.database.file.ExistenceOperations.updateExistence(existence, fileWritableDb);
    }

    public static void performDeleteExistence(Element element, SQLiteDatabase fileWritableDb) throws DataConversionException {
        Existence existence = Existence.newInstance();
        existence.setPattern(element.getAttribute(LogContract.Existence.Delete.pattern).getLongValue());
        data.database.file.ExistenceOperations.deleteExistence(existence, fileWritableDb);
    }

    public static void performClearExistenceFlag(Element element, SQLiteDatabase fileWritableDb) throws DataConversionException {
        int flag = element.getAttribute(LogContract.Existence.ClearExistenceFlag.flag).getIntValue();
        data.database.file.ExistenceOperations.clearExistenceFlag(flag, fileWritableDb);
    }

    //=========================================== Write ============================================
    public static void addExistence(Existence existence, Context context) {
        if (!existence.isInitialized())
            throw new RuntimeException("Existence is not initialized");
        Element element = new Element(LogContract.Existence.Add.itemName);
        element.setAttribute(LogContract.Existence.Add.pattern, String.valueOf(existence.getPattern()));
        element.setAttribute(LogContract.Existence.Add.type, String.valueOf(existence.getType()));
        element.setAttribute(LogContract.Existence.Add.existenceFlags, String.valueOf(existence.getExistenceFlags()));
        element.setAttribute(LogContract.Existence.Add.state, String.valueOf(existence.getState()));
        if (existence.getProfile() != null) {
            element.setAttribute(LogContract.Existence.Add.profile, String.valueOf(existence.getProfile()));
        }
        if (existence.getData1() != null) {
            element.setAttribute(LogContract.Existence.Add.data1, String.valueOf(existence.getData1()));
        }
        LogOperations.addExistenceOperation(element, context);
    }


    public static void updateExistence(Existence existence, Context context) {
        if (!existence.isInitialized() || !existence.isRealized())
            throw new RuntimeException("Existence is not initialized or realized");
        Element element = new Element(LogContract.Existence.Update.itemName);
        element.setAttribute(LogContract.Existence.Update.pattern, String.valueOf(existence.getPattern()));
        element.setAttribute(LogContract.Existence.Update.type, String.valueOf(existence.getType()));
        element.setAttribute(LogContract.Existence.Update.existenceFlags, String.valueOf(existence.getExistenceFlags()));
        element.setAttribute(LogContract.Existence.Update.state, String.valueOf(existence.getState()));
        if (existence.getProfile() != null) {
            element.setAttribute(LogContract.Existence.Update.profile, String.valueOf(existence.getProfile()));
        }
        if (existence.getData1() != null) {
            element.setAttribute(LogContract.Existence.Update.data1, String.valueOf(existence.getData1()));
        }
        LogOperations.addExistenceOperation(element, context);
    }


    public static void deleteExistence(Existence existence, Context context) {
        Element element = new Element(LogContract.Existence.Delete.itemName);
        element.setAttribute(LogContract.Existence.Delete.pattern, String.valueOf(existence.getPattern()));
        LogOperations.addExistenceOperation(element, context);
    }


    public static void clearExistenceFlag(int flag, Context context) {
        Element element = new Element(LogContract.Existence.ClearExistenceFlag.itemName);
        element.setAttribute(LogContract.Existence.ClearExistenceFlag.flag, String.valueOf(flag));
        LogOperations.addExistenceOperation(element, context);
    }
}
