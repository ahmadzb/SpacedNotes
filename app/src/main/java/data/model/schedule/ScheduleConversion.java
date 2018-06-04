package data.model.schedule;

import javax.annotation.concurrent.Immutable;

/**
 * Created by Ahmad on 01/05/18.
 * All rights reserved.
 */

@Immutable
public class ScheduleConversion {
    private long fromOccurrenceId;
    private long toScheduleId;
    private int toOccurrenceNumber;

    private ScheduleConversion() {
    }

    public static ScheduleConversion newInstance() {
        return new ScheduleConversion();
    }

    public long getFromOccurrenceId() {
        return fromOccurrenceId;
    }

    public long getToScheduleId() {
        return toScheduleId;
    }

    public int getToOccurrenceNumber() {
        return toOccurrenceNumber;
    }

    public ScheduleConversion setFromOccurrenceId(long fromOccurrenceId) {
        this.fromOccurrenceId = fromOccurrenceId;
        return this;
    }

    public ScheduleConversion setToScheduleId(long toScheduleId) {
        this.toScheduleId = toScheduleId;
        return this;
    }

    public ScheduleConversion setToOccurrenceNumber(int toOccurrenceNumber) {
        this.toOccurrenceNumber = toOccurrenceNumber;
        return this;
    }
}
