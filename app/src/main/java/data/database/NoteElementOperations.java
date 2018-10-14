package data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.TreeMap;

import data.model.note.Element;
import data.model.note.ElementDivider;
import data.model.note.ElementList;
import data.model.note.ElementPicture;
import data.model.note.ElementText;
import data.model.note.Note;
import data.xml.port.IdProvider;

/**
 * Created by Ahmad on 02/02/18.
 * All rights reserved.
 */

public class NoteElementOperations {
    public static ArrayList<Element> getNoteElements(Note note, SQLiteDatabase readableDb) {
        String selection = Contract.NoteData.noteId + " = " + note.getId();
        ArrayList<NoteData> noteDataList = getNoteData(selection, readableDb);

        TreeMap<ElementKey, Element> elementsMap = new TreeMap<>();
        for (NoteData noteData : noteDataList) {
            final ElementKey key = new ElementKey(noteData.elementId, noteData.groupId);
            Element element = elementsMap.get(key);
            final boolean containsKey = (element != null);
            if (Element.isElementText(noteData.pattern)) {
                if (containsKey)
                    throw new RuntimeException("two or more text with same group id is invalid");
                {
                    element = ElementText.newInstance();
                    updateBaseElementByNoteData(element, noteData);
                    element.setRealized(true);
                    elementsMap.put(key, element);
                }
                {
                    ElementText text = (ElementText) element;
                    text.setDataId(noteData.id);
                    text.setText(noteData.data3);
                }
            } else if (Element.isElementListItem(noteData.pattern)) {
                if (!containsKey) {
                    element = ElementList.newInstance();
                    updateBaseElementByNoteData(element, noteData);
                    element.setRealized(true);
                    elementsMap.put(key, element);
                }
                {
                    ElementList.ListItem item = ElementList.ListItem.newInstance();
                    item.setDataId(noteData.id);
                    item.setPosition((int) (long) noteData.data1);
                    item.setText(noteData.data3);
                    item.setSecondText(noteData.data4);

                    ElementList list = (ElementList) element;
                    list.addItem(item);
                }
            } else if (Element.isElementPictureItem(noteData.pattern)) {
                if (!containsKey) {
                    element = ElementPicture.newInstance();
                    updateBaseElementByNoteData(element, noteData);
                    element.setRealized(true);
                    elementsMap.put(key, element);
                }
                {
                    ElementPicture.PictureItem item = ElementPicture.PictureItem.newInstance();
                    item.setDataId(noteData.id);
                    item.setPictureId(noteData.data1);
                    item.setPosition((int) (long) noteData.data2);

                    ElementPicture pictures = (ElementPicture) element;
                    pictures.addItem(item);
                }
            } else if (Element.isElementDividerItem(noteData.pattern)) {
                if (containsKey)
                    throw new RuntimeException("two or more divider with same group id is invalid");
                {
                    element = ElementDivider.newInstance();
                    updateBaseElementByNoteData(element, noteData);
                    element.setRealized(true);
                    elementsMap.put(key, element);
                }
                {
                    ElementDivider divider = (ElementDivider) element;
                    divider.setDataId(noteData.id);
                }
            }  else {
                throw new RuntimeException("Pattern was not recognized");
            }
        }
        return new ArrayList<>(elementsMap.values());
    }

    private static void updateBaseElementByNoteData(Element element, NoteData noteData) {
        element.setElementId(noteData.elementId);
        element.setGroupId(noteData.groupId);
        element.setNoteId(noteData.noteId);
        element.setPosition(noteData.position);
    }

    public static void updateNoteElements(Note note, ArrayList<Element> elements, SQLiteDatabase writableDb, Context context) {
        //Delete old ones
        {
            String selection = Contract.NoteData.noteId + " = " + note.getId();
            writableDb.delete(Contract.NoteData.table, selection, null);
        }
        //Add new ones
        {
            for (Element element : elements) {
                if (element instanceof ElementText) {
                    ElementText text = (ElementText) element;
                    NoteData noteData = new NoteData();
                    updateBaseNoteDataByElement(noteData, text);
                    if (text.isRealized()) {
                        noteData.id = text.getDataId();
                    } else {
                        noteData.id = IdProvider.nextNoteDataId(context);
                    }
                    noteData.pattern = Element.PATTERN_TEXT_ITEM;
                    noteData.data3 = text.getText();
                    text.setDataId(writableDb.insert(Contract.NoteData.table, null, getNoteDataContentValues(noteData)));
                } else if (element instanceof ElementList) {
                    ElementList list = (ElementList) element;
                    for (int i = 0; i < list.getItemCount(); i++) {
                        ElementList.ListItem item = list.getItemAt(i);
                        NoteData noteData = new NoteData();
                        updateBaseNoteDataByElement(noteData, element);
                        if (list.isRealized()) {
                            noteData.id = item.getDataId();
                        } else {
                            noteData.id = IdProvider.nextNoteDataId(context);
                        }
                        noteData.pattern = Element.PATTERN_LIST_ITEM;
                        noteData.data1 = (long) (int) item.getPosition();
                        noteData.data3 = item.getText();
                        noteData.data4 = item.getSecondText();
                        item.setDataId(writableDb.insert(Contract.NoteData.table, null, getNoteDataContentValues(noteData)));
                    }
                } else if (element instanceof ElementPicture) {
                    ElementPicture pictures = (ElementPicture) element;
                    for (int i = 0; i < pictures.getItemCount(); i++) {
                        ElementPicture.PictureItem item = pictures.getItemAt(i);
                        NoteData noteData = new NoteData();
                        updateBaseNoteDataByElement(noteData, element);
                        if (pictures.isRealized()) {
                            noteData.id = item.getDataId();
                        } else {
                            noteData.id = IdProvider.nextNoteDataId(context);
                        }
                        noteData.pattern = Element.PATTERN_PICTURE_ITEM;
                        noteData.data1 = item.getPictureId();
                        noteData.data2 = (long) (int) item.getPosition();
                        item.setDataId(writableDb.insert(Contract.NoteData.table, null, getNoteDataContentValues(noteData)));
                    }
                } else if (element instanceof ElementDivider) {
                    ElementDivider divider = (ElementDivider) element;
                    NoteData noteData = new NoteData();
                    updateBaseNoteDataByElement(noteData, divider);
                    if (divider.isRealized()) {
                        noteData.id = divider.getDataId();
                    } else {
                        noteData.id = IdProvider.nextNoteDataId(context);
                    }
                    noteData.pattern = Element.PATTERN_DIVIDER_ITEM;
                    divider.setDataId(writableDb.insert(Contract.NoteData.table, null, getNoteDataContentValues(noteData)));
                } else
                    throw new RuntimeException("Given element is not recognized");
            }
        }
    }

    private static void updateBaseNoteDataByElement(NoteData noteData, Element element) {
        noteData.isRealized = element.isRealized();
        noteData.elementId = element.getElementId();
        noteData.groupId = element.getGroupId();
        noteData.noteId = element.getNoteId();
        noteData.position = element.getPosition();
    }

    public static boolean hasRelatedElements(data.model.type.Element typeElement, SQLiteDatabase readableDb) {
        String countColumn = "countColumn";
        String sql = "SELECT COUNT(*) AS " + countColumn +
                " FROM " + Contract.NoteData.table +
                " WHERE " + Contract.NoteData.elementId + " = " + typeElement.getId();
        Cursor cursor = readableDb.rawQuery(sql, null);
        cursor.moveToFirst();
        int count = cursor.getInt(cursor.getColumnIndex(countColumn));
        cursor.close();
        return count != 0;
    }

    public static int deleteAllElementsByTypeElement(data.model.type.Element typeElement, SQLiteDatabase writableDb) {
        String selection = Contract.NoteData.elementId + " = " + typeElement.getId();
        int count = writableDb.delete(Contract.NoteData.table, selection, null);
        return count;
    }

    public static int deleteAllElementsByNote(Note note, SQLiteDatabase writableDb) {
        String selection = Contract.NoteData.noteId + " = " + note.getId();
        int count = writableDb.delete(Contract.NoteData.table, selection, null);
        return count;
    }

    //============================================ Read ============================================
    private static ArrayList<NoteData> getNoteData(String selection, SQLiteDatabase readableDb) {
        Cursor cursor = readableDb.query(Contract.NoteData.table, null, selection, null, null, null, null);

        int indexId = cursor.getColumnIndex(Contract.NoteData.id);
        int indexNoteId = cursor.getColumnIndex(Contract.NoteData.noteId);
        int indexElementId = cursor.getColumnIndex(Contract.NoteData.elementId);
        int indexGroupId = cursor.getColumnIndex(Contract.NoteData.groupId);
        int indexPosition = cursor.getColumnIndex(Contract.NoteData.position);
        int indexPattern = cursor.getColumnIndex(Contract.NoteData.pattern);
        int indexData1 = cursor.getColumnIndex(Contract.NoteData.data1);
        int indexData2 = cursor.getColumnIndex(Contract.NoteData.data2);
        int indexData3 = cursor.getColumnIndex(Contract.NoteData.data3);
        int indexData4 = cursor.getColumnIndex(Contract.NoteData.data4);

        ArrayList<NoteData> noteDataList = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            NoteData noteData = new NoteData();
            noteData.id = cursor.getLong(indexId);
            noteData.noteId = cursor.getLong(indexNoteId);
            noteData.elementId = cursor.getLong(indexElementId);
            if (!cursor.isNull(indexGroupId)) {
                noteData.groupId = cursor.getLong(indexGroupId);
            }
            if (!cursor.isNull(indexPosition)) {
                noteData.position = cursor.getInt(indexPosition);
            }
            noteData.pattern = cursor.getInt(indexPattern);
            if (!cursor.isNull(indexData1)) {
                noteData.data1 = cursor.getLong(indexData1);
            }
            if (!cursor.isNull(indexData2)) {
                noteData.data2 = cursor.getLong(indexData2);
            }
            if (!cursor.isNull(indexData3)) {
                noteData.data3 = cursor.getString(indexData3);
            }
            if (!cursor.isNull(indexData4)) {
                noteData.data4 = cursor.getString(indexData4);
            }
            noteDataList.add(noteData);
        }
        cursor.close();
        return noteDataList;
    }

    //============================================ Write ===========================================
    private static ContentValues getNoteDataContentValues(NoteData noteData) {
        ContentValues values = new ContentValues();
        values.put(Contract.NoteData.id, noteData.id);
        values.put(Contract.NoteData.noteId, noteData.noteId);
        values.put(Contract.NoteData.elementId, noteData.elementId);
        values.put(Contract.NoteData.groupId, noteData.groupId);
        values.put(Contract.NoteData.position, noteData.position);
        values.put(Contract.NoteData.pattern, noteData.pattern);
        if (noteData.data1 != null) {
            values.put(Contract.NoteData.data1, noteData.data1);
        } else {
            values.putNull(Contract.NoteData.data1);
        }
        if (noteData.data2 != null) {
            values.put(Contract.NoteData.data2, noteData.data2);
        } else {
            values.putNull(Contract.NoteData.data2);
        }
        if (noteData.data3 != null) {
            values.put(Contract.NoteData.data3, noteData.data3);
        } else {
            values.putNull(Contract.NoteData.data3);
        }
        if (noteData.data4 != null) {
            values.put(Contract.NoteData.data4, noteData.data4);
        } else {
            values.putNull(Contract.NoteData.data4);
        }
        return values;
    }

    //===================================== Inner Representation ===================================
    private static class NoteData implements Comparable<NoteData> {
        long id;
        long noteId;
        long elementId;
        long groupId;
        int position;
        int pattern;
        Long data1;
        Long data2;
        String data3;
        String data4;
        boolean isRealized;

        @Override
        public int compareTo(@NonNull NoteData o) {
            int result = Long.compare(elementId, o.elementId);
            if (result == 0) {
                result = Long.compare(groupId, o.groupId);
            }
            return result;
        }
    }

    private static class ElementKey implements Comparable<ElementKey> {
        final long elementId;
        final Long groupId;

        public ElementKey(long elementId, Long groupId) {
            this.elementId = elementId;
            this.groupId = groupId;
        }

        @Override
        public int compareTo(@NonNull ElementKey o) {
            int result = Long.compare(elementId, o.elementId);
            if (result == 0 && groupId != null && o.groupId != null) {
                result = Long.compare(groupId, o.groupId);
            }
            return result;
        }
    }
}
