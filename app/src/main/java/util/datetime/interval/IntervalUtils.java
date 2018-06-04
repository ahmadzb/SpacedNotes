package util.datetime.interval;

import android.content.res.Resources;

import com.diplinkblaze.spacednote.R;

import org.joda.time.Days;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.chrono.PersianChronology;
import util.datetime.format.DateTimeFormatter;

/**
 * Created by Ahmad on 11/20/17.
 * All rights reserved.
 */

public class IntervalUtils {
    public static final int INTERVAL_IRREGULAR = -1;
    public static final int INTERVAL_LAST_DAY = 0;
    public static final int INTERVAL_LAST_7_DAYS = 1;
    public static final int INTERVAL_LAST_WEEK = 2;
    public static final int INTERVAL_LAST_30_DAYS = 3;
    public static final int INTERVAL_LAST_MONTH = 4;
    public static final int INTERVAL_LAST_YEAR = 5;
    public static final int INTERVAL_NEXT_DAY = 6;
    public static final int INTERVAL_NEXT_7_DAYS = 7;
    public static final int INTERVAL_NEXT_WEEK = 8;
    public static final int INTERVAL_NEXT_30_DAYS = 9;
    public static final int INTERVAL_NEXT_MONTH = 10;
    public static final int INTERVAL_NEXT_YEAR = 11;
    public static final int INTERVAL_THIS_DAY = 12;
    public static final int INTERVAL_THIS_WEEK = 13;
    public static final int INTERVAL_THIS_MONTH = 14;
    public static final int INTERVAL_THIS_YEAR = 15;

    public static final int TYPE_YEAR = 1;
    public static final int TYPE_MONTH = 2;
    public static final int TYPE_OTHER = 3;

    public static Interval makeInterval(int interval) {
        LocalDate from;
        LocalDate to;
        LocalDate now = LocalDate.now(PersianChronology.getInstance());
        if (interval == INTERVAL_LAST_DAY) {
            from = now.minusDays(1);
            to = now.minusDays(1);
        } else if (interval == INTERVAL_LAST_7_DAYS) {
            from = now.minusDays(7);
            to = now.minusDays(1);
        } else if (interval == INTERVAL_LAST_WEEK) {
            from = now.plusDays(2).minusWeeks(1).withDayOfWeek(1).minusDays(2);
            to = now.plusDays(2).minusWeeks(1).withDayOfWeek(7).minusDays(2);
        } else if (interval == INTERVAL_LAST_30_DAYS) {
            from = now.minusDays(30);
            to = now.minusDays(1);
        } else if (interval == INTERVAL_LAST_MONTH) {
            from = now.withDayOfMonth(1).minusMonths(1);
            to = now.withDayOfMonth(1).minusDays(1);
        } else if (interval == INTERVAL_LAST_YEAR) {
            from = now.withDayOfYear(1).minusYears(1);
            to = now.withDayOfYear(1).minusDays(1);
        } else if (interval == INTERVAL_NEXT_DAY) {
            from = now.plusDays(1);
            to = now.plusDays(1);
        } else if (interval == INTERVAL_NEXT_7_DAYS) {
            from = now.plusDays(1);
            to = now.plusDays(7);
        } else if (interval == INTERVAL_NEXT_WEEK) {
            from = now.plusDays(2).plusWeeks(1).withDayOfWeek(1).minusDays(2);
            to = now.plusDays(2).plusWeeks(1).withDayOfWeek(7).minusDays(2);
        } else if (interval == INTERVAL_NEXT_30_DAYS) {
            from = now.plusDays(1);
            to = now.plusDays(30);
        } else if (interval == INTERVAL_NEXT_MONTH) {
            from = now.withDayOfMonth(1).plusMonths(1);
            to = now.withDayOfMonth(1).plusMonths(2).minusDays(1);
        } else if (interval == INTERVAL_NEXT_YEAR) {
            from = now.withDayOfYear(1).plusYears(1);
            to = now.withDayOfYear(1).plusYears(2).minusDays(1);
        } else if (interval == INTERVAL_THIS_DAY) {
            from = now;
            to = now;
        } else if (interval == INTERVAL_THIS_WEEK) {
            from = now.plusDays(2).withDayOfWeek(1).minusDays(2);
            to = now.plusDays(2).withDayOfWeek(7).minusDays(2);
        } else if (interval == INTERVAL_THIS_MONTH) {
            from = now.withDayOfMonth(1);
            to = now.withDayOfMonth(1).plusMonths(1).minusDays(1);
        } else if (interval == INTERVAL_THIS_YEAR) {
            from = now.withDayOfYear(1);
            to = now.withDayOfYear(1).plusYears(1).minusDays(1);
        } else
            throw new RuntimeException("given interval was not recognized");
        return new Interval(from.toDateTimeAtStartOfDay(), to.plusDays(1).toDateTimeAtStartOfDay().minusMillis(1));
    }

    public static int findInterval(Interval interval) {
        int code;
        if (interval.equals(makeInterval(INTERVAL_LAST_DAY))) {
            code = INTERVAL_LAST_DAY;
        } else if (interval.equals(makeInterval(INTERVAL_LAST_7_DAYS))) {
            code = INTERVAL_LAST_7_DAYS;
        } else if (interval.equals(makeInterval(INTERVAL_LAST_WEEK))) {
            code = INTERVAL_LAST_WEEK;
        } else if (interval.equals(makeInterval(INTERVAL_LAST_30_DAYS))) {
            code = INTERVAL_LAST_30_DAYS;
        } else if (interval.equals(makeInterval(INTERVAL_LAST_MONTH))) {
            code = INTERVAL_LAST_MONTH;
        } else if (interval.equals(makeInterval(INTERVAL_LAST_YEAR))) {
            code = INTERVAL_LAST_YEAR;
        } else if (interval.equals(makeInterval(INTERVAL_NEXT_DAY))) {
            code = INTERVAL_NEXT_DAY;
        } else if (interval.equals(makeInterval(INTERVAL_NEXT_7_DAYS))) {
            code = INTERVAL_NEXT_7_DAYS;
        } else if (interval.equals(makeInterval(INTERVAL_NEXT_WEEK))) {
            code = INTERVAL_NEXT_WEEK;
        } else if (interval.equals(makeInterval(INTERVAL_NEXT_30_DAYS))) {
            code = INTERVAL_NEXT_30_DAYS;
        } else if (interval.equals(makeInterval(INTERVAL_NEXT_MONTH))) {
            code = INTERVAL_NEXT_MONTH;
        } else if (interval.equals(makeInterval(INTERVAL_NEXT_YEAR))) {
            code = INTERVAL_NEXT_YEAR;
        } else if (interval.equals(makeInterval(INTERVAL_THIS_DAY))) {
            code = INTERVAL_THIS_DAY;
        } else if (interval.equals(makeInterval(INTERVAL_THIS_WEEK))) {
            code = INTERVAL_THIS_WEEK;
        } else if (interval.equals(makeInterval(INTERVAL_THIS_MONTH))) {
            code = INTERVAL_THIS_MONTH;
        } else if (interval.equals(makeInterval(INTERVAL_THIS_YEAR))) {
            code = INTERVAL_THIS_YEAR;
        } else
            code = INTERVAL_IRREGULAR;
        return code;
    }

    public static String makeTextForInterval(Interval interval, Resources resources) {
        DateTimeFormatter.Builder builder =
                DateTimeFormatter.Builder.getInstance(resources);
        LocalDate now = LocalDate.now(PersianChronology.getInstance());
        if (interval.getStart().getYear() == now.getYear() && interval.getEnd().getYear() == now.getYear()) {
            builder.addDayOfMonth(2).addText(" ").addMonthOfYearFull();
        } else {
            builder.addYear(2).addText(resources.getString(R.string.partial_slash)).addMonthOfYear(2)
                    .addText(resources.getString(R.string.partial_slash)).addDayOfMonth(2);
        }
        DateTimeFormatter formatter = builder.build();
        return formatter.print(interval.getStart()) + " " + resources.getString(R.string.partial_hyphen) +
                " " + formatter.print(interval.getEnd());
    }

    public static int getIntervalType(Interval interval) {
        LocalDate from = interval.getStart().toLocalDate();
        LocalDate to = interval.getEnd().toLocalDate();
        if (from.getDayOfYear() == 1 && to.plusDays(1).getDayOfYear() == 1 && from.getYear() == to.getYear())
            return TYPE_YEAR;
        else if (from.getDayOfMonth() == 1 && to.plusDays(1).getDayOfMonth() == 1 && from.getMonthOfYear() == to.getMonthOfYear())
            return TYPE_MONTH;
        else
            return TYPE_OTHER;
    }

    public static Interval getPreviousInterval(LocalDate from, LocalDate to) {
        return getPreviousInterval(toInterval(from, to));
    }

    public static Interval getPreviousInterval(Interval interval) {
        if (interval == null)
            return null;
        int type = getIntervalType(interval);
        if (type == TYPE_YEAR)
            return new Interval(interval.getStart().minusYears(1).withTimeAtStartOfDay(),
                    interval.getEnd().plusDays(1).minusYears(1).withTimeAtStartOfDay().minusMillis(1));
        else if (type == TYPE_MONTH)
            return new Interval(interval.getStart().minusMonths(1).withTimeAtStartOfDay(),
                    interval.getEnd().plusDays(1).minusMonths(1).withTimeAtStartOfDay().minusMillis(1));
        else {
            int days = Days.daysIn(interval).getDays() + 1;
            return new Interval(interval.getStart().minusDays(days).withTimeAtStartOfDay(),
                    interval.getEnd().minusDays(days - 1).withTimeAtStartOfDay().minusMillis(1));
        }
    }

    public static Interval getNextInterval(LocalDate from, LocalDate to) {
        return getNextInterval(toInterval(from, to));
    }

    public static Interval getNextInterval(Interval interval) {
        if (interval == null)
            return null;
        int type = getIntervalType(interval);
        if (type == TYPE_YEAR)
            return new Interval(interval.getStart().plusYears(1).withTimeAtStartOfDay(),
                    interval.getEnd().plusDays(1).plusYears(1).withTimeAtStartOfDay().minusMillis(1));
        else if (type == TYPE_MONTH)
            return new Interval(interval.getStart().plusMonths(1).withTimeAtStartOfDay(),
                    interval.getEnd().plusDays(1).plusMonths(1).withTimeAtStartOfDay().minusMillis(1));
        else {
            int days = Days.daysIn(interval).getDays() + 1;
            return new Interval(interval.getStart().plusDays(days).withTimeAtStartOfDay(),
                    interval.getEnd().plusDays(days + 1).withTimeAtStartOfDay().minusMillis(1));
        }
    }

    public static Interval toInterval(LocalDate from, LocalDate to) {
        return new Interval(from.toDateTimeAtStartOfDay(), to.plusDays(1).toDateTimeAtStartOfDay().minusMillis(1));
    }

    public static boolean equalLocalDates(Interval a, Interval b) {
        return a.getStart().toLocalDate().equals(b.getStart().toLocalDate()) &&
                a.getEnd().toLocalDate().equals(b.getEnd().toLocalDate());
    }

    public static boolean isSubset(Interval subset, Interval parent) {
        return !parent.getStart().isAfter(subset.getStart()) && !parent.getEnd().isBefore(subset.getEnd());
    }
}
