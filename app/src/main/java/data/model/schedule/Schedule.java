package data.model.schedule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import javax.annotation.concurrent.Immutable;

import exceptions.InvalidItemException;

/**
 * Created by Ahmad on 01/01/18.
 * All rights reserved.
 */

@Immutable
public class Schedule {

    private long id;
    private Integer color;
    private String title;
    private OccurrenceSet occurrenceSet;
    private int position;
    private boolean isRealized;
    private boolean isInitialized;

    private Schedule() {
    }

    public static Schedule newInstance() {
        return new Schedule();
    }

    public long getId() {
        return id;
    }

    public Schedule setId(long id) {
        this.id = id;
        return this;
    }

    public Integer getColor() {
        return color;
    }

    public Schedule setColor(Integer color) {
        this.color = color;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Schedule setTitle(String title) {
        this.title = title;
        return this;
    }

    public OccurrenceSet getOccurrenceSet() {
        return occurrenceSet;
    }

    public Schedule setOccurrenceSet(OccurrenceSet occurrenceSet) {
        this.occurrenceSet = occurrenceSet;
        return this;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isRealized() {
        return isRealized;
    }

    public Schedule setRealized(boolean realized) {
        isRealized = realized;
        return this;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public Schedule setInitialized(boolean initialized) {
        isInitialized = initialized;
        return this;
    }


    public int getOccurrencesCount() {
        if (occurrenceSet == null)
            return 0;
        return occurrenceSet.getCount();
    }

    public Occurrence getFirstOccurrence() {
        if (occurrenceSet == null)
            return null;
        int minNumber = Integer.MAX_VALUE;
        Occurrence minOccurrence = null;
        for (Occurrence occurrence : occurrenceSet.occurrences) {
            if (occurrence.getNumber() < minNumber) {
                minNumber = occurrence.getNumber();
                minOccurrence = occurrence;
            }
        }
        return minOccurrence;
    }

    public Occurrence getOccurrenceByNumber(int number) {
        if (occurrenceSet == null)
            return null;
        if (occurrenceSet.getCount() <= number)
            return null;
        else
            return occurrenceSet.getOccurrenceByNumber(number);
    }

    public Occurrence findOccurrenceById(long id) {
        if (occurrenceSet == null)
            return null;
        return occurrenceSet.findOccurrenceById(id);
    }

    public static class OccurrenceSet {
        private ArrayList<Occurrence> occurrences;

        private OccurrenceSet() {

        }

        public static OccurrenceSet newInstance(Collection<Occurrence> occurrences, long scheduleIdCheck) {
            if (occurrences == null)
                return null;
            OccurrenceSet occurrenceSet = new OccurrenceSet();
            occurrenceSet.occurrences = new ArrayList<>(occurrences);
            Collections.sort(occurrenceSet.occurrences, new Occurrence.OccurrenceNumberComparator());
            int count = occurrenceSet.occurrences.size();
            for (int i = 0; i < count; i++) {
                if (occurrenceSet.occurrences.get(i).getNumber() != i ||
                        occurrenceSet.occurrences.get(i).getScheduleId() != scheduleIdCheck)
                    throw new RuntimeException("Given occurrence set is inconsistent");
            }
            return occurrenceSet;
        }

        public int getCount() {
            return occurrences.size();
        }

        public Occurrence getOccurrenceByNumber(int number) {
            return occurrences.get(number);
        }

        public Occurrence findOccurrenceById(long id) {
            for (Occurrence occurrence : occurrences) {
                if (occurrence.getId() == id) {
                    return occurrence;
                }
            }
            return null;
        }
    }

    public static Comparator<Schedule> getPositionComparator() {
        return new PositionComparator();
    }

    private static class PositionComparator implements Comparator<Schedule> {
        @Override
        public int compare(Schedule o1, Schedule o2) {
            return Integer.compare(o1.getPosition(), o2.getPosition());
        }
    }
}
