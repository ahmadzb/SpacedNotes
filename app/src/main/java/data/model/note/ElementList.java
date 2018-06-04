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
    }

    private static class ListItemComparator implements Comparator<ListItem> {
        @Override
        public int compare(ListItem o1, ListItem o2) {
            return Integer.compare(o1.getPosition(), o2.getPosition());
        }
    }
}
