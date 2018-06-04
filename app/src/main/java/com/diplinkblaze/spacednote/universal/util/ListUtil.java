package com.diplinkblaze.spacednote.universal.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.diplinkblaze.spacednote.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;

import data.model.label.Label;
import data.model.label.LabelCatalog;
import data.model.label.LabelList;
import data.model.label.LabelListCatalog;
import data.model.note.Note;
import data.model.note.NoteCatalog;
import data.model.schedule.Occurrence;
import data.model.schedule.OccurrenceCatalog;
import data.model.schedule.ScheduleCatalog;
import data.model.type.Element;
import data.model.type.ElementCatalog;
import data.model.type.TypeCatalog;
import util.TypeFaceUtils;

/**
 * Created by Ahmad on 09/19/17.
 * All rights reserved.
 */

public class ListUtil {

    public static class Type {
        public static ListData createAvailable(SQLiteDatabase readableDb) {
            ArrayList<data.model.type.Type> types = TypeCatalog.getTypesAvailable(readableDb);
            ArrayList<TypeEntity> entities = new ArrayList<>(types.size());
            for (data.model.type.Type type : types) {
                entities.add(new TypeEntity(type));
            }
            Collections.sort(entities, new TypeEntityComparator());
            TypeEntity root = new TypeEntity(TypeEntity.ROOT_ID);
            root.addChildren(entities);
            return new ListData(root, ListData.MODE_FLAG_SWAPPABLE);
        }

        public static ListData createArchived(SQLiteDatabase readableDb) {
            ArrayList<data.model.type.Type> types = TypeCatalog.getTypesArchived(readableDb);
            ArrayList<TypeEntity> entities = new ArrayList<>(types.size());
            for (data.model.type.Type type : types) {
                entities.add(new TypeEntity(type));
            }
            Collections.sort(entities, new TypeEntityComparator());
            TypeEntity root = new TypeEntity(TypeEntity.ROOT_ID);
            root.addChildren(entities);
            return new ListData(root, 0);
        }

        public static class TypeEntity extends ListData.Entity {
            public static final long ROOT_ID = -1;
            private long entityId;
            private data.model.type.Type type;

            public TypeEntity(long id) {
                entityId = id;
            }

            public TypeEntity(data.model.type.Type type) {
                this.type = type;
                entityId = type.getId();
            }

            @Override
            public long getEntityId() {
                return entityId;
            }

            @Override
            public long getId() {
                if (type == null) {
                    return 0;
                }
                return type.getId();
            }

            @Override
            public String getTitle(Resources resources) {
                if (type == null)
                    return null;
                return type.getTitle();
            }

            @Override
            public ListData.MetaData getMetadata() {
                ListData.MetaData metadata = new ListData.MetaData();
                if (type != null) {
                    metadata.iconMode = ListData.MetaData.ICON_MODE_VISIBLE;
                    metadata.colorIcon = type.getColor();
                    metadata.hasColorIcon = true;
                }
                return metadata;
            }

            public data.model.type.Type getType() {
                return type;
            }
        }

        private static class TypeEntityComparator implements Comparator<TypeEntity> {
            @Override
            public int compare(TypeEntity o1, TypeEntity o2) {
                data.model.type.Type t1 = o1.getType();
                data.model.type.Type t2 = o2.getType();
                if (t1 == t2)
                    return 0;
                if (t1 == null)
                    return 1;
                if (t2 == null)
                    return -1;
                return Integer.compare(t1.getPosition(), t2.getPosition());
            }
        }

        public static void updatePositions(ListData data, @Nullable ListData.Entity root,
                                           SQLiteDatabase writableDb, Context context) {
            int position = 0;
            if (root == null)
                root = data.getItems();
            for (ListData.Entity entity : root.flatReferences()) {
                position++;
                TypeEntity typeEntity = (TypeEntity) entity;
                data.model.type.Type type = typeEntity.getType();
                if (type != null) {
                    TypeCatalog.updateTypePosition(type, position, writableDb, context);
                }
            }
        }

    }

    public static class TypeElement {
        public static ListData createAvailable(data.model.type.Type type, SQLiteDatabase readableDb) {
            type = TypeCatalog.getTypeById(type.getId(), readableDb);
            ElementEntity.Cache cache = ElementEntity.produceCache(type);
            ArrayList<Element> elements = ElementCatalog.getElementsAvailable(type, readableDb);
            ArrayList<ElementEntity> entities = new ArrayList<>(elements.size());
            for (Element element : elements) {
                entities.add(new ElementEntity(element, cache));
            }
            Collections.sort(entities, new ElementEntityComparator());
            ElementEntity root = new ElementEntity(ElementEntity.ROOT_ID);
            root.addChildren(entities);
            return new ListData(root, ListData.MODE_FLAG_SWAPPABLE);
        }

        public static ListData createArchived(data.model.type.Type type, SQLiteDatabase readableDb) {
            type = TypeCatalog.getTypeById(type.getId(), readableDb);
            ElementEntity.Cache cache = ElementEntity.produceCache(type);
            ArrayList<Element> Elements = ElementCatalog.getElementsArchived(type, readableDb);
            ArrayList<ElementEntity> entities = new ArrayList<>(Elements.size());
            for (Element Element : Elements) {
                entities.add(new ElementEntity(Element, cache));
            }
            Collections.sort(entities, new ElementEntityComparator());
            ElementEntity root = new ElementEntity(ElementEntity.ROOT_ID);
            root.addChildren(entities);
            return new ListData(root, 0);
        }

        public static class ElementEntity extends ListData.Entity {
            public static final long ROOT_ID = -1;
            private long entityId;
            private Element element;
            private Cache cache;

            public ElementEntity(long id) {
                entityId = id;
            }

            public ElementEntity(Element element, Cache cache) {
                this.element = element;
                entityId = element.getId();
                this.cache = cache;
            }

            @Override
            public long getEntityId() {
                return entityId;
            }

            @Override
            public long getId() {
                if (element == null) {
                    return 0;
                }
                return element.getId();
            }

            @Override
            public String getTitle(Resources resources) {
                if (element == null)
                    return null;
                String title = element.getTitle();
                if (title == null || title.length() == 0) {
                    if (element.getPattern() == Element.PATTERN_TEXT) {
                        title = "(" + resources.getString(R.string.text) + ")";
                    } else if (element.getPattern() == Element.PATTERN_LIST) {
                        title = "(" + resources.getString(R.string.list) + ")";
                    } else if (element.getPattern() == Element.PATTERN_PICTURE) {
                        title = "(" + resources.getString(R.string.pictures) + ")";
                    } else if (element.getPattern() == Element.PATTERN_DIVIDER) {
                        title = "(" + resources.getString(R.string.divider) + ")";
                    }
                }
                return title;
            }

            @Override
            public int getIconResId() {
                if (element != null) {
                    if (element.getPattern() == Element.PATTERN_TEXT) {
                        return R.drawable.ic_short_text_white;
                    } else if (element.getPattern() == Element.PATTERN_LIST) {
                        return R.drawable.ic_list_white;
                    } else if (element.getPattern() == Element.PATTERN_PICTURE) {
                        return R.drawable.ic_photo_white;
                    } else if (element.getPattern() == Element.PATTERN_DIVIDER) {
                        return R.drawable.ic_divider_white;
                    }
                }

                return 0;
            }

            @Override
            public ListData.MetaData getMetadata() {
                ListData.MetaData metadata = new ListData.MetaData();
                if (element != null) {
                    metadata.iconMode = ListData.MetaData.ICON_MODE_VISIBLE_RES_ID;
                    metadata.colorIcon = cache.typeColor;
                    metadata.hasColorIcon = true;
                }
                return metadata;
            }

            public Element getElement() {
                return element;
            }

            public static Cache produceCache(data.model.type.Type type) {
                if (!type.isInitialized())
                    throw new RuntimeException("Type should be initialized");
                Cache cache = new Cache();
                cache.typeColor = type.getColor();
                return cache;
            }

            public static class Cache {
                int typeColor;
            }
        }

        private static class ElementEntityComparator implements Comparator<ElementEntity> {
            @Override
            public int compare(ElementEntity o1, ElementEntity o2) {
                Element t1 = o1.getElement();
                Element t2 = o2.getElement();
                if (t1 == t2)
                    return 0;
                if (t1 == null)
                    return 1;
                if (t2 == null)
                    return -1;
                return Integer.compare(t1.getPosition(), t2.getPosition());
            }
        }

        public static void updatePositions(ListData data, @Nullable ListData.Entity root,
                                           SQLiteDatabase writableDb, Context context) {
            int position = 0;
            if (root == null)
                root = data.getItems();
            for (ListData.Entity entity : root.flatReferences()) {
                position++;
                ElementEntity ElementEntity = (ElementEntity) entity;
                Element Element = ElementEntity.getElement();
                if (Element != null) {
                    ElementCatalog.updateElementPosition(Element, position, writableDb, context);
                }
            }
        }
    }

    public static class Schedule {

        public static ListData create(SQLiteDatabase readableDb) {
            ArrayList<data.model.schedule.Schedule> schedules = ScheduleCatalog.getSchedules(readableDb);
            ArrayList<ScheduleEntity> entities = new ArrayList<>(schedules.size());
            for (data.model.schedule.Schedule schedule : schedules) {
                entities.add(new ScheduleEntity(schedule));
            }
            Collections.sort(entities, new ScheduleEntityComparator());
            ScheduleEntity root = new ScheduleEntity(ScheduleEntity.ROOT_ID);
            root.addChildren(entities);
            return new ListData(root, ListData.MODE_FLAG_SWAPPABLE);
        }

        public static class ScheduleEntity extends ListData.Entity {
            public static final long ROOT_ID = -1;

            private long entityId;
            private data.model.schedule.Schedule schedule;

            public ScheduleEntity(long entityId) {
                this.entityId = entityId;
            }

            public ScheduleEntity(data.model.schedule.Schedule schedule) {
                this.schedule = schedule;
                this.entityId = schedule.getId();
            }

            @Override
            public long getEntityId() {
                return entityId;
            }

            @Override
            public long getId() {
                if (schedule == null)
                    return 0;
                else
                    return schedule.getId();
            }

            @Override
            public String getTitle(Resources resources) {
                if (schedule == null)
                    return null;
                else
                    return schedule.getTitle();
            }

            @Override
            public ListData.MetaData getMetadata() {
                ListData.MetaData metaData = new ListData.MetaData();
                metaData.iconMode = ListData.MetaData.ICON_MODE_VISIBLE;
                if (schedule != null) {
                    metaData.hasColorIcon = true;
                    metaData.colorIcon = schedule.getColor();
                }
                return metaData;
            }

            public data.model.schedule.Schedule getSchedule() {
                return schedule;
            }
        }

        private static class ScheduleEntityComparator implements Comparator<ScheduleEntity> {
            @Override
            public int compare(ScheduleEntity o1, ScheduleEntity o2) {
                data.model.schedule.Schedule t1 = o1.getSchedule();
                data.model.schedule.Schedule t2 = o2.getSchedule();
                if (t1 == t2)
                    return 0;
                if (t1 == null)
                    return 1;
                if (t2 == null)
                    return -1;
                return Integer.compare(t1.getPosition(), t2.getPosition());
            }
        }

        public static void updatePositions(ListData data, @Nullable ListData.Entity root,
                                           SQLiteDatabase writableDb, Context context) {
            int position = 0;
            if (root == null)
                root = data.getItems();
            for (ListData.Entity entity : root.flatReferences()) {
                position++;
                ScheduleEntity scheduleEntity = (ScheduleEntity) entity;
                data.model.schedule.Schedule schedule = scheduleEntity.getSchedule();
                if (schedule != null) {
                    ScheduleCatalog.updateSchedulePosition(schedule, position, writableDb, context);
                }
            }
        }
    }

    public static class ScheduleOccurrence {

        public static ListData create(data.model.schedule.Schedule schedule, SQLiteDatabase readableDb) {
            ArrayList<Occurrence> occurrences = OccurrenceCatalog.getOccurrencesByScheduleId(
                    schedule.getId(), false, readableDb);
            ArrayList<OccurrenceEntity> entities = new ArrayList<>(occurrences.size());
            for (Occurrence occurrence : occurrences) {
                entities.add(new OccurrenceEntity(occurrence));
            }
            Collections.sort(entities, new OccurrenceEntityComparator());
            OccurrenceEntity root = new OccurrenceEntity(OccurrenceEntity.ROOT_ID);
            root.addChildren(entities);
            return new ListData(root, 0);
        }

        public static class OccurrenceEntity extends ListData.Entity {
            public static final long ROOT_ID = -1;
            private long entityId;
            private Occurrence occurrence;

            public OccurrenceEntity(long entityId) {
                this.entityId = entityId;
            }

            public OccurrenceEntity(Occurrence occurrence) {
                this.occurrence = occurrence;
                this.entityId = occurrence.getId();
            }

            @Override
            public long getEntityId() {
                return entityId;
            }

            @Override
            public long getId() {
                if (occurrence == null) {
                    return 0;
                } else {
                    return occurrence.getId();
                }
            }


            @Override
            public String getIconText(Resources resources) {
                return "#" + occurrence.getEndUserNumber();
            }

            @Override
            public String getTitle(Resources resources) {
                if (occurrence == null)
                    return null;
                return " +" + occurrence.getPlusDays() + " " +
                        (occurrence.getPlusDays() == 1 ? resources.getString(R.string.partial_day) :
                                resources.getString(R.string.partial_days));
            }

            @Override
            public ListData.MetaData getMetadata() {
                ListData.MetaData metaData = new ListData.MetaData();
                metaData.iconMode = ListData.MetaData.ICON_MODE_VISIBLE;
                return metaData;
            }

            public Occurrence getOccurrence() {
                return occurrence;
            }
        }


        private static class OccurrenceEntityComparator implements Comparator<OccurrenceEntity> {
            @Override
            public int compare(OccurrenceEntity o1, OccurrenceEntity o2) {
                data.model.schedule.Occurrence t1 = o1.getOccurrence();
                data.model.schedule.Occurrence t2 = o2.getOccurrence();
                if (t1 == t2)
                    return 0;
                if (t1 == null)
                    return 1;
                if (t2 == null)
                    return -1;
                return Integer.compare(t1.getNumber(), t2.getNumber());
            }
        }
    }

    public static class Label {
        public static ListData create(SQLiteDatabase readableDb) {
            ArrayList<data.model.label.Label> labels = LabelCatalog.getLabels(readableDb);

            LabelEntity.Cache cache = LabelEntity.generateCache(readableDb);

            ArrayList<LabelEntity> entities = new ArrayList<>(labels.size());
            for (data.model.label.Label label : labels) {
                entities.add(new LabelEntity(label, cache));
            }
            Collections.sort(entities, new LabelEntityComparator());

            ListData.Entity entity = new LabelEntity(LabelEntity.ENTITY_ID_ROOT, cache);
            entity.addChildren(entities);
            return new ListData(entity, ListData.MODE_FLAG_MORE_BUTTON);
        }

        public static ListData createDeleted(SQLiteDatabase readableDb) {
            ArrayList<data.model.label.Label> labels = LabelCatalog.getLabelsDeleted(readableDb);
            LabelEntity.Cache cache = LabelEntity.generateCache(readableDb);

            ArrayList<LabelEntity> entities = new ArrayList<>(labels.size());
            for (data.model.label.Label label : labels) {
                entities.add(new LabelEntity(label, cache));
            }
            Collections.sort(entities, new LabelEntityComparator());

            ListData.Entity entity = new LabelEntity(LabelEntity.ENTITY_ID_DELETED_ROOT, cache);
            entity.addChildren(entities);
            return new ListData(entity, ListData.MODE_FLAG_MORE_BUTTON);
        }

        public static class LabelEntity extends ListData.Entity {
            public static long ENTITY_ID_ROOT = -1;
            public static long ENTITY_ID_DELETED_ROOT = -2;

            Cache cache;

            private long entityId;
            private data.model.label.Label label;

            public LabelEntity(data.model.label.Label label, Cache cache) {
                this.label = label;
                this.entityId = label.getId();
                this.cache = cache;
            }

            public LabelEntity(long entityId, Cache cache) {
                this.entityId = entityId;
                this.cache = cache;
            }

            @Override
            public long getEntityId() {
                return entityId;
            }

            @Override
            public long getId() {
                return label.getId();
            }

            @Override
            public boolean isValid() {
                return label != null;
            }

            @Override
            public String getTitle(Resources resources) {
                if (label != null) {
                    return label.getTitle();
                } else if (entityId == ENTITY_ID_DELETED_ROOT) {
                    return resources.getString(R.string.labels);
                } else {
                    return null;
                }
            }

            @Override
            public String getValue(Context context) {
                if (label != null) {
                    Integer noteCount = cache.noteCountLabelMap.get(label.getId());
                    if (noteCount != null) {
                        return TypeFaceUtils.withNumberFormat(noteCount);
                    }
                }
                return null;
            }

            @Override
            public boolean hasMoreButton() {
                return isValid();
            }

            @Override
            public ListData.MetaData getMetadata() {
                ListData.MetaData metaData = new ListData.MetaData();
                metaData.iconMode = ListData.MetaData.ICON_MODE_VISIBLE;
                return metaData;
            }

            public data.model.label.Label getLabel() {
                return label;
            }

            private static Cache generateCache(SQLiteDatabase readableDb) {
                Cache cache = new Cache();
                cache.noteCountLabelMap = NoteCatalog.getNotesCountByLabelMap(readableDb);
                return cache;
            }

            private static class Cache {
                private TreeMap<Long, Integer> noteCountLabelMap;
            }
        }

        private static class LabelEntityComparator implements Comparator<LabelEntity> {
            @Override
            public int compare(LabelEntity o1, LabelEntity o2) {
                return o1.getLabel().getTitle().compareToIgnoreCase(o2.getLabel().getTitle());
            }
        }
    }

    public static class LabelList {
        public static ListData createTree(SQLiteDatabase readableDb, boolean withLabels) {
            TreeMap<Long, data.model.label.LabelList> map;
            if (withLabels) {
                map = LabelListCatalog.getLabelListsMapWithLabels(readableDb);
            } else {
                map = LabelListCatalog.getLabelListsMap(readableDb);
            }
            LabelListEntity.Cache cache = LabelListEntity.generateCache(readableDb);
            TreeMap<Long, LabelListEntity> labelListEntityMap = new TreeMap<>();

            for (data.model.label.LabelList labelList : map.values()) {
                labelListEntityMap.put(labelList.getId(), new LabelListEntity(labelList, !withLabels, cache));
            }

            LabelListEntity root = new LabelListEntity(LabelListEntity.ENTITY_ID_ROOT, cache);
            root.clearChildren(map.size());
            for (LabelListEntity entity : labelListEntityMap.values()) {
                if (entity.getLabelList().getParentId() != null) {
                    LabelListEntity parent = labelListEntityMap.get(entity.getLabelList().getParentId());
                    parent.addChild(entity);
                } else {
                    root.addChild(entity);
                }
            }
            if (withLabels) {
                for (LabelListEntity entity : labelListEntityMap.values()) {
                    ArrayList<data.model.label.Label> labels = entity.getLabelList().getLabels();
                    if (labels != null) {
                        ArrayList<LabelListEntity> children = new ArrayList<>(labels.size());
                        for (data.model.label.Label label : labels) {
                            children.add(new LabelListEntity(label, cache));
                        }
                        entity.addChildren(children);
                    }
                }
            }
            //Sort
            {
                LabelListEntityComparator comparator = new LabelListEntityComparator();
                ArrayList<ListData.Entity> entities = root.flatReferences();
                entities.add(root);
                for (ListData.Entity entity : entities) {
                    ArrayList<ListData.Entity> children = entity.getChildren();
                    if (children != null) {
                        Collections.sort(children, comparator);
                    }
                }
            }
            if (withLabels) {
                return new ListData(root, ListData.MODE_FLAG_TREE |
                        ListData.MODE_FLAG_MORE_BUTTON | ListData.MODE_FLAG_SWAPPABLE);
            } else {
                return new ListData(root, ListData.MODE_FLAG_TREE |
                        ListData.MODE_FLAG_SWAPPABLE);
            }
        }


        public static void updatePositions(ListData data, @Nullable ListData.Entity root,
                                           SQLiteDatabase writableDb, Context context) {
            int position = 0;
            if (root == null)
                root = data.getItems();
            LabelListEntity rootLabelListEntity = (LabelListEntity) root;
            data.model.label.LabelList rootLabelList = rootLabelListEntity.getLabelList();
            for (ListData.Entity entity : root.getChildren()) {
                position++;
                LabelListEntity labelListEntity = (LabelListEntity) entity;
                if (labelListEntity.getLabel() != null) {
                    LabelListCatalog.updateLabelListLabelPosition(
                            rootLabelList.getId(),
                            labelListEntity.getLabel().getId(),
                            position,
                            writableDb,
                            context);
                } else if (labelListEntity.getLabelList() != null) {
                    LabelListCatalog.updateLabelListPosition(
                            labelListEntity.getLabelList().getId(),
                            position,
                            writableDb,
                            context
                    );
                }
            }
        }

        public static class LabelListEntity extends ListData.Entity {
            public static long ENTITY_ID_ROOT = -1;

            Cache cache;

            private long entityId;
            private data.model.label.Label label;
            private data.model.label.LabelList labelList;
            private boolean validLists;

            public LabelListEntity(data.model.label.Label label, Cache cache) {
                this.label = label;
                this.entityId = (label.getId() << 1) | 0b0;
                this.cache = cache;
            }

            public LabelListEntity(data.model.label.LabelList labelList, boolean validLists, Cache cache) {
                this.labelList = labelList;
                this.entityId = (labelList.getId() << 1) | 0b1;
                this.validLists = validLists;
                this.cache = cache;
            }

            public LabelListEntity(long entityId, Cache cache) {
                this.entityId = entityId;
                this.cache = cache;
            }

            @Override
            public long getEntityId() {
                return entityId;
            }

            @Override
            public long getId() {
                if (label != null) {
                    return label.getId();
                } else if (labelList != null) {
                    return labelList.getId();
                } else {
                    return 0;
                }
            }

            @Override
            public boolean isValid() {
                if (label != null)
                    return true;
                else if (labelList != null)
                    return validLists;
                return false;
            }

            @Override
            public String getTitle(Resources resources) {
                if (label != null) {
                    return label.getTitle();
                } else if (labelList != null) {
                    return labelList.getTitle();
                } else if (entityId == ENTITY_ID_ROOT) {
                    return resources.getString(R.string.all);
                }
                return null;
            }

            @Override
            public String getValue(Context context) {
                if (label != null) {
                    Integer noteCount = cache.noteCountLabelMap.get(label.getId());
                    if (noteCount != null) {
                        return TypeFaceUtils.withNumberFormat(noteCount);
                    }
                }
                return null;
            }

            @Override
            public Long getHeaderId() {
                if (labelList != null)
                    return 0l;
                else if (label != null)
                    return 1l;
                else
                    return null;
            }

            @Override
            public String getHeader(Resources resources) {
                if (labelList != null)
                    return resources.getString(R.string.label_lists);
                else if (label != null)
                    return resources.getString(R.string.labels);
                else
                    return null;
            }

            @Override
            public boolean hasMoreButton() {
                if (labelList != null)
                    return true;
                else
                    return super.hasMoreButton();
            }

            @Override
            public boolean hideNextButton() {
                return !validLists;
            }

            @Override
            public ListData.MetaData getMetadata() {
                ListData.MetaData metaData = new ListData.MetaData();
                metaData.iconMode = ListData.MetaData.ICON_MODE_VISIBLE;

                if (labelList != null) {
                    metaData.hasColorIcon = true;
                    metaData.colorIcon = labelList.getColor();
                }
                return metaData;
            }

            public data.model.label.Label getLabel() {
                return label;
            }

            public data.model.label.LabelList getLabelList() {
                return labelList;
            }

            private static Cache generateCache(SQLiteDatabase readableDb) {
                Cache cache = new Cache();
                cache.noteCountLabelMap = NoteCatalog.getNotesCountByLabelMap(readableDb);
                return cache;
            }

            private static class Cache {
                private TreeMap<Long, Integer> noteCountLabelMap;
            }
        }

        private static class LabelListEntityComparator implements Comparator<ListData.Entity> {
            @Override
            public int compare(ListData.Entity entity1, ListData.Entity entity2) {
                LabelListEntity o1 = (LabelListEntity) entity1;
                LabelListEntity o2 = (LabelListEntity) entity2;
                int position1 = 0;
                int position2 = 0;
                if (o1.getLabelList() != null) {
                    position1 = o1.getLabelList().getPosition();
                } else if (o1.getLabel() != null) {
                    if (o1.getLabel().getForeignPosition() != null) {
                        position1 = o1.getLabel().getForeignPosition();
                    }
                }
                if (o2.getLabelList() != null) {
                    position2 = o2.getLabelList().getPosition();
                } else if (o2.getLabel() != null) {
                    if (o2.getLabel().getForeignPosition() != null) {
                        position2 = o2.getLabel().getForeignPosition();
                    }
                }
                return Integer.compare(position1, position2);
            }
        }
    }

    public static class Color {
        public static ListData create(Resources resources) {
            int[] colors = resources.getIntArray(R.array.materialColors);
            String[] names = resources.getStringArray(R.array.materialColorNames);
            if (colors.length != names.length)
                throw new RuntimeException("colors and names should correspond to each other " +
                        "and thus be of the same size");

            ColorEntity items = new ColorEntity();

            int size = colors.length;
            for (int i = 0; i < size; i++) {
                items.addChild(new ColorEntity(colors[i], names[i]));
            }
            return new ListData(items, 0);
        }

        public static int getColorByEntity(ListData.Entity entity) {
            if (entity instanceof ColorEntity)
                return ((ColorEntity) entity).getColor();
            else
                throw new RuntimeException("entity is not an instance of ColorEntity");
        }

        public static String getTextForColor(Resources resources, int color) {
            int[] colors = resources.getIntArray(R.array.materialColors);
            String[] names = resources.getStringArray(R.array.materialColorNames);
            if (colors.length != names.length)
                throw new RuntimeException("colors and names should correspond to each other " +
                        "and thus be of the same size");

            int size = colors.length;
            for (int i = 0; i < size; i++) {
                if (colors[i] == color)
                    return names[i];
            }
            return resources.getString(R.string.universal_color);
        }

        private static class ColorEntity extends ListData.Entity {
            int color;
            String name;
            boolean isValid;

            public ColorEntity() {
                isValid = false;
            }

            public ColorEntity(int color, String name) {
                this.color = color;
                this.name = name;
                isValid = true;
            }

            @Override
            public long getEntityId() {
                return color;
            }

            @Override
            public boolean isValid() {
                return isValid;
            }

            @Override
            public long getId() {
                return color;
            }

            @Override
            public String getTitle(Resources resources) {
                return name;
            }

            @Override
            public ListData.MetaData getMetadata() {
                ListData.MetaData metaData = new ListData.MetaData();
                metaData.hasColorIcon = true;
                metaData.colorIcon = color;
                metaData.iconMode = ListData.MetaData.ICON_MODE_VISIBLE;

                return metaData;
            }

            public int getColor() {
                return color;
            }
        }
    }

    public static class Note {
        public static ListData createDeleted(SQLiteDatabase readableDb) {
            ArrayList<data.model.note.Note> notes = NoteCatalog.getNotesDeleted(readableDb);


            ArrayList<NoteEntity> entities = new ArrayList<>(notes.size());
            for (data.model.note.Note note : notes) {
                entities.add(new NoteEntity(note, true));
            }
            Collections.sort(entities, new NoteEntityCreateDateComparator());

            ListData.Entity entity = new NoteEntity(NoteEntity.ENTITY_ID_DELETED_ROOT, false);
            entity.addChildren(entities);
            return new ListData(entity, 0);
        }

        public static class NoteEntity extends ListData.Entity {
            public static long ENTITY_ID_ROOT = -1;
            public static long ENTITY_ID_DELETED_ROOT = -2;

            private long entityId;
            private data.model.note.Note note;
            private boolean moreButton;

            public NoteEntity(data.model.note.Note note, boolean moreButton) {
                this.note = note;
                this.entityId = note.getId();
                this.moreButton = moreButton;
            }

            public NoteEntity(long entityId, boolean moreButton) {
                this.entityId = entityId;
                this.moreButton = moreButton;
            }

            @Override
            public long getEntityId() {
                return entityId;
            }

            @Override
            public long getId() {
                return note.getId();
            }

            @Override
            public boolean isValid() {
                return note != null;
            }

            @Override
            public String getTitle(Resources resources) {
                if (note != null) {
                    return note.getDisplayTitleFront();
                } else if (entityId == ENTITY_ID_DELETED_ROOT) {
                    return resources.getString(R.string.notes);
                } else {
                    return null;
                }
            }

            @Override
            public boolean hasMoreButton() {
                return moreButton;
            }

            @Override
            public String getDetails(Resources resources) {
                if (note == null) return null;
                if (note.getDisplayDetailsFront() != null && note.getDisplayDetailsFront().length() != 0) {
                    return note.getDisplayDetailsFront();
                } else if (note.getDisplayTitleBack() != null && note.getDisplayTitleBack().length() != 0) {
                    return note.getDisplayTitleBack();
                } else {
                    return null;
                }
            }

            @Override
            public ListData.MetaData getMetadata() {
                ListData.MetaData metaData = new ListData.MetaData();
                metaData.iconMode = ListData.MetaData.ICON_MODE_GONE;
                return metaData;
            }

            public data.model.note.Note getNote() {
                return note;
            }
        }

        private static class NoteEntityCreateDateComparator implements Comparator<NoteEntity> {
            @Override
            public int compare(NoteEntity o1, NoteEntity o2) {
                return Long.compare(o1.getNote().getCreateDate(), o2.getNote().getCreateDate());
            }
        }
    }

    public static class Wrapper {

        public static WrapperEntity wrapEntity(ListData.Entity entity) {
            if (entity instanceof WrapperEntity)
                throw new RuntimeException("entity is already wrapped");
            WrapperEntity wrapped = new WrapperEntity(entity);
            if (entity.getChildrenCountImmediate() != 0) {
                for (ListData.Entity e : entity.getChildren()) {
                    wrapped.addChild(wrapEntity(e));
                }
            }
            return wrapped;
        }

        public static class WrapperEntity extends ListData.Entity {
            public static final int ROOT_ID = -1;

            private static final int INNER_ENTITY_LABEL = 1;
            private static final int INNER_ENTITY_NOTE = 2;

            private ListData.Entity entity;
            private long entityId;
            private int titleResId;

            public WrapperEntity(ListData.Entity entity) {
                this.entity = entity;
                this.entityId = generateEntityId(entity);
            }

            public WrapperEntity(long entityId, int titleResId) {
                this.entityId = entityId;
                this.titleResId = titleResId;
                if (titleResId == 0)
                    throw new RuntimeException("invalid title resource id");
            }

            @Override
            public long getEntityId() {
                return entityId;
            }

            @Override
            public boolean isValid() {
                return entity == null ? false : entity.isValid();
            }

            @Override
            public long getId() {
                if (entity == null)
                    throw new RuntimeException("not supported");
                return entity.getId();
            }

            @Override
            public String getTitle(Resources resources) {
                if (entity == null)
                    return resources.getString(titleResId);
                return entity.getTitle(resources);
            }

            @Override
            public String getDetails(Resources resources) {
                if (entity == null)
                    throw new RuntimeException("not supported");
                return entity.getDetails(resources);
            }

            @Override
            public String getValue(Context context) {
                if (entity == null)
                    throw new RuntimeException("not supported");
                return entity.getValue(context);
            }

            @Override
            public String getFooter() {
                if (entity == null)
                    throw new RuntimeException("not supported");
                return entity.getFooter();
            }

            @Override
            public Long getHeaderId() {
                if (entity == null)
                    return null;
                else
                    return entity.getHeaderId();
            }

            @Override
            public String getHeader(Resources resources) {
                if (entity == null)
                    throw new RuntimeException("not supported");
                return entity.getHeader(resources);
            }

            @Override
            public String getHeaderValue(Context context) {
                if (entity == null)
                    throw new RuntimeException("not supported");
                else
                    return entity.getHeaderValue(context);
            }

            @Override
            public String getHeaderValue2(Context context) {
                if (entity == null)
                    throw new RuntimeException("not supported");
                else
                    return entity.getHeaderValue2(context);
            }

            @Override
            public boolean hasMoreButton() {
                return entity.hasMoreButton();
            }

            @Override
            public ListData.MetaData getMetadata() {
                if (entity == null) {
                    ListData.MetaData metaData = new ListData.MetaData();
                    metaData.iconMode = ListData.MetaData.ICON_MODE_VISIBLE;
                    return metaData;
                } else {
                    return entity.getMetadata();
                }
            }

            @Override
            public boolean overrideMetadataForChildren() {
                if (entity == null)
                    return true;
                else
                    return entity.overrideMetadataForChildren();
            }

            public ListData.Entity getInnerEntity() {
                return entity;
            }

            public static long generateEntityId(ListData.Entity innerEntity) {
                return (innerEntity.getEntityId() << 2) + getInnerEntityNumber(innerEntity);
            }

            private static int getInnerEntityNumber(ListData.Entity innerEntity) {
                int innerNumber;
                if (innerEntity instanceof Label.LabelEntity)
                    innerNumber = INNER_ENTITY_LABEL;
                else if (innerEntity instanceof Note.NoteEntity)
                    innerNumber = INNER_ENTITY_NOTE;
                else
                    throw new RuntimeException("innerEntity has a type that is not supported");
                return innerNumber;
            }
        }
    }

    public static class Deleted {
        public static ListData create(Context context, SQLiteDatabase readableDb) {
            Resources resources = context.getResources();
            Wrapper.WrapperEntity root = new Wrapper.WrapperEntity(Wrapper.WrapperEntity.ROOT_ID, R.string.bin);

            //labels:
            {
                ListData.Entity items = Label.createDeleted(readableDb).getItems();
                if (items.getChildrenCountImmediate() != 0)
                    root.addChild(Wrapper.wrapEntity(items));
            }
            //notes:
            {
                ListData.Entity items = Note.createDeleted(readableDb).getItems();
                if (items.getChildrenCountImmediate() != 0)
                    root.addChild(Wrapper.wrapEntity(items));
            }
            return new ListData(root, ListData.MODE_FLAG_TREE | ListData.MODE_FLAG_MORE_BUTTON);
        }

        public static void restore(Wrapper.WrapperEntity restoreEntity, SQLiteDatabase writableDb, Context context) {
            if (restoreEntity.entity instanceof Label.LabelEntity) {
                data.model.label.Label label = data.model.label.Label.newInstance().setId(restoreEntity.getId());
                LabelCatalog.markLabelAsNotDeleted(label, writableDb, context);
            } else if (restoreEntity.entity instanceof Note.NoteEntity) {
                data.model.note.Note note = data.model.note.Note.newInstance().setId(restoreEntity.getId());
                NoteCatalog.markAsNotDeleted(note, writableDb, context);
            }
        }

        public static void delete(Wrapper.WrapperEntity deleteEntity, SQLiteDatabase writableDb,
                                  SQLiteDatabase fileWritableDb, Context context) {
            if (deleteEntity.entity instanceof Label.LabelEntity) {
                data.model.label.Label label = data.model.label.Label.newInstance().setId(deleteEntity.getId());
                LabelCatalog.deleteLabel(label, writableDb, context);
            } else if (deleteEntity.entity instanceof Note.NoteEntity) {
                data.model.note.Note note = data.model.note.Note.newInstance().setId(deleteEntity.getId());
                NoteCatalog.deleteNote(note, writableDb, fileWritableDb, context);
            }
        }
    }

    public static class PlaceHolderEntity extends ListData.Entity {
        long entityId;

        public PlaceHolderEntity(long entityId) {
            this.entityId = entityId;
        }

        @Override
        public long getEntityId() {
            return entityId;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public long getId() {
            return 0;
        }
    }
}
