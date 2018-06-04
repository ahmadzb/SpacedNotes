package data.model.scheduler;

import javax.annotation.concurrent.Immutable;

/**
 * Created by Ahmad on 01/03/18.
 * All rights reserved.
 */

@Immutable
public class RevisionFuture {
    private long noteId; //Primary key
    private int dueDate;
    private long scheduleId;
    private int occurrenceNumber;
    private boolean isRealized;
    private boolean isInitialized;

    private RevisionFuture() {
    }

    public static RevisionFuture newInstance() {
        return new RevisionFuture();
    }

    public long getNoteId() {
        return noteId;
    }

    public RevisionFuture setNoteId(long noteId) {
        this.noteId = noteId;
        return this;
    }

    public int getDueDate() {
        return dueDate;
    }

    public RevisionFuture setDueDate(int dueDate) {
        this.dueDate = dueDate;
        return this;
    }

    public long getScheduleId() {
        return scheduleId;
    }

    public RevisionFuture setScheduleId(long scheduleId) {
        this.scheduleId = scheduleId;
        return this;
    }

    public int getOccurrenceNumber() {
        return occurrenceNumber;
    }

    public RevisionFuture setOccurrenceNumber(int occurrenceNumber) {
        this.occurrenceNumber = occurrenceNumber;
        return this;
    }

    public boolean isRealized() {
        return isRealized;
    }

    public RevisionFuture setRealized(boolean realized) {
        isRealized = realized;
        return this;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public RevisionFuture setInitialized(boolean initialized) {
        isInitialized = initialized;
        return this;
    }
}
