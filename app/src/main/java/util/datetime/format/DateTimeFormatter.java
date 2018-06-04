package util.datetime.format;

import android.content.res.Resources;

import com.diplinkblaze.spacednote.R;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.chrono.PersianChronology;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;

/**
 * Created by Ahmad on 09/27/17.
 * All rights reserved.
 */

public class DateTimeFormatter {

    private static TextFields textFieldsPersian;
    private static TextFields textFieldsGregorian;

    private ArrayList<Mapper> mappers;

    private DateTimeFormatter(Resources resources) {
        if (DateTimeFormatter.textFieldsPersian == null)
            DateTimeFormatter.textFieldsPersian = TextFields.getInstancePersian(resources);
        if (DateTimeFormatter.textFieldsGregorian == null)
            DateTimeFormatter.textFieldsGregorian = TextFields.getInstanceGregorian(resources);
    }

    private static TextFields getTextField(Chronology chronology) {
        if (chronology instanceof PersianChronology)
            return textFieldsPersian;
        else
            return textFieldsGregorian;
    }

    public String print(long instant) {
        DateTime dateTime = new DateTime(instant, PersianChronology.getInstance());
        StringBuilder builder = new StringBuilder();
        for (Mapper mapper : mappers)
            mapper.map(builder, dateTime);
        return builder.toString();
    }

    public String print(DateTime dateTime) {
        if (dateTime == null)
            throw new RuntimeException("dateTime is null");

        StringBuilder builder = new StringBuilder();
        for (Mapper mapper : mappers)
            mapper.map(builder, dateTime);
        return builder.toString();
    }

    public String print(LocalDate localDate) {
        if (localDate == null)
            throw new RuntimeException("localDate is null");

        StringBuilder builder = new StringBuilder();
        for (Mapper mapper : mappers)
            mapper.map(builder, localDate);
        return builder.toString();
    }

    public String print(LocalTime localTime) {
        if (localTime == null)
            throw new RuntimeException("LocalTime is null");

        StringBuilder builder = new StringBuilder();
        for (Mapper mapper : mappers)
            mapper.map(builder, localTime);
        return builder.toString();
    }

    private static abstract class Mapper {
        public abstract void map(StringBuilder builder, DateTime dateTime);

        public abstract void map(StringBuilder builder, LocalDate localDate);

        public abstract void map(StringBuilder builder, LocalTime localTime);
    }

    public static class Builder {
        private DateTimeFormatter formatter;

        private Builder(Resources resources) {
            this(-1, resources);
        }

        private Builder(int initialMapperSize, Resources resources) {

            this.formatter = new DateTimeFormatter(resources);
            if (initialMapperSize > 0)
                formatter.mappers = new ArrayList<Mapper>(initialMapperSize);
            else
                formatter.mappers = new ArrayList<Mapper>();
        }

        public static Builder getInstance(Resources resources) {
            Builder builder = new Builder(resources);

            return builder;
        }

        public static Builder getInstance(String pattern, Resources resources) {
            Builder builder = new Builder(resources);

            char[] chars = pattern.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == '\'' && i != chars.length - 1) {
                    i++;
                    builder.addText(chars[i]);
                } else if (chars[i] == 'G') {
                    while (i != chars.length - 1 && chars[i + 1] == 'G')
                        i++;
                    builder.addEra();
                } else if (chars[i] == 'C') {
                    while (i != chars.length - 1 && chars[i + 1] == 'C')
                        i++;
                    builder.addCenturyOfEra();
                } else if (chars[i] == 'Y') {
                    while (i != chars.length - 1 && chars[i + 1] == 'Y')
                        i++;
                    builder.addYearOfEra();
                } else if (chars[i] == 'x') {
                    while (i != chars.length - 1 && chars[i + 1] == 'x')
                        i++;
                    builder.addWeekYear();
                } else if (chars[i] == 'w') {
                    while (i != chars.length - 1 && chars[i + 1] == 'w')
                        i++;
                    builder.addWeekOfWeekYear();
                } else if (chars[i] == 'e') {
                    while (i != chars.length - 1 && chars[i + 1] == 'e')
                        i++;
                    builder.addDayOfWeekNumber();
                } else if (chars[i] == 'E') {
                    int digits = 1;
                    while (i != chars.length - 1 && chars[i + 1] == 'E') {
                        i++;
                        digits++;
                    }
                    if (digits > 3)
                        builder.addDayOfWeekFull();
                    else
                        builder.addDayOfWeekShort();
                } else if (chars[i] == 'y') {
                    int digits = 1;
                    while (i != chars.length - 1 && chars[i + 1] == 'y') {
                        i++;
                        digits++;
                    }
                    builder.addYear(digits);
                } else if (chars[i] == 'D') {
                    int digits = 1;
                    while (i != chars.length - 1 && chars[i + 1] == 'D') {
                        i++;
                        digits++;
                    }
                    builder.addDayOfYear(digits);
                } else if (chars[i] == 'M') {
                    int digits = 1;
                    while (i != chars.length - 1 && chars[i + 1] == 'M') {
                        i++;
                        digits++;
                    }
                    if (digits > 2)
                        builder.addMonthOfYearFull();
                    else
                        builder.addMonthOfYear(digits);
                } else if (chars[i] == 'd') {
                    int digits = 1;
                    while (i != chars.length - 1 && chars[i + 1] == 'd') {
                        i++;
                        digits++;
                    }
                    builder.addDayOfMonth(digits);
                } else if (chars[i] == 'a') {
                    while (i != chars.length - 1 && chars[i + 1] == 'a')
                        i++;
                    builder.addHalfDayOfDay();
                } else if (chars[i] == 'K') {
                    int digits = 1;
                    while (i != chars.length - 1 && chars[i + 1] == 'K') {
                        i++;
                        digits++;
                    }
                    builder.addHourOfHalfDay(digits);
                } else if (chars[i] == 'h') {
                    int digits = 1;
                    while (i != chars.length - 1 && chars[i + 1] == 'h') {
                        i++;
                        digits++;
                    }
                    builder.addClockHourOfHalfDay(digits);
                } else if (chars[i] == 'H') {
                    int digits = 1;
                    while (i != chars.length - 1 && chars[i + 1] == 'H') {
                        i++;
                        digits++;
                    }
                    builder.addHourOfDay(digits);
                } else if (chars[i] == 'k') {
                    int digits = 1;
                    while (i != chars.length - 1 && chars[i + 1] == 'k') {
                        i++;
                        digits++;
                    }
                    builder.addClockHourOfDay(digits);
                } else if (chars[i] == 'm') {
                    int digits = 1;
                    while (i != chars.length - 1 && chars[i + 1] == 'm') {
                        i++;
                        digits++;
                    }
                    builder.addMinuteOfHour(digits);
                } else if (chars[i] == 's') {
                    int digits = 1;
                    while (i != chars.length - 1 && chars[i + 1] == 's') {
                        i++;
                        digits++;
                    }
                    builder.addSecondOfMinute(digits);
                } else if (chars[i] == 'S') {
                    int digits = 1;
                    while (i != chars.length - 1 && chars[i + 1] == 'S') {
                        i++;
                        digits++;
                    }
                    builder.addFractionOfSeconds(digits);
                } else if (chars[i] == 'z') {
                    while (i != chars.length - 1 && chars[i + 1] == 'z')
                        i++;
                    builder.addTimeZone();
                } else if (chars[i] == 'Z') {
                    while (i != chars.length - 1 && chars[i + 1] == 'Z')
                        i++;
                    builder.addTimeZoneOffsetId();
                } else {
                    builder.addText(chars[i]);
                }

            }

            return builder;
        }

        public Builder addText(char text) {
            int size = formatter.mappers.size();
            Mapper lastMapper;
            if (size != 0 &&
                    (lastMapper = formatter.mappers.get(size - 1)) instanceof TextMapper)
                ((TextMapper) lastMapper).append(text);
            else
                formatter.mappers.add(new TextMapper(text));
            return this;
        }

        public Builder addText(String text) {
            int size = formatter.mappers.size();
            Mapper lastMapper;
            if (size != 0 &&
                    (lastMapper = formatter.mappers.get(size - 1)) instanceof TextMapper)
                ((TextMapper) lastMapper).append(text);
            else
                formatter.mappers.add(new TextMapper(text));
            return this;
        }

        public Builder addEra() {
            formatter.mappers.add(new EraMapper());
            return this;
        }

        public Builder addCenturyOfEra() {
            formatter.mappers.add(new CenturyOfEraMapper());
            return this;
        }

        public Builder addYearOfEra() {
            formatter.mappers.add(new YearOfEraMapper());
            return this;
        }

        public Builder addWeekYear() {
            formatter.mappers.add(new WeekYearMapper());
            return this;
        }

        public Builder addWeekOfWeekYear() {
            formatter.mappers.add(new WeekOfWeekYearMapper());
            return this;
        }

        public Builder addDayOfWeekFull() {
            formatter.mappers.add(new DayOfWeekMapper(DayOfWeekMapper.MODE_FULL));
            return this;
        }

        public Builder addDayOfWeekShort() {
            formatter.mappers.add(new DayOfWeekMapper(DayOfWeekMapper.MODE_SHORT));
            return this;
        }

        public Builder addDayOfWeekNumber() {
            formatter.mappers.add(new DayOfWeekMapper(DayOfWeekMapper.MODE_NUMBER));
            return this;
        }

        public Builder addYear(int digits) {
            formatter.mappers.add(new YearMapper(digits));
            return this;
        }

        public Builder addDayOfYear(int minDigits) {
            formatter.mappers.add(new DayOfYearMapper(minDigits));
            return this;
        }

        public Builder addMonthOfYearFull() {
            formatter.mappers.add(new MonthOfYearMapper(MonthOfYearMapper.MODE_FULL));
            return this;
        }

        public Builder addMonthOfYear(int minDigits) {
            formatter.mappers.add(new MonthOfYearMapper(MonthOfYearMapper.MODE_NUMBER, minDigits));
            return this;
        }

        public Builder addDayOfMonth(int minDigits) {
            formatter.mappers.add(new DayOfMonthMapper(minDigits));
            return this;
        }

        public Builder addHalfDayOfDay() {
            formatter.mappers.add(new HalfDayOfDayMapper());
            return this;
        }

        public Builder addHourOfHalfDay(int minDigits) {
            formatter.mappers.add(new HourOfHalfDayMapper(minDigits));
            return this;
        }

        public Builder addClockHourOfHalfDay(int minDigits) {
            formatter.mappers.add(new ClockHourOfHalfDayMapper(minDigits));
            return this;
        }

        public Builder addHourOfDay(int minDigits) {
            formatter.mappers.add(new HourOfDayMapper(minDigits));
            return this;
        }

        public Builder addClockHourOfDay(int minDigits) {
            formatter.mappers.add(new ClockHourOfDayMapper(minDigits));
            return this;
        }

        public Builder addMinuteOfHour(int minDigits) {
            formatter.mappers.add(new MinuteOfHourMapper(minDigits));
            return this;
        }

        public Builder addSecondOfMinute(int minDigits) {
            formatter.mappers.add(new SecondOfMinuteMapper(minDigits));
            return this;
        }

        public Builder addFractionOfSeconds(int minDigits) {
            formatter.mappers.add(new FractionOfSecondsMapper(minDigits));
            return this;
        }

        public Builder addTimeZone() {
            formatter.mappers.add(new TimeZoneMapper());
            return this;
        }

        public Builder addTimeZoneOffsetId() {
            formatter.mappers.add(new TimeZoneOffsetIdMapper());
            return this;
        }

        public DateTimeFormatter build() {
            return formatter;
        }

        private static class TextMapper extends Mapper {
            public StringBuilder text = new StringBuilder();

            public TextMapper(char text) {
                this.text.append(text);
            }

            public TextMapper(String text) {
                this.text.append(text);
            }

            public void append(String textToAppend) {
                this.text.append(textToAppend);
            }

            public void append(char textToAppend) {
                this.text.append(textToAppend);
            }

            @Override
            public void map(StringBuilder builder, DateTime dateTime) {
                builder.append(this.text);
            }

            @Override
            public void map(StringBuilder builder, LocalDate localDate) {
                builder.append(this.text);
            }

            @Override
            public void map(StringBuilder builder, LocalTime localTime) {
                builder.append(this.text);
            }
        }

        private static class EraMapper extends Mapper {
            @Override
            public void map(StringBuilder builder, DateTime dateTime) {
                builder.append(dateTime.getEra());
            }

            @Override
            public void map(StringBuilder builder, LocalDate localDate) {
                builder.append(localDate.getEra());
            }

            @Override
            public void map(StringBuilder builder, LocalTime localTime) {
                throw new RuntimeException("Local org.joda.time does not have this field");
            }
        }

        private static class CenturyOfEraMapper extends Mapper {
            @Override
            public void map(StringBuilder builder, DateTime dateTime) {
                builder.append(dateTime.getCenturyOfEra());
            }

            @Override
            public void map(StringBuilder builder, LocalDate localDate) {
                builder.append(localDate.getCenturyOfEra());
            }

            @Override
            public void map(StringBuilder builder, LocalTime localTime) {
                throw new RuntimeException("Local org.joda.time does not have this field");
            }
        }

        private static class YearOfEraMapper extends Mapper {
            @Override
            public void map(StringBuilder builder, DateTime dateTime) {
                builder.append(dateTime.getYearOfEra());
            }

            @Override
            public void map(StringBuilder builder, LocalDate localDate) {
                builder.append(localDate.getYearOfEra());
            }

            @Override
            public void map(StringBuilder builder, LocalTime localTime) {
                throw new RuntimeException("Local org.joda.time does not have this field");
            }
        }

        private static class WeekYearMapper extends Mapper {
            @Override
            public void map(StringBuilder builder, DateTime dateTime) {
                builder.append(dateTime.getWeekyear());
            }

            @Override
            public void map(StringBuilder builder, LocalDate localDate) {
                builder.append(localDate.getWeekyear());
            }

            @Override
            public void map(StringBuilder builder, LocalTime localTime) {
                throw new RuntimeException("Local org.joda.time does not have this field");
            }
        }

        private static class WeekOfWeekYearMapper extends Mapper {
            @Override
            public void map(StringBuilder builder, DateTime dateTime) {
                builder.append(dateTime.getWeekOfWeekyear());
            }

            @Override
            public void map(StringBuilder builder, LocalDate localDate) {
                builder.append(localDate.getWeekOfWeekyear());
            }

            @Override
            public void map(StringBuilder builder, LocalTime localTime) {
                throw new RuntimeException("Local org.joda.time does not have this field");
            }
        }

        private static class DayOfWeekMapper extends Mapper {
            public static final int MODE_FULL = 1;
            public static final int MODE_SHORT = 2;
            public static final int MODE_NUMBER = 3;

            private int mode;

            public DayOfWeekMapper(int mode) {
                this.mode = mode;
            }

            @Override
            public void map(StringBuilder builder, DateTime dateTime) {
                if (mode == MODE_FULL)
                    builder.append(
                            getTextField(dateTime.getChronology()).weekDaysFull[dateTime.getDayOfWeek() - 1]);
                else if (mode == MODE_SHORT)
                    builder.append(
                            getTextField(dateTime.getChronology()).weekDaysShort[dateTime.getDayOfWeek() - 1]);
                else if (mode == MODE_NUMBER)
                    builder.append(dateTime.getDayOfWeek());
            }

            @Override
            public void map(StringBuilder builder, LocalDate localDate) {
                if (mode == MODE_FULL)
                    builder.append(
                            getTextField(localDate.getChronology()).weekDaysFull[localDate.getDayOfWeek() - 1]);
                else if (mode == MODE_SHORT)
                    builder.append(
                            getTextField(localDate.getChronology()).weekDaysShort[localDate.getDayOfWeek() - 1]);
                else if (mode == MODE_NUMBER)
                    builder.append(localDate.getDayOfWeek());
            }

            @Override
            public void map(StringBuilder builder, LocalTime localTime) {
                throw new RuntimeException("Local org.joda.time does not have this field");
            }
        }

        private static class YearMapper extends Mapper {
            private String digitsFormat;
            private int tenPower;

            public YearMapper(int digits) {
                this.digitsFormat = "%0" + digits + "d";
                this.tenPower = (int) Math.pow(10, digits);
            }

            @Override
            public void map(StringBuilder builder, DateTime dateTime) {
                builder.append(String.format(digitsFormat, dateTime.getYear() % tenPower));
            }

            @Override
            public void map(StringBuilder builder, LocalDate localDate) {
                builder.append(String.format(digitsFormat, localDate.getYear() % tenPower));
            }

            @Override
            public void map(StringBuilder builder, LocalTime localTime) {
                throw new RuntimeException("Local org.joda.time does not have this field");
            }
        }

        private static class DayOfYearMapper extends Mapper {
            private String digitsFormat;

            public DayOfYearMapper(int digits) {
                this.digitsFormat = "%0" + digits + "d";
            }

            @Override
            public void map(StringBuilder builder, DateTime dateTime) {
                builder.append(String.format(digitsFormat, dateTime.getDayOfYear()));
            }

            @Override
            public void map(StringBuilder builder, LocalDate localDate) {
                builder.append(String.format(digitsFormat, localDate.getDayOfYear()));
            }

            @Override
            public void map(StringBuilder builder, LocalTime localTime) {
                throw new RuntimeException("Local org.joda.time does not have this field");
            }
        }

        private static class MonthOfYearMapper extends Mapper {
            public static final int MODE_FULL = 1;
            public static final int MODE_NUMBER = 3;

            private int mode;
            private String digitsFormat;


            public MonthOfYearMapper(int mode) {
                this.mode = mode;
                if (mode == MODE_NUMBER) {
                    digitsFormat = "%02d";
                }
            }

            public MonthOfYearMapper(int mode, int digits) {
                this.mode = mode;
                this.digitsFormat = "%0" + digits + "d";
            }

            @Override
            public void map(StringBuilder builder, DateTime dateTime) {
                if (this.mode == MODE_FULL)
                    builder.append(getTextField(dateTime.getChronology()).monthsFull[dateTime.getMonthOfYear() - 1]);
                else if (this.mode == MODE_NUMBER)
                    builder.append(String.format(digitsFormat, dateTime.getMonthOfYear()));
            }

            @Override
            public void map(StringBuilder builder, LocalDate localDate) {
                if (this.mode == MODE_FULL)
                    builder.append(getTextField(localDate.getChronology()).monthsFull[localDate.getMonthOfYear() - 1]);
                else if (this.mode == MODE_NUMBER)
                    builder.append(String.format(digitsFormat, localDate.getMonthOfYear()));
            }

            @Override
            public void map(StringBuilder builder, LocalTime localTime) {
                throw new RuntimeException("Local org.joda.time does not have this field");
            }
        }

        private static class DayOfMonthMapper extends Mapper {
            private String digitsFormat;

            public DayOfMonthMapper(int digits) {
                this.digitsFormat = "%0" + digits + "d";
            }

            @Override
            public void map(StringBuilder builder, DateTime dateTime) {
                builder.append(String.format(digitsFormat, dateTime.getDayOfMonth()));
            }

            @Override
            public void map(StringBuilder builder, LocalDate localDate) {
                builder.append(String.format(digitsFormat, localDate.getDayOfMonth()));
            }

            @Override
            public void map(StringBuilder builder, LocalTime localTime) {
                throw new RuntimeException("Local org.joda.time does not have this field");
            }
        }

        private static class HalfDayOfDayMapper extends Mapper {
            @Override
            public void map(StringBuilder builder, DateTime dateTime) {
                builder.append(dateTime.getHourOfDay() >= 12 ?
                        getTextField(dateTime.getChronology()).pm : getTextField(dateTime.getChronology()).am);
            }

            @Override
            public void map(StringBuilder builder, LocalDate localDate) {
                throw new RuntimeException("Local date does not have this field");
            }

            @Override
            public void map(StringBuilder builder, LocalTime localTime) {
                builder.append(localTime.getHourOfDay() >= 12 ?
                        getTextField(localTime.getChronology()).pm : getTextField(localTime.getChronology()).am);
            }
        }

        private static class HourOfHalfDayMapper extends Mapper {
            private String digitsFormat;

            public HourOfHalfDayMapper(int digits) {
                this.digitsFormat = "%0" + digits + "d";
            }

            @Override
            public void map(StringBuilder builder, DateTime dateTime) {
                builder.append(String.format(digitsFormat, dateTime.getHourOfDay() % 12));
            }

            @Override
            public void map(StringBuilder builder, LocalDate localDate) {
                throw new RuntimeException("Not Supported");
            }

            @Override
            public void map(StringBuilder builder, LocalTime localTime) {
                builder.append(String.format(digitsFormat, localTime.getHourOfDay() % 12));
            }
        }

        private static class ClockHourOfHalfDayMapper extends Mapper {
            private String digitsFormat;

            public ClockHourOfHalfDayMapper(int digits) {
                this.digitsFormat = "%0" + digits + "d";
            }

            @Override
            public void map(StringBuilder builder, DateTime dateTime) {
                builder.append(String.format(digitsFormat, ((dateTime.getHourOfDay() + 11) % 12) + 1));
            }

            @Override
            public void map(StringBuilder builder, LocalDate localDate) {
                throw new RuntimeException("Not Supported");
            }

            @Override
            public void map(StringBuilder builder, LocalTime localTime) {
                builder.append(String.format(
                        digitsFormat, ((localTime.getHourOfDay() + 11) % 12) + 1));
            }
        }

        private static class HourOfDayMapper extends Mapper {
            private String digitsFormat;

            public HourOfDayMapper(int digits) {
                this.digitsFormat = "%0" + digits + "d";
            }

            @Override
            public void map(StringBuilder builder, DateTime dateTime) {
                builder.append(String.format(
                        digitsFormat, dateTime.getHourOfDay()));
            }

            @Override
            public void map(StringBuilder builder, LocalDate localDate) {
                throw new RuntimeException("Not Supported");
            }

            @Override
            public void map(StringBuilder builder, LocalTime localTime) {
                builder.append(String.format(
                        digitsFormat, localTime.getHourOfDay()));
            }
        }

        private static class ClockHourOfDayMapper extends Mapper {
            private String digitsFormat;

            public ClockHourOfDayMapper(int digits) {
                this.digitsFormat = "%0" + digits + "d";
            }

            @Override
            public void map(StringBuilder builder, DateTime dateTime) {
                builder.append(String.format(
                        digitsFormat, ((dateTime.getHourOfDay() + 23) % 24) + 1));
            }

            @Override
            public void map(StringBuilder builder, LocalDate localDate) {
                throw new RuntimeException("Not Supported");
            }

            @Override
            public void map(StringBuilder builder, LocalTime localTime) {
                builder.append(String.format(
                        digitsFormat, ((localTime.getHourOfDay() + 23) % 24) + 1));
            }
        }

        private static class MinuteOfHourMapper extends Mapper {
            private String digitsFormat;

            public MinuteOfHourMapper(int digits) {
                this.digitsFormat = "%0" + digits + "d";
            }

            @Override
            public void map(StringBuilder builder, DateTime dateTime) {
                builder.append(String.format(
                        digitsFormat, dateTime.getMinuteOfHour()));
            }

            @Override
            public void map(StringBuilder builder, LocalDate localDate) {
                throw new RuntimeException("Not Supported");
            }

            @Override
            public void map(StringBuilder builder, LocalTime localTime) {
                builder.append(String.format(
                        digitsFormat, localTime.getMinuteOfHour()));
            }
        }

        private static class SecondOfMinuteMapper extends Mapper {
            private String digitsFormat;

            public SecondOfMinuteMapper(int digits) {
                this.digitsFormat = "%0" + digits + "d";
            }

            @Override
            public void map(StringBuilder builder, DateTime dateTime) {
                builder.append(String.format(
                        digitsFormat, dateTime.getSecondOfMinute()));
            }

            @Override
            public void map(StringBuilder builder, LocalDate localDate) {
                throw new RuntimeException("Not Supported");
            }

            @Override
            public void map(StringBuilder builder, LocalTime localTime) {
                builder.append(String.format(
                        digitsFormat, localTime.getSecondOfMinute()));
            }
        }

        private static class FractionOfSecondsMapper extends Mapper {
            private String digitsFormat;

            public FractionOfSecondsMapper(int digits) {
                this.digitsFormat = "%0" + digits + "d";
            }

            @Override
            public void map(StringBuilder builder, DateTime dateTime) {
                builder.append(String.format(
                        digitsFormat, dateTime.getMillisOfSecond()));
            }

            @Override
            public void map(StringBuilder builder, LocalDate localDate) {
                throw new RuntimeException("Not Supported");
            }

            @Override
            public void map(StringBuilder builder, LocalTime localTime) {
                builder.append(String.format(
                        digitsFormat, localTime.getMillisOfSecond()));
            }
        }

        private static class TimeZoneMapper extends Mapper {


            @Override
            public void map(StringBuilder builder, DateTime dateTime) {
                builder.append(dateTime.toString(DateTimeFormat.forPattern("z")));
            }

            @Override
            public void map(StringBuilder builder, LocalDate localDate) {
                throw new RuntimeException("Not Supported");
            }

            @Override
            public void map(StringBuilder builder, LocalTime localTime) {
                builder.append(localTime.toString(DateTimeFormat.forPattern("z")));
            }
        }

        private static class TimeZoneOffsetIdMapper extends Mapper {


            @Override
            public void map(StringBuilder builder, DateTime dateTime) {
                builder.append(dateTime.toString(DateTimeFormat.forPattern("Z")));
            }

            @Override
            public void map(StringBuilder builder, LocalDate localDate) {
                throw new RuntimeException("Not Supported");
            }

            @Override
            public void map(StringBuilder builder, LocalTime localTime) {
                builder.append(localTime.toString(DateTimeFormat.forPattern("Z")));
            }
        }

    }

    private static class TextFields {
        private String[] weekDaysShort;
        private String[] weekDaysFull;
        private String[] monthsFull;
        private String am;
        private String pm;

        private TextFields() {

        }

        public static TextFields getInstancePersian(Resources resources) {
            TextFields textFields = new TextFields();
            textFields.weekDaysShort = resources.getStringArray(R.array.week_short);
            textFields.weekDaysFull = resources.getStringArray(R.array.week_full);
            textFields.monthsFull = resources.getStringArray(R.array.month_full_persian);
            textFields.am = resources.getString(R.string.half_day_before_noon);
            textFields.pm = resources.getString(R.string.half_day_after_noon);
            return textFields;
        }

        public static TextFields getInstanceGregorian(Resources resources) {
            TextFields textFields = new TextFields();
            textFields.weekDaysShort = resources.getStringArray(R.array.week_short);
            textFields.weekDaysFull = resources.getStringArray(R.array.week_full);
            textFields.monthsFull = resources.getStringArray(R.array.month_full_gregorian);
            textFields.am = resources.getString(R.string.half_day_before_noon);
            textFields.pm = resources.getString(R.string.half_day_after_noon);
            return textFields;
        }

        public String[] getWeekDaysShort() {
            return weekDaysShort;
        }

        public String[] getWeekDaysFull() {
            return weekDaysFull;
        }

        public String[] getMonthsFull() {
            return monthsFull;
        }
    }
}
