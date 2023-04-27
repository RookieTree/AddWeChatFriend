
/*
 * 深圳市有信网络技术有限公司
 * Copyright (c) 2016 All Rights Reserved.
 */

package com.rookie.addfriend;

import android.content.Context;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;

public class TimerUtils {
    /*
     * Try to use String.format() as little as possible, because it creates a
     * new Formatter every time you call it, which is very inefficient. Reusing
     * an existing Formatter more than tripled the speed of makeTimeString().
     * This Formatter/StringBuilder are also used by makeAlbumSongsLabel()
     */
    private static StringBuilder sFormatBuilder = new StringBuilder();
    private static Formatter sFormatter = new Formatter(sFormatBuilder, Locale.ENGLISH);
    private static final Object[] sTimeArgs = new Object[5];

    /**
     * 将时间长度格式化成00:00:00
     * @param context
     * @param secs
     * @return
     */
    public static String makeTimeString(Context context, long secs) {
//        String durationformat = context.getString(secs < 3600 ? "R.string.durationformatshort" : "R.string.durationformatlong");

        /*
         * Provide multiple arguments so the format can be changed easily by
         * modifying the xml.
         */
        sFormatBuilder.setLength(0);

        final Object[] timeArgs = sTimeArgs;
        timeArgs[0] = secs / 3600;
        timeArgs[1] = secs / 60;
        timeArgs[2] = (secs / 60) % 60;
        timeArgs[3] = secs;
        timeArgs[4] = secs % 60;

        return sFormatter.format("durationformat", timeArgs).toString();
    }
    
    
    /**
     * @param format 格式
     * @return	时间戳	yyyyMMddHHmmss
     */
    public static String getTimeStampInFormat(long time, String format) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            Date date = new Date(time);
            String timeStmap = formatter.format(date);
            return timeStmap;
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * @return	当前时间的时间戳(秒)
     */
    public static String getCurrentTimeStampInFormat() {
        long currentTime = System.currentTimeMillis();
        String fomat = "yyyyMMddHHmmss";
        return getTimeStampInFormat(currentTime, fomat);
    }

    /** 
    * @Description: 按照格式返回当前时间
    * @author liuhaikang 
    * @date 2015-10-15 下午5:25:39 
    * @param format
    * @return 
    */
    public static String getCurrentTimeInFormat(String format) {
        long currentTime = System.currentTimeMillis();
        return getTimeStampInFormat(currentTime, format);
    }
    
    /**
     * @return 当前时间戳
     */
    public static long getCurrentTimeStamp() {
        Calendar calendar = Calendar.getInstance();
        // calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

}
