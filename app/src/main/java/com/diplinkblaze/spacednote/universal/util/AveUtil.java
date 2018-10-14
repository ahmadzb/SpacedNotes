package com.diplinkblaze.spacednote.universal.util;

import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.diplinkblaze.spacednote.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import data.model.label.LabelCatalog;
import data.model.label.LabelListCatalog;
import data.model.schedule.Occurrence;
import data.model.schedule.OccurrenceCatalog;
import data.model.schedule.ScheduleCatalog;
import data.model.schedule.ScheduleConversion;
import data.model.type.Element;
import data.model.type.ElementCatalog;
import data.model.type.TypeCatalog;
import data.model.profiles.ProfileCatalog;
import util.Colors;
import util.TypeFaceUtils;


/**
 * Created by Ahmad on 09/17/17.
 * All rights reserved.
 */

public class AveUtil {

    private static int[] componentIds = {R.id.ave_id1, R.id.ave_id2, R.id.ave_id3, R.id.ave_id4,
            R.id.ave_id5, R.id.ave_id6, R.id.ave_id7, R.id.ave_id8, R.id.ave_id9, R.id.ave_id10,
            R.id.ave_id11, R.id.ave_id12, R.id.ave_id13, R.id.ave_id14, R.id.ave_id15, R.id.ave_id16,
            R.id.ave_id17, R.id.ave_id18, R.id.ave_id19, R.id.ave_id20, R.id.ave_id21, R.id.ave_id22,
            R.id.ave_id23, R.id.ave_id24, R.id.ave_id25, R.id.ave_id26, R.id.ave_id27, R.id.ave_id28,
            R.id.ave_id29, R.id.ave_id30, R.id.ave_id31, R.id.ave_id32, R.id.ave_id33, R.id.ave_id34,
            R.id.ave_id35, R.id.ave_id36, R.id.ave_id37, R.id.ave_id38, R.id.ave_id39, R.id.ave_id40,
            R.id.ave_id41, R.id.ave_id42, R.id.ave_id43, R.id.ave_id44, R.id.ave_id45, R.id.ave_id46,
            R.id.ave_id47, R.id.ave_id48, R.id.ave_id49, R.id.ave_id50};

    public static class Type {
        public static AveComponentSet create(Resources resources) {
            return create(null, resources);
        }

        public static AveComponentSet create(data.model.type.Type type, Resources resources) {
            AveComponentSet componentSet = new AveComponentSet(Tag.componentSet);
            if (type != null && type.isRealized()) {
                if (!type.isInitialized())
                    throw new RuntimeException("Item is not initialized");
                componentSet.menuResId = R.menu.type_menu;
                componentSet.setStateEdit();
                componentSet.id = type.getId();
            }

            int componentId = 0;
            //Title
            {
                AveComponentSet.Text textComponent = new AveComponentSet.Text(Tag.title, componentIds[componentId++]);
                textComponent.isRequired = true;
                textComponent.hintResId = R.string.title;
                textComponent.maxLines = 1;
                if (type != null) {
                    textComponent.text = type.getTitle();
                }
                componentSet.headComponent = textComponent;
            }

            //Color
            {
                AveComponentSet.Color colorComponent = new AveComponentSet.Color(Tag.color, componentIds[componentId++]);
                colorComponent.isRequired = true;
                colorComponent.color = Colors.getRandomColor(resources);
                if (type != null) {
                    colorComponent.color = type.getColor();
                }
                componentSet.components.add(colorComponent);
            }

            return componentSet;
        }

        private static data.model.type.Type toObjectModel(AveComponentSet componentSet, SQLiteDatabase readableDb) {
            Collection<AveComponentSet.Component> components = componentSet.getAllComponents();
            data.model.type.Type type = data.model.type.Type.newInstance();
            for (AveComponentSet.Component component : components) {
                if (Tag.title.equals(component.tag)) {
                    AveComponentSet.Text text = (AveComponentSet.Text) component;
                    type.setTitle(text.text);
                } else if (Tag.color.equals(component.tag)) {
                    AveComponentSet.Color color = (AveComponentSet.Color) component;
                    type.setColor(color.color);
                }
            }
            if (componentSet.isStateNew()) {
                type.setRealized(false);
                type.setArchived(false);
                type.setPosition(TypeCatalog.getCountAll(readableDb));
            } else {
                data.model.type.Type old = TypeCatalog.getTypeById(componentSet.id, readableDb);
                if (old == null) {
                    throw new RuntimeException("given component set is not realized and is not in new mode");
                }
                type.setId(old.getId());
                type.setRealized(true);
                type.setArchived(old.isArchived());
                type.setPosition(old.getPosition());
            }
            type.setInitialized(true);
            return type;
        }

        public static long save(AveComponentSet componentSet, SQLiteDatabase writableDb, Context context) {
            data.model.type.Type type = toObjectModel(componentSet, writableDb);
            long id;
            if (componentSet.isStateNew()) {
                id = TypeCatalog.addType(type, writableDb, context);
            } else {
                TypeCatalog.updateType(type, writableDb, context);
                id = type.getId();
            }
            return id;
        }

        public static void delete(AveComponentSet componentSet, SQLiteDatabase writableDb, Context context) {
            data.model.type.Type type = data.model.type.Type.newInstance();
            type.setId(componentSet.id);
            TypeCatalog.deleteType(type, writableDb, context);
        }

        public static class Tag {
            public static final String componentSet = "Type.create";
            public static final String title = "title";
            public static final String color = "color";
        }
    }

    public static class TypeElement {
        private static int[] sizes = {8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 32, 36, 48, 60, 72};
        private static int[] dividers = {Element.DividerInterpreter.DIVIDER_TYPE_LINE,
                Element.DividerInterpreter.DIVIDER_TYPE_DASHED_LINE,
                Element.DividerInterpreter.DIVIDER_TYPE_TITLE,
                Element.DividerInterpreter.DIVIDER_TYPE_TITLE_BACKGROUND,
                Element.DividerInterpreter.DIVIDER_TYPE_SPACE};
        private static int[] dividerNames = {R.string.divider_type_line,
                R.string.divider_type_dashed_line,
                R.string.divider_type_title,
                R.string.divider_type_title_background,
                R.string.divider_type_space};
        private static int[] listTypes = {Element.ListInterpreter.LIST_TYPE_BULLETS,
                Element.ListInterpreter.LIST_TYPE_BULLETS_EMPTY,
                Element.ListInterpreter.LIST_TYPE_NUMBERS,
                Element.ListInterpreter.LIST_TYPE_NAME_VALUE};
        private static int[] listTypeNames = {R.string.list_type_bullets,
                R.string.list_type_bullets_empty,
                R.string.list_type_numbers,
                R.string.list_type_name_value};

        public static AveComponentSet create(@NonNull Element element, Resources resources) {
            if (element.getPattern() == Element.PATTERN_TEXT) {
                return createText(element, resources);
            } else if (element.getPattern() == Element.PATTERN_LIST) {
                return createList(element, resources);
            } else if (element.getPattern() == Element.PATTERN_PICTURE) {
                return createPictures(element, resources);
            } else if (element.getPattern() == Element.PATTERN_DIVIDER) {
                return createDivider(element, resources);
            } else
                throw new RuntimeException("Pattern was not recognized");
        }

        public static AveComponentSet createText(Resources resources) {
            return createText(null, resources);
        }

        public static AveComponentSet createText(@Nullable Element element, Resources resources) {
            AveComponentSet componentSet = new AveComponentSet(Tag.componentSetText);
            Element.TextInterpreter interpreter = null;
            if (element != null && element.isRealized()) {
                if (!element.isInitialized())
                    throw new RuntimeException("Item is not initialized");
                componentSet.setStateEdit();
                componentSet.id = element.getId();
                if (element.isArchived()) {
                    componentSet.menuResId = R.menu.type_element_archive_menu;
                } else {
                    componentSet.menuResId = R.menu.type_element_available_menu;
                }
            }
            if (element != null) {
                interpreter = (Element.TextInterpreter) element.getInterpreter();
            }

            //Title
            {
                AveComponentSet.Text textComponent = new AveComponentSet.Text(Tag.title, R.id.ave_id1);
                textComponent.isRequired = false;
                textComponent.hintResId = R.string.title;
                textComponent.maxLines = 1;
                if (element != null) {
                    textComponent.text = element.getTitle();
                }
                componentSet.headComponent = textComponent;
            }

            //Hint
            {
                AveComponentSet.Text textComponent = new AveComponentSet.Text(Tag.hint, R.id.ave_id2);
                textComponent.isRequired = false;
                textComponent.hintResId = R.string.hint;
                textComponent.maxLines = 1;
                if (element != null) {
                    textComponent.text = interpreter.getHint();
                }
                componentSet.components.add(textComponent);
            }

            //text size
            {
                AveComponentSet.Choice choiceComponent = new AveComponentSet.Choice(Tag.textSize, R.id.ave_id3);
                choiceComponent.isRequired = true;
                choiceComponent.choiceList = new ArrayList<>();
                String sp = resources.getString(R.string.partial_fontSizeSp);
                for (int i : sizes) {
                    choiceComponent.choiceList.add(i + " " + sp);
                }
                if (interpreter != null) {
                    int fontSize = interpreter.getTextSize();
                    int position = 5;
                    for (int i = 0; i < sizes.length; i++) {
                        if (sizes[i] == fontSize) {
                            position = i;
                            break;
                        }
                    }
                    choiceComponent.currentPosition = position;
                } else {
                    choiceComponent.currentPosition = 5;
                }
                componentSet.components.add(choiceComponent);
            }

            //Color
            {
                AveComponentSet.Color colorComponent = new AveComponentSet.Color(Tag.color, R.id.ave_id4);
                colorComponent.isRequired = true;
                colorComponent.color = resources.getColor(R.color.colorNamedGray2);
                if (interpreter != null) {
                    colorComponent.color = interpreter.getColor();
                }
                componentSet.components.add(colorComponent);
            }

            //Font
            {
                AveComponentSet.Choice choiceComponent = new AveComponentSet.Choice(Tag.font, R.id.ave_id5);
                choiceComponent.isRequired = true;
                choiceComponent.choiceList = new ArrayList<>();
                Collection<String> list = TypeFaceUtils.getFontList();
                for (String font : list) {
                    choiceComponent.choiceList.add(TypeFaceUtils.toUserFriendlyName(font));
                }
                if (interpreter != null) {
                    String oldFont = interpreter.getFontName();
                    int position = 0;
                    for (String font : list) {
                        if (font.equals(oldFont)) {
                            break;
                        }
                        position++;
                    }
                    choiceComponent.currentPosition = position;
                }
                componentSet.components.add(choiceComponent);
            }

            //Bold
            {
                AveComponentSet.Property property = new AveComponentSet.Property(Tag.bold, R.id.ave_id6);
                property.textResId = R.string.bold;
                if (interpreter != null) {
                    property.value = interpreter.isBold();
                }
                componentSet.components.add(property);
            }

            //Italic
            {
                AveComponentSet.Property property = new AveComponentSet.Property(Tag.italic, R.id.ave_id7);
                property.textResId = R.string.italic;
                if (interpreter != null) {
                    property.value = interpreter.isItalic();
                }
                componentSet.components.add(property);
            }

            //Multiline
            {
                AveComponentSet.Property property = new AveComponentSet.Property(Tag.multiline, R.id.ave_id8);
                property.textResId = R.string.multiline;
                if (interpreter != null) {
                    property.value = interpreter.isMultiline();
                }
                componentSet.components.add(property);
            }

            //Sides
            {
                AveComponentSet.Choice choiceComponent = new AveComponentSet.Choice(Tag.sides, R.id.ave_id9);
                choiceComponent.choiceListResIds = new ArrayList<>();
                choiceComponent.choiceListResIds.add(R.string.front);
                choiceComponent.choiceListResIds.add(R.string.back);
                choiceComponent.choiceListResIds.add(R.string.front_and_back);
                choiceComponent.choiceListResIds.add(R.string.none);
                if (element != null) {
                    int position = 3;
                    if (element.hasSideFront() && element.hasSideBack()) {
                        position = 2;
                    } else if (element.hasSideBack()) {
                        position = 1;
                    } else if (element.hasSideFront()) {
                        position = 0;
                    }
                    choiceComponent.currentPosition = position;
                }
                componentSet.components.add(choiceComponent);
            }

            //InitialCopy
            {
                AveComponentSet.Property property = new AveComponentSet.Property(Tag.initialCopy, R.id.ave_id10);
                property.textResId = R.string.initial_copy;
                if (element != null) {
                    property.value = element.isInitialCopy();
                }
                componentSet.components.add(property);
            }

            return componentSet;
        }

        public static AveComponentSet createList(Resources resources) {
            return createList(null, resources);
        }

        public static AveComponentSet createList(@Nullable Element element, Resources resources) {
            AveComponentSet componentSet = new AveComponentSet(Tag.componentSetList);
            Element.ListInterpreter interpreter = null;
            if (element != null && element.isRealized()) {
                if (!element.isInitialized())
                    throw new RuntimeException("Item is not initialized");
                componentSet.setStateEdit();
                componentSet.id = element.getId();
                if (element.isArchived()) {
                    componentSet.menuResId = R.menu.type_element_archive_menu;
                } else {
                    componentSet.menuResId = R.menu.type_element_available_menu;
                }
            }
            if (element != null) {
                interpreter = (Element.ListInterpreter) element.getInterpreter();
            }

            //Title
            {
                AveComponentSet.Text textComponent = new AveComponentSet.Text(Tag.title, R.id.ave_id1);
                textComponent.isRequired = false;
                textComponent.hintResId = R.string.title;
                textComponent.maxLines = 1;
                if (element != null) {
                    textComponent.text = element.getTitle();
                }
                componentSet.headComponent = textComponent;
            }

            //list item type
            {
                AveComponentSet.Choice choiceComponent = new AveComponentSet.Choice(Tag.listItemType, R.id.ave_id2);
                choiceComponent.choiceListResIds = new ArrayList<>();
                for (int i = 0; i < listTypes.length; i++) {
                    choiceComponent.choiceListResIds.add(listTypeNames[i]);
                }
                if (interpreter != null) {
                    int listType = interpreter.getListType();
                    if (listType == Element.ListInterpreter.LIST_TYPE_BULLETS)
                        choiceComponent.currentPosition = 0;
                    else if (listType == Element.ListInterpreter.LIST_TYPE_BULLETS_EMPTY)
                        choiceComponent.currentPosition = 1;
                    else if (listType == Element.ListInterpreter.LIST_TYPE_NUMBERS)
                        choiceComponent.currentPosition = 2;
                    else if (listType == Element.ListInterpreter.LIST_TYPE_NAME_VALUE)
                        choiceComponent.currentPosition = 3;
                }
                componentSet.components.add(choiceComponent);
            }

            //text size
            {
                AveComponentSet.Choice choiceComponent = new AveComponentSet.Choice(Tag.textSize, R.id.ave_id3);
                choiceComponent.isRequired = true;
                choiceComponent.choiceList = new ArrayList<>();
                String sp = resources.getString(R.string.partial_fontSizeSp);
                for (int i : sizes) {
                    choiceComponent.choiceList.add(i + " " + sp);
                }
                if (interpreter != null) {
                    int fontSize = interpreter.getTextSize();
                    int position = 5;
                    for (int i = 0; i < sizes.length; i++) {
                        if (sizes[i] == fontSize) {
                            position = i;
                            break;
                        }
                    }
                    choiceComponent.currentPosition = position;
                } else {
                    choiceComponent.currentPosition = 5;
                }
                componentSet.components.add(choiceComponent);
            }

            //Color
            {
                AveComponentSet.Color colorComponent = new AveComponentSet.Color(Tag.color, R.id.ave_id4);
                colorComponent.isRequired = true;
                colorComponent.color = resources.getColor(R.color.colorNamedGray2);
                if (interpreter != null) {
                    colorComponent.color = interpreter.getColor();
                }
                componentSet.components.add(colorComponent);
            }

            //Font
            {
                AveComponentSet.Choice choiceComponent = new AveComponentSet.Choice(Tag.font, R.id.ave_id5);
                choiceComponent.isRequired = true;
                choiceComponent.choiceList = new ArrayList<>();
                Collection<String> list = TypeFaceUtils.getFontList();
                for (String font : list) {
                    choiceComponent.choiceList.add(TypeFaceUtils.toUserFriendlyName(font));
                }
                if (interpreter != null) {
                    String oldFont = interpreter.getFontName();
                    int position = 0;
                    for (String font : list) {
                        if (font.equals(oldFont)) {
                            break;
                        }
                        position++;
                    }
                    choiceComponent.currentPosition = position;
                }
                componentSet.components.add(choiceComponent);
            }

            //Bold
            {
                AveComponentSet.Property property = new AveComponentSet.Property(Tag.bold, R.id.ave_id6);
                property.textResId = R.string.bold;
                if (interpreter != null) {
                    property.value = interpreter.isBold();
                }
                componentSet.components.add(property);
            }

            //Italic
            {
                AveComponentSet.Property property = new AveComponentSet.Property(Tag.italic, R.id.ave_id7);
                property.textResId = R.string.italic;
                if (interpreter != null) {
                    property.value = interpreter.isItalic();
                }
                componentSet.components.add(property);
            }

            //Multiline
            {
                AveComponentSet.Property property = new AveComponentSet.Property(Tag.multiline, R.id.ave_id8);
                property.textResId = R.string.multiline;
                if (interpreter != null) {
                    property.value = interpreter.isMultiline();
                }
                componentSet.components.add(property);
            }

            //Sides
            {
                AveComponentSet.Choice choiceComponent = new AveComponentSet.Choice(Tag.sides, R.id.ave_id9);
                choiceComponent.choiceListResIds = new ArrayList<>();
                choiceComponent.choiceListResIds.add(R.string.front);
                choiceComponent.choiceListResIds.add(R.string.back);
                choiceComponent.choiceListResIds.add(R.string.front_and_back);
                choiceComponent.choiceListResIds.add(R.string.none);
                if (element != null) {
                    int position = 3;
                    if (element.hasSideFront() && element.hasSideBack()) {
                        position = 2;
                    } else if (element.hasSideBack()) {
                        position = 1;
                    } else if (element.hasSideFront()) {
                        position = 0;
                    }
                    choiceComponent.currentPosition = position;
                }
                componentSet.components.add(choiceComponent);
            }

            //InitialCopy
            {
                AveComponentSet.Property property = new AveComponentSet.Property(Tag.initialCopy, R.id.ave_id10);
                property.textResId = R.string.initial_copy;
                if (element != null) {
                    property.value = element.isInitialCopy();
                }
                componentSet.components.add(property);
            }

            return componentSet;
        }

        public static AveComponentSet createPictures(Resources resources) {
            return createPictures(null, resources);
        }

        public static AveComponentSet createPictures(@Nullable Element element, Resources resources) {
            AveComponentSet componentSet = new AveComponentSet(Tag.componentSetPictures);
            Element.PictureInterpreter interpreter = null;
            if (element != null && element.isRealized()) {
                if (!element.isInitialized())
                    throw new RuntimeException("Item is not initialized");
                componentSet.setStateEdit();
                componentSet.id = element.getId();
                if (element.isArchived()) {
                    componentSet.menuResId = R.menu.type_element_archive_menu;
                } else {
                    componentSet.menuResId = R.menu.type_element_available_menu;
                }
            }
            if (element != null) {
                interpreter = (Element.PictureInterpreter) element.getInterpreter();
            }

            //Title
            {
                AveComponentSet.Text textComponent = new AveComponentSet.Text(Tag.title, R.id.ave_id1);
                textComponent.isRequired = false;
                textComponent.hintResId = R.string.title;
                textComponent.maxLines = 1;
                if (element != null) {
                    textComponent.text = element.getTitle();
                }
                componentSet.headComponent = textComponent;
            }

            //single mode
            {
                AveComponentSet.Property property = new AveComponentSet.Property(Tag.singleMode, R.id.ave_id2);
                property.textResId = R.string.singleMode;
                if (interpreter != null) {
                    property.value = interpreter.isSingleMode();
                }
                componentSet.components.add(property);
            }

            //Sides
            {
                AveComponentSet.Choice choiceComponent = new AveComponentSet.Choice(Tag.sides, R.id.ave_id3);
                choiceComponent.choiceListResIds = new ArrayList<>();
                choiceComponent.choiceListResIds.add(R.string.front);
                choiceComponent.choiceListResIds.add(R.string.back);
                choiceComponent.choiceListResIds.add(R.string.front_and_back);
                choiceComponent.choiceListResIds.add(R.string.none);
                if (element != null) {
                    int position = 3;
                    if (element.hasSideFront() && element.hasSideBack()) {
                        position = 2;
                    } else if (element.hasSideBack()) {
                        position = 1;
                    } else if (element.hasSideFront()) {
                        position = 0;
                    }
                    choiceComponent.currentPosition = position;
                }
                componentSet.components.add(choiceComponent);
            }

            //InitialCopy
            {
                AveComponentSet.Property property = new AveComponentSet.Property(Tag.initialCopy, R.id.ave_id4);
                property.textResId = R.string.initial_copy;
                if (element != null) {
                    property.value = element.isInitialCopy();
                }
                componentSet.components.add(property);
            }

            return componentSet;
        }

        public static AveComponentSet createDivider(Resources resources) {
            return createDivider(null, resources);
        }

        public static AveComponentSet createDivider(@Nullable Element element, Resources resources) {
            AveComponentSet componentSet = new AveComponentSet(Tag.componentSetDivider);
            Element.DividerInterpreter interpreter = null;
            if (element != null && element.isRealized()) {
                if (!element.isInitialized())
                    throw new RuntimeException("Item is not initialized");
                componentSet.setStateEdit();
                componentSet.id = element.getId();
                if (element.isArchived()) {
                    componentSet.menuResId = R.menu.type_element_archive_menu;
                } else {
                    componentSet.menuResId = R.menu.type_element_available_menu;
                }
            }
            if (element != null) {
                interpreter = (Element.DividerInterpreter) element.getInterpreter();
            }

            //Title
            {
                AveComponentSet.Text textComponent = new AveComponentSet.Text(Tag.title, R.id.ave_id1);
                textComponent.isRequired = false;
                textComponent.hintResId = R.string.title;
                textComponent.maxLines = 1;
                if (element != null) {
                    textComponent.text = element.getTitle();
                }
                componentSet.headComponent = textComponent;
            }

            //divider type
            {
                AveComponentSet.Choice choiceComponent = new AveComponentSet.Choice(Tag.dividerType, R.id.ave_id2);
                choiceComponent.choiceListResIds = new ArrayList<>();
                for (int i = 0; i < dividers.length; i++)
                    choiceComponent.choiceListResIds.add(dividerNames[i]);
                if (interpreter != null) {
                    int dividerType = interpreter.getDividerType();
                    for (int i = 0; i < dividers.length; i++) {
                        if (dividerType == dividers[i])
                            choiceComponent.currentPosition = i;
                    }
                }
                componentSet.components.add(choiceComponent);
            }

            final AveComponentSet.HideConstraint.ShouldHide shouldHide = new AveComponentSet.HideConstraint.ShouldHide() {
                @Override
                public boolean shouldHide(AveComponentSet componentSet) {
                    AveComponentSet.Choice choice = (AveComponentSet.Choice) componentSet.components.get(0);
                    return dividers[choice.currentPosition] != Element.DividerInterpreter.DIVIDER_TYPE_TITLE &&
                            dividers[choice.currentPosition] != Element.DividerInterpreter.DIVIDER_TYPE_TITLE_BACKGROUND;
                }
            };

            //text size
            {
                AveComponentSet.Choice choiceComponent = new AveComponentSet.Choice(Tag.textSize, R.id.ave_id3);
                choiceComponent.isRequired = true;
                choiceComponent.choiceList = new ArrayList<>();
                String sp = resources.getString(R.string.partial_fontSizeSp);
                for (int i : sizes) {
                    choiceComponent.choiceList.add(i + " " + sp);
                }
                if (interpreter != null) {
                    int fontSize = interpreter.getTextSize();
                    int position = 5;
                    for (int i = 0; i < sizes.length; i++) {
                        if (sizes[i] == fontSize) {
                            position = i;
                            break;
                        }
                    }
                    choiceComponent.currentPosition = position;
                } else {
                    choiceComponent.currentPosition = 5;
                }
                componentSet.components.add(choiceComponent);
            }

            //Constraint (hide): text size
            {
                AveComponentSet.HideConstraint constraint = new AveComponentSet.HideConstraint();
                constraint.subject = 1;
                constraint.shouldHide = shouldHide;
                componentSet.constraints.add(constraint);
            }

            //Color
            {
                AveComponentSet.Color colorComponent = new AveComponentSet.Color(Tag.color, R.id.ave_id4);
                colorComponent.isRequired = true;
                colorComponent.color = resources.getColor(R.color.colorNamedGray3);
                if (interpreter != null) {
                    colorComponent.color = interpreter.getColor();
                }
                componentSet.components.add(colorComponent);
            }

            //Constraint (hide): color
            {
                AveComponentSet.HideConstraint constraint = new AveComponentSet.HideConstraint();
                constraint.subject = 2;
                constraint.shouldHide = shouldHide;
                componentSet.constraints.add(constraint);
            }

            //Font
            {
                AveComponentSet.Choice choiceComponent = new AveComponentSet.Choice(Tag.font, R.id.ave_id5);
                choiceComponent.isRequired = true;
                choiceComponent.choiceList = new ArrayList<>();
                Collection<String> list = TypeFaceUtils.getFontList();
                for (String font : list) {
                    choiceComponent.choiceList.add(TypeFaceUtils.toUserFriendlyName(font));
                }
                if (interpreter != null) {
                    String oldFont = interpreter.getFontName();
                    int position = 0;
                    for (String font : list) {
                        if (font.equals(oldFont)) {
                            break;
                        }
                        position++;
                    }
                    choiceComponent.currentPosition = position;
                }
                componentSet.components.add(choiceComponent);
            }

            //Constraint (hide): font
            {
                AveComponentSet.HideConstraint constraint = new AveComponentSet.HideConstraint();
                constraint.subject = 3;
                constraint.shouldHide = shouldHide;
                componentSet.constraints.add(constraint);
            }

            //Bold
            {
                AveComponentSet.Property property = new AveComponentSet.Property(Tag.bold, R.id.ave_id6);
                property.textResId = R.string.bold;
                if (interpreter != null) {
                    property.value = interpreter.isBold();
                }
                componentSet.components.add(property);
            }

            //Constraint (hide): bold
            {
                AveComponentSet.HideConstraint constraint = new AveComponentSet.HideConstraint();
                constraint.subject = 4;
                constraint.shouldHide = shouldHide;
                componentSet.constraints.add(constraint);
            }

            //Italic
            {
                AveComponentSet.Property property = new AveComponentSet.Property(Tag.italic, R.id.ave_id7);
                property.textResId = R.string.italic;
                if (interpreter != null) {
                    property.value = interpreter.isItalic();
                }
                componentSet.components.add(property);
            }

            //Constraint (hide): italic
            {
                AveComponentSet.HideConstraint constraint = new AveComponentSet.HideConstraint();
                constraint.subject = 5;
                constraint.shouldHide = shouldHide;
                componentSet.constraints.add(constraint);
            }

            //Multiline
            {
                AveComponentSet.Property property = new AveComponentSet.Property(Tag.multiline, R.id.ave_id8);
                property.textResId = R.string.multiline;
                if (interpreter != null) {
                    property.value = interpreter.isMultiline();
                }
                componentSet.components.add(property);
            }

            //Constraint (hide): multiline
            {
                AveComponentSet.HideConstraint constraint = new AveComponentSet.HideConstraint();
                constraint.subject = 6;
                constraint.shouldHide = shouldHide;
                componentSet.constraints.add(constraint);
            }

            //Sides
            {
                AveComponentSet.Choice choiceComponent = new AveComponentSet.Choice(Tag.sides, R.id.ave_id9);
                choiceComponent.choiceListResIds = new ArrayList<>();
                choiceComponent.choiceListResIds.add(R.string.front);
                choiceComponent.choiceListResIds.add(R.string.back);
                choiceComponent.choiceListResIds.add(R.string.front_and_back);
                choiceComponent.choiceListResIds.add(R.string.none);
                if (element != null) {
                    int position = 3;
                    if (element.hasSideFront() && element.hasSideBack()) {
                        position = 2;
                    } else if (element.hasSideBack()) {
                        position = 1;
                    } else if (element.hasSideFront()) {
                        position = 0;
                    }
                    choiceComponent.currentPosition = position;
                }
                componentSet.components.add(choiceComponent);
            }

            //InitialCopy
            {
                AveComponentSet.Property property = new AveComponentSet.Property(Tag.initialCopy, R.id.ave_id10);
                property.textResId = R.string.initial_copy;
                if (element != null) {
                    property.value = element.isInitialCopy();
                }
                componentSet.components.add(property);
            }

            return componentSet;
        }

        private static Element toObjectModel(AveComponentSet componentSet, SQLiteDatabase readableDb) {
            Collection<AveComponentSet.Component> components = componentSet.getAllComponents();
            Element element = Element.newInstance();
            if (componentSet.tag.equals(Tag.componentSetText)) {
                element.setPattern(Element.PATTERN_TEXT);
            } else if (componentSet.tag.equals(Tag.componentSetList)) {
                element.setPattern(Element.PATTERN_LIST);
            } else if (componentSet.tag.equals(Tag.componentSetPictures)) {
                element.setPattern(Element.PATTERN_PICTURE);
            } else if (componentSet.tag.equals(Tag.componentSetDivider)) {
                element.setPattern(Element.PATTERN_DIVIDER);
            }
            Element.Interpreter interpreter = element.getInterpreter();

            for (AveComponentSet.Component component : components) {
                if (Tag.title.equals(component.tag)) {
                    AveComponentSet.Text text = (AveComponentSet.Text) component;
                    element.setTitle(text.text);
                } else if (Tag.listItemType.equals(component.tag)) {
                    AveComponentSet.Choice choice = (AveComponentSet.Choice) component;
                    Element.ListInterpreter listInterpreter = (Element.ListInterpreter) interpreter;
                    listInterpreter.setListType(listTypes[choice.currentPosition]);
                } else if (Tag.dividerType.equals(component.tag)) {
                    AveComponentSet.Choice choice = (AveComponentSet.Choice) component;
                    Element.DividerInterpreter dividerInterpreter = (Element.DividerInterpreter) interpreter;
                    dividerInterpreter.setDividerType(dividers[choice.currentPosition]);
                } else if (Tag.hint.equals(component.tag)) {
                    AveComponentSet.Text text = (AveComponentSet.Text) component;
                    Element.TextInterpreter baseInterpreter = (Element.TextInterpreter) interpreter;
                    baseInterpreter.setHint(text.text);
                } else if (Tag.textSize.equals(component.tag)) {
                    AveComponentSet.Choice choice = (AveComponentSet.Choice) component;
                    Element.TextBaseInterpreter baseInterpreter = (Element.TextBaseInterpreter) interpreter;
                    baseInterpreter.setTextSize(sizes[choice.currentPosition]);
                } else if (Tag.color.equals(component.tag)) {
                    AveComponentSet.Color color = (AveComponentSet.Color) component;
                    Element.TextBaseInterpreter baseInterpreter = (Element.TextBaseInterpreter) interpreter;
                    baseInterpreter.setColor(color.color);
                } else if (Tag.font.equals(component.tag)) {
                    AveComponentSet.Choice choice = (AveComponentSet.Choice) component;
                    Element.TextBaseInterpreter baseInterpreter = (Element.TextBaseInterpreter) interpreter;
                    ArrayList<String> fonts = new ArrayList<>(TypeFaceUtils.getFontList());
                    baseInterpreter.setFontName(fonts.get(choice.currentPosition));
                } else if (Tag.bold.equals(component.tag)) {
                    AveComponentSet.Property property = (AveComponentSet.Property) component;
                    Element.TextBaseInterpreter baseInterpreter = (Element.TextBaseInterpreter) interpreter;
                    baseInterpreter.setBold(property.value);
                } else if (Tag.italic.equals(component.tag)) {
                    AveComponentSet.Property property = (AveComponentSet.Property) component;
                    Element.TextBaseInterpreter baseInterpreter = (Element.TextBaseInterpreter) interpreter;
                    baseInterpreter.setItalic(property.value);
                } else if (Tag.multiline.equals(component.tag)) {
                    AveComponentSet.Property property = (AveComponentSet.Property) component;
                    Element.TextBaseInterpreter baseInterpreter = (Element.TextBaseInterpreter) interpreter;
                    baseInterpreter.setMultiline(property.value);
                } else if (Tag.sides.equals(component.tag)) {
                    AveComponentSet.Choice choice = (AveComponentSet.Choice) component;
                    if (choice.currentPosition == 0) {
                        element.setSideBack(false);
                        element.setSideFront(true);
                    } else if (choice.currentPosition == 1) {
                        element.setSideBack(true);
                        element.setSideFront(false);
                    } else if (choice.currentPosition == 2) {
                        element.setSideBack(true);
                        element.setSideFront(true);
                    } else if (choice.currentPosition == 3) {
                        element.setSideBack(false);
                        element.setSideFront(false);
                    }
                } else if (Tag.singleMode.equals(component.tag)) {
                    AveComponentSet.Property property = (AveComponentSet.Property) component;
                    Element.PictureInterpreter pictureInterpreter = (Element.PictureInterpreter) interpreter;
                    pictureInterpreter.setSingleMode(property.value);
                } else if (Tag.initialCopy.equals(component.tag)) {
                    AveComponentSet.Property property = (AveComponentSet.Property) component;
                    element.setInitialCopy(property.value);
                }
            }

            if (componentSet.isStateNew()) {
                element.setRealized(false);
                element.setArchived(false);
                element.setPosition(ElementCatalog.getCountAll(readableDb));
            } else {
                Element old = ElementCatalog.getElementById(componentSet.id, readableDb);
                if (old == null) {
                    throw new RuntimeException("given component set is not realized and is not in new mode");
                }
                element.setId(old.getId());
                element.setRealized(true);
                element.setArchived(old.isArchived());
                element.setPosition(old.getPosition());
            }
            element.setInitialized(true);
            return element;
        }

        public static long save(AveComponentSet componentSet, data.model.type.Type type, SQLiteDatabase writableDb, Context context) {
            Element element = toObjectModel(componentSet, writableDb);
            element.setTypeId(type.getId());
            long id;
            if (componentSet.isStateNew()) {
                id = ElementCatalog.addElement(element, writableDb, context);
            } else {
                ElementCatalog.updateElement(element, writableDb, context);
                id = element.getId();
            }
            return id;
        }

        public static class Tag {
            public static final String componentSetText = "Type.createText";
            public static final String componentSetList = "Type.createList";
            public static final String componentSetPictures = "Type.createPictures";
            public static final String componentSetDivider = "Type.createDivider";
            public static final String title = "title";
            public static final String listItemType = "listItemType";
            public static final String dividerType = "dividerType";
            public static final String hint = "hint";
            public static final String textSize = "textSize";
            public static final String color = "color";
            public static final String font = "font";
            public static final String bold = "bold";
            public static final String italic = "italic";
            public static final String multiline = "multiline";
            public static final String sides = "sides";
            public static final String singleMode = "singleMode";
            public static final String initialCopy = "initialCopy";
        }
    }

    public static class Schedule {
        public static AveComponentSet create(Resources resources) {
            return create(null, resources);
        }

        public static AveComponentSet create(data.model.schedule.Schedule schedule, Resources resources) {
            AveComponentSet componentSet = new AveComponentSet(Tag.componentSet);
            if (schedule != null && schedule.isRealized()) {
                if (!schedule.isInitialized())
                    throw new RuntimeException("Item is not initialized");
                componentSet.menuResId = R.menu.type_menu;
                componentSet.setStateEdit();
                componentSet.id = schedule.getId();
            }

            //Title
            {
                AveComponentSet.Text textComponent = new AveComponentSet.Text(Tag.title, R.id.ave_id1);
                textComponent.isRequired = true;
                textComponent.hintResId = R.string.title;
                textComponent.maxLines = 1;
                if (schedule != null) {
                    textComponent.text = schedule.getTitle();
                }
                componentSet.headComponent = textComponent;
            }

            //Color
            {
                AveComponentSet.Color colorComponent = new AveComponentSet.Color(Tag.color, R.id.ave_id2);
                colorComponent.isRequired = true;
                colorComponent.color = Colors.getRandomColor(resources);
                if (schedule != null) {
                    colorComponent.color = schedule.getColor();
                }
                componentSet.components.add(colorComponent);
            }

            return componentSet;
        }


        private static data.model.schedule.Schedule toObjectModel(AveComponentSet componentSet, SQLiteDatabase readableDb) {
            Collection<AveComponentSet.Component> components = componentSet.getAllComponents();
            data.model.schedule.Schedule schedule = data.model.schedule.Schedule.newInstance();
            for (AveComponentSet.Component component : components) {
                if (Tag.title.equals(component.tag)) {
                    AveComponentSet.Text text = (AveComponentSet.Text) component;
                    schedule.setTitle(text.text);
                } else if (Tag.color.equals(component.tag)) {
                    AveComponentSet.Color color = (AveComponentSet.Color) component;
                    schedule.setColor(color.color);
                }
            }
            if (componentSet.isStateNew()) {
                schedule.setRealized(false);
                schedule.setPosition(ScheduleCatalog.getCountAll(readableDb));
            } else {
                data.model.schedule.Schedule old = ScheduleCatalog.getScheduleById(componentSet.id, readableDb);
                if (old == null) {
                    throw new RuntimeException("given component set is not realized and is not in new mode");
                }
                schedule.setId(old.getId());
                schedule.setRealized(true);
                schedule.setPosition(old.getPosition());
            }
            schedule.setInitialized(true);
            return schedule;
        }

        public static long save(AveComponentSet componentSet, SQLiteDatabase writableDb, Context context) {
            data.model.schedule.Schedule schedule = toObjectModel(componentSet, writableDb);
            long id;
            if (componentSet.isStateNew()) {
                id = ScheduleCatalog.addSchedule(schedule, writableDb, context);
            } else {
                ScheduleCatalog.updateSchedule(schedule, writableDb, context);
                id = schedule.getId();
            }
            return id;
        }

        public static class Tag {
            public static final String componentSet = "Schedule.create";
            public static final String title = "title";
            public static final String color = "color";
        }
    }

    public static class ScheduleOccurrence {
        public static AveComponentSet create(data.model.schedule.Schedule parent,
                                             SQLiteDatabase readableDb,  Resources resources) {
            return create(null, parent, readableDb, resources);
        }

        public static AveComponentSet create(
                Occurrence occurrence, data.model.schedule.Schedule parent, SQLiteDatabase readableDb, Resources resources) {
            if (parent == null || !parent.isInitialized() || !parent.isRealized()) {
                throw new RuntimeException("parent Schedule must be non-null, realized and initialized");
            }

            AveComponentSet componentSet = new AveComponentSet(Tag.componentSet);
            if (occurrence != null && occurrence.isRealized()) {
                if (!occurrence.isInitialized())
                    throw new RuntimeException("Item is not initialized");
                //TODO componentSet.menuResId = R.menu.;
                componentSet.setStateEdit();
                componentSet.id = occurrence.getId();
            }

            int componentId = 0;
            //Plus days
            {
                AveComponentSet.Number number = new AveComponentSet.Number(Tag.plusDays, componentIds[componentId++]);
                number.isRequired = true;
                number.hintResId = R.string.plusDays;
                if (occurrence != null) {
                    number.number = String.valueOf(occurrence.getPlusDays());
                }
                componentSet.headComponent = number;
            }

            String partialDays = resources.getString(R.string.partial_days);
            String partialDay = resources.getString(R.string.partial_day);
            String partialConversionFor = resources.getString(R.string.partial_conversion_for);
            ArrayList<data.model.schedule.Schedule> schedules = ScheduleCatalog.getSchedulesWithOccurrences(readableDb);
            for (int i = 0; i < schedules.size(); i++) {
                if (componentId + 2 > componentIds.length) {
                    Log.e("Spaced Notes Ave", "Too many components, cannot fit!");
                    break;
                }

                data.model.schedule.Schedule schedule = schedules.get(i);
                if (parent.getId() != schedule.getId()) {
                    ScheduleConversion conversion = null;
                    if (occurrence != null) {
                        conversion = occurrence.getConversionForSchedule(schedule);
                    }

                    //Conversion: To Occurrence
                    {
                        AveComponentSet.Choice choice = new AveComponentSet.Choice(
                                Tag.conversionToOccurrencePrefix + schedule.getId(), componentIds[componentId++]);
                        int occurrenceCount = schedule.getOccurrencesCount();
                        choice.choiceList = new ArrayList<>(occurrenceCount);
                        for (int k = 0; k < occurrenceCount; k++) {
                            Occurrence toOccurrence = schedule.getOccurrenceByNumber(k);
                            int plusDays = toOccurrence.getPlusDays();
                            String text = partialConversionFor + " " + schedule.getTitle() +
                                    ": #" + toOccurrence.getEndUserNumber() + " +" + plusDays + " " +
                                    ((plusDays == 1) ? partialDay : partialDays);
                            choice.choiceList.add(text);
                        }
                        if (conversion != null) {
                            choice.currentPosition = conversion.getToOccurrenceNumber();
                            if (choice.currentPosition >= choice.choiceList.size())
                                choice.currentPosition = choice.choiceList.size() - 1;
                        }
                        componentSet.components.add(choice);
                    }
                }
            }
            return componentSet;
        }

        private static Occurrence toObjectModel(
                AveComponentSet componentSet, data.model.schedule.Schedule schedule, SQLiteDatabase readableDb) {
            Collection<AveComponentSet.Component> components = componentSet.getAllComponents();
            Occurrence occurrence = Occurrence.newInstance();
            TreeMap<Long, ScheduleConversion> conversionMap = new TreeMap<>();
            occurrence.setId(componentSet.id).setConversions(conversionMap);
            for (AveComponentSet.Component component : components) {
                if (Tag.plusDays.equals(component.tag)) {
                    AveComponentSet.Number number = (AveComponentSet.Number) component;
                    occurrence.setPlusDays(Integer.parseInt(number.number));
                } else if (component.tag.startsWith(Tag.conversionToOccurrencePrefix)) {
                    AveComponentSet.Choice choice = (AveComponentSet.Choice) component;
                    long scheduleId = Long.parseLong(component.tag.split("_")[1]);
                    ScheduleConversion conversion = conversionMap.get(scheduleId);
                    if (conversion == null) {
                        conversion = ScheduleConversion.newInstance();
                        conversion.setToScheduleId(scheduleId);
                        conversion.setFromOccurrenceId(occurrence.getId());
                        conversionMap.put(scheduleId, conversion);
                    }
                    conversion.setToOccurrenceNumber(Math.max(0, choice.currentPosition));
                }
            }
            occurrence.setScheduleId(schedule.getId());
            if (componentSet.isStateNew()) {
                occurrence.setRealized(false);
                int number = OccurrenceCatalog.getCountAllByScheduleId(schedule.getId(), readableDb);
                occurrence.setNumber(number);
            } else {
                Occurrence old = OccurrenceCatalog.getOccurrenceById(componentSet.id, false, readableDb);
                if (old == null) {
                    throw new RuntimeException("given component set is not realized and is not in new mode");
                }
                occurrence.setId(old.getId());
                occurrence.setRealized(true);
                occurrence.setNumber(old.getNumber());
            }

            occurrence.setInitialized(true);
            return occurrence;
        }

        public static long save(AveComponentSet componentSet, data.model.schedule.Schedule schedule,
                                SQLiteDatabase writableDb, Context context) {
            Occurrence occurrence = toObjectModel(componentSet, schedule, writableDb);
            long id;
            if (componentSet.isStateNew()) {
                id = OccurrenceCatalog.addOccurrence(occurrence, writableDb, context);
            } else {
                OccurrenceCatalog.updateOccurrence(occurrence, writableDb, context);
                id = occurrence.getId();
            }
            return id;
        }

        public static class Tag {
            public static final String componentSet = "ScheduleOccurrence.create";
            public static final String plusDays = "plusDays";
            public static final String conversionToOccurrencePrefix = "conversionToOccurrencePrefix_";
        }
    }

    public static class Label {
        public static AveComponentSet create(data.model.label.Label label) {
            AveComponentSet componentSet = new AveComponentSet(Tag.componentSet);
            if (label != null && label.isRealized()) {
                if (!label.isInitialized())
                    throw new RuntimeException("Item is not initialized");
                componentSet.menuResId = R.menu.label_menu;
                componentSet.setStateEdit();
                componentSet.id = label.getId();
            }

            int componentId = 0;
            //Title
            {
                AveComponentSet.Text text = new AveComponentSet.Text(Tag.title, componentIds[componentId++]);
                text.hintResId = R.string.title;
                if (label != null) {
                    text.text = label.getTitle();
                }
                componentSet.components.add(text);
            }

            return componentSet;
        }

        public static data.model.label.Label toObjectModel(AveComponentSet componentSet, SQLiteDatabase readableDb) {
            data.model.label.Label label = data.model.label.Label.newInstance();

            ArrayList<AveComponentSet.Component> components = componentSet.getAllComponents();
            for (AveComponentSet.Component component : components) {
                if (component.tag.equals(Tag.title)) {
                    AveComponentSet.Text text = (AveComponentSet.Text) component;
                    label.setTitle(text.text);
                }
            }

            if (!componentSet.isStateNew()) {
                label.setId(componentSet.id);
                label.setRealized(true);
                data.model.label.Label old = LabelCatalog.getLabelById(componentSet.id, readableDb);
                label.setDeleted(old.getDeleted());
            }

            label.setInitialized(true);

            return label;
        }

        public static long save(AveComponentSet componentSet, SQLiteDatabase writableDb, Context context) {
            data.model.label.Label label = toObjectModel(componentSet, writableDb);
            if (componentSet.isStateNew()) {
                label.setId(LabelCatalog.addLabel(label, writableDb, context));
            } else {
                LabelCatalog.updateLabel(label, writableDb, context);
            }
            return label.getId();
        }

        public static class Tag {
            public static final String componentSet = "Label.create";
            public static final String title = "title";
        }
    }

    public static class LabelList {
        public static AveComponentSet create(data.model.label.LabelList labelList, Resources resources) {
            AveComponentSet componentSet = new AveComponentSet(Tag.componentSet);
            if (labelList != null && labelList.isRealized()) {
                if (!labelList.isInitialized())
                    throw new RuntimeException("Item is not initialized");
                componentSet.menuResId = R.menu.label_list_menu;
                componentSet.setStateEdit();
                componentSet.id = labelList.getId();
            }

            int componentId = 0;

            //Title
            {
                AveComponentSet.Text text = new AveComponentSet.Text(Tag.title, componentIds[componentId++]);
                text.isRequired = true;
                text.hintResId = R.string.title;
                if (labelList != null) {
                    text.text = labelList.getTitle();
                }
                componentSet.components.add(text);
            }

            //Color
            {
                AveComponentSet.Color color = new AveComponentSet.Color(Tag.color, componentIds[componentId++]);
                color.color = Colors.getRandomColor(resources);
                color.isRequired = true;
                if (labelList != null) {
                    color.color = labelList.getColor();
                }
                componentSet.components.add(color);
            }

            //parent
            {
                AveComponentSet.LabelList parent = new AveComponentSet.LabelList(Tag.parent, componentIds[componentId++]);
                parent.hintResId = R.string.parent_list;
                if (labelList != null) {
                    parent.labelListId = labelList.getParentId();
                }
                componentSet.components.add(parent);
            }

            return componentSet;
        }

        public static data.model.label.LabelList toObjectModel(AveComponentSet componentSet) {
            data.model.label.LabelList labelList = data.model.label.LabelList.newInstance();

            ArrayList<AveComponentSet.Component> components = componentSet.getAllComponents();
            for (AveComponentSet.Component component : components) {
                if (component.tag.equals(Tag.title)) {
                    AveComponentSet.Text text = (AveComponentSet.Text) component;
                    labelList.setTitle(text.text);
                } else if (component.tag.equals(Tag.color)) {
                    AveComponentSet.Color color = (AveComponentSet.Color) component;
                    labelList.setColor(color.color);
                } else if (component.tag.equals(Tag.parent)) {
                    AveComponentSet.LabelList parent = (AveComponentSet.LabelList) component;
                    labelList.setParentId(parent.labelListId);
                }
            }

            if (!componentSet.isStateNew()) {
                labelList.setId(componentSet.id);
                labelList.setRealized(true);
            }

            labelList.setInitialized(true);

            return labelList;
        }

        public static long save(AveComponentSet componentSet, SQLiteDatabase writableDb, Context context) {
            data.model.label.LabelList labelList = toObjectModel(componentSet);
            if (componentSet.isStateNew()) {
                labelList.setId(LabelListCatalog.addLabelList(labelList, writableDb, context));
            } else {
                if (labelList.getParentId() != null && labelList.getParentId() == labelList.getId()) {
                    labelList.setParentId(null);
                }
                LabelListCatalog.updateLabelList(labelList, writableDb, context);
            }
            return labelList.getId();
        }

        public static class Tag {
            public static final String componentSet = "Label.create";
            public static final String title = "title";
            public static final String color = "color";
            public static final String parent = "parent";
        }
    }

    public static class Profile {
        public static AveComponentSet create(@Nullable data.model.profiles.Profile profile, Context context) {
            AveComponentSet componentSet = new AveComponentSet(Tag.componentSet);
            if (profile != null && profile.isRealized()) {
                if (!profile.isInitialized())
                    throw new RuntimeException("Item is not initialized");
                componentSet.setStateEdit();
                componentSet.id = profile.getId();
            }

            int componentId = 0;
            //Title
            {
                AveComponentSet.Text text = new AveComponentSet.Text(Tag.title, componentIds[componentId++]);
                text.hintResId = R.string.title;
                if (profile != null) {
                    text.text = profile.getName();
                }
                componentSet.components.add(text);
            }

            //Color
            {
                AveComponentSet.Color color = new AveComponentSet.Color(Tag.color, componentIds[componentId++]);
                color.color = Colors.getRandomColor(context.getResources());
                if (profile != null) {
                    color.color = profile.getColor();
                }
                componentSet.components.add(color);
            }

            //Image Quality
            {
                AveComponentSet.Choice qualityChoice = new AveComponentSet.Choice(Tag.imageQuality, componentIds[componentId++]);
                qualityChoice.choiceList = new ArrayList<>(100 / 5);
                String qualityText = context.getString(R.string.image_quality);
                for (int i = 0; i <= 100 / 5; i++) {
                    qualityChoice.choiceList.add(qualityText + ": " + String.valueOf(i * 5));
                }
                qualityChoice.currentPosition = 50 / 5;
                qualityChoice.defaultPosition = 50 / 5;
                qualityChoice.iconResId = R.drawable.ic_photo_quality;
                if (profile != null) {
                    qualityChoice.currentPosition = profile.getImageQualityPercentage() / 5;
                }
                componentSet.components.add(qualityChoice);
            }

            //Offline
            {
                AveComponentSet.Property property = new AveComponentSet.Property (Tag.offline, componentIds[componentId++]);
                property.textResId = R.string.sentence_available_offline;
                if (profile != null) {
                    property.value = profile.isOffline();
                } else {
                    property.value = true;
                }
                componentSet.components.add(property);
            }

            return componentSet;
        }

        public static data.model.profiles.Profile toObjectModel(AveComponentSet componentSet, Context context) {
            data.model.profiles.Profile profile = data.model.profiles.Profile.newInstance();

            ArrayList<AveComponentSet.Component> components = componentSet.getAllComponents();
            for (AveComponentSet.Component component : components) {
                if (component.tag.equals(Tag.title)) {
                    AveComponentSet.Text text = (AveComponentSet.Text) component;
                    profile.setName(text.text);
                } else if (component.tag.equals(Tag.color)) {
                    AveComponentSet.Color color = (AveComponentSet.Color) component;
                    profile.setColor(color.color);
                } else if (component.tag.equals(Tag.offline)) {
                    AveComponentSet.Property property = (AveComponentSet.Property) component;
                    profile.setOffline(property.value);
                } else if (component.tag.equals(Tag.imageQuality)) {
                    AveComponentSet.Choice choice = (AveComponentSet.Choice) component;
                    profile.setImageQualityPercentage(choice.currentPosition * 5);
                }
            }

            if (!componentSet.isStateNew()) {
                profile.setId(componentSet.id);
                profile.setRealized(true);
                data.model.profiles.Profile old = ProfileCatalog.getProfileById(componentSet.id, context);
                profile.setPosition(old.getPosition());
                profile.setArchived(old.isArchived());
            } else {
                ArrayList<data.model.profiles.Profile> profiles = ProfileCatalog.getProfiles(context);
                profile.setPosition(profiles.size());
                profile.setArchived(false);
            }

            profile.setInitialized(true);

            return profile;
        }

        public static long save(AveComponentSet componentSet, Context context) {
            data.model.profiles.Profile profile = toObjectModel(componentSet, context);
            if (componentSet.isStateNew()) {
                profile.setId(ProfileCatalog.addProfile(profile, context));
            } else {
                ProfileCatalog.updateProfile(profile, context);
            }
            return profile.getId();
        }

        public static class Tag {
            public static final String componentSet = "Profile.create";
            public static final String title = "title";
            public static final String color = "color";
            public static final String imageQuality = "imageQuality";
            public static final String offline = "offline";
        }
    }

    public static class SingleText {
        public static AveComponentSet create() {
            return create(null, null);
        }

        public static AveComponentSet create(String text, String hint) {
            AveComponentSet componentSet = new AveComponentSet(Tag.componentSet);
            //head. text:
            AveComponentSet.Text textComponent = new AveComponentSet.Text(Tag.text, R.id.ave_id1);
            textComponent.isRequired = true;
            textComponent.text = text;
            textComponent.hint = hint;
            textComponent.maxLines = 1;
            componentSet.headComponent = textComponent;

            return componentSet;
        }

        public static String getValue(AveComponentSet componentSet) {
            if (componentSet.components.size() == 0 && componentSet.headComponent != null &&
                    componentSet.headComponent instanceof AveComponentSet.Text) {
                return ((AveComponentSet.Text) componentSet.headComponent).text;
            } else
                throw new RuntimeException("componentSet is invalid");
        }

        public static class Tag {
            public static final String componentSet = "SingleText.createBudgets";
            public static final String text = "text";
        }
    }
}
