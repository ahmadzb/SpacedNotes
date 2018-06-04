package data.model.schedule;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeMap;

import javax.annotation.concurrent.Immutable;

/**
 * Created by Ahmad on 01/04/18.
 * All rights reserved.
 */

@Immutable
public class Occurrence {
    private long id;
    /**
     * Starting at 0
     */
    private Integer number;
    private Integer plusDays;
    private Long scheduleId;
    private TreeMap<Long, ScheduleConversion> conversions;//Key is the destination Schedule id
    private boolean isRealized;
    private boolean isInitialized;

    private Occurrence() {
    }

    public static Occurrence newInstance() {
        return new Occurrence();
    }

    public long getId() {
        return id;
    }

    public Occurrence setId(long id) {
        this.id = id;
        return this;
    }

    public Integer getNumber() {
        return number;
    }

    public Integer getEndUserNumber() {
        return number + 2;
    }

    public Occurrence setNumber(Integer number) {
        this.number = number;
        return this;
    }

    public Integer getPlusDays() {
        return plusDays;
    }

    public Occurrence setPlusDays(Integer plusDays) {
        this.plusDays = plusDays;
        return this;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public Occurrence setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
        return this;
    }

    public Occurrence setConversions(TreeMap<Long, ScheduleConversion> conversions) {
        this.conversions = conversions;
        return this;
    }

    public boolean isRealized() {
        return isRealized;
    }

    public Occurrence setRealized(boolean realized) {
        isRealized = realized;
        return this;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public Occurrence setInitialized(boolean initialized) {
        isInitialized = initialized;
        return this;
    }

    public ScheduleConversion getConversionForSchedule(Schedule schedule) {
        if (conversions == null)
            return null;
        return conversions.get(schedule.getId());
    }

    public Collection<ScheduleConversion> getConversions() {
        if (conversions == null)
            return null;
        return conversions.values();
    }

    public static class OccurrenceNumberComparator implements Comparator<Occurrence> {

        @Override
        public int compare(Occurrence o1, Occurrence o2) {
            return Integer.compare(o1.getNumber(), o2.getNumber());
        }
    }

    @Override
    public Occurrence clone() {
        Occurrence occurrence = new Occurrence();
        occurrence.id = this.id;
        occurrence.number = this.number;
        occurrence.plusDays = this.plusDays;
        occurrence.scheduleId = this.scheduleId;
        occurrence.conversions = new TreeMap<>(this.conversions);
        occurrence.isRealized = this.isRealized;
        occurrence.isInitialized = this.isInitialized;
        return occurrence;
    }
}
