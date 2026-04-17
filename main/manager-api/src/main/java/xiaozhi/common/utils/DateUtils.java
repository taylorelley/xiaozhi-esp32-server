package xiaozhi.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * dateprocess
 * Copyright (c) Renren Opensource All rights reserved.
 * Website: https://www.renren.io
 */
public class DateUtils {
    /**
     * timeformat(yyyy-MM-dd)
     */
    public final static String DATE_PATTERN = "yyyy-MM-dd";
    /**
     * timeformat(yyyy-MM-dd HH:mm:ss)
     */
    public final static String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public final static String DATE_TIME_MILLIS_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";


    /**
     * dateformat dateformatas：yyyy-MM-dd
     *
     * @param date date
     * @return returnyyyy-MM-ddformatdate
     */
    public static String format(Date date) {
        return format(date, DATE_PATTERN);
    }

    /**
     * dateformat dateformatas：yyyy-MM-dd
     *
     * @param date    date
     * @param pattern format，For example: DateUtils.DATE_TIME_PATTERN
     * @return returnyyyy-MM-ddformatdate
     */
    public static String format(Date date, String pattern) {
        if (date != null) {
            SimpleDateFormat df = new SimpleDateFormat(pattern);
            return df.format(date);
        }
        return null;
    }

    /**
     * dateparse
     *
     * @param date    date
     * @param pattern format，For example: DateUtils.DATE_TIME_PATTERN
     * @return returnDate
     */
    public static Date parse(String date, String pattern) {
        try {
            return new SimpleDateFormat(pattern).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String getDateTimeNow() {
        return getDateTimeNow(DATE_TIME_PATTERN);
    }

    public static String getDateTimeNow(String pattern) {
        return format(new Date(), pattern);
    }

    public static String millsToSecond(long mills) {
        return String.format("%.3f", mills / 1000.0);
    }

    /**
     * getsimpleshort timestring：10secondsbeforereturnjust now，multiplefewsecondsbefore，fewsmallwhenbefore，exceedoneweekreturnyear-month-daywhenseconds
     * @param date
     * @return
     */
    public static String getShortTime(Date date) {
        if (date == null) {
            return null;
        }
        // will Date convert to Instant
        LocalDateTime localDateTime = date.toInstant()
                // getsystemdefaultwhenarea
                .atZone(ZoneId.systemDefault())
                // convert to LocalDateTime
                .toLocalDateTime();
        // currenttime
        LocalDateTime now = LocalDateTime.now();
        // timedifference，unitasseconds
        long secondsBetween = ChronoUnit.SECONDS.between(localDateTime, now);

        if (secondsBetween <= 10) {
            return "just now";
        } else if (secondsBetween < 60) {
            return secondsBetween + "secondsbefore";
        } else if (secondsBetween < 60 * 60) {
            return secondsBetween / 60 + "before";
        } else if (secondsBetween < 86400) {
            return secondsBetween / 3600 + "smallwhenbefore";
        } else if (secondsBetween < 604800) {
            return secondsBetween / 86400 + "daybefore";
        } else {
            // exceedoneweek，displaycompletedatetime
            return format(date,DATE_TIME_PATTERN);
        }
    }
}
