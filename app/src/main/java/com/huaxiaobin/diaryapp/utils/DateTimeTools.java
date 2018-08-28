package com.huaxiaobin.diaryapp.utils;

import android.util.Log;

import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTimeTools {

    public static String getWeek(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        int i = calendar.get(Calendar.DAY_OF_WEEK);
        switch (i) {
            case 1:
                return "周日";
            case 2:
                return "周一";
            case 3:
                return "周二";
            case 4:
                return "周三";
            case 5:
                return "周四";
            case 6:
                return "周五";
            case 7:
                return "周六";
        }
        return "";
    }

    public static String getTime(long timestamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        return simpleDateFormat.format(timestamp);
    }

    public static String getYearMonthDay(long timestamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy.MM.dd");
        return simpleDateFormat.format(timestamp);
    }

    public static String getDateTime(long timestamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return simpleDateFormat.format(timestamp) + " " + getWeek(timestamp);
    }

    public static String getTimeInterval(long timestamp) {
        String result;
        long second = (System.currentTimeMillis() - timestamp) / 1000;
        long minute = second / 60;
        long hour = minute / 60;
        long day = hour / 24;
        if (second < 60) {
            result = "刚刚";
        } else if (minute < 60) {
            result = minute + "分钟前";
        } else if (hour < 24) {
            result = hour + "小时前";
        } else {
            result = day + "天前";
        }
        return result;
    }
}
