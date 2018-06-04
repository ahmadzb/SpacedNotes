package data.xml.log.operations;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.Collection;

import data.model.note.ElementDivider;
import data.model.note.ElementList;
import data.model.note.ElementPicture;
import data.model.note.ElementText;
import data.model.note.Note;
import data.xml.log.operator.LogContract;
import data.xml.log.operator.LogOperations;

/**
 * Created by Ahmad on 02/02/18.
 * All rights reserved.
 */

public class NoteElementOperations {
    //============================================ Read ============================================
    public static void performOperation(Element element, long time, SQLiteDatabase writableDb, Context context) {
        String name = element.getName();
        try {
            if (name.equals(LogContract.NoteElement.UpdateNoteElements.itemName)) {
                performUpdateNoteElements(element, writableDb, context);
            } else if (name.equals(LogContract.NoteElement.DeleteAllElementsByTypeElement.itemName)) {
                performDeleteAllElementsByTypeElement(element, writableDb);
            } else if (name.equals(LogContract.NoteElement.DeleteAllElementsByNote.itemName)) {
                performDeleteAllElementsByNote(element, writableDb);
            }
        } catch (DataConversionException e) {
            e.printStackTrace();
        }
    }

    private static void performUpdateNoteElements(Element element, SQLiteDatabase writableDb, Context context) throws DataConversionException {
        Note note = Note.newInstance().setId(element.getAttribute(LogContract.NoteElement.UpdateNoteElements.noteId).getLongValue());
        Collection<Element> children = element.getChildren();
        ArrayList<data.model.note.Element> elements = new ArrayList<>(children.size());
        for (Element child : children) {
            long typeElementId = child.getAttribute(LogContract.NoteElement.UpdateNoteElements.Element.typeElementId).getLongValue();
            Long groupId = null;
            {
                Attribute attr = child.getAttribute(LogContract.NoteElement.UpdateNoteElements.Element.groupId);
                if (attr != null) {
                    groupId = attr.getLongValue();
                }
            }
            Integer position = null;
            {
                Attribute attr = child.getAttribute(LogContract.NoteElement.UpdateNoteElements.Element.position);
                if (attr != null) {
                    position = attr.getIntValue();
                }
            }
            int pattern = child.getAttribute(LogContract.NoteElement.UpdateNoteElements.Element.pattern).getIntValue();

            data.model.note.Element noteElement;
            if (pattern == data.model.note.Element.PATTERN_TEXT_ITEM) {
                Element textElement = child.getChild(LogContract.NoteElement.UpdateNoteElements.Element.Text.itemName);

                ElementText text = ElementText.newInstance();
                noteElement = text;
                text.setDataId(textElement.getAttribute(LogContract.NoteElement.UpdateNoteElements.Element.Text.dataId).getLongValue());
                {
                    Attribute attr = textElement.getAttribute(LogContract.NoteElement.UpdateNoteElements.Element.Text.text);
                    if (attr != null) {
                        text.setText(attr.getValue());
                    }
                }
            } else if (pattern == data.model.note.Element.PATTERN_LIST_ITEM) {
                ElementList list = ElementList.newInstance();
                noteElement = list;

                Collection<Element> listElementChildren = child.getChildren();
                ArrayList<ElementList.ListItem> items = new ArrayList<>(listElementChildren.size());
                for (Element listElementChild : listElementChildren) {
                    ElementList.ListItem item = ElementList.ListItem.newInstance();
                    item.setDataId(listElementChild.getAttribute(
                            LogContract.NoteElement.UpdateNoteElements.Element.ListItem.dataId).getLongValue());
                    item.setPosition(listElementChild.getAttribute(
                            LogContract.NoteElement.UpdateNoteElements.Element.ListItem.position).getIntValue());
                    {
                        Attribute attr = listElementChild.getAttribute(
                                LogContract.NoteElement.UpdateNoteElements.Element.ListItem.text);
                        if (attr != null) {
                            item.setText(attr.getValue());
                        }
                    }
                    items.add(item);
                }
                list.addAllItems(items);
            } else if (pattern == data.model.note.Element.PATTERN_PICTURE_ITEM) {
                ElementPicture picture = ElementPicture.newInstance();
                noteElement = picture;

                Collection<Element> listElementChildren = child.getChildren();
                ArrayList<ElementPicture.PictureItem> items = new ArrayList<>(listElementChildren.size());
                for (Element listElementChild : listElementChildren) {
                    ElementPicture.PictureItem item = ElementPicture.PictureItem.newInstance();
                    item.setDataId(listElementChild.getAttribute(
                            LogContract.NoteElement.UpdateNoteElements.Element.PictureItem.dataId).getLongValue());
                    item.setPosition(listElementChild.getAttribute(
                            LogContract.NoteElement.UpdateNoteElements.Element.PictureItem.position).getIntValue());
                    item.setPictureId(listElementChild.getAttribute(
                            LogContract.NoteElement.UpdateNoteElements.Element.PictureItem.pictureId).getLongValue());

                    items.add(item);
                }
                picture.addAllItems(items);
            } else if (pattern == data.model.note.Element.PATTERN_DIVIDER_ITEM) {
                Element dividerElement = child.getChild(LogContract.NoteElement.UpdateNoteElements.Element.Divider.itemName);

                ElementDivider divider = ElementDivider.newInstance();
                noteElement = divider;
                divider.setDataId(dividerElement.getAttribute(LogContract.NoteElement.UpdateNoteElements.Element.Divider.dataId).getLongValue());
            } else {
                throw new RuntimeException("Given element is not recognized");
            }
            noteElement.setElementId(typeElementId);
            noteElement.setNoteId(note.getId());
            if (groupId != null)
                noteElement.setGroupId(groupId);
            if (position != null)
                noteElement.setPosition(position);
            noteElement.setRealized(true);
            elements.add(noteElement);
        }
        data.database.NoteElementOperations.updateNoteElements(note, elements, writableDb, context);
    }

    private static void performDeleteAllElementsByTypeElement(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        data.model.type.Element typeElement = data.model.type.Element.newInstance();
        typeElement.setId(element.getAttribute(
                LogContract.NoteElement.DeleteAllElementsByTypeElement.typeElementId).getLongValue());
        data.database.NoteElementOperations.deleteAllElementsByTypeElement(typeElement, writableDb);
    }

    private static void performDeleteAllElementsByNote(Element element, SQLiteDatabase writableDb) throws DataConversionException {
        Note note = Note.newInstance();
        note.setId(element.getAttribute(
                LogContract.NoteElement.DeleteAllElementsByNote.noteId).getLongValue());
        data.database.NoteElementOperations.deleteAllElementsByNote(note, writableDb);
    }

    //=========================================== Write ============================================
    public static void updateNoteElements(Note note, ArrayList<data.model.note.Element> elements, Context context) {
        Element updateNoteElement = new Element(LogContract.NoteElement.UpdateNoteElements.itemName);
        updateNoteElement.setAttribute(LogContract.NoteElement.UpdateNoteElements.noteId, String.valueOf(note.getId()));

        for (data.model.note.Element element : elements) {
            Element noteElement = new Element(LogContract.NoteElement.UpdateNoteElements.Element.itemName);
            noteElement.setAttribute(LogContract.NoteElement.UpdateNoteElements.Element.typeElementId,
                    String.valueOf(element.getElementId()));
            noteElement.setAttribute(LogContract.NoteElement.UpdateNoteElements.Element.groupId,
                    String.valueOf(element.getGroupId()));
            noteElement.setAttribute(LogContract.NoteElement.UpdateNoteElements.Element.position,
                    String.valueOf(element.getPosition()));

            if (element instanceof ElementText) {
                noteElement.setAttribute(LogContract.NoteElement.UpdateNoteElements.Element.pattern,
                        String.valueOf(data.model.note.Element.PATTERN_TEXT_ITEM));
                ElementText elementText = (ElementText) element;
                Element text = new Element(LogContract.NoteElement.UpdateNoteElements.Element.Text.itemName);
                text.setAttribute(LogContract.NoteElement.UpdateNoteElements.Element.Text.dataId,
                        String.valueOf(elementText.getDataId()));
                if (elementText.getText() != null) {
                    text.setAttribute(LogContract.NoteElement.UpdateNoteElements.Element.Text.text, elementText.getText());
                }
                noteElement.addContent(text);
            } else if (element instanceof ElementList) {
                noteElement.setAttribute(LogContract.NoteElement.UpdateNoteElements.Element.pattern,
                        String.valueOf(data.model.note.Element.PATTERN_LIST_ITEM));
                ElementList elementList = (ElementList) element;
                for (int i = 0; i < elementList.getItemCount(); i++) {
                    ElementList.ListItem listItem = elementList.getItemAt(i);
                    Element item = new Element(LogContract.NoteElement.UpdateNoteElements.Element.ListItem.itemName);
                    item.setAttribute(LogContract.NoteElement.UpdateNoteElements.Element.ListItem.dataId,
                            String.valueOf(listItem.getDataId()));
                    if (listItem.getText() != null) {
                        item.setAttribute(LogContract.NoteElement.UpdateNoteElements.Element.ListItem.text,
                                listItem.getText());
                    }
                    item.setAttribute(LogContract.NoteElement.UpdateNoteElements.Element.ListItem.position,
                            String.valueOf(listItem.getPosition()));
                    noteElement.addContent(item);
                }
            } else if (element instanceof ElementPicture) {
                noteElement.setAttribute(LogContract.NoteElement.UpdateNoteElements.Element.pattern,
                        String.valueOf(data.model.note.Element.PATTERN_PICTURE_ITEM));
                ElementPicture elementPicture = (ElementPicture) element;
                for (int i = 0; i < elementPicture.getItemCount(); i++) {
                    ElementPicture.PictureItem pictureItem = elementPicture.getItemAt(i);
                    Element item = new Element(LogContract.NoteElement.UpdateNoteElements.Element.PictureItem.itemName);
                    item.setAttribute(LogContract.NoteElement.UpdateNoteElements.Element.PictureItem.dataId,
                            String.valueOf(pictureItem.getDataId()));
                    item.setAttribute(LogContract.NoteElement.UpdateNoteElements.Element.PictureItem.pictureId,
                            String.valueOf(pictureItem.getPictureId()));
                    item.setAttribute(LogContract.NoteElement.UpdateNoteElements.Element.PictureItem.position,
                            String.valueOf(pictureItem.getPosition()));
                    noteElement.addContent(item);
                }
            } else if (element instanceof ElementDivider) {
                noteElement.setAttribute(LogContract.NoteElement.UpdateNoteElements.Element.pattern,
                        String.valueOf(data.model.note.Element.PATTERN_DIVIDER_ITEM));
                ElementDivider elementDivider = (ElementDivider) element;
                Element divider = new Element(LogContract.NoteElement.UpdateNoteElements.Element.Divider.itemName);
                divider.setAttribute(LogContract.NoteElement.UpdateNoteElements.Element.Divider.dataId,
                        String.valueOf(elementDivider.getDataId()));
                noteElement.addContent(divider);
            } else
                throw new RuntimeException("Given element is not recognized");
            updateNoteElement.addContent(noteElement);
        }
        LogOperations.addNoteElementOperation(updateNoteElement, context);
    }

    public static void deleteAllElementsByTypeElement(data.model.type.Element typeElement, Context context) {
        Element element = new Element(LogContract.NoteElement.DeleteAllElementsByTypeElement.itemName);
        element.setAttribute(LogContract.NoteElement.DeleteAllElementsByTypeElement.typeElementId,
                String.valueOf(typeElement.getId()));
        LogOperations.addNoteElementOperation(element, context);
    }

    public static void deleteAllElementsByNote(Note note, Context context) {
        Element element = new Element(LogContract.NoteElement.DeleteAllElementsByNote.itemName);
        element.setAttribute(LogContract.NoteElement.DeleteAllElementsByNote.noteId,
                String.valueOf(note.getId()));
        LogOperations.addNoteElementOperation(element, context);
    }
}
