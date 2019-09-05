package com.filipnowakdev.gps_offline_tracker.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils
{
    public static String getFormattedDateString(long time)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy HH:mm:ss", Locale.US);
        return dateFormat.format(time);
    }
}

