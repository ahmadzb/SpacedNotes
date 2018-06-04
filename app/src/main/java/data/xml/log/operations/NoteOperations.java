package data.xml.log.operations;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;

import data.model.note.Note;
import data.model.profiles.Profile;
import data.xml.log.operator.LogContract;
import data.xml.log.operator.LogOperations;

/**
 * Created by Ahmad on 02/02/18.
 * All rights reserved.
 */

public class NoteOperations {
    //============================================ Read ============================================
    public static void performOperation(Element element, long time, SQLiteDatabase writableDb,
                                        SQLiteDatabase fileWritableDb, Context context, long profileId) {
        String name = element.getName();
        try {
            if (name.equals(LogContract.Note.Add.itemName)) {
                performAdd(element, writableDb, context);
            } else if (name.equals(LogContract.Note.Update.itemName)) {
                performUpdate(element, writableDb);
            } else if (name.equals(LogContract.Note.MarkAsDeleted.itemName)) {
                performMarkAsDeleted(element, writableDb);
            } else if (name.equals(LogContract.Note.MarkAsNotDeleted.itemName)) {
                performMarkAsNotDeleted(element, writableDb);
            } else if (name.equals(LogContract.Note.Delete.itemName)) {
                performDelete(element, writableDb, fileWritableDb, profileId);
            } else if (name.equals(LogContract.Note.SetLabelToNote.itemName)) {
                performSetLabelToNote(element, writableDb);
            } else if (name.equals(LogContract.Note.UnsetLabelFromNote.itemName)) {
                performUnsetLabelFromNote(element, writableDb);
            } else if (name.equals(LogContract.Note.UnsetAllLabelsFromNote.itemName)) {
                performUnsetAllLabelsFromNote(element, writableDb);
            }
        } catch (DataConversionException e) {
            e.printStackTrace();
        }
    }

    private static void performAdd(Element element, SQLiteDatabase writableDb, Context context) throws DataConversionException {
        Note note = Note.newInstance();
        Attribute attr;
        note.setId(element.getAttribute(LogContract.Note.Add.id).getLongValue());
        note.setTypeId(element.getAttribute(LogContract.Note.Add.typeId).getLongValue());
        note.setCreateDate(element.getAttribute(LogContract.Note.Add.createDate).getLongValue());
        note.setModifyDate(element.getAttribute(LogContract.Note.Add.modifyDate).getLongValue());
        if ((attr = element.getAttribute(LogContract.Note.Add.displayTitleFront)) != null) {
            note.setDisplayTitleFront(attr.getValue());
        }
        if ((attr = element.getAttribute(LogContract.Note.Add.displayDetailsFront)) != null) {
            note.setDisplayDetailsFront(attr.getValue());
        }
        if ((attr = element.getAttribute(LogContract.Note.Add.displayTitleBack)) != null) {
            note.setDisplayTitleBack(attr.getValue());
        }
        if ((attr = element.getAttribute(LogContract.Note.Add.displayDetailsBack)) != null) {
            note.setDisplayDetailsBack(attr.getValue());
        }
        if ((attr = element.getAttribute(LogContract.Note.Add.deleted)) != null) {
            note.setDeleted(attr.getLongValue());
        }
        note.setRealized(true);
        note.setInitialized(true);
        data.database.NoteOperations.addNote(note, writableDb, context);
    }

    private static void performUpdate(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        Note note = Note.newInstance();
        Attribute attr;
        note.setId(element.getAttribute(LogContract.Note.Update.id).getLongValue());
        note.setTypeId(element.getAttribute(LogContract.Note.Update.typeId).getLongValue());
        note.setCreateDate(element.getAttribute(LogContract.Note.Update.createDate).getLongValue());
        note.setModifyDate(element.getAttribute(LogContract.Note.Update.modifyDate).getLongValue());
        if ((attr = element.getAttribute(LogContract.Note.Update.displayTitleFront)) != null) {
            note.setDisplayTitleFront(attr.getValue());
        }
        if ((attr = element.getAttribute(LogContract.Note.Update.displayDetailsFront)) != null) {
            note.setDisplayDetailsFront(attr.getValue());
        }
        if ((attr = element.getAttribute(LogContract.Note.Update.displayTitleBack)) != null) {
            note.setDisplayTitleBack(attr.getValue());
        }
        if ((attr = element.getAttribute(LogContract.Note.Update.displayDetailsBack)) != null) {
            note.setDisplayDetailsBack(attr.getValue());
        }
        if ((attr = element.getAttribute(LogContract.Note.Update.deleted)) != null) {
            note.setDeleted(attr.getLongValue());
        }
        note.setRealized(true);
        note.setInitialized(true);
        data.database.NoteOperations.updateNote(note, writableDb);
    }

    private static void performMarkAsDeleted(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        Note note = Note.newInstance();
        note.setId(element.getAttribute(LogContract.Note.MarkAsDeleted.id).getLongValue());
        note.setDeleted(element.getAttribute(LogContract.Note.MarkAsDeleted.deleted).getLongValue());
        data.database.NoteOperations.markAsDeleted(note, writableDb);
    }

    private static void performMarkAsNotDeleted(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        Note note = Note.newInstance();
        note.setId(element.getAttribute(LogContract.Note.MarkAsNotDeleted.id).getLongValue());
        data.database.NoteOperations.markAsNotDeleted(note, writableDb);
    }

    private static void performDelete(Element element, SQLiteDatabase writableDb, SQLiteDatabase fileWritableDb, long profileId) throws DataConversionException {
        Note note = Note.newInstance();
        note.setId(element.getAttribute(LogContract.Note.Delete.id).getLongValue());
        data.database.NoteOperations.deleteNoteWithRelatedContent(note, writableDb, fileWritableDb, profileId);
    }

    private static void performSetLabelToNote(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        Note note = Note.newInstance();
        note.setId(element.getAttribute(LogContract.Note.SetLabelToNote.noteId).getLongValue());
        long labelId = element.getAttribute(LogContract.Note.SetLabelToNote.labelId).getLongValue();
        data.database.NoteOperations.setLabelToNote(note, labelId, writableDb);
    }

    private static void performUnsetLabelFromNote(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        Note note = Note.newInstance();
        note.setId(element.getAttribute(LogContract.Note.UnsetLabelFromNote.noteId).getLongValue());
        long labelId = element.getAttribute(LogContract.Note.UnsetLabelFromNote.labelId).getLongValue();
        data.database.NoteOperations.unsetLabelFromNote(note, labelId, writableDb);
    }

    private static void performUnsetAllLabelsFromNote(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        Note note = Note.newInstance();
        note.setId(element.getAttribute(LogContract.Note.UnsetAllLabelsFromNote.noteId).getLongValue());
        data.database.NoteOperations.unsetAllLabelsFromNote(note, writableDb);
    }

    //=========================================== Write ============================================
    public static void addNote(Note note, Context context) {
        Element element = new Element(LogContract.Note.Add.itemName);
        element.setAttribute(LogContract.Note.Add.id, String.valueOf(note.getId()));
        element.setAttribute(LogContract.Note.Add.typeId, String.valueOf(note.getTypeId()));
        element.setAttribute(LogContract.Note.Add.createDate, String.valueOf(note.getCreateDate()));
        element.setAttribute(LogContract.Note.Add.modifyDate, String.valueOf(note.getModifyDate()));
        if (note.getDisplayTitleFront() != null) {
            element.setAttribute(LogContract.Note.Add.displayTitleFront, note.getDisplayTitleFront());
        }
        if (note.getDisplayDetailsFront() != null) {
            element.setAttribute(LogContract.Note.Add.displayDetailsFront, note.getDisplayDetailsFront());
        }
        if (note.getDisplayTitleBack() != null) {
            element.setAttribute(LogContract.Note.Add.displayTitleBack, note.getDisplayTitleBack());
        }
        if (note.getDisplayDetailsBack() != null) {
            element.setAttribute(LogContract.Note.Add.displayDetailsBack, note.getDisplayDetailsBack());
        }
        if (note.getDeleted() != null) {
            element.setAttribute(LogContract.Note.Add.deleted, String.valueOf(note.getDeleted()));
        }
        LogOperations.addNoteOperation(element, context);
    }

    public static void updateNote(Note note, Context context) {
        Element element = new Element(LogContract.Note.Update.itemName);
        element.setAttribute(LogContract.Note.Update.id, String.valueOf(note.getId()));
        element.setAttribute(LogContract.Note.Update.typeId, String.valueOf(note.getTypeId()));
        element.setAttribute(LogContract.Note.Update.createDate, String.valueOf(note.getCreateDate()));
        element.setAttribute(LogContract.Note.Update.modifyDate, String.valueOf(note.getModifyDate()));
        if (note.getDisplayTitleFront() != null) {
            element.setAttribute(LogContract.Note.Update.displayTitleFront, note.getDisplayTitleFront());
        }
        if (note.getDisplayDetailsFront() != null) {
            element.setAttribute(LogContract.Note.Update.displayDetailsFront, note.getDisplayDetailsFront());
        }
        if (note.getDisplayTitleBack() != null) {
            element.setAttribute(LogContract.Note.Update.displayTitleBack, note.getDisplayTitleBack());
        }
        if (note.getDisplayDetailsBack() != null) {
            element.setAttribute(LogContract.Note.Update.displayDetailsBack, note.getDisplayDetailsBack());
        }
        if (note.getDeleted() != null) {
            element.setAttribute(LogContract.Note.Update.deleted, String.valueOf(note.getDeleted()));
        }
        LogOperations.addNoteOperation(element, context);
    }

    public static void deleteNote(Note note, Context context) {
        Element element = new Element(LogContract.Note.Delete.itemName);
        element.setAttribute(LogContract.Note.Delete.id, String.valueOf(note.getId()));
        LogOperations.addNoteOperation(element, context);
    }

    public static void markAsDeleted(Note note, Context context) {
        Element element = new Element(LogContract.Note.MarkAsDeleted.itemName);
        element.setAttribute(LogContract.Note.MarkAsDeleted.id, String.valueOf(note.getId()));
        element.setAttribute(LogContract.Note.MarkAsDeleted.deleted, String.valueOf(note.getDeleted()));
        LogOperations.addNoteOperation(element, context);
    }

    public static void markAsNotDeleted(Note note, Context context) {
        Element element = new Element(LogContract.Note.MarkAsNotDeleted.itemName);
        element.setAttribute(LogContract.Note.MarkAsNotDeleted.id, String.valueOf(note.getId()));
        LogOperations.addNoteOperation(element, context);
    }

    public static void setLabelToNote(Note note, long labelId, Context context) {
        Element element = new Element(LogContract.Note.SetLabelToNote.itemName);
        element.setAttribute(LogContract.Note.SetLabelToNote.noteId, String.valueOf(note.getId()));
        element.setAttribute(LogContract.Note.SetLabelToNote.labelId, String.valueOf(labelId));
        LogOperations.addNoteOperation(element, context);
    }

    public static void unsetLabelFromNote(Note note, long labelId, Context context) {
        Element element = new Element(LogContract.Note.UnsetLabelFromNote.itemName);
        element.setAttribute(LogContract.Note.UnsetLabelFromNote.noteId, String.valueOf(note.getId()));
        element.setAttribute(LogContract.Note.UnsetLabelFromNote.labelId, String.valueOf(labelId));
        LogOperations.addNoteOperation(element, context);
    }

    public static void unsetAllLabelsFromNote(Note note, Context context) {
        Element element = new Element(LogContract.Note.UnsetAllLabelsFromNote.itemName);
        element.setAttribute(LogContract.Note.UnsetAllLabelsFromNote.noteId, String.valueOf(note.getId()));
        LogOperations.addNoteOperation(element, context);
    }
}
