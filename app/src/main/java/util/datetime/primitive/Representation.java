package util.datetime.primitive;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 * Created by Ahmad on 01/03/18.
 * All rights reserved.
 */

public final class Representation {
    public static int fromLocalDate(LocalDate localDate) {
        LocalDate startDate = new LocalDate(0, localDate.getChronology());
        return Days.daysBetween(startDate, localDate).getDays();
    }

    public static LocalDate toLocalDate(int date) {
        return toLocalDate(date, null);
    }

    public static LocalDate toLocalDate(int date, Chronology chronology) {
        LocalDate startDate = chronology == null? new LocalDate(0) : new LocalDate(0, chronology);
        return startDate.plusDays((int) date);
    }

    public static long fromDateTime(DateTime dateTime) {
        return dateTime.getMillis();
    }

    public static DateTime toDateTime(long dateTime) {
        return toDateTime(dateTime, null);
    }

    public static DateTime toDateTime(long dateTime, Chronology chronology) {
        return chronology == null ? new DateTime(dateTime) : new DateTime(dateTime, chronology);
    }
}
