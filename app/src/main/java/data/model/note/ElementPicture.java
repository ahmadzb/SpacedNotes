package data.model.note;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import data.database.Contract;

/**
 * Created by Ahmad on 01/08/18.
 * All rights reserved.
 */

public class ElementPicture extends Element {
    private ArrayList<PictureItem> pictures;

    private ElementPicture() {
        pictures = new ArrayList<>();
    }

    public static ElementPicture newInstance() {
        return new ElementPicture();
    }

    public int getItemCount() {
        return pictures.size();
    }

    public void addItem(PictureItem item) {
        pictures.add(item);
    }

    public void addAllItems(Collection<PictureItem> pictures) {
        this.pictures.addAll(pictures);
    }

    public PictureItem findItemByDataId(long dataId) {
        for (PictureItem item : pictures) {
            if (item.dataId == dataId)
                return item;
        }
        return null;
    }

    public ArrayList<PictureItem> getSortedList() {
        ArrayList<PictureItem> items = new ArrayList<>(this.pictures);
        Collections.sort(items, new PictureItemComparator());
        return items;
    }

    public PictureItem getItemAt(int index) {
        return pictures.get(index);
    }



    @Override
    public boolean areElementsEqual(Element second) {
        if (second instanceof ElementPicture) {
            ElementPicture listSecond = (ElementPicture) second;
            if (listSecond.pictures != null && pictures != null && listSecond.pictures.size() == pictures.size()) {
                int size = pictures.size();
                boolean equal = true;
                for (int i = 0; i < size; i++) {
                    PictureItem item = pictures.get(i);
                    PictureItem secondItem = listSecond.pictures.get(i);
                    equal = equal && item.equals(secondItem);
                }
                return equal;
            }
        }
        return false;
    }

    @Override
    public boolean hasContent() {
        if (pictures != null && pictures.size() != 0) {
            for (PictureItem item : pictures) {
                if (item.pictureId > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public static class PictureItem {
        long dataId;
        long pictureId;
        int position;

        private PictureItem() {

        }

        public static PictureItem newInstance() {
            return new PictureItem();
        }

        public long getDataId() {
            return dataId;
        }

        public void setDataId(long dataId) {
            this.dataId = dataId;
        }

        public long getPictureId() {
            return pictureId;
        }

        public void setPictureId(long pictureId) {
            this.pictureId = pictureId;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }


        public boolean equals(PictureItem second) {
            boolean equal = second != null;
            equal = equal && dataId == second.dataId;
            equal = equal && position == second.position;
            equal = equal && pictureId == second.pictureId;
            return equal;
        }
    }

    private static class PictureItemComparator implements Comparator<PictureItem> {
        @Override
        public int compare(PictureItem o1, PictureItem o2) {
            return Integer.compare(o1.getPosition(), o2.getPosition());
        }
    }
}
