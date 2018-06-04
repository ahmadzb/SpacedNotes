package data.model.label;


import java.util.Comparator;

import javax.annotation.concurrent.Immutable;

import exceptions.InvalidItemException;

/**
 * Created by Ahmad on 01/01/18.
 * All rights reserved.
 */

@Immutable
public class Label {
    private long id;
    private String title;
    private Long deleted;
    private boolean isRealized;
    private boolean isInitialized;

    private Integer foreignPosition;

    private Label() {
    }

    public static Label newInstance() {
        return new Label();
    }

    public long getId() {
        return id;
    }

    public Label setId(long id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Label setTitle(String title) {
        this.title = title;
        return this;
    }

    public Long getDeleted() {
        return deleted;
    }

    public Label setDeleted(Long deleted) {
        this.deleted = deleted;
        return this;
    }

    public boolean isRealized() {
        return isRealized;
    }

    public Label setRealized(boolean realized) {
        isRealized = realized;
        return this;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public Label setInitialized(boolean initialized) {
        isInitialized = initialized;
        return this;
    }

    public Integer getForeignPosition() {
        return foreignPosition;
    }

    public Label setForeignPosition(Integer foreignPosition) {
        this.foreignPosition = foreignPosition;
        return this;
    }

    public Label clone() {
        Label clone = new Label();
        clone.id = id;
        clone.title = title;
        clone.deleted = deleted;
        clone.isRealized = isRealized;
        clone.isInitialized = isInitialized;
        clone.foreignPosition = foreignPosition;
        return clone;
    }

    public static Comparator<Label> getForeignPositionComparator() {
        return new Comparator<Label>() {
            @Override
            public int compare(Label o1, Label o2) {
                if (o1.getForeignPosition() == null && o2.getForeignPosition() == null) {
                    return 0;
                } else if (o1.getForeignPosition() == null) {
                    return -1;
                } else if (o2.getForeignPosition() == null) {
                    return 1;
                } else {
                    return Integer.compare(o1.getForeignPosition(), o2.getForeignPosition());
                }
            }
        };
    }
}
