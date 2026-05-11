package com.chupchia.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateTimeUtils {
    
    // ThreadLocal đảm bảo mỗi luồng có instance SimpleDateFormat riêng
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = 
        ThreadLocal.withInitial(() -> new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()));
    
    private static final ThreadLocal<SimpleDateFormat> DATE_TIME_FORMAT = 
        ThreadLocal.withInitial(() -> new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()));
    
    /**
     * Định dạng ngày thành chuỗi (dd/MM/yyyy)
     */
    public static String formatDate(Date date) {
        return DATE_FORMAT.get().format(date);
    }
    
    /**
     * Định dạng ngày từ timestamp
     */
    public static String formatDate(long timestamp) {
        return DATE_FORMAT.get().format(new Date(timestamp));
    }
    
    /**
     * Định dạng ngày giờ
     */
    public static String formatDateTime(long timestamp) {
        return DATE_TIME_FORMAT.get().format(new Date(timestamp));
    }
    
    /**
     * Lấy ngày hiện tại
     */
    public static Calendar getCurrentDate() {
        return Calendar.getInstance();
    }
    
    /**
     * Lấy chuỗi thời gian trước đây
     */
    public static String getTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        
        if (diff < TimeUnit.MINUTES.toMillis(1)) {
            return "Vừa xong";
        } else if (diff < TimeUnit.HOURS.toMillis(1)) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            return minutes + " phút trước";
        } else if (diff < TimeUnit.DAYS.toMillis(1)) {
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            return hours + " giờ trước";
        } else if (diff < TimeUnit.DAYS.toMillis(7)) {
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            return days + " ngày trước";
        } else {
            return formatDate(timestamp);
        }
    }
}
