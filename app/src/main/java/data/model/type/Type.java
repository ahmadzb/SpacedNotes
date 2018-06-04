package data.model.type;

import java.util.Comparator;

import data.database.Contract;

/**
 * Created by Ahmad on 01/08/18.
 * All rights reserved.
 */

public class Type {

    private long id;
    private String title;
    private int color;
    private int position;
    private boolean isArchived;
    private boolean isRealized;
    private boolean isInitialized;

    private Type() {
    }

    public static Type newInstance() {
        return new Type();
    }

    public long getId() {
        return id;
    }

    public Type setId(long id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Type setTitle(String title) {
        this.title = title;
        return this;
    }

    public int getColor() {
        return color;
    }

    public Type setColor(int color) {
        this.color = color;
        return this;
    }

    public int getPosition() {
        return position;
    }

    public Type setPosition(int position) {
        this.position = position;
        return this;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public Type setArchived(boolean archived) {
        isArchived = archived;
        return this;
    }

    public boolean isRealized() {
        return isRealized;
    }

    public Type setRealized(boolean realized) {
        isRealized = realized;
        return this;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public Type setInitialized(boolean initialized) {
        isInitialized = initialized;
        return this;
    }

    @Override
    public Type clone() {
        Type clone = new Type();
        clone.id = this.id;
        clone.title = this.title;
        clone.color = this.color;
        clone.position = this.position;
        clone.isArchived = this.isArchived;
        clone.isRealized = this.isRealized;
        clone.isInitialized = this.isInitialized;
        return clone;
    }

    public static Comparator<Type> getTypeComparator() {
        return new Comparator<Type>() {
            @Override
            public int compare(Type o1, Type o2) {
                return Integer.compare(o1.getPosition(), o2.getPosition());
            }
        };
    }
}

