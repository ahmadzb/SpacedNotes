package data.model.scheduler;

import org.joda.time.LocalDate;

import java.util.Comparator;

import javax.annotation.concurrent.Immutable;

/**
 * Created by Ahmad on 01/03/18.
 * All rights reserved.
 */

@Immutable
public class RevisionPast {
    private long noteId;
    private int date;
    private boolean initialized;
    private boolean realized;

    private RevisionPast() {
    }

    public static RevisionPast newInstance() {
        return new RevisionPast();
    }

    public long getNoteId() {
        return noteId;
    }

    public RevisionPast setNoteId(long noteId) {
        this.noteId = noteId;
        return this;
    }

    public int getDate() {
        return date;
    }

    public RevisionPast setDate(int date) {
        this.date = date;
        return this;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public RevisionPast setInitialized(boolean initialized) {
        this.initialized = initialized;
        return this;
    }

    public boolean isRealized() {
        return realized;
    }

    public RevisionPast setRealized(boolean realized) {
        this.realized = realized;
        return this;
    }

    public static Comparator<RevisionPast> getDateComparator() {
        return new Comparator<RevisionPast>() {
            @Override
            public int compare(RevisionPast o1, RevisionPast o2) {
                return Integer.compare(o1.getDate(), o2.getDate());
            }
        };
    }
}
