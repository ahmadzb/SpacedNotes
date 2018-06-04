package org.joda.time.chrono;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

/**
 * Created by Ahmad on 09/29/17.
 * All rights reserved.
 */

public class Holidays {

    private static final int[] persianHolidayMonths = {1, 1, 1, 1, 1, 1, 3, 3, 11, 12, 12};
    private static final int[] persianHolidayDays = {1, 2, 3, 4, 12, 13, 14, 15, 22, 29, 30};

    private static final int[] islamicHolidayMonths = {1, 1, 2, 2, 2, 3, 3, 6, 7, 7, 8, 9, 10, 10, 10, 12, 12};
    private static final int[] islamicHolidayDays = {9, 10, 20, 28, 30, 8, 17, 3, 13, 27, 15, 21, 1, 2, 25, 10, 18};

    public static boolean isPersianHoliday(LocalDate date)
    {
        if (!(date.getChronology() instanceof PersianChronology))
            date = new LocalDate(date.toDateTime(new LocalTime(date.getChronology())),
                    PersianChronology.getInstance());
        for (int i = 0; i < persianHolidayMonths.length; i++)
            if (date.getMonthOfYear() == persianHolidayMonths[i] &&
                    date.getDayOfMonth() == persianHolidayDays[i])
                return true;
        return false;
    }

    public static boolean isPersianHolidayOrWeekend(LocalDate date)
    {
        if (!(date.getChronology() instanceof PersianChronology))
            date = new LocalDate(date.toDateTime(new LocalTime(date.getChronology())),
                    PersianChronology.getInstance());
        if (date.getDayOfWeek() == 5)
            return true;
        for (int i = 0; i < persianHolidayMonths.length; i++)
            if (date.getMonthOfYear() == persianHolidayMonths[i] &&
                    date.getDayOfMonth() == persianHolidayDays[i])
                return true;
        return false;
    }

    public static boolean isIslamicHoliday(LocalDate date)
    {
        if (!(date.getChronology() instanceof IslamicChronology))
            date = new LocalDate(date.toDateTime(new LocalTime(date.getChronology())),
                    IslamicChronology.getInstance(DateTimeZone.getDefault()));
        for (int i = 0; i < islamicHolidayMonths.length; i++)
            if (date.getMonthOfYear() == islamicHolidayMonths[i] &&
                    date.getDayOfMonth() == islamicHolidayDays[i])
                return true;
        return false;
    }
}
