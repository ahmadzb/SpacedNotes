package data.xml.log.operations;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.jdom2.DataConversionException;
import org.jdom2.Element;

import data.model.note.Note;
import data.xml.log.operator.LogContract;
import data.xml.log.operator.LogOperations;

/**
 * Created by Ahmad on 02/04/18.
 * All rights reserved.
 */

public class PictureOperations {
    //============================================ Read ============================================
    public static void performOperation(Element element, long time, SQLiteDatabase writableDb, Context context) {
        String name = element.getName();
        try {
            if (name.equals(LogContract.Pictures.SubmitPicture.itemName)) {
                performSubmitPicture(element, writableDb);
            } else if (name.equals(LogContract.Pictures.DeletePicture.itemName)) {
                performDeletePicture(element, writableDb);
            }
        } catch (DataConversionException e) {
            e.printStackTrace();
        }
    }

    private static void performSubmitPicture(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        Note note = Note.newInstance();
        note.setId(element.getAttribute(LogContract.Pictures.SubmitPicture.noteId).getLongValue());
        long pictureId = element.getAttribute(LogContract.Pictures.SubmitPicture.pictureId).getLongValue();
        data.database.PictureOperations.submitPicture(note, pictureId, writableDb);
    }

    private static void performDeletePicture(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        long pictureId = element.getAttribute(LogContract.Pictures.DeletePicture.pictureId).getLongValue();
        data.database.PictureOperations.markAsDeleted(pictureId, writableDb);
    }

    //=========================================== Write ============================================
    public static void submitPicture(Note note, long pictureId, Context context) {
        Element element = new Element(LogContract.Pictures.SubmitPicture.itemName);
        element.setAttribute(LogContract.Pictures.SubmitPicture.noteId, String.valueOf(note.getId()));
        element.setAttribute(LogContract.Pictures.SubmitPicture.pictureId, String.valueOf(pictureId));
        LogOperations.addPictureOperation(element, context);
    }

    public static void deletePicture(long pictureId, Context context) {
        Element element = new Element(LogContract.Pictures.DeletePicture.itemName);
        element.setAttribute(LogContract.Pictures.DeletePicture.pictureId, String.valueOf(pictureId));
        LogOperations.addPictureOperation(element, context);
    }
}
