package data.model.profiles;

import android.support.annotation.IntRange;

import java.util.Comparator;

/**
 * Created by Ahmad on 01/31/18.
 * All rights reserved.
 */

public class Profile {

    private long id;
    private int position;
    private int color;
    private String name;
    private boolean isArchived;
    private boolean offline;
    private int imageQualityPercentage;

    private boolean isRealized;
    private boolean isInitialized;

    private Profile() {

    }

    public static Profile newInstance() {
        return new Profile();
    }

    public long getId() {
        return id;
    }

    public Profile setId(long id) {
        this.id = id;
        return this;
    }

    public int getPosition() {
        return position;
    }

    public Profile setPosition(int position) {
        this.position = position;
        return this;
    }

    public int getColor() {
        return color;
    }

    public Profile setColor(int color) {
        this.color = color;
        return this;
    }

    public String getName() {
        return name;
    }

    public Profile setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public Profile setArchived(boolean archived) {
        isArchived = archived;
        return this;
    }

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    @IntRange(from = 0, to = 100)
    public int getImageQualityPercentage() {
        return imageQualityPercentage;
    }

    public void setImageQualityPercentage(@IntRange(from = 0, to = 100) int imageQualityPercentage) {
        this.imageQualityPercentage = imageQualityPercentage;
    }

    public boolean isRealized() {
        return isRealized;
    }

    public Profile setRealized(boolean realized) {
        isRealized = realized;
        return this;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public Profile setInitialized(boolean initialized) {
        isInitialized = initialized;
        return this;
    }

    public Profile clone() {
        Profile clone = new Profile();
        clone.id = this.id;
        clone.position = this.position;
        clone.color = this.color;
        clone.name = this.name;
        clone.isArchived = this.isArchived;
        clone.imageQualityPercentage = this.imageQualityPercentage;
        clone.isRealized = this.isRealized;
        clone.isInitialized = this.isInitialized;
        return clone;
    }

    public static class PositionComparator implements Comparator<Profile> {
        @Override
        public int compare(Profile o1, Profile o2) {
            return Integer.compare(o1.getPosition(), o2.getPosition());
        }
    }
}
