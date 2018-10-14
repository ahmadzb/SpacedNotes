package data.model.note;

import android.content.res.Resources;
import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;

import javax.annotation.concurrent.Immutable;

import data.model.scheduler.RevisionFuture;
import util.Texts;

/**
 * Created by Ahmad on 01/01/18.
 * All rights reserved.
 */

@Immutable
public class Note implements Serializable {
    private long id;
    private long typeId;
    private long createDate;
    private long modifyDate;
    private String displayTitleFront;
    private String displayDetailsFront;
    private String displayTitleBack;
    private String displayDetailsBack;
    private Long deleted;
    private boolean isRealized;
    private boolean isInitialized;

    private RevisionFuture revisionFuture;

    private Note() {
    }

    public static Note newInstance() {
        return new Note();
    }

    public long getId() {
        return id;
    }

    public Note setId(long id) {
        this.id = id;
        return this;
    }

    public long getCreateDate() {
        return createDate;
    }

    public Note setCreateDate(long createDate) {
        this.createDate = createDate;
        return this;
    }

    public long getModifyDate() {
        return modifyDate;
    }

    public Note setModifyDate(long modifyDate) {
        this.modifyDate = modifyDate;
        return this;
    }

    public long getTypeId() {
        return typeId;
    }

    public Note setTypeId(long typeId) {
        this.typeId = typeId;
        return this;
    }

    public String getDisplayTitleFront() {
        return displayTitleFront;
    }

    public void setDisplayTitleFront(String displayTitleFront) {
        this.displayTitleFront = displayTitleFront;
    }

    public String getDisplayDetailsFront() {
        return displayDetailsFront;
    }

    public void setDisplayDetailsFront(String displayDetailsFront) {
        this.displayDetailsFront = displayDetailsFront;
    }

    public String getDisplayTitleBack() {
        return displayTitleBack;
    }

    public void setDisplayTitleBack(String displayTitleBack) {
        this.displayTitleBack = displayTitleBack;
    }

    public String getDisplayDetailsBack() {
        return displayDetailsBack;
    }

    public void setDisplayDetailsBack(String displayDetailsBack) {
        this.displayDetailsBack = displayDetailsBack;
    }

    public Long getDeleted() {
        return deleted;
    }

    public Note setDeleted(Long deleted) {
        this.deleted = deleted;
        return this;
    }

    public boolean isRealized() {
        return isRealized;
    }

    public Note setRealized(boolean realized) {
        isRealized = realized;
        return this;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public Note setInitialized(boolean initialized) {
        isInitialized = initialized;
        return this;
    }

    public static String generateNoteDisplayTitleFront(ArrayList<Element> noteElements,
                                                       TreeMap<Long, data.model.type.Element> typeElements,
                                                       Resources resources) {
        int count = 0;
        for (Element noteElement : noteElements) {
            data.model.type.Element typeElement = typeElements.get(noteElement.getElementId());
            if (typeElement.hasSideFront() && (typeElement.getPattern() == data.model.type.Element.PATTERN_TEXT ||
                    typeElement.getPattern() == data.model.type.Element.PATTERN_LIST)) {
                count++;
            }
            if (count == 1) {
                return getPrimaryDisplayTextForElement(noteElement, resources);
            }
        }
        return null;
    }

    public static String generateNoteDisplayDetailsFront(ArrayList<Element> noteElements,
                                                         TreeMap<Long, data.model.type.Element> typeElements,
                                                         Resources resources) {
        int count = 0;
        for (Element noteElement : noteElements) {
            data.model.type.Element typeElement = typeElements.get(noteElement.getElementId());
            if (typeElement.hasSideFront() && (typeElement.getPattern() == data.model.type.Element.PATTERN_TEXT ||
                    typeElement.getPattern() == data.model.type.Element.PATTERN_LIST)) {
                count++;
            }
            if (count == 2) {
                return getPrimaryDisplayTextForElement(noteElement, resources);
            }
        }
        return null;
    }

    public static String generateNoteDisplayTitleBack(ArrayList<Element> noteElements,
                                                      TreeMap<Long, data.model.type.Element> typeElements,
                                                      Resources resources) {
        int count = 0;
        for (Element noteElement : noteElements) {
            data.model.type.Element typeElement = typeElements.get(noteElement.getElementId());
            if (typeElement.hasSideBack() && !typeElement.hasSideFront() && (typeElement.getPattern() == data.model.type.Element.PATTERN_TEXT ||
                    typeElement.getPattern() == data.model.type.Element.PATTERN_LIST)) {
                count++;
            }
            if (count == 1) {
                return getPrimaryDisplayTextForElement(noteElement, resources);
            }
        }
        return null;
    }

    public static String generateNoteDisplayDetailsBack(ArrayList<Element> noteElements,
                                                        TreeMap<Long, data.model.type.Element> typeElements,
                                                        Resources resources) {
        int count = 0;
        for (Element noteElement : noteElements) {
            data.model.type.Element typeElement = typeElements.get(noteElement.getElementId());
            if (typeElement.hasSideBack() && !typeElement.hasSideFront() && (typeElement.getPattern() == data.model.type.Element.PATTERN_TEXT ||
                    typeElement.getPattern() == data.model.type.Element.PATTERN_LIST)) {
                count++;
            }
            if (count == 2) {
                return getPrimaryDisplayTextForElement(noteElement, resources);
            }
        }
        return null;
    }

    private static String getPrimaryDisplayTextForElement(Element noteElement, Resources resources) {
        String result = null;
        if (noteElement instanceof ElementText) {
            ElementText text = (ElementText) noteElement;
            result = text.getText();
        } else if (noteElement instanceof ElementList) {
            ElementList list = (ElementList) noteElement;
            ArrayList<String> items = new ArrayList<>(list.getItemCount());
            for (int i = 0; i < list.getItemCount(); i++) {
                items.add(list.getItemAt(i).getDisplayText());
            }
            result = Texts.nameItems(items, new Texts.NameItemsCache(null, resources));
        }
        if (result != null && result.length() > 125) {
            result = result.substring(0, 125) + "...";
        }
        return result;
    }

    public RevisionFuture getRevisionFuture() {
        return revisionFuture;
    }

    public void setRevisionFuture(RevisionFuture revisionFuture) {
        this.revisionFuture = revisionFuture;
    }

    public static Comparator<Note> revisionFutureDueDateThenNoteDateComparator() {
        return new Comparator<Note>() {
            @Override
            public int compare(Note o1, Note o2) {
                if (o1.revisionFuture == null && o2.revisionFuture == null) {
                    return Long.compare(o1.createDate, o2.createDate);
                } else if (o1.revisionFuture == null && o2.revisionFuture != null) {
                    return 1;
                } else if (o1.revisionFuture != null && o2.revisionFuture == null) {
                    return -1;
                } else {
                    return Long.compare(o1.revisionFuture.getDueDate(), o2.revisionFuture.getDueDate());
                }
            }
        };
    }

    public static Comparator<Note> createDateComparator() {
        return new Comparator<Note>() {
            @Override
            public int compare(Note o1, Note o2) {
                return -Long.compare(o1.createDate, o2.createDate);
            }
        };
    }
}
