package org.joda.time.chrono;


import org.joda.time.Chronology;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Ahmad Zendeboodi on 09/26/17.
 * All rights reserved.
 */

public class PersianChronology extends BasicChronology {

    private static final long ESF_30 = 365 * DateTimeConstants.MILLIS_PER_DAY;
    /** Singleton instance of a UTC GregorianChronology */
    private static final Chronology INSTANCE_UTC;

    /** Cache of zone to chronology arrays */
    private static final ConcurrentHashMap<DateTimeZone, Chronology[]> cCache = new ConcurrentHashMap<DateTimeZone, Chronology[]>();

    static {
        INSTANCE_UTC = getInstance(DateTimeZone.UTC);
    }

    PersianChronology(Chronology base, Object param,  int minDaysInFirstWeek) {
        super(base, param, minDaysInFirstWeek);
    }

    public static Chronology getInstanceUTC()
    {
        return getInstance(DateTimeZone.UTC, 4);
    }

    public static Chronology getInstance()
    {
        return getInstance(DateTimeZone.getDefault(), 4);
    }

    public static Chronology getInstance(DateTimeZone zone)
    {
        return getInstance(zone, 4);
    }

    public static Chronology getInstance(DateTimeZone zone, int minDaysInFirstWeek)
    {
        if (zone == null) {
            zone = DateTimeZone.getDefault();
        }
        Chronology chrono;
        Chronology[] chronos = cCache.get(zone);
        if (chronos == null) {
            chronos = new Chronology[7];
            Chronology[] oldChronos = cCache.putIfAbsent(zone, chronos);
            if (oldChronos != null) {
                chronos = oldChronos;
            }
        }
        try {
            chrono = chronos[minDaysInFirstWeek - 1];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException
                    ("Invalid min days in first week: " + minDaysInFirstWeek);
        }
        if (chrono == null) {
            synchronized (chronos) {
                chrono = chronos[minDaysInFirstWeek - 1];
                if (chrono == null) {
                    if (zone == DateTimeZone.UTC) {
                        chrono = new PersianChronology(null, null, minDaysInFirstWeek);
                    } else {
                        chrono = getInstance(DateTimeZone.UTC, minDaysInFirstWeek);
                        chrono = ZonedChronology.getInstance(chrono, zone);
                    }
                    chronos[minDaysInFirstWeek - 1] = chrono;
                }
            }
        }
        return chrono;
    }

    @Override
    int getMonthOfYear(long millis, int year) {
        long startMillis = getYearMillis(year);
        int dayOfYear = (int) ((millis - startMillis) / DateTimeConstants.MILLIS_PER_DAY) + 1;
        if (dayOfYear > 0 && dayOfYear < 187)
            return (dayOfYear - 1) / 31 + 1;
        else if (dayOfYear > 186 && dayOfYear < 367)
            return (dayOfYear - 187) / 30 + 7;
        else
            throw new RuntimeException("Invalid year");

    }

    @Override
    long getYearDifference(long minuendInstant, long subtrahendInstant) {
        int minuendYear = getYear(minuendInstant);
        int subtrahendYear = getYear(subtrahendInstant);

        // Inlined remainder method to avoid duplicate calls to get.
        long minuendRem = minuendInstant - getYearMillis(minuendYear);
        long subtrahendRem = subtrahendInstant - getYearMillis(subtrahendYear);

        // Balance leap year differences on subtrahend remainder.
        if (subtrahendRem >= ESF_30 && !isLeapYear(minuendYear))
        {
            subtrahendRem -= DateTimeConstants.MILLIS_PER_DAY;
        }

        int difference = minuendYear - subtrahendYear;
        if (minuendRem < subtrahendRem) {
            difference--;
        }
        return difference;
    }

    @Override
    boolean isLeapYear(int year) {
        return getLeapCount(year, year + 1) == 1;
    }

    @Override
    int getDaysInYearMonth(int year, int month) {
        if (month > 0 && month < 7)
            return 31;
        else if (month > 6 && month < 12)
            return 30;
        else if (month == 12)
            return isLeapYear(year)? 30 : 29;
        else
            throw new RuntimeException("invalid month value");
    }

    @Override
    int getDaysInMonthMax(int month) {
        if (month > 0 && month < 7)
            return 31;
        else if (month > 6 && month < 13)
            return 30;
        else
            throw new RuntimeException("invalid month value");
    }

    @Override
    long getTotalMillisByYearMonth(int year, int month) {
        if (month > 0 && month < 8)
            return (month - 1) * 31 * (long) DateTimeConstants.MILLIS_PER_DAY;
        else if (month > 7 && month < 13)
            return ((month - 1) * 31 - month + 7) * (long) DateTimeConstants.MILLIS_PER_DAY;
        else
            throw new RuntimeException("invalid month value");
    }

    @Override
    long calculateFirstDayOfYearMillis(int year) {
        long millis = 1426896000000L; //millis for 1394/1/1 00:00 UTC
        long days = 365 * (year - 1394) + getLeapCount(1394, year);
        return millis + days * DateTimeConstants.MILLIS_PER_DAY;
    }

    @Override
    int getMinYear() {
        return -292271021;
    }

    @Override
    int getMaxYear() {
        return 292271022;
    }

    @Override
    long getAverageMillisPerYear() {
        return 31556925000L;
    }

    @Override
    long getAverageMillisPerYearDividedByTwo() {
        return 15778463000L;
    }

    @Override
    long getAverageMillisPerMonth() {
        return 2629744000L;
    }

    @Override
    long getApproxMillisAtEpochDividedByTwo() {
        return 21281722650000L;
    }

    @Override
    long setYear(long instant, int year) {
        int thisYear = getYear(instant);
        int dayOfYear = getDayOfYear(instant, thisYear);
        int millisOfDay = getMillisOfDay(instant);

        // Current year is leap, and day is leap.
        if (dayOfYear > 365 && !isLeapYear(year)) {
            // Moving to a non-leap year, leap day doesn't exist.
            dayOfYear--;
        }

        instant = getYearMonthDayMillis(year, 1, dayOfYear);
        instant += millisOfDay;

        return instant;
    }

    @Override
    public Chronology withUTC() {
        return INSTANCE_UTC;
    }

    @Override
    public Chronology withZone(DateTimeZone zone) {
        if (zone == null) {
            zone = DateTimeZone.getDefault();
        }
        if (zone == getZone()) {
            return this;
        }
        return getInstance(zone);
    }

    /**
     * the period is half-open
     * @param fromYear from year is inclusive
     * @param toYear to year is exclusive
     * @return
     */
    public int getLeapCount(int fromYear, int toYear)
    {
        //Unit is seconds here
        int from = (fromYear - 1394) * 20925 + 8111;
        int to = (toYear - 1394) * 20925 + 8111;

        int signFrom = from < 12 * 60 * 60? -1 : 1;
        int signTo = to < 12 * 60 * 60? -1 : 1;

        int leapFrom = (from + signFrom * 12 * 60 * 60) / (24 * 60 * 60);
        int leapTo = (to + signTo * 12 * 60 * 60) / (24 * 60 * 60);

        return leapTo - leapFrom ;
    }
}
