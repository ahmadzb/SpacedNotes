package util.datetime.format;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateFormat;

/**
 * Created by Ahmad on 09/27/17.
 * All rights reserved.
 */

public class DateTimeFormat {


    /**
     * * Symbol  Meaning                      Presentation  Examples
     * ------  -------                      ------------  -------
     * G       era                          text          AD
     * C       century of era (&gt;=0)         number        20
     * Y       year of era (&gt;=0)            year          1996
     * <p>
     * x       weekyear                     year          1996
     * w       week of weekyear             number        27
     * e       day of week                  number        2
     * E       day of week                  text          Tuesday; Tue
     * <p>
     * y       year                         year          1996
     * D       day of year                  number        189
     * M       month of year                month         July; Jul; 07
     * d       day of month                 number        10
     * <p>
     * a       halfday of day               text          PM
     * K       hour of halfday (0~11)       number        0
     * h       clockhour of halfday (1~12)  number        12
     * <p>
     * H       hour of day (0~23)           number        0
     * k       clockhour of day (1~24)      number        24
     * m       minute of hour               number        30
     * s       second of minute             number        55
     * S       fraction of second           millis        978
     * <p>
     * z       org.joda.time zone                    text          Pacific Standard Time; PST
     * Z       org.joda.time zone offset/id          zone          -0800; -08:00; America/Los_Angeles
     * <p>
     * '       escape for text              delimiter
     * ''      single quote                 literal       '
     *
     * @param pattern
     * @return
     */
    public static DateTimeFormatter
    forPattern(String pattern, Resources resources) {
        return DateTimeFormatter.Builder.getInstance(pattern, resources).build();
    }

    public static DateTimeFormatter fullTime(Context context) {
        DateTimeFormatter.Builder builder =
                DateTimeFormatter.Builder.getInstance(context.getResources());
        if (DateFormat.is24HourFormat(context))
            builder.addHourOfDay(2).addText(":").addMinuteOfHour(2);
        else
            builder.addClockHourOfHalfDay(2).addText(":").addMinuteOfHour(2).addText(" ").addHalfDayOfDay();
        return builder.build();
    }

    public static DateTimeFormatter mediumDateTime(Context context) {

        DateTimeFormatter.Builder builder =
                DateTimeFormatter.Builder.getInstance(context.getResources());

        builder.addYear(4).addText("/").addMonthOfYear(2).addText("/").addDayOfMonth(2).addText(" ");

        if (DateFormat.is24HourFormat(context))
            builder.addHourOfDay(2).addText(":").addMinuteOfHour(2);
        else
            builder.addClockHourOfHalfDay(2).addText(":").addMinuteOfHour(2).addText(" ").addHalfDayOfDay();
        return builder.build();
    }

    public static DateTimeFormatter mediumDate(Resources resources) {
        DateTimeFormatter.Builder builder =
                DateTimeFormatter.Builder.getInstance(resources);

        builder.addYear(4).addText("/").addMonthOfYear(2).addText("/").addDayOfMonth(2);
        return builder.build();
    }

    public static DateTimeFormatter fullDateTime(Context context) {
        DateTimeFormatter.Builder builder =
                DateTimeFormatter.Builder.getInstance(context.getResources());

        builder.addDayOfWeekFull().addText(" ").addDayOfMonth(2).addText(" ")
                .addMonthOfYearFull().addText(" ").addYear(4).addText(" ");
        if (DateFormat.is24HourFormat(context))
            builder.addHourOfDay(2).addText(":").addMinuteOfHour(2);
        else
            builder.addClockHourOfHalfDay(2).addText(":").addMinuteOfHour(2).addText(" ").addHalfDayOfDay();
        return builder.build();
    }

    public static DateTimeFormatter fullDate(Resources resources) {
        DateTimeFormatter.Builder builder =
                DateTimeFormatter.Builder.getInstance(resources);

        builder.addDayOfWeekFull().addText(" ").addDayOfMonth(2).addText(" ")
                .addMonthOfYearFull().addText(" ").addYear(4);
        return builder.build();
    }

    public static DateTimeFormatter longDateTime(Context context) {
        DateTimeFormatter.Builder builder =
                DateTimeFormatter.Builder.getInstance(context.getResources());

        builder.addDayOfMonth(2).addText(" ")
                .addMonthOfYearFull().addText(" ").addYear(4).addText(" ");
        if (DateFormat.is24HourFormat(context))
            builder.addHourOfDay(2).addText(":").addMinuteOfHour(2);
        else
            builder.addClockHourOfHalfDay(2).addText(":").addMinuteOfHour(2).addText(" ").addHalfDayOfDay();
        return builder.build();
    }

    public static DateTimeFormatter longDate(Resources resources) {
        DateTimeFormatter.Builder builder =
                DateTimeFormatter.Builder.getInstance(resources);

        builder.addDayOfMonth(2).addText(" ")
                .addMonthOfYearFull().addText(" ").addYear(4);
        return builder.build();
    }

    public static DateTimeFormatter dayMonth(Resources resources) {
        DateTimeFormatter.Builder builder =
                DateTimeFormatter.Builder.getInstance(resources);

        builder.addDayOfMonth(2).addText(" ")
                .addMonthOfYearFull();
        return builder.build();
    }

    public static DateTimeFormatter localTime(Context context) {
        DateTimeFormatter.Builder builder =
                DateTimeFormatter.Builder.getInstance(context.getResources());
        if (DateFormat.is24HourFormat(context))
            builder.addHourOfDay(2).addText(":").addMinuteOfHour(2);
        else
            builder.addClockHourOfHalfDay(2).addText(":").addMinuteOfHour(2).addText(" ").addHalfDayOfDay();
        return builder.build();
    }
}
