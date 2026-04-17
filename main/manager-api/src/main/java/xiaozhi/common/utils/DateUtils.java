package xiaozhi.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * dateprocess
 * Copyright (c) 人人开source All rights reserved.
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
     * get简短 timestring：10seconds前return刚刚，多少seconds前，几小时前，超一周return年月日时分seconds
     * @param date
     * @return
     */
    public static String getShortTime(Date date) {
        if (date == null) {
            return null;
        }
        // will Date convert to Instant
        LocalDateTime localDateTime = date.toInstant()
                // getsystemdefault时区
                .atZone(ZoneId.systemDefault())
                // convert to LocalDateTime
                .toLocalDateTime();
        // currenttime
        LocalDateTime now = LocalDateTime.now();
        // time差，unitasseconds
        long secondsBetween = ChronoUnit.SECONDS.between(localDateTime, now);

        if (secondsBetween <= 10) {
            return "刚刚";
        } else if (secondsBetween < 60) {
            return secondsBetween + "seconds前";
        } else if (secondsBetween < 60 * 60) {
            return secondsBetween / 60 + "分钟前";
        } else if (secondsBetween < 86400) {
            return secondsBetween / 3600 + "小时前";
        } else if (secondsBetween < 604800) {
            return secondsBetween / 86400 + "天前";
        } else {
            // 超一周，显示completedatetime
            return format(date,DATE_TIME_PATTERN);
        }
    }
}
