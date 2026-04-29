package com.chupchia.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtils {

    private static final NumberFormat VND_FORMAT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    /**
     * Format currency to VND format (e.g., 35,000đ)
     */
    public static String formatVND(long amount) {
        return VND_FORMAT.format(amount) + "đ";
    }

    /**
     * Format currency without currency symbol
     */
    public static String formatNumber(long amount) {
        return VND_FORMAT.format(amount);
    }

    /**
     * Format with "đ" suffix
     */
    public static String format(long amount) {
        return formatVND(amount);
    }

    /**
     * Parse formatted string to long value
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
     * Format amount for display with per person info
     */
    public static String formatPerPersonAmount(long totalAmount, int memberCount) {
        if (memberCount == 0) return formatVND(0);
        long perPerson = totalAmount / memberCount;
        return formatVND(perPerson);
    }
}
