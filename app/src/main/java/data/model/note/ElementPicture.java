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
    }

    private static class PictureItemComparator implements Comparator<PictureItem> {
        @Override
        public int compare(PictureItem o1, PictureItem o2) {
            return Integer.compare(o1.getPosition(), o2.getPosition());
        }
    }
}
