package com.diplinkblaze.spacednote.universal.util;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import util.Flags;

/**
 * Created by Ahmad on 09/18/17.
 * All rights reserved.
 */

public class ListData {
    private static final String KEY_MARKED_ITEMS = "markedItems";

    public static final int MODE_FLAG_HEADERS = 0b1;
    public static final int MODE_FLAG_TREE = 0b10;
    public static final int MODE_FLAG_SWAPPABLE = 0b100;
    public static final int MODE_FLAG_MARKABLE = 0b1000;
    public static final int MODE_FLAG_MORE_BUTTON = 0b10000;

    public static final int MARK_ON_FIRST = 1;
    public static final int MARK_ALWAYS = 2;
    public static final int MARK_ALWAYS_CHOOSER = 3;

    private ArrayList<InfoRow> infoRows;

    private int modeFlags;
    private Entity items;

    private int markMode;
    private ArrayList<Long> markedItemsIds;

    public ListData(Entity items, int modeFlags) {
        this(items, modeFlags, MARK_ON_FIRST);
    }

    public ListData(Entity items, int modeFlags, int markMode) {
        this(items, modeFlags, markMode, null);
    }

    public ListData(Entity items, int modeFlags, int markMode, ArrayList<Long> markedItemsEntityId) {
        this.modeFlags = modeFlags;
        this.items = items;
        this.markMode = markMode;
        if (Flags.hasFlags(modeFlags, MODE_FLAG_MARKABLE)) {
            if (markedItemsEntityId == null) {
                this.markedItemsIds = new ArrayList<>();
            } else {
                this.markedItemsIds = new ArrayList<>(markedItemsEntityId);
            }
        }
    }

    public int getCount() {
        return items.getChildrenCountOverall();
    }

    public int getModeFlags() {
        return modeFlags;
    }

    public void setModeFlags(int modeFlags) {
        this.modeFlags = modeFlags;
    }

    public void setMarkMode(int markMode) {
        this.markMode = markMode;
    }

    public Entity getItems() {
        return items;
    }

    public int getMarkMode() {
        return markMode;
    }

    public boolean isMarkMode() {
        return Flags.hasFlags(modeFlags, MODE_FLAG_MARKABLE) && (markMode != MARK_ON_FIRST || markedItemsIds.size() != 0);
    }

    public boolean isItemMarked(Entity item) {
        if (isMarkMode()) {
            for (Long id : markedItemsIds)
                if (id == item.getId())
                    return true;
        }
        return false;
    }

    public ArrayList<Long> getMarkedItems() {
        return markedItemsIds;
    }

    public void setMarkedItemsIds(ArrayList<Long> markedItemsIds) {
        this.markedItemsIds = markedItemsIds;
    }

    public boolean hasMarkedItems() {
        return Flags.hasFlags(modeFlags, MODE_FLAG_MARKABLE) && markedItemsIds.size() != 0;
    }

    public void clearMarkedItems() {
        markedItemsIds.clear();
    }

    public void markItem(Entity item) {
        for (Long id : markedItemsIds)
            if (id == item.getId())
                return;
        markedItemsIds.add(item.getId());
    }

    public void markAllItems(ArrayList<Entity> items) {
        for (Entity entity : items)
            markItem(entity);
    }

    public void removeMarkedItem(Entity item) {
        for (Long id : markedItemsIds) {
            if (id == item.getId()) {
                markedItemsIds.remove(id);
                return;
            }
        }
    }

    public ArrayList<InfoRow> getInfoRows() {
        return infoRows;
    }

    public void setInfoRows(ArrayList<InfoRow> infoRows) {
        if (Flags.hasFlags(modeFlags, MODE_FLAG_SWAPPABLE)) {
            throw new RuntimeException("cannot have actions and swappable at the same time");
        }
        this.infoRows = infoRows;
    }

    public Entity findItemByEntityId(long entityId) {
        if (items.getEntityId() == entityId)
            return items;
        else
            return items.findChildByEntityId(entityId);
    }

    public ArrayList<Entity> findItemsByEntityIdsIfExist(ArrayList<Long> entityIds) {
        ArrayList<Entity> entities = new ArrayList<>(entityIds.size());
        TreeSet<Long> map = new TreeSet<Long>(entityIds);
        if (map.contains(items.getEntityId()))
            entities.add(items);
        items.addChildrenByEntityIds(entities, map);
        if (entities.size() > entityIds.size())
            throw new RuntimeException("More entities are returned than entity ids, something " +
                    "must have gone wrong");
        return entities;
    }

    public ArrayList<Entity> findItemsByEntityIds(ArrayList<Long> entityIds) {
        ArrayList<Entity> entities = new ArrayList<>(entityIds.size());
        TreeSet<Long> map = new TreeSet<Long>(entityIds);
        if (map.contains(items.getEntityId()))
            entities.add(items);
        items.addChildrenByEntityIds(entities, map);
        if (entities.size() < entityIds.size())
            throw new RuntimeException("One or more entities weren't found");
        else if (entities.size() > entityIds.size())
            throw new RuntimeException("More entities are returned than entity ids, something " +
                    "must have gone wrong");
        return entities;
    }


    public void saveInstanceState(Bundle outState, String keyPrefix) {
        if (Flags.hasFlags(modeFlags, MODE_FLAG_MARKABLE)) {
            outState.putSerializable(keyPrefix + KEY_MARKED_ITEMS, markedItemsIds);
        }
    }

    public void restoreInstanceState(Bundle savedInstanceState, String keyPrefix) {
        if (Flags.hasFlags(modeFlags, MODE_FLAG_MARKABLE)) {
            markedItemsIds = (ArrayList<Long>) savedInstanceState.getSerializable(keyPrefix + KEY_MARKED_ITEMS);
        }
    }

    public static ArrayList<Long> makeEntityIdList(ArrayList<Entity> entities) {
        ArrayList<Long> idList = new ArrayList<>(entities.size());
        for (Entity entity : entities)
            idList.add(entity.getEntityId());
        return idList;
    }

    public static ArrayList<Long> makeIdList(ArrayList<Entity> entities) {
        ArrayList<Long> idList = new ArrayList<>(entities.size());
        for (Entity entity : entities) {
            if (entity.isValid())
                idList.add(entity.getId());
            else
                throw new RuntimeException("One or more entities are not valid");
        }
        return idList;
    }


    //=============================================================================
    public static abstract class Entity {
        private ArrayList<Entity> children;
        private Entity backReference;

        public abstract long getEntityId();

        public abstract long getId();

        public boolean isValid() {
            return true;
        }

        public String getTitle(Resources resources) {
            return null;
        }

        public Integer getTitleIconResId() {
            return null;
        }

        public String getDetails(Resources resources) {
            return null;
        }

        public String getValue(Context context) {
            return null;
        }

        public String getFooter() {
            return null;
        }

        public String getFooter2() {
            return null;
        }

        public String getExtra1() {
            return null;
        }

        public Integer getExtra1Icon() {
            return null;
        }

        public String getExtra2() {
            return null;
        }

        public Integer getExtra2Icon() {
            return null;
        }

        public Long getHeaderId() {
            return null;
        }

        public String getHeader(Resources resources) {
            return null;
        }

        public String getHeaderValue(Context context) {
            return null;
        }

        public String getHeaderValue2(Context context) {
            return null;
        }

        public int getIconResId() {
            return 0;
        }

        public boolean hasMoreButton() {
            return false;
        }

        public boolean hideNextButton() {
            return false;
        }

        public String getIconText(Resources resources) {
            String iconText = getTitle(resources);
            if (iconText != null && iconText.length() != 0) {
                iconText = iconText.substring(0, 1);
            }
            return iconText;
        }

        public MetaData getMetadata() {
            return new MetaData();
        }

        public boolean overrideMetadataForChildren() {
            return false;
        }

        public ArrayList<Entity> getChildren() {
            return children;
        }

        public Entity getChildAt(int index) {
            return children.get(index);
        }

        public Entity removeChildAt(int index) {
            return children.remove(index);
        }

        public boolean removeChild(Entity entity) {
            return children.remove(entity);
        }

        public void removeChildren() {
            children = null;
        }

        public boolean hasChildren() {
            return children != null && children.size() != 0;
        }

        public Entity getBackReference() {
            return backReference;
        }

        public boolean isRoot() {
            return backReference == null;
        }

        public int getChildrenCountImmediate() {
            if (children == null)
                return 0;
            else
                return children.size();
        }

        public int getChildrenCountOverall() {
            if (!hasChildren())
                return 0;
            else {
                int count = children.size();
                for (Entity entity : children)
                    count += entity.getChildrenCountOverall();
                return count;
            }
        }

        public ArrayList<Entity> flatReferences() {
            ArrayList<Entity> flatReferences = new ArrayList<Entity>(getChildrenCountOverall());
            if (children != null) {
                for (Entity entity : children) {
                    flatten(flatReferences, entity);
                }
            }
            return flatReferences;
        }

        public void flatten() {
            if (children != null) {
                ArrayList<Entity> flattened = new ArrayList<Entity>(getChildrenCountOverall());
                for (Entity entity : children) {
                    flatten(flattened, entity);
                }
                for (Entity entity : flattened) {
                    entity.removeChildren();
                }
                this.children = flattened;
            }
        }

        private void flatten(ArrayList<Entity> flattened, Entity parent) {
            flattened.add(parent);
            if (parent.children != null)
                for (Entity entity : parent.children)
                    flatten(flattened, entity);
        }

        public int getDepth() {
            int depth = 1;
            Entity entity = this;
            while (entity.getBackReference() != null) {
                depth++;
                entity = entity.getBackReference();
            }

            return depth;
        }

        public Entity findChildById(long id) {
            if (hasChildren())
                for (Entity entity : children) {
                    if (entity.getId() == id)
                        return entity;
                    Entity result = entity.findChildById(id);
                    if (result != null) return result;
                }
            return null;
        }

        public Entity findChildByEntityId(long id) {
            if (hasChildren())
                for (Entity entity : children) {
                    if (entity.getEntityId() == id)
                        return entity;
                    Entity result = entity.findChildByEntityId(id);
                    if (result != null) return result;
                }
            return null;
        }

        public ArrayList<Entity> findChildrenByEntityIds(ArrayList<Long> entityIds) {
            ArrayList<Entity> entities = new ArrayList<>(entityIds.size());
            TreeSet<Long> map = new TreeSet<Long>(entityIds);
            addChildrenByEntityIds(entities, map);
            if (entities.size() < entityIds.size())
                throw new RuntimeException("One or more entities weren't found");
            else if (entities.size() > entityIds.size())
                throw new RuntimeException("More entities are returned than entity ids, something " +
                        "must have gone wrong");
            return entities;
        }

        public void addChildrenByEntityIds(ArrayList<Entity> entities, TreeSet<Long> ids) {
            if (hasChildren()) {
                for (Entity entity : children) {
                    if (ids.contains(entity.getEntityId()))
                        entities.add(entity);
                    entity.addChildrenByEntityIds(entities, ids);
                }
            }
        }

        public ArrayList<Entity> findChildrenByIds(ArrayList<Long> ids) {
            ArrayList<Entity> entities = new ArrayList<>(ids.size());
            TreeSet<Long> map = new TreeSet<Long>(ids);
            addChildrenByIds(entities, map);
            if (entities.size() < ids.size())
                throw new RuntimeException("One or more entities weren't found");
            else if (entities.size() > ids.size())
                throw new RuntimeException("More entities are returned than entity ids, something " +
                        "must have gone wrong");
            return entities;
        }

        public void addChildrenByIds(ArrayList<Entity> entities, TreeSet<Long> ids) {
            if (hasChildren()) {
                for (Entity entity : children) {
                    if (ids.contains(entity.getId()))
                        entities.add(entity);
                    entity.addChildrenByIds(entities, ids);
                }
            }
        }

        public void clearChildren(int capacity) {
            children = new ArrayList<>(capacity);
        }

        public void addChild(Entity entity) {
            if (children == null)
                children = new ArrayList<Entity>();
            children.add(entity);
            entity.backReference = this;
        }

        public void addChildren(Collection<? extends Entity> entities) {
            if (children == null)
                children = new ArrayList<Entity>();
            children.addAll(entities);
            for (Entity entity : entities)
                entity.backReference = this;
        }

        @Override
        public String toString() {
            return "EntityId: " + getEntityId();
        }
    }

    public static class MetaData implements Serializable {
        public static final int ICON_MODE_VISIBLE_RES_ID = 1;
        public static final int ICON_MODE_VISIBLE = 2;
        public static final int ICON_MODE_INVISIBLE = 3;
        public static final int ICON_MODE_GONE = 4;

        public int iconMode = ICON_MODE_GONE;
        public boolean hasColorTitle = false;
        public boolean hasColorDetails = false;
        public boolean hasColorValue = false;
        public boolean hasColorIcon = false;
        public boolean hasDivider = false;
        public boolean hasHeaderValueColor = false;
        public boolean hasHeaderValue2Color = false;
        public int colorTitle = 0;
        public int colorDetails = 0;
        public int colorValue = 0;
        public int colorIcon = 0;
        public int colorHeaderValue = 0;
        public int colorHeaderValue2 = 0;
    }

    public static class InfoRow implements Serializable {
        private String text;
        private Integer textResId;
        private double amount;
        private Integer iconResId;
        private int textColor;
        private int valueColor;
        private int iconColor;

        public InfoRow clone() {
            InfoRow infoRow = new InfoRow();
            infoRow.text = this.text;
            infoRow.textResId = this.textResId;
            infoRow.amount = this.amount;
            infoRow.iconResId = this.iconResId;
            infoRow.textColor = this.textColor;
            infoRow.valueColor = this.valueColor;
            infoRow.iconColor = this.iconColor;
            return infoRow;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Integer getTextResId() {
            return textResId;
        }

        public void setTextResId(Integer textResId) {
            this.textResId = textResId;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public Integer getIconResId() {
            return iconResId;
        }

        public void setIconResId(Integer iconResId) {
            this.iconResId = iconResId;
        }

        public int getTextColor() {
            return textColor;
        }

        public void setTextColor(int textColor) {
            this.textColor = textColor;
        }

        public int getValueColor() {
            return valueColor;
        }

        public void setValueColor(int valueColor) {
            this.valueColor = valueColor;
        }

        public int getIconColor() {
            return iconColor;
        }

        public void setIconColor(int iconColor) {
            this.iconColor = iconColor;
        }
    }
}
