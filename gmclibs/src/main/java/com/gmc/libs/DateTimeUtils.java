package com.gmc.libs;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTimeUtils {

    public static final String FORMAT_LONG_DAY_OF_WEEK = "EEEE";
    public static final String FORMAT_SHORT_DAY_OF_WEEK = "EEE";

    /**
     * 125s => 00:02:05<br/>
     * if (format = "mm:ss") 125s => 02:05
     */
    public static String secondsToTime(long secondsParams, String format){
        int seconds = (int) secondsParams % 60 ;
        int minutes = (int) ((secondsParams / 60) % 60);
        int hours   = (int) ((secondsParams / (60*60)) % 24);

        String str_hours = hours < 10 ? "0" + hours : String.valueOf(hours);
        String str_minutes = minutes < 10 ? "0" + minutes : String.valueOf(minutes);
        String str_seconds = seconds < 10 ? "0" + seconds : String.valueOf(seconds);
        if(hours < 1 && "mm:ss".equals(format)){
            return str_minutes+":"+str_seconds;
        }
        return str_hours +":"+str_minutes+":"+str_seconds;
    }

    /**
     * 125400ms => 00:02:05<br/>
     * if (format = "mm:ss") 125400s => 02:05
     */
    public static String millisecondsToTime(long milliseconds, String format){
        return secondsToTime((milliseconds / 1000), format);
    }

    public static String dateToString(Date date, String format) {
        if (date == null) {
            date = new Date();
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

    public static String dateToString(String format) {
        return dateToString(null, format);
    }

    public static String dateToString(long timeMills, String format) {
        Date date = new Date(timeMills);
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }
    /**
     * Return: Monday, Tuesday, ..., Sunday
     * */
    public static String getFullDayOfWeek(Date date) {
        try {
            if (date == null) {
                date = new Date();
            }
            SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_LONG_DAY_OF_WEEK);
            return sdf.format(date);
        } catch (Exception e) {

        }
        return null;
    }

    /**
     * Return: Mon, Tue, ..., Sun
     * */
    public static String getShortDayOfWeek(Date date) {
        try {
            if (date == null) {
                date = new Date();
            }
            SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_SHORT_DAY_OF_WEEK);
            return sdf.format(date);
        } catch (Exception e) {

        }
        return null;
    }

    /**
     * Use with Localization language
     * Ex: dayName = ["Monday", "Tuesday", ..., "Sunday"]
     *  or dayName = ["Thứ 2", "Thứ 3", ..., "Chủ Nhật"]
     *  ...
     * */
    public static String getDayOfWeek(Date date, String[] dayName) {
        try {
            if (date == null) {
                date = new Date();
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            return dayName[day];
        } catch (Exception e) { }
        return null;
    }
}
