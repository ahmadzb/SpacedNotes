package data.model.note;

import java.util.Comparator;

/**
 * Created by Ahmad on 01/08/18.
 * All rights reserved.
 */

public abstract class Element {
    public static final int PATTERN_TEXT_ITEM = 1;
    public static final int PATTERN_LIST_ITEM = 2;
    public static final int PATTERN_PICTURE_ITEM = 3;
    public static final int PATTERN_DIVIDER_ITEM = 4;

    private long noteId;
    private long elementId;
    private long groupId;
    private int position;
    private boolean isRealized;

    public long getNoteId() {
        return noteId;
    }

    public void setNoteId(long noteId) {
        this.noteId = noteId;
    }

    public long getElementId() {
        return elementId;
    }

    public void setElementId(long elementId) {
        this.elementId = elementId;
    }

    public static boolean isElementText(int pattern) {
        return pattern == PATTERN_TEXT_ITEM;
    }

    public static boolean isElementPictureItem(int pattern) {
        return pattern == PATTERN_PICTURE_ITEM;
    }

    public static boolean isElementListItem(int pattern) {
        return pattern == PATTERN_LIST_ITEM;
    }

    public static boolean isElementDividerItem(int pattern) {
        return pattern == PATTERN_DIVIDER_ITEM;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isRealized() {
        return isRealized;
    }

    public void setRealized(boolean realized) {
        isRealized = realized;
    }

    public boolean equals(Element second) {
        boolean areEqual = second != null;
        areEqual = areEqual && second.isRealized == isRealized;
        areEqual = areEqual && second.elementId == elementId;
        areEqual = areEqual && second.groupId == groupId;
        areEqual = areEqual && second.position == position;
        areEqual = areEqual && second.noteId == noteId;
        return areEqual && areSubFieldsEqual(second);
    }

    public boolean equalContents(Element second) {
        boolean areEqual = second != null;
        areEqual = areEqual && second.elementId == elementId;
        return areEqual && areSubFieldsContentEqual(second);
    }

    protected abstract boolean areSubFieldsEqual(Element second);
    protected abstract boolean areSubFieldsContentEqual(Element second);
    public abstract boolean hasContent();

    //========================================= Utils ==============================================

    public static class PositionComparator implements Comparator<Element> {
        @Override
        public int compare(Element o1, Element o2) {
            return Integer.compare(o1.getPosition(), o2.getPosition());
        }
    }
}
