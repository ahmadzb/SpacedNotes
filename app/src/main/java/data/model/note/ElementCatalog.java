package data.model.note;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import data.database.NoteElementOperations;

/**
 * Created by Ahmad on 01/09/18.
 * All rights reserved.
 */

public class ElementCatalog {
    public static ArrayList<Element> getNoteElements(Note note, SQLiteDatabase readableDb) {
        return NoteElementOperations.getNoteElements(note, readableDb);
    }

    public static boolean hasRelatedElements(data.model.type.Element typeElement, SQLiteDatabase readableDb) {
        return NoteElementOperations.hasRelatedElements(typeElement, readableDb);
    }

    public static void updateNoteElements(Note note, ArrayList<Element> elements, SQLiteDatabase writableDb, Context context) {
        NoteElementOperations.updateNoteElements(note, elements, writableDb, context);
        data.xml.log.operations.NoteElementOperations.updateNoteElements(note, elements, context);
    }

    public static int deleteAllElementsByTypeElement(data.model.type.Element typeElement, SQLiteDatabase writableDb, Context context) {
        int count = NoteElementOperations.deleteAllElementsByTypeElement(typeElement, writableDb);
        data.xml.log.operations.NoteElementOperations.deleteAllElementsByTypeElement(typeElement, context);
        return count;
    }

    public static int deleteAllElementsByNote(Note note, SQLiteDatabase writableDb, Context context) {
        int count = NoteElementOperations.deleteAllElementsByNote(note, writableDb);
        data.xml.log.operations.NoteElementOperations.deleteAllElementsByNote(note, context);
        return count;
    }
}
