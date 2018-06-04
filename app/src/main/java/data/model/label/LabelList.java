package data.model.label;

import java.util.ArrayList;

import javax.annotation.concurrent.Immutable;

import exceptions.InvalidItemException;

/**
 * Created by Ahmad on 01/01/18.
 * All rights reserved.
 */

@Immutable
public class LabelList {
    private long id;
    private String title;
    private Integer color;
    private Long parentId;
    private int position;
    private ArrayList<Label> labels;
    private boolean isRealized;
    private boolean isInitialized;

    private LabelList() {

    }

    public static LabelList newInstance() {
        return new LabelList();
    }

    public long getId() {
        return id;
    }

    public LabelList setId(long id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public LabelList setTitle(String title) {
        this.title = title;
        return this;
    }

    public Integer getColor() {
        return color;
    }

    public LabelList setColor(Integer color) {
        this.color = color;
        return this;
    }

    public Long getParentId() {
        return parentId;
    }

    public LabelList setParentId(Long parentId) {
        this.parentId = parentId;
        return this;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public ArrayList<Label> getLabels() {
        return labels;
    }

    public LabelList setLabels(ArrayList<Label> labels) {
        this.labels = labels;
        return this;
    }

    public boolean isRealized() {
        return isRealized;
    }

    public LabelList setRealized(boolean realized) {
        isRealized = realized;
        return this;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public LabelList setInitialized(boolean initialized) {
        isInitialized = initialized;
        return this;
    }
}
