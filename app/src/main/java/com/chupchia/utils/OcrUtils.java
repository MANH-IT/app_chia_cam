package com.chupchia.utils;

import android.graphics.Bitmap;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OcrUtils {
    
    private static TextRecognizer recognizer;
    
    public interface OcrCallback {
        void onSuccess(String amount);
        void onFailure();
    }
    
    public static void extractAmountFromBitmap(Bitmap bitmap, OcrCallback callback) {
        if (recognizer == null) {
            recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        }
        
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        
        recognizer.process(image)
            .addOnSuccessListener(visionText -> {
                String amount = extractAmount(visionText.getText());
                if (amount != null) {
                    callback.onSuccess(amount);
                } else {
                    callback.onFailure();
                }
            })
            .addOnFailureListener(e -> {
                callback.onFailure();
            });
    }
    
    /**
     * Trích xuất số tiền từ văn bản
     */
    static String extractAmount(String text) {
        if (text == null || text.isEmpty()) return null;
        
        // Loại bỏ ký tự đặc biệt có thể gây nhiễu nhưng giữ lại chữ số và dấu phân cách
        String cleanText = text.replace("đ", "").replace("VND", "").replace("VNĐ", "");
        
        // Chiến lược 1: Tìm từ khóa thường đứng trước tổng tiền
        String[] lines = cleanText.split("\n");
        String[] keywords = {"TỔNG CỘNG", "THANH TOÁN", "TOTAL", "CỘNG", "TIỀN HÀNG", "SUM"};
        
        for (String line : lines) {
            String upperLine = line.toUpperCase();
            for (String keyword : keywords) {
                if (upperLine.contains(keyword)) {
                    // Tìm số lớn nhất trong dòng này hoặc dòng tiếp theo
                    String amount = findLargestNumberInLine(line);
                    if (amount != null) return amount;
                }
            }
        }
        
        // Chiến lược 2: Tìm định dạng tiền Việt Nam: 35.000, 35000, 35,000
        // Ưu tiên số lớn hơn và số có từ 3 chữ số trở lên
        Pattern pattern = Pattern.compile("\\b(\\d{1,3}(?:[.,]\\d{3})+|\\d{4,9})\\b");
        Matcher matcher = pattern.matcher(cleanText);
        
        long maxAmount = 0;
        String bestMatch = null;
        
        while (matcher.find()) {
            String rawMatch = matcher.group(1);
            // Bỏ qua nếu trông giống ngày tháng (chứa / hoặc -)
            if (rawMatch.contains("/") || rawMatch.contains("-")) {
                continue;
            }
            
            String match = rawMatch.replace(".", "").replace(",", "");
            try {
                long val = Long.parseLong(match);
                // Phương pháp hàm lợi: đa số hóa đơn từ 5k đến 10M
                // Bỏ qua các năm thường gặp như 202x
                if (val == 2024 || val == 2025 || val == 2026) continue;
                
                if (val > maxAmount && val >= 1000 && val <= 10000000) {
                    maxAmount = val;
                    bestMatch = match;
                }
            } catch (NumberFormatException ignored) {}
        }
        
        return bestMatch;
    }

    private static String findLargestNumberInLine(String line) {
        // Regex tìm các số có dấu phân cách hàng nghìn (tùy chọn)
        Pattern pattern = Pattern.compile("\\b(\\d{1,3}(?:[.,]\\d{3})+|\\d{3,9})\\b");
        Matcher matcher = pattern.matcher(line);
        long maxVal = -1;
        String bestMatch = null;
        
        while (matcher.find()) {
            String rawMatch = matcher.group();
            // Kiểm tra ngày cơ bản: nếu có nhiều hơn một dấu chấm/gạch chéo, có thể là ngày tháng
            if (rawMatch.chars().filter(ch -> ch == '.' || ch == '/' || ch == '-').count() > 1) {
                continue;
            }
            
            String match = rawMatch.replace(".", "").replace(",", "");
            try {
                long val = Long.parseLong(match);
                if (val > maxVal && val < 20000000) { // Giới hạn 20 triệu để an toàn
                    maxVal = val;
                    bestMatch = match;
                }
            } catch (NumberFormatException ignored) {}
        }
        return bestMatch;
    }
}
