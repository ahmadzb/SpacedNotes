package com.diplinkblaze.spacednote.universal.util;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;

import javax.annotation.concurrent.Immutable;

/**
 * Created by Ahmad on 09/17/17.
 * All rights reserved.
 */

public class AveComponentSet {

    public static final int STATE_NEW = 0;
    public static final int STATE_EDIT = 1;
    public static final int STATE_VIEW = 2;

    private static final String KEY_TAG = "tag";
    private static final String KEY_HEAD_COMPONENT = "headComponent";
    private static final String KEY_COMPONENTS_COUNT = "componentCount";
    private static final String KEY_CONSTRAINTS_COUNT = "constraintCount";
    private static final String KEY_PREFIX_COMPONENT = "component#";
    private static final String KEY_PREFIX_CONSTRAINT = "constraint#";
    private static final String KEY_STATE = "state";
    private static final String KEY_ID = "id";
    private static final String KEY_MENU_RES_ID = "menuResId";
    private static final String KEY_HAS_VIEW_COMPONENTS = "hasViewComponents";
    private static final String KEY_TUTORIALS_ID = "tutorialsId";
    private static final String KEY_IS_EDITABLE = "isEditable";

    public String tag;
    public Component headComponent;
    public ArrayList<Component> components;
    public ArrayList<Constraint> constraints;
    public int state;
    public boolean isEditable = true;
    public long id;
    public int menuResId;
    public boolean hasViewComponents;
    public Integer tutorialsId;

    public AveComponentSet(String tag) {
        components = new ArrayList<Component>();
        constraints = new ArrayList<Constraint>();
        this.tag = tag;
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        bundle.putString(KEY_TAG, tag);

        if (headComponent != null)
            bundle.putBundle(KEY_HEAD_COMPONENT, headComponent.toBundle());

        bundle.putInt(KEY_COMPONENTS_COUNT, components.size());
        int i = 1;
        for (Component component : components)
            bundle.putBundle(KEY_PREFIX_COMPONENT + i++, component.toBundle());

        bundle.putInt(KEY_CONSTRAINTS_COUNT, constraints.size());
        i = 1;
        for (Constraint constraint : constraints)
            bundle.putBundle(KEY_PREFIX_CONSTRAINT + i++, constraint.toBundle());

        bundle.putInt(KEY_STATE, state);
        bundle.putLong(KEY_ID, id);
        bundle.putInt(KEY_MENU_RES_ID, menuResId);
        bundle.putBoolean(KEY_HAS_VIEW_COMPONENTS, hasViewComponents);
        bundle.putSerializable(KEY_TUTORIALS_ID, tutorialsId);
        bundle.putBoolean(KEY_IS_EDITABLE, isEditable);

        return bundle;
    }

    public static AveComponentSet fromBundle(Bundle bundle) {
        AveComponentSet componentSet = new AveComponentSet(bundle.getString(KEY_TAG));

        componentSet.headComponent = Component.fromBundle(bundle.getBundle(KEY_HEAD_COMPONENT));
        int count = bundle.getInt(KEY_COMPONENTS_COUNT);
        for (int i = 1; i <= count; i++) {
            componentSet.components.add(Component.fromBundle(bundle.getBundle(KEY_PREFIX_COMPONENT + i)));
        }

        count = bundle.getInt(KEY_CONSTRAINTS_COUNT);
        for (int i = 1; i <= count; i++) {
            componentSet.constraints.add(Constraint.fromBundle(bundle.getBundle(KEY_PREFIX_CONSTRAINT + i)));
        }

        componentSet.state = bundle.getInt(KEY_STATE);
        componentSet.id = bundle.getLong(KEY_ID);
        componentSet.menuResId = bundle.getInt(KEY_MENU_RES_ID);
        componentSet.hasViewComponents = bundle.getBoolean(KEY_HAS_VIEW_COMPONENTS);
        componentSet.tutorialsId = (Integer) bundle.getSerializable(KEY_TUTORIALS_ID);
        componentSet.isEditable = bundle.getBoolean(KEY_IS_EDITABLE);

        return componentSet;
    }

    public boolean isComponentConstrained(Component component) {
        for (int i = 0; i < components.size(); i++)
            if (component == components.get(i))
                return isComponentConstrained(i);
        return false;
    }

    public boolean isComponentConstrained(int componentIndex) {
        for (Constraint constraint : constraints)
            if (constraint.doesConstraint(componentIndex))
                return true;
        return false;
    }

    public boolean isComponentSetComplete() {
        if (headComponent != null && headComponent.isRequired && !headComponent.hasValue())
            return false;
        for (Component component : components) {
            if (component.isRequired && !component.hasValue())
                return false;
        }
        for (Constraint constraint : constraints) {
            if (constraint.isRequired && !constraint.isComplete(components))
                return false;
        }
        return true;
    }

    /**
     * It does not includes head component
     *
     * @return
     */
    public ArrayList<Component> getComponents() {
        return components;
    }

    /**
     * It includes head component
     *
     * @return
     */
    public ArrayList<Component> getAllComponents() {
        ArrayList<Component> result =
                new ArrayList<Component>(components.size() + 1);
        if (headComponent != null) result.add(headComponent);
        result.addAll(components);
        return result;
    }

    @Nullable
    public Integer getComponentIndex(Component component) {
        for (int i = 0; i < components.size(); i++)
            if (component == components.get(i))
                return i;
        return null;
    }

    public void setStateView() {
        state = STATE_VIEW;
    }

    public void setStateEdit() {
        state = STATE_EDIT;
    }

    public boolean isReadOnly() {
        return state == STATE_VIEW;
    }

    public boolean isStateNew() {
        return state == STATE_NEW;
    }

    public boolean hasHiddenComponents() {
        ArrayList<Component> components = getAllComponents();
        boolean result = false;
        for (Component component : components)
            result = component.canHide() || result;
        return result;
    }

    @Override
    public AveComponentSet clone() {
        AveComponentSet clone = new AveComponentSet(this.tag);
        clone.copyFromUnrestricted(this);
        return clone;
    }

    public void copyFromUnrestricted(AveComponentSet sample) {
        //Copy fields:
        {
            this.id = sample.id;
            this.state = sample.state;
            this.tutorialsId = sample.tutorialsId;
            this.hasViewComponents = sample.hasViewComponents;
            this.menuResId = sample.menuResId;
            this.isEditable = sample.isEditable;
        }
        //Copy components:
        {
            if (sample.headComponent != null) {
                this.headComponent = sample.headComponent.clone();
            } else {
                this.headComponent = null;
            }
            final int size = sample.components.size();
            this.components.clear();
            for (int i = 0; i < size; i++) {
                this.components.add(sample.components.get(i).clone());
            }
        }
        //Copy constraints:
        {
            final int size = sample.constraints.size();
            this.constraints.clear();
            for (int i = 0; i < size; i++) {
                this.constraints.add(sample.constraints.get(i).clone());
            }
        }
    }

    /**
     * DO NOT USE THIS METHOD FOR CLONING.
     * This method requires both componentSets to have identical component and constraint
     * counts and types. This method is useful is you want to maintain current references
     * to components while setting new values to them.
     *
     * @param sample
     */
    public void copyFrom(AveComponentSet sample) {
        //validity check:
        if (this.headComponent == null && sample.headComponent != null ||
                this.headComponent != null && sample.headComponent == null ||
                this.components.size() != sample.components.size() ||
                this.constraints.size() != sample.constraints.size())
            throw new RuntimeException("This and sample components or constraints do not correspond");
        //Copy fields:
        {
            this.id = sample.id;
            this.state = sample.state;
            this.tutorialsId = sample.tutorialsId;
            this.hasViewComponents = sample.hasViewComponents;
            this.menuResId = sample.menuResId;
            this.isEditable = sample.isEditable;
        }
        //Copy components:
        {
            if (this.headComponent != null) {
                this.headComponent.copyFrom(sample.headComponent);
            }
            final int size = this.components.size();
            for (int i = 0; i < size; i++) {
                this.components.get(i).copyFrom(sample.components.get(i));
            }
        }
        //Copy constraints:
        {
            final int size = this.constraints.size();
            for (int i = 0; i < size; i++) {
                this.constraints.get(i).copyFrom(sample.constraints.get(i));
            }
        }
    }

    //================================= Components ===================================
    public static abstract class Component {
        public static final String KEY_TAG = "tag";
        public static final String KEY_COMPONENT_ID = "componentId";
        public static final String KEY_IS_REQUIRED = "isRequired";
        public static final String KEY_SHOULD_HIDE = "shouldHide";
        public static final String KEY_HAS_ICON = "hasIcon";
        public static final String KEY_HAS_DIVIDER = "hasDivider";
        public static final String KEY_COMPONENT = "component";
        public static final int COMPONENT_LABEL_LIST = 1;
        public static final int COMPONENT_AMOUNT = 3;
        public static final int COMPONENT_DATE = 4;
        public static final int COMPONENT_TEXT = 7;
        public static final int COMPONENT_COLOR = 8;
        public static final int COMPONENT_NUMBER = 9;
        public static final int COMPONENT_PROPERTY = 10;
        public static final int COMPONENT_CHOICE = 11;
        public static final int COMPONENT_DATE_PERIOD = 15;

        public String tag;
        public int componentId;
        public boolean isRequired = false;
        public boolean shouldHide = false;
        public boolean hasIcon = true;
        public boolean hasDivider = true;

        public Component(String tag, int componentId) {
            this.tag = tag;
            this.componentId = componentId;
        }

        public static Component fromBundle(Bundle bundle) {
            if (bundle == null)
                return null;

            Component component;
            int componentCode = bundle.getInt(KEY_COMPONENT, -1);

            String tag = bundle.getString(KEY_TAG);
            int id = bundle.getInt(KEY_COMPONENT_ID);

            switch (componentCode) {
                case COMPONENT_LABEL_LIST:
                    component = new LabelList(tag, id);
                    break;
                case COMPONENT_AMOUNT:
                    component = new Amount(tag, id);
                    break;
                case COMPONENT_DATE:
                    component = new Date(tag, id);
                    break;
                case COMPONENT_TEXT:
                    component = new Text(tag, id);
                    break;
                case COMPONENT_COLOR:
                    component = new Color(tag, id);
                    break;
                case COMPONENT_NUMBER:
                    component = new Number(tag, id);
                    break;
                case COMPONENT_PROPERTY:
                    component = new Property(tag, id);
                    break;
                case COMPONENT_CHOICE:
                    component = new Choice(tag, id);
                    break;
                case COMPONENT_DATE_PERIOD:
                    component = new DatePeriod(tag, id);
                    break;
                default:
                    throw new RuntimeException("Bundle didn't contain a correct component code: " + componentCode);
            }

            component.componentId = bundle.getInt(KEY_COMPONENT_ID);
            component.isRequired = bundle.getBoolean(KEY_IS_REQUIRED);
            component.shouldHide = bundle.getBoolean(KEY_SHOULD_HIDE);
            component.hasIcon = bundle.getBoolean(KEY_HAS_ICON);
            component.hasDivider = bundle.getBoolean(KEY_HAS_DIVIDER);
            component.retrieveFromBundle(bundle);
            return component;
        }

        public Bundle toBundle() {
            Bundle bundle = new Bundle();
            int code = -1;
            if (this instanceof LabelList)
                code = COMPONENT_LABEL_LIST;
            else if (this instanceof Amount)
                code = COMPONENT_AMOUNT;
            else if (this instanceof Date)
                code = COMPONENT_DATE;
            else if (this instanceof Text)
                code = COMPONENT_TEXT;
            else if (this instanceof Color)
                code = COMPONENT_COLOR;
            else if (this instanceof Number)
                code = COMPONENT_NUMBER;
            else if (this instanceof Property)
                code = COMPONENT_PROPERTY;
            else if (this instanceof Choice)
                code = COMPONENT_CHOICE;
            else if (this instanceof DatePeriod)
                code = COMPONENT_DATE_PERIOD;
            else
                throw new RuntimeException("This component cannot convert to bundle: " + this);

            bundle.putString(KEY_TAG, this.tag);
            bundle.putBoolean(KEY_IS_REQUIRED, isRequired);
            bundle.putBoolean(KEY_SHOULD_HIDE, shouldHide);
            bundle.putInt(KEY_COMPONENT, code);
            bundle.putBoolean(KEY_HAS_ICON, hasIcon);
            bundle.putBoolean(KEY_HAS_DIVIDER, hasDivider);
            bundle.putInt(KEY_COMPONENT_ID, componentId);
            this.addToBundle(bundle);
            return bundle;
        }

        public boolean canHide() {
            return shouldHide && !hasContent();
        }

        protected abstract void addToBundle(Bundle bundle);

        protected abstract void retrieveFromBundle(Bundle bundle);

        public abstract boolean hasValue();

        public abstract boolean hasContent();

        public abstract void removeValue();

        public abstract Component clone();

        public void copyFrom(Component sample) {
            this.tag = sample.tag;
            this.componentId = sample.componentId;
            this.isRequired = sample.isRequired;
            this.shouldHide = sample.shouldHide;
            this.hasIcon = sample.hasIcon;
            this.hasDivider = sample.hasDivider;
        }
    }

    public static class LabelList extends Component {
        private static final String KEY_LABEL_LIST_ID = "labelListId";
        private static final String KEY_HINT_RES_ID = "hintResId";
        public Long labelListId;
        public int hintResId;

        public LabelList(String tag, int componentId) {
            super(tag, componentId);
        }

        @Override
        protected void addToBundle(Bundle bundle) {
            bundle.putSerializable(KEY_LABEL_LIST_ID, labelListId);
            bundle.putInt(KEY_HINT_RES_ID, hintResId);
        }

        @Override
        protected void retrieveFromBundle(Bundle bundle) {
            labelListId = (Long) bundle.getSerializable(KEY_LABEL_LIST_ID);
            hintResId = bundle.getInt(KEY_HINT_RES_ID);
        }

        @Override
        public boolean hasValue() {
            return labelListId != null;
        }

        @Override
        public boolean hasContent() {
            return labelListId != null;
        }

        @Override
        public void removeValue() {
            labelListId = null;
        }

        @Override
        public Component clone() {
            LabelList clone = new LabelList(tag, componentId);
            clone.copyFrom(this);
            return clone;
        }

        @Override
        public void copyFrom(Component sample) {
            super.copyFrom(sample);
            if (sample instanceof LabelList) {
                LabelList labelList = (LabelList) sample;
                this.labelListId = labelList.labelListId;
                this.hintResId = labelList.hintResId;
            } else
                throw new RuntimeException("sample component is not a LabelList");
        }
    }

    public static class Amount extends Component {
        private static final String KEY_AMOUNT = "amount";
        private static final String KEY_HINT_RES_ID = "hintResId";
        public double amount;
        public int hintResId;

        public Amount(String tag, int componentId) {
            super(tag, componentId);
        }

        @Override
        protected void addToBundle(Bundle bundle) {
            bundle.putDouble(KEY_AMOUNT, amount);
            bundle.putInt(KEY_HINT_RES_ID, hintResId);
        }

        @Override
        protected void retrieveFromBundle(Bundle bundle) {
            amount = bundle.getDouble(KEY_AMOUNT);
            hintResId = bundle.getInt(KEY_HINT_RES_ID);
        }

        @Override
        public boolean hasValue() {
            return amount != 0;
        }

        @Override
        public boolean hasContent() {
            return amount != 0;
        }

        @Override
        public void removeValue() {
            amount = 0;
        }

        @Override
        public Component clone() {
            Amount clone = new Amount(tag, componentId);
            clone.copyFrom(this);
            return clone;
        }

        @Override
        public void copyFrom(Component sample) {
            super.copyFrom(sample);
            if (sample instanceof Amount) {
                Amount amount = (Amount) sample;
                this.amount = amount.amount;
                this.hintResId = amount.hintResId;
            } else
                throw new RuntimeException("sample component is not an Amount");
        }
    }

    public static class Date extends Component {
        private static final String KEY_DATE = "date";
        private static final String KEY_TIME = "time";
        private static final String KEY_ALWAYS_HAS_CONTENT = "alwaysHasContent";
        private static final String KEY_REMOVABLE = "removable";
        private static final String KEY_HAS_VALUE = "hasValue";
        private static final String KEY_HINT = "hint";
        private static final String KEY_HINT_RES_ID = "hintResId";
        private static final String KEY_MIN_DATE = "minDate";
        private static final String KEY_MAX_DATE = "maxDate";
        public long date;
        public boolean time;
        public boolean alwaysHasContent = true;
        public boolean removable = false;
        public boolean hasValue = false;
        public String hint;
        public Integer hintResId;
        public Long minDate;
        public Long maxDate;

        public Date(String tag, int componentId) {
            super(tag, componentId);
        }

        @Override
        protected void addToBundle(Bundle bundle) {
            bundle.putLong(KEY_DATE, date);
            bundle.putBoolean(KEY_TIME, time);
            bundle.putBoolean(KEY_ALWAYS_HAS_CONTENT, alwaysHasContent);
            bundle.putBoolean(KEY_REMOVABLE, removable);
            bundle.putBoolean(KEY_HAS_VALUE, hasValue);
            bundle.putString(KEY_HINT, hint);
            bundle.putSerializable(KEY_HINT_RES_ID, hintResId);
            bundle.putSerializable(KEY_MIN_DATE, minDate);
            bundle.putSerializable(KEY_MAX_DATE, maxDate);
        }

        @Override
        protected void retrieveFromBundle(Bundle bundle) {
            date = bundle.getLong(KEY_DATE);
            time = bundle.getBoolean(KEY_TIME);
            alwaysHasContent = bundle.getBoolean(KEY_ALWAYS_HAS_CONTENT);
            removable = bundle.getBoolean(KEY_REMOVABLE);
            hasValue = bundle.getBoolean(KEY_HAS_VALUE);
            hint = bundle.getString(KEY_HINT);
            hintResId = (Integer) bundle.getSerializable(KEY_HINT_RES_ID);
            minDate = (Long) bundle.getSerializable(KEY_MIN_DATE);
            maxDate = (Long) bundle.getSerializable(KEY_MAX_DATE);
        }

        @Override
        public boolean hasValue() {
            return !removable || hasValue;
        }

        @Override
        public boolean hasContent() {
            return alwaysHasContent || removable && hasValue;
        }

        @Override
        public void removeValue() {
            hasValue = false;
            date = 0;
        }

        @Override
        public Component clone() {
            Date clone = new Date(tag, componentId);
            clone.copyFrom(this);
            return clone;
        }

        @Override
        public void copyFrom(Component sample) {
            super.copyFrom(sample);
            if (sample instanceof Date) {
                Date date = (Date) sample;
                this.date = date.date;
                this.time = date.time;
                this.alwaysHasContent = date.alwaysHasContent;
                this.removable = date.removable;
                this.hasValue = date.hasValue;
                this.hint = date.hint;
                this.hintResId = date.hintResId;
                this.minDate = date.minDate;
                this.maxDate = date.maxDate;
            } else
                throw new RuntimeException("sample component is not a Date");
        }
    }

    public static class DatePeriod extends Component {
        private static final String KEY_DATE_FROM = "dateFrom";
        private static final String KEY_DATE_TO = "dateTo";
        private static final String KEY_HAS_CONTENT = "hasContent";
        private static final String KEY_TYPE = "periodType";
//
//        public static final int TYPE_PAST = CalendarFragment.DateState.TYPE_PAST;
//        public static final int TYPE_FUTURE = CalendarFragment.DateState.TYPE_FUTURE;
//        public static final int TYPE_ALL = CalendarFragment.DateState.TYPE_ALL;
//        public static final int TYPE_NOT_PAST = CalendarFragment.DateState.TYPE_NOT_PAST;
//        public static final int TYPE_NOT_FUTURE = CalendarFragment.DateState.TYPE_NOT_FUTURE;

        public long dateFrom;
        public long dateTo;
        public int type;
        public boolean hasContent = true;

        public DatePeriod(String tag, int componentId) {
            super(tag, componentId);
        }

        @Override
        protected void addToBundle(Bundle bundle) {
            bundle.putLong(KEY_DATE_FROM, dateFrom);
            bundle.putLong(KEY_DATE_TO, dateTo);
            bundle.putBoolean(KEY_HAS_CONTENT, hasContent);
            bundle.putInt(KEY_TYPE, type);
        }

        @Override
        protected void retrieveFromBundle(Bundle bundle) {
            dateFrom = bundle.getLong(KEY_DATE_FROM);
            dateTo = bundle.getLong(KEY_DATE_TO);
            hasContent = bundle.getBoolean(KEY_HAS_CONTENT);
            type = bundle.getInt(KEY_TYPE);
        }

        @Override
        public boolean hasValue() {
            return true;
        }

        @Override
        public boolean hasContent() {
            return hasContent;
        }

        @Override
        public void removeValue() {
            dateFrom = 0;
            dateTo = 0;
        }

        @Override
        public Component clone() {
            DatePeriod clone = new DatePeriod(tag, componentId);
            clone.copyFrom(this);
            return clone;
        }

        @Override
        public void copyFrom(Component sample) {
            super.copyFrom(sample);
            if (sample instanceof DatePeriod) {
                DatePeriod datePeriod = (DatePeriod) sample;
                dateFrom = datePeriod.dateFrom;
                dateTo = datePeriod.dateTo;
                hasContent = datePeriod.hasContent;
                type = datePeriod.type;
            } else
                throw new RuntimeException("sample component is not a Date");
        }

        public int toDateStateType() {
            return type;
        }
    }

    public static class Text extends Component {
        private static final String KEY_TEXT = "text";
        private static final String KEY_HINT = "hint";
        private static final String KEY_HINT_RES_ID = "hintResId";
        private static final String KEY_MAX_LINES = "lines";
        public String text;
        public String hint;
        public Integer hintResId;
        public Integer maxLines;

        public Text(String tag, int componentId) {
            super(tag, componentId);
        }

        @Override
        protected void addToBundle(Bundle bundle) {
            bundle.putString(KEY_TEXT, text);
            bundle.putString(KEY_HINT, hint);
            bundle.putSerializable(KEY_HINT_RES_ID, hintResId);
            bundle.putSerializable(KEY_MAX_LINES, maxLines);
        }

        @Override
        protected void retrieveFromBundle(Bundle bundle) {
            text = bundle.getString(KEY_TEXT);
            hint = bundle.getString(KEY_HINT);
            hintResId = (Integer) bundle.getSerializable(KEY_HINT_RES_ID);
            maxLines = (Integer) bundle.getSerializable(KEY_MAX_LINES);
        }

        @Override
        public boolean hasValue() {
            return text != null && !text.isEmpty();
        }

        @Override
        public boolean hasContent() {
            return hasValue();
        }

        @Override
        public void removeValue() {
            text = null;
        }

        @Override
        public Component clone() {
            Text clone = new Text(tag, componentId);
            clone.copyFrom(this);
            return clone;
        }

        @Override
        public void copyFrom(Component sample) {
            super.copyFrom(sample);
            if (sample instanceof Text) {
                Text text = (Text) sample;
                this.text = text.text;
                this.hint = text.hint;
                this.hintResId = text.hintResId;
                this.maxLines = text.maxLines;
            } else
                throw new RuntimeException("sample component is not a Text");
        }
    }

    public static class Color extends Component {
        private static final String KEY_COLOR = "color";
        private static final String KEY_DEFAULT_COLOR = "defaultColor";
        public int color = 0;
        public int defaultColor = 0;

        public Color(String tag, int componentId) {
            super(tag, componentId);
        }

        @Override
        protected void addToBundle(Bundle bundle) {
            bundle.putInt(KEY_COLOR, color);
            bundle.putInt(KEY_DEFAULT_COLOR, defaultColor);
        }

        @Override
        protected void retrieveFromBundle(Bundle bundle) {
            color = bundle.getInt(KEY_COLOR);
            defaultColor = bundle.getInt(KEY_DEFAULT_COLOR);
        }

        @Override
        public boolean hasValue() {
            return true;
        }

        @Override
        public boolean hasContent() {
            return color != defaultColor;
        }

        @Override
        public void removeValue() {
            color = 0;
        }

        @Override
        public Component clone() {
            Color clone = new Color(tag, componentId);
            clone.color = this.color;
            clone.copyFrom(this);
            return clone;
        }

        @Override
        public void copyFrom(Component sample) {
            super.copyFrom(sample);
            if (sample instanceof Color) {
                Color color = (Color) sample;
                this.color = color.color;
                this.defaultColor = color.defaultColor;
            } else
                throw new RuntimeException("sample component is not a Color");
        }
    }

    public static class Number extends Component {
        private static final String KEY_NUMBER = "number";
        private static final String KEY_HINT = "hint";
        private static final String KEY_HINT_RES_ID = "hintResId";

        public String number;
        public String hint;
        public Integer hintResId;

        public Number(String tag, int componentId) {
            super(tag, componentId);
        }

        @Override
        protected void addToBundle(Bundle bundle) {
            bundle.putString(KEY_NUMBER, number);
            bundle.putString(KEY_HINT, hint);
            bundle.putSerializable(KEY_HINT_RES_ID, hintResId);
        }

        @Override
        protected void retrieveFromBundle(Bundle bundle) {
            number = bundle.getString(KEY_NUMBER);
            hint = bundle.getString(KEY_HINT);
            hintResId = (Integer) bundle.getSerializable(KEY_HINT_RES_ID);
        }


        @Override
        public boolean hasValue() {
            return number != null && !number.isEmpty();
        }

        @Override
        public boolean hasContent() {
            return hasValue();
        }

        @Override
        public void removeValue() {
            number = null;
        }

        @Override
        public Component clone() {
            Number clone = new Number(tag, componentId);
            clone.copyFrom(this);
            return clone;
        }

        @Override
        public void copyFrom(Component sample) {
            super.copyFrom(sample);
            if (sample instanceof Number) {
                Number number = (Number) sample;
                this.number = number.number;
                this.hint = number.hint;
                this.hintResId = number.hintResId;
            } else
                throw new RuntimeException("sample component is not a Number");
        }
    }

    public static class Property extends Component {
        private static final String KEY_TEXT = "text";
        private static final String KEY_TEXT_RES_ID = "textResId";
        private static final String KEY_VALUE = "value";
        private static final String KEY_DEFAULT_VALUE = "defaultValue";
        private static final String KEY_ICON_RES_ID = "iconResId";

        public String text;
        public int textResId;
        public boolean value;
        public boolean defaultValue;
        public int iconResId;

        public Property(String tag, int componentId) {
            super(tag, componentId);
            hasIcon = false;
        }

        @Override
        protected void addToBundle(Bundle bundle) {
            bundle.putString(KEY_TEXT, text);
            bundle.putInt(KEY_TEXT_RES_ID, textResId);
            bundle.putBoolean(KEY_VALUE, value);
            bundle.putBoolean(KEY_DEFAULT_VALUE, defaultValue);
            bundle.putInt(KEY_ICON_RES_ID, iconResId);
        }

        @Override
        protected void retrieveFromBundle(Bundle bundle) {
            text = bundle.getString(KEY_TEXT);
            textResId = bundle.getInt(KEY_TEXT_RES_ID);
            value = bundle.getBoolean(KEY_VALUE);
            defaultValue = bundle.getBoolean(KEY_DEFAULT_VALUE);
            iconResId = bundle.getInt(KEY_ICON_RES_ID);
        }

        @Override
        public boolean hasValue() {
            return true;
        }

        @Override
        public boolean hasContent() {
            return value != defaultValue;
        }

        @Override
        public void removeValue() {
            value = false;
        }

        @Override
        public Component clone() {
            Property clone = new Property(tag, componentId);
            clone.copyFrom(this);
            return clone;
        }

        @Override
        public void copyFrom(Component sample) {
            super.copyFrom(sample);
            if (sample instanceof Property) {
                Property property = (Property) sample;
                this.text = property.text;
                this.textResId = property.textResId;
                this.value = property.value;
                this.defaultValue = property.defaultValue;
                this.iconResId = property.iconResId;
            } else
                throw new RuntimeException("sample component is not a Property");
        }
    }

    public static class Choice extends Component {
        private static final String KEY_CHOICE_LIST = "keyChoiceList";
        private static final String KEY_CHOICE_LIST_RES_IDS = "keyChoiceListResIds";
        private static final String KEY_CURRENT_POSITION = "keyCurrentPosition";
        private static final String KEY_DEFAULT_POSITION = "keyDefaultPosition";
        private static final String KEY_CUSTOM_MODE = "keyCustomMode";
        private static final String KEY_CUSTOM = "keyCustom";
        private static final String KEY_ICON_RES_ID = "keyIconResId";

        public static final int CUSTOM_MODE_UNAVAILABLE = 0;
        public static final int CUSTOM_MODE_AVAILABLE = 1;
        public static final int CUSTOM_MODE_SELECTED = 2;

        public ArrayList<String> choiceList;
        public ArrayList<Integer> choiceListResIds;
        public int currentPosition;
        public int defaultPosition;
        public int customMode = CUSTOM_MODE_UNAVAILABLE;
        public String custom;
        public int iconResId;

        public Choice(String tag, int componentId) {
            super(tag, componentId);
            hasIcon = false;
        }

        @Override
        protected void addToBundle(Bundle bundle) {
            bundle.putStringArrayList(KEY_CHOICE_LIST, choiceList);
            bundle.putIntegerArrayList(KEY_CHOICE_LIST_RES_IDS, choiceListResIds);
            bundle.putInt(KEY_CURRENT_POSITION, currentPosition);
            bundle.putInt(KEY_DEFAULT_POSITION, defaultPosition);
            bundle.putInt(KEY_CUSTOM_MODE, customMode);
            bundle.putString(KEY_CUSTOM, custom);
            bundle.putInt(KEY_ICON_RES_ID, iconResId);
        }

        @Override
        protected void retrieveFromBundle(Bundle bundle) {
            choiceList = bundle.getStringArrayList(KEY_CHOICE_LIST);
            choiceListResIds = bundle.getIntegerArrayList(KEY_CHOICE_LIST_RES_IDS);
            currentPosition = bundle.getInt(KEY_CURRENT_POSITION);
            defaultPosition = bundle.getInt(KEY_DEFAULT_POSITION);
            customMode = bundle.getInt(KEY_CUSTOM_MODE);
            custom = bundle.getString(KEY_CUSTOM);
            iconResId = bundle.getInt(KEY_ICON_RES_ID);
        }

        @Override
        public boolean hasValue() {
            return true;
        }

        @Override
        public boolean hasContent() {
            return customMode == CUSTOM_MODE_SELECTED || currentPosition != defaultPosition;
        }

        @Override
        public void removeValue() {
            currentPosition = defaultPosition;
        }

        @Override
        public Component clone() {
            Choice clone = new Choice(tag, componentId);
            clone.copyFrom(this);
            return clone;
        }

        @Override
        public void copyFrom(Component sample) {
            super.copyFrom(sample);
            if (sample instanceof Choice) {
                Choice choice = (Choice) sample;
                if (choice.choiceList != null)
                    this.choiceList = new ArrayList<>(choice.choiceList);
                if (choice.choiceListResIds != null)
                    this.choiceListResIds = new ArrayList<>(choice.choiceListResIds);
                this.currentPosition = choice.currentPosition;
                this.defaultPosition = choice.defaultPosition;
                this.customMode = choice.customMode;
                this.custom = choice.custom;
                this.iconResId = choice.iconResId;
            } else
                throw new RuntimeException("sample component is not a Choice");
        }

        public String getCurrentText(Resources resources) {
            if (customMode == CUSTOM_MODE_SELECTED)
                return custom;
            else if (choiceList != null)
                return choiceList.get(currentPosition);
            else if (choiceListResIds != null)
                return resources.getString(choiceListResIds.get(currentPosition));
            else
                return null;
        }
    }

    //====================================== ViewComponents ========================================
    public static abstract class ViewComponent {
        public boolean hasIcon = true;
    }

    public static class ViewText extends ViewComponent {
        public int iconResId;
        public String text;
        public String value;
        public Integer textResId;
    }

    //====================================== Constraints =========================================
    public static abstract class Constraint {
        private static final String KEY_IS_REQUIRED = "keyIsRequired";
        private static final String KEY_CONSTRAINT_CODE = "code";
        private static final int CODE_HIDE = 2;

        public boolean isRequired;

        public Bundle toBundle() {
            Bundle bundle = new Bundle();
            bundle.putBoolean(KEY_IS_REQUIRED, isRequired);
            if (this instanceof HideConstraint)
                bundle.putInt(KEY_CONSTRAINT_CODE, CODE_HIDE);
            else
                throw new RuntimeException("Constraint type unknown");

            this.addToBundle(bundle);
            return bundle;
        }

        public static Constraint fromBundle(Bundle bundle) {
            Constraint constraint;
            int code = bundle.getInt(KEY_CONSTRAINT_CODE);
            if (code == CODE_HIDE)
                constraint = new HideConstraint();
            else
                throw new RuntimeException("Constraint type unknown");

            constraint.setFromBundle(bundle);
            constraint.isRequired = bundle.getBoolean(KEY_IS_REQUIRED);
            return constraint;
        }

        public abstract void addToBundle(Bundle bundle);

        public abstract void setFromBundle(Bundle bundle);

        public abstract boolean doesConstraint(int componentIndex);

        public abstract boolean isComplete(ArrayList<Component> components);

        public abstract Constraint clone();

        public void copyFrom(Constraint sample) {
            this.isRequired = sample.isRequired;
        }
    }

    public static class HideConstraint extends Constraint {
        public static final String KEY_SUBJECT = "subject";
        public static final String KEY_SHOULD_HIDE = "shouldHide";

        public int subject;
        public ShouldHide shouldHide;

        @Override
        public void addToBundle(Bundle bundle) {
            bundle.putInt(KEY_SUBJECT, subject);
            bundle.putSerializable(KEY_SHOULD_HIDE, shouldHide);
        }

        @Override
        public void setFromBundle(Bundle bundle) {
            subject = bundle.getInt(KEY_SUBJECT);
            shouldHide = (ShouldHide) bundle.getSerializable(KEY_SHOULD_HIDE);
        }

        @Override
        public boolean doesConstraint(int componentIndex) {
            return componentIndex == subject;
        }

        @Override
        public boolean isComplete(ArrayList<Component> components) {
            return true;
        }

        @Override
        public Constraint clone() {
            HideConstraint clone = new HideConstraint();
            clone.copyFrom(this);
            return clone;
        }

        public void copyFrom(HideConstraint sample) {
            this.subject = sample.subject;
            this.shouldHide = sample.shouldHide;
        }

        @Immutable
        public static abstract class ShouldHide implements Serializable {
            public abstract boolean shouldHide(AveComponentSet componentSet);
        }
    }
}
