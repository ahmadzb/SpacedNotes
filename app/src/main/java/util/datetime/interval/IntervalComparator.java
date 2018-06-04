package util.datetime.interval;

import org.joda.time.Interval;

import java.util.Comparator;

/**
 * Created by Ahmad on 11/17/17.
 * All rights reserved.
 */

public class IntervalComparator implements Comparator<Interval> {

    @Override
    public int compare(Interval o1, Interval o2) {
        int result = Long.compare(o1.getEndMillis(), o2.getEndMillis());
        if (result == 0) {
            result = Long.compare(o1.getStartMillis(), o2.getStartMillis());
        }
        return result;
    }
}
