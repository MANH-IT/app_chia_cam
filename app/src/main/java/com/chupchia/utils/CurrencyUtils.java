package com.chupchia.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtils {

    private static final NumberFormat VND_FORMAT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    /**
     * Định dạng tiền tệ theo VND (ví dụ: 35.000đ)
     */
    public static String formatVND(long amount) {
        return VND_FORMAT.format(amount) + "đ";
    }

    /**
     * Định dạng tiền tệ không có ký hiệu tiền tệ
     */
    public static String formatNumber(long amount) {
        return VND_FORMAT.format(amount);
    }

    /**
     * Định dạng với hậu tố "đ"
     */
    public static String format(long amount) {
        return formatVND(amount);
    }

    /**
     * Phân tích chuỗi đã định dạng thành giá trị long
     */
    public static long parseVND(String formatted) {
        if (formatted == null || formatted.isEmpty()) return 0;
        String cleaned = formatted.replaceAll("[^0-9]", "");
        if (cleaned.isEmpty()) return 0;
        try {
            return Long.parseLong(cleaned);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Định dạng số tiền để hiển thị với thông tin mỗi người
     */
    public static String formatPerPersonAmount(long totalAmount, int memberCount) {
        if (memberCount == 0) return formatVND(0);
        long perPerson = totalAmount / memberCount;
        return formatVND(perPerson);
    }
}
