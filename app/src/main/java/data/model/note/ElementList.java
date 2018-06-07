package data.model.note;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Ahmad on 01/08/18.
 * All rights reserved.
 */

public class ElementList extends Element {
    ArrayList<ListItem> items;

    private ElementList() {
        items = new ArrayList<>();
    }

    public static ElementList newInstance() {
        return new ElementList();
    }

    public int getItemCount() {
        return items.size();
    }

    public void addItem(ListItem item) {
        items.add(item);
    }

    public void addAllItems(Collection<ListItem> items) {
        this.items.addAll(items);
    }

    public ListItem findItemByDataId(long dataId) {
        for (ListItem item : items) {
            if (item.dataId == dataId)
                return item;
        }
        return null;
    }

    public ListItem getItemAt(int index) {
        return items.get(index);
    }

    public ArrayList<ListItem> getSortedList() {
        ArrayList<ListItem> items = new ArrayList<>(this.items);
        Collections.sort(items, new ListItemComparator());
        return items;
    }


    @Override
    public boolean areSubFieldsEqual(Element second) {
        if (second instanceof ElementList) {
            ElementList listSecond = (ElementList) second;
            if (listSecond.items != null && items != null && listSecond.items.size() == items.size()) {
                int size = items.size();
                boolean equal = true;
                for (int i = 0; i < size; i++) {
                    ListItem item = items.get(i);
                    ListItem secondItem = listSecond.items.get(i);
                    equal = equal && item.equals(secondItem);
                }
                return equal;
            }
        }
        return false;
    }

    @Override
    public boolean hasContent() {
        if (items != null && items.size() != 0) {
            for (ListItem item : items) {
                if (item.text != null && !item.text.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static class ListItem {
        long dataId;
        String text;
        int position;

        private ListItem () {

        }

        public static ListItem newInstance() {
            return new ListItem();
        }

        public long getDataId() {
            return dataId;
        }

        public void setDataId(long dataId) {
            this.dataId = dataId;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public boolean equals(ListItem second) {
            boolean equal = second != null;
            equal = equal && dataId == second.dataId;
            equal = equal && position == second.position;
            equal = equal && (text == null? "" : text).equals(second.text);
            return equal;
        }
    }

    private static class ListItemComparator implements Comparator<ListItem> {
        @Override
        public int compare(ListItem o1, ListItem o2) {
            return Integer.compare(o1.getPosition(), o2.getPosition());
        }
    }

}
