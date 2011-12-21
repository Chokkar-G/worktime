/*
 *  Copyright 2011 Dirk Vranckaert
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.vranckaert.worktime.utils.date;

import android.content.Context;
import android.util.Log;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.enums.reporting.ReportingDisplayDuration;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.utils.context.ContextUtils;
import eu.vranckaert.worktime.utils.preferences.Preferences;
import eu.vranckaert.worktime.utils.preferences.TimePrecisionPreference;
import eu.vranckaert.worktime.utils.string.StringUtils;
import org.joda.time.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Date utils.
 * @author Dirk Vranckaert
 */
public class DateUtils {
    private static final String LOG_TAG = DateUtils.class.getSimpleName();

    /**
     * Contains all methods that will convert date and/or time to string.
     */
    public static class DateTimeConverter {
        /**
         * Converts a date to an entire date-time-string based on the users locale in the context.
         * @param date The date to convert.
         * @param dateFormat The date format to use.
         * @param timeFormat The time format to use.
         * @param context The context in which the locale is stored.
         * @return The formatted string.
         */
        public static final String convertDateTimeToString(Date date, DateFormat dateFormat, TimeFormat timeFormat, Context context) {
            Locale locale = ContextUtils.getCurrentLocale(context);
            return convertDateToString(date, dateFormat, locale)
                    + " "
                    + convertTimeToString(date, timeFormat, context);
        }
        
        /**
         * Converts a certain date to a date-string based on the users locale in the context.
         * @param date The date to convert.
         * @param format The date format to use.
         * @param context The context in which the locale is stored.
         * @return The formatted string.
         */
        public static final String convertDateToString(Date date, DateFormat format, Context context) {
            Locale locale = ContextUtils.getCurrentLocale(context);
            return convertDateToString(date, format, locale);
        }

        /**
         * Converts a certain date to a date-string based on the users locale.
         * @param date The date to convert.
         * @param format The date format to use.
         * @param locale The users locale.
         * @return The formatted string.
         */
        public static final String convertDateToString(Date date, DateFormat format, Locale locale) {
            java.text.DateFormat dateFormat = java.text.DateFormat.getDateInstance(format.getStyle(), locale);
            return dateFormat.format(date);
        }

        /**
         * Converts a certain date to a time-string based on the users locale in the context.
         * @param date The date to convert.
         * @param format The date format to use.
         * @return The formatted string.
         */
        public static final String convertTimeToString(Date date, TimeFormat format, Context context) {
            HourPreference12Or24 preference = Preferences.getDisplayHour1224Format(context);
            return convertTimeToString(context, date, format, preference);
        }

        /**
         * Converts a certain date to a time-string based on the users locale.
         * @param ctx The context.
         * @param date The date to convert.
         * @param format The date format to use.
         * @param preference The hour preference, 12-hour or 24-hours based.
         * @return The formatted string.
         */
        public static final String convertTimeToString(Context ctx, Date date, TimeFormat format, HourPreference12Or24 preference) {
            //TODO TimePrecision!
            String separator = ":";
            String seconds = "ss";
            String minutes = "mm";
            String hours24 = "HH";
            String hours12 = "hh";
            String amPmMarker = " a";

            String dateFormat = null;

            switch(format) {
                case MEDIUM: {
                    TimePrecisionPreference timePrecisionPreference = Preferences.getTimePrecision(ctx);
                    switch (timePrecisionPreference) {
                        case SECOND: {
                            dateFormat = separator + minutes + separator + seconds;
                            break;
                        }
                        case MINUTE: {
                            dateFormat = separator + minutes;
                            break;
                        }
                    }
                    break;
                }
                case SHORT: {
                    dateFormat = separator + minutes;
                    break;
                }
            }

            switch(preference) {
                case HOURS_12: {
                    dateFormat = hours12 + dateFormat + amPmMarker;
                    break;
                }
                case HOURS_24: {
                    dateFormat = hours24 + dateFormat;
                    break;
                }
            }

            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            return sdf.format(date);
        }

        /**
         * Convert a date to a string including the year, month and day.
         * @param date The {@link java.util.Date} to convert.
         * @return The converted date in year, month and day.
         */
        private static String getYearMonthDayAsString(Date date) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            String yearMonthDay = "";
            int year = cal.get(Calendar.YEAR);
            int month = (cal.get(Calendar.MONTH) + 1);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            yearMonthDay += year;
            if (month < 10) {
                yearMonthDay += "0";
            }
            yearMonthDay += month;
            if (day < 10) {
                yearMonthDay += "0";
            }
            yearMonthDay += day;

            Log.d(LOG_TAG, "Current time in yearMonthDay format is " + yearMonthDay);

            return yearMonthDay;
        }

        /**
         * Convert a date to a string including the hour, minute and seconds.
         * @param date The {@link java.util.Date} to convert.
         * @return The converted date in hours, minutes and seconds.
         */
        private static String getHourMinuteSecondAsString(Date date) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            String hourMinuteSeconds = "";
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            int second = cal.get(Calendar.SECOND);

            if (hour < 10) {
                hourMinuteSeconds += "0";
            }
            hourMinuteSeconds += hour;
            if (minute < 10) {
                hourMinuteSeconds += "0";
            }
            hourMinuteSeconds += minute;
            if (second < 10) {
                hourMinuteSeconds += "0";
            }
            hourMinuteSeconds += second;

            return hourMinuteSeconds;
        }

        /**
         * Builds a string with a date time representation that can be used to make something unique based on the current
         * time.
         * @return A string with the date and time representation like 20110912231845-PM. The format is:<br/>
         *         YYYYMMDDHHMMSS-AMPM<br/>
         *         YYYY = four number representing the year
         *         MM = two numbers representing the month (1-based)
         *         DD = two numbers representing the day of the month (1-based)
         *         HH = two numbers representing the hour
         *         MM = two numbers representing the minute
         *         SS = two numbers representing the seconds
         *         AMPM = 1 number represting AM or PM (AM = 0, PM = 1)
         */
        public static String getUniqueTimestampString() {
            Date date = new Date();

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            return getYearMonthDayAsString(date)
                    + getHourMinuteSecondAsString(date)
                    + "-" + cal.get(Calendar.AM_PM);
        }
    }

    /**
     * Contains all methods that calculates with time.
     */
    public static class TimeCalculator {
        /**
         * Calculates the time ({@link org.joda.time.Interval}) between two dates. If the startDate is not before the endDate the dates
         * will be swapped.
         * @param ctx The context.
         * @param startDate The start date for the interval.
         * @param endDate The ending date for the interval.
         * @return The {@link org.joda.time.Interval} between the two dates.
         */
        public static final Interval calculateInterval(Context ctx, Date startDate, Date endDate) {
            Calendar start = TimePrecision.applyTimePrecisionCalendar(ctx, startDate);

            Calendar end = TimePrecision.applyTimePrecisionCalendar(ctx, endDate);

            if(end.before(start)) {
                Calendar swap = start;
                start = end;
                end = swap;
            }

            Interval interval = new Interval(start.getTime().getTime(), end.getTime().getTime());

            return interval;
        }

        /**
         * Calculates the time ({@link org.joda.time.Duration}) between two dates. If the startDate is not before the endDate the dates
         * will be swapped.
         * @param ctx The context.
         * @param startDate The start date for the interval.
         * @param endDate The ending date for the interval.
         * @return The {@link org.joda.time.Duration} between the two dates.
         */
        public static final Duration calculateDuration(Context ctx, Date startDate, Date endDate) {
            Interval interval = calculateInterval(ctx, startDate, endDate);
            Duration duration = interval.toDuration();
            return duration;
        }

        /**
         * Calculates the time ({@link org.joda.time.Period}) between two dates. If the startDate is not before the endDate the dates
         * will be swapped.
         * @param ctx The context.
         * @param startDate The start date for the interval.
         * @param endDate The ending date for the interval.
         * @param periodType The type of period ({@link org.joda.time.PeriodType}) to return.
         * @return The {@link org.joda.time.Period} between the two dates.
         */
        public static final Period calculatePeriod(Context ctx, Date startDate, Date endDate, PeriodType periodType) {
            Interval interval = calculateInterval(ctx, startDate, endDate);
            Period period = interval.toPeriod(periodType);
            return period;
        }

        /**
         * Calculates the time between the start and end time of a {@link eu.vranckaert.worktime.model.TimeRegistration} in hours, minutes and seconds
         * (long-text: "5 hours, 36 minutes, 33 seconds")
         * @param ctx The context.
         * @param registration The {@link eu.vranckaert.worktime.model.TimeRegistration} instance.
         * @return The formatted string that represents the time between start and end time.
         */
        public static final String calculatePeriod(Context ctx, TimeRegistration registration) {
            Period period = null;
            if (registration.isOngoingTimeRegistration()) {
                period = calculatePeriod(ctx, registration.getStartTime(), new Date(), PeriodType.time());
            } else {
                period = calculatePeriod(ctx, registration.getStartTime(), registration.getEndTime(), PeriodType.time());
            }

            int hours = period.getHours();
            int minutes = period.getMinutes();
            int seconds = period.getSeconds();

            String hoursString = "";
            String minutesString = "";
            String secondsString = "";
            String periodString = "";
            
            TimePrecisionPreference preference = Preferences.getTimePrecision(ctx);
            switch (preference) {
                case SECOND: {
                    hoursString = hours + " " + ctx.getString(R.string.hours) + ", ";
                    minutesString = minutes + " " + ctx.getString(R.string.minutes) + ", ";
                    secondsString = seconds + " " + ctx.getString(R.string.seconds);
                    if (hours > 0) {
                        periodString = hoursString + minutesString + secondsString;
                    } else if (minutes > 0) {
                        periodString = minutesString + secondsString;
                    } else {
                        periodString = secondsString;
                    }
                    break;
                }
                case MINUTE: {
                    hoursString = hours + " " + ctx.getString(R.string.hours) + ", ";
                    minutesString = minutes + " " + ctx.getString(R.string.minutes);
                    if (hours > 0) {
                        periodString = hoursString + minutesString;
                    } else {
                        periodString = minutesString;
                    }
                    break;
                }
            }
            return periodString;
        }

        /**
         * Calculates the time between the start and end time of a list of {@link eu.vranckaert.worktime.model.TimeRegistration} instances in days,
         * hours, minutes and seconds (short-text: 1d 4h 13m 0s)
         *
         * @param ctx The context.
         * @param registrations A list of {@link eu.vranckaert.worktime.model.TimeRegistration} instances.
         * @param displayDuration The format that defines the output.
         * @return The formatted string that represents the sum of the duration for each {@link eu.vranckaert.worktime.model.TimeRegistration}.
         */
        public static final String calculatePeriod(Context ctx, List<TimeRegistration> registrations, ReportingDisplayDuration displayDuration) {
            Long duration = 0L;

            Log.d(LOG_TAG, "Calculating period for " + registrations.size() + " TR's...");
            for (TimeRegistration registration : registrations) {
                Duration regDuration = null;
                if (registration.isOngoingTimeRegistration()) {
                    regDuration = calculateDuration(ctx, registration.getStartTime(), new Date());
                } else {
                    regDuration = calculateDuration(ctx, registration.getStartTime(), registration.getEndTime());
                }
                Log.d(LOG_TAG, "Calculated duration: " + regDuration);
                Log.d(LOG_TAG, "About to add milis: " + regDuration.getMillis());
                duration += regDuration.getMillis();
                Log.d(LOG_TAG, "Total duration with new calcuation added: " + duration);
            }
            Log.d(LOG_TAG, "Total duration calculated: " + duration);
            Duration totalDuration = new Duration(duration);
            Log.d(LOG_TAG, "Total duration created from milis: " + totalDuration);
            Period period = totalDuration.toPeriod(PeriodType.time());
            Log.d(LOG_TAG,  "Total period: " + period);

            int days = 0;
            int hours = period.getHours();
            int minutes = period.getMinutes();
            int seconds = period.getSeconds();

            switch (displayDuration) {
                case DAYS_HOUR_MINUTES_SECONDS_24H: {
                    days = hours/24;
                    if (days > 0) {
                        hours = hours - (days * 24);
                    }
                    break;
                }
                case DAYS_HOUR_MINUTES_SECONDS_08H: {
                    days = hours/8;
                    if (days > 0) {
                        hours = hours - (days * 8);
                    }
                    break;
                }
            }

            Log.d(LOG_TAG, days + "d " + hours + "h " + minutes + "m " + seconds + "s");

            String daysString = "";
            String hoursString = "";
            String minutesString = "";
            String secondsString = "";
            String periodString = "";

            TimePrecisionPreference preference = Preferences.getTimePrecision(ctx);
            switch (preference) {
                case SECOND: {
                    daysString = StringUtils.leftPad(String.valueOf(days), "0", 2) + ctx.getString(R.string.daysShort) + " ";
                    hoursString = StringUtils.leftPad(String.valueOf(hours), "0", 2) + ctx.getString(R.string.hoursShort) + " ";
                    minutesString = StringUtils.leftPad(String.valueOf(minutes), "0", 2) + ctx.getString(R.string.minutesShort) + " ";
                    secondsString = StringUtils.leftPad(String.valueOf(seconds), "0", 2) + ctx.getString(R.string.secondsShort);
                    if (days > 0) {
                        periodString = daysString + hoursString + minutesString + secondsString;
                    } else if (hours > 0) {
                        periodString = hoursString + minutesString + secondsString;
                    } else if (minutes > 0) {
                        periodString = minutesString + secondsString;
                    } else {
                        periodString = secondsString;
                    }
                    break;
                }
                case MINUTE: {
                    daysString = StringUtils.leftPad(String.valueOf(days), "0", 2) + ctx.getString(R.string.daysShort) + " ";
                    hoursString = StringUtils.leftPad(String.valueOf(hours), "0", 2) + ctx.getString(R.string.hoursShort) + " ";
                    minutesString = StringUtils.leftPad(String.valueOf(minutes), "0", 2) + ctx.getString(R.string.minutesShort) + " ";
                    if (days > 0) {
                        periodString = daysString + hoursString + minutesString;
                    } else if (hours > 0) {
                        periodString = hoursString + minutesString;
                    } else {
                        periodString = minutesString;
                    }
                    break;
                }
            }
            return periodString;
        }

        public static Map<Integer, Date> calculateWeekBoundaries(int weekDiff, Context ctx) {
            int weekStartsOn = Preferences.getWeekStartsOn(ctx);

            LocalDate now = new LocalDate();
            LocalDate firstDayOfWeek = new LocalDate();
            LocalDate lastDayOfWeek = new LocalDate();

            now = addWeeksToDate(now, weekDiff);
            firstDayOfWeek = addWeeksToDate(firstDayOfWeek, weekDiff);
            lastDayOfWeek = addWeeksToDate(lastDayOfWeek, weekDiff);

            firstDayOfWeek = firstDayOfWeek.withDayOfWeek(weekStartsOn);
            if (firstDayOfWeek.isAfter(now)) {
                DateTimeFieldType weekOfWeekyear = DateTimeFieldType.weekOfWeekyear();
                int weekOfYear = firstDayOfWeek.get(weekOfWeekyear);
                firstDayOfWeek = firstDayOfWeek.withWeekOfWeekyear(weekOfYear - 1);
            }

            switch (weekStartsOn) {
                case DateTimeConstants.MONDAY:
                    lastDayOfWeek = lastDayOfWeek.withDayOfWeek(DateTimeConstants.SUNDAY);
                    break;
                case DateTimeConstants.TUESDAY:
                    lastDayOfWeek = lastDayOfWeek.withDayOfWeek(DateTimeConstants.MONDAY);
                    break;
                case DateTimeConstants.WEDNESDAY:
                    lastDayOfWeek = lastDayOfWeek.withDayOfWeek(DateTimeConstants.TUESDAY);
                    break;
                case DateTimeConstants.THURSDAY:
                    lastDayOfWeek = lastDayOfWeek.withDayOfWeek(DateTimeConstants.WEDNESDAY);
                    break;
                case DateTimeConstants.FRIDAY:
                    lastDayOfWeek = lastDayOfWeek.withDayOfWeek(DateTimeConstants.THURSDAY);
                    break;
                case DateTimeConstants.SATURDAY:
                    lastDayOfWeek = lastDayOfWeek.withDayOfWeek(DateTimeConstants.FRIDAY);
                    break;
                case DateTimeConstants.SUNDAY:
                    lastDayOfWeek = lastDayOfWeek.withDayOfWeek(DateTimeConstants.SATURDAY);
                    break;
            }

            Map<Integer, Date> result = new HashMap<Integer, Date>();
            result.put(DateConstants.FIRST_DAY_OF_WEEK, firstDayOfWeek.toDate());
            result.put(DateConstants.LAST_DAY_OF_WEEK, lastDayOfWeek.toDate());

            return result;
        }

        /**
         * Add an amount of weeks to a certain date. If amount of weeks to add is negative it will be subtracted.
         * @param date The {@link org.joda.time.LocalDate} instance to calculate on.
         * @param weekDiff The number of weeks to add to the provided date.
         * @return The {@link org.joda.time.LocalDate} with the weeks added or subtracted.
         */
        private static LocalDate addWeeksToDate(LocalDate date, int weekDiff) {
            int weekOfYear = date.get(DateTimeFieldType.weekOfWeekyear());
            date = date.withWeekOfWeekyear(weekOfYear + weekDiff);

            return date;
        }
    }

    /**
     * Contains all system specific methods.
     */
    public static class System {
        /**
         * Find out if a 24 hours clock is preferred or not. The check will be done based on the user's locale.
         * @param context The context to find the uers's locale.
         * @return {@link Boolean#TRUE} if the 24 hours format is preferred. {@link Boolean#FALSE} if the AM/PM notation
         * is preferred.
         */
        public static boolean is24HourClock(Context context) {
            Locale locale = ContextUtils.getCurrentLocale(context);
            return is24HourClock(locale);
        }

        /**
         * Find out, based on the user's locale, if a 24 hours clock is preferred or not.
         * @param locale The user's locale.
         * @return {@link Boolean#TRUE} if the 24 hours format is preferred. {@link Boolean#FALSE} if the AM/PM notation
         * is preferred.
         */
        public static boolean is24HourClock(Locale locale) {
            java.text.DateFormat dateFormat = java.text.DateFormat.getTimeInstance(java.text.DateFormat.FULL, locale);
            String t = dateFormat.format(new Date());

            java.text.DateFormat stdFormat = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT, Locale.US);
            java.text.DateFormat localeFormat = java.text.DateFormat.getTimeInstance(java.text.DateFormat.LONG, locale);
            String check = "";
            try {
                check = localeFormat.format(stdFormat.parse("7:00 PM"));
            } catch (ParseException ignore) {
                return false;
            }
            boolean is24HourClock = check.contains("19");

            Log.d(LOG_TAG, "Result of is24HourClock: " + is24HourClock);

            return is24HourClock;
        }
    }

    /**
     * Contains various date and time methods.
     */
    public static class Various {
        /**
         * Reset the time-part of a date to midnight (00:00:00.000000).
         * @param date The date to reset.
         * @return The time reset to midnight.
         */
        public static Date resetToMidnight(Date date) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();
        }
    }

    /**
     * Containing some methods to determine the time precision to be used in this utility class.
     */
    private static class TimePrecision {
        /**
         * Apply the user's selected {@link TimePrecisionPreference} on the provided {@link Date}.
         * @param ctx The context to search for the preference setting.
         * @param date The {@link Date} to which the the {@link TimePrecisionPreference} must be applied.
         * @return The {@link Date} with the applied {@link TimePrecisionPreference}.
         */
        private static Date applyTimePrecision(Context ctx, Date date) {
            return applyTimePrecisionCalendar(ctx, date).getTime();
        }

        /**
         * Apply the user's selected {@link TimePrecisionPreference} on the provided {@link Calendar}.
         * @param ctx The context to search for the preference setting.
         * @param cal The {@link Calendar} to which the the {@link TimePrecisionPreference} must be applied.
         * @return The {@link Date} with the applied {@link TimePrecisionPreference}.
         */
        private static Date applyTimePrecision(Context ctx, Calendar cal) {
            return applyTimePrecisionCalendar(ctx, cal).getTime();
        }

        /**
         * Apply the user's selected {@link TimePrecisionPreference} on the provided {@link Date}.
         * @param ctx The context to search for the preference setting.
         * @param date The {@link Date} to which the the {@link TimePrecisionPreference} must be applied.
         * @return The {@link Calendar} with the applied {@link TimePrecisionPreference}.
         */
        private static Calendar applyTimePrecisionCalendar(Context ctx, Date date) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return applyTimePrecisionCalendar(ctx, cal);
        }

        /**
         * Apply the user's selected {@link TimePrecisionPreference} on the provided {@link Calendar}.
         * @param ctx The context to search for the preference setting.
         * @param cal The {@link Calendar} to which the the {@link TimePrecisionPreference} must be applied.
         * @return The {@link Calendar} with the applied {@link TimePrecisionPreference}.
         */
        private static Calendar applyTimePrecisionCalendar(Context ctx, Calendar cal) {
            TimePrecisionPreference preference = Preferences.getTimePrecision(ctx);
            
            switch (preference) {
                case SECOND:
                    cal.set(Calendar.MILLISECOND, 0);
                    break;
                case MINUTE:
                    cal.set(Calendar.MILLISECOND, 0);
                    cal.set(Calendar.SECOND, 0);
                    break;
            }
            
            return cal;
        }
    }

}
