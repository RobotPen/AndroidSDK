package cn.robotpen.core.utils;

import android.annotation.SuppressLint;

/**
 * 时间转换工具类
 */


public class TimeUtil {

    @SuppressLint("SimpleDateFormat")
	public static String TimeStamp2Date(String timestampString, String formats) {
        Long timestamp = Long.parseLong(timestampString) * 1000;
        String date = new java.text.SimpleDateFormat(formats).format(new java.util.Date(timestamp));
        return date;
    }


    /**
     * 将毫秒数转化为日期格式数据
     * @param time
     * @return
     */
    public static String getFormatedDateTime(String time) {
        return TimeStamp2Date(time, "yyyy-MM-dd HH:mm");
    }


}
