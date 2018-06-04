package data.xml.log.operations;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.jdom2.DataConversionException;
import org.jdom2.Element;

import java.util.Collection;
import java.util.TreeMap;

import data.model.schedule.Occurrence;
import data.model.schedule.ScheduleConversion;
import data.xml.log.operator.LogContract;
import data.xml.log.operator.LogOperations;

/**
 * Created by Ahmad on 02/02/18.
 * All rights reserved.
 */

public class OccurrenceOperations {
    //============================================ Read ============================================
    public static void performOperation(Element element, long time, SQLiteDatabase writableDb, Context context) {
        String name = element.getName();
        try {
            if (name.equals(LogContract.Occurrence.Add.itemName)) {
                performAdd(element, writableDb, context);
            } else if (name.equals(LogContract.Occurrence.Update.itemName)) {
                performUpdate(element, writableDb);
            } else if (name.equals(LogContract.Occurrence.Delete.itemName)) {
                performDelete(element, writableDb);
            }
        } catch (DataConversionException e) {
            e.printStackTrace();
        }
    }

    private static void performAdd(Element element, SQLiteDatabase writableDb, Context context) throws DataConversionException {
        Occurrence occurrence = Occurrence.newInstance();
        occurrence.setId(element.getAttribute(LogContract.Occurrence.Add.id).getLongValue());
        occurrence.setNumber(element.getAttribute(LogContract.Occurrence.Add.number).getIntValue());
        occurrence.setPlusDays(element.getAttribute(LogContract.Occurrence.Add.plusDays).getIntValue());
        occurrence.setScheduleId(element.getAttribute(LogContract.Occurrence.Add.scheduleId).getLongValue());
        Collection<Element> children = element.getChildren();
        TreeMap<Long, ScheduleConversion> conversionMap = new TreeMap<>();
        for (Element child : children) {
            ScheduleConversion conversion = ScheduleConversion.newInstance();
            conversion.setFromOccurrenceId(child.getAttribute(
                    LogContract.Occurrence.Add.Conversion.fromOccurrenceId).getLongValue());
            conversion.setToOccurrenceNumber(child.getAttribute(
                    LogContract.Occurrence.Add.Conversion.toOccurrenceNumber).getIntValue());
            conversion.setToScheduleId(child.getAttribute(
                    LogContract.Occurrence.Add.Conversion.toScheduleId).getLongValue());
            conversionMap.put(conversion.getToScheduleId(), conversion);
        }
        occurrence.setConversions(conversionMap);
        occurrence.setRealized(true);
        occurrence.setInitialized(true);
        data.database.OccurrenceOperations.addOccurrence(occurrence, writableDb, context);
    }

    private static void performUpdate(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        Occurrence occurrence = Occurrence.newInstance();
        occurrence.setId(element.getAttribute(LogContract.Occurrence.Update.id).getLongValue());
        occurrence.setNumber(element.getAttribute(LogContract.Occurrence.Update.number).getIntValue());
        occurrence.setPlusDays(element.getAttribute(LogContract.Occurrence.Update.plusDays).getIntValue());
        occurrence.setScheduleId(element.getAttribute(LogContract.Occurrence.Update.scheduleId).getLongValue());
        Collection<Element> children = element.getChildren();
        TreeMap<Long, ScheduleConversion> conversionMap = new TreeMap<>();
        for (Element child : children) {
            ScheduleConversion conversion = ScheduleConversion.newInstance();
            conversion.setFromOccurrenceId(child.getAttribute(
                    LogContract.Occurrence.Update.Conversion.fromOccurrenceId).getLongValue());
            conversion.setToOccurrenceNumber(child.getAttribute(
                    LogContract.Occurrence.Update.Conversion.toOccurrenceNumber).getIntValue());
            conversion.setToScheduleId(child.getAttribute(
                    LogContract.Occurrence.Update.Conversion.toScheduleId).getLongValue());
            conversionMap.put(conversion.getToScheduleId(), conversion);
        }
        occurrence.setConversions(conversionMap);
        occurrence.setRealized(true);
        occurrence.setInitialized(true);
        data.database.OccurrenceOperations.updateOccurrence(occurrence, writableDb);
    }

    private static void performDelete(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        Occurrence occurrence = Occurrence.newInstance();
        occurrence.setId(element.getAttribute(LogContract.Occurrence.Delete.id).getLongValue());
        data.database.OccurrenceOperations.deleteOccurrence(occurrence, writableDb);
    }

    //=========================================== Write ============================================
    public static void addOccurrence(Occurrence occurrence, Context context) {
        Element element = new Element(LogContract.Occurrence.Add.itemName);
        element.setAttribute(LogContract.Occurrence.Add.id, String.valueOf(occurrence.getId()));
        element.setAttribute(LogContract.Occurrence.Add.number, String.valueOf(occurrence.getNumber()));
        element.setAttribute(LogContract.Occurrence.Add.plusDays, String.valueOf(occurrence.getPlusDays()));
        element.setAttribute(LogContract.Occurrence.Add.scheduleId, String.valueOf(occurrence.getScheduleId()));

        Collection<ScheduleConversion> conversions = occurrence.getConversions();
        for (ScheduleConversion conversion : conversions) {
            Element conversionElement = new Element(LogContract.Occurrence.Add.Conversion.itemName);
            conversionElement.setAttribute(LogContract.Occurrence.Add.Conversion.fromOccurrenceId,
                    String.valueOf(conversion.getFromOccurrenceId()));
            conversionElement.setAttribute(LogContract.Occurrence.Add.Conversion.toOccurrenceNumber,
                    String.valueOf(conversion.getToOccurrenceNumber()));
            conversionElement.setAttribute(LogContract.Occurrence.Add.Conversion.toScheduleId,
                    String.valueOf(conversion.getToScheduleId()));
            element.addContent(conversionElement);
        }
        LogOperations.addOccurrenceOperation(element, context);
    }

    public static void updateOccurrence(Occurrence occurrence, Context context) {
        Element element = new Element(LogContract.Occurrence.Update.itemName);
        element.setAttribute(LogContract.Occurrence.Update.id, String.valueOf(occurrence.getId()));
        element.setAttribute(LogContract.Occurrence.Update.number, String.valueOf(occurrence.getNumber()));
        element.setAttribute(LogContract.Occurrence.Update.plusDays, String.valueOf(occurrence.getPlusDays()));
        element.setAttribute(LogContract.Occurrence.Update.scheduleId, String.valueOf(occurrence.getScheduleId()));

        Collection<ScheduleConversion> conversions = occurrence.getConversions();
        for (ScheduleConversion conversion : conversions) {
            Element conversionElement = new Element(LogContract.Occurrence.Update.Conversion.itemName);
            conversionElement.setAttribute(LogContract.Occurrence.Update.Conversion.fromOccurrenceId,
                    String.valueOf(conversion.getFromOccurrenceId()));
            conversionElement.setAttribute(LogContract.Occurrence.Update.Conversion.toOccurrenceNumber,
                    String.valueOf(conversion.getToOccurrenceNumber()));
            conversionElement.setAttribute(LogContract.Occurrence.Update.Conversion.toScheduleId,
                    String.valueOf(conversion.getToScheduleId()));
            element.addContent(conversionElement);
        }
        LogOperations.addOccurrenceOperation(element, context);
    }

    public static void deleteOccurrence(Occurrence occurrence, Context context) {
        Element element = new Element(LogContract.Occurrence.Delete.itemName);
        element.setAttribute(LogContract.Occurrence.Delete.id, String.valueOf(occurrence.getId()));
        LogOperations.addOccurrenceOperation(element, context);
    }
}
