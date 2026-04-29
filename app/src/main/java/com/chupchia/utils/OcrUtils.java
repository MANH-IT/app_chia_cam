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
     * Extract amount from text
     */
    private static String extractAmount(String text) {
        // Look for Vietnamese currency format: 35.000, 35000, 35,000
        Pattern pattern1 = Pattern.compile("\\b(\\d{1,3}(?:\\.\\d{3})*)\\b");
        Pattern pattern2 = Pattern.compile("\\b(\\d{5,7})\\b");
        
        Matcher matcher1 = pattern1.matcher(text);
        if (matcher1.find()) {
            String amountStr = matcher1.group(1).replace(".", "");
            return amountStr;
        }
        
        Matcher matcher2 = pattern2.matcher(text);
        if (matcher2.find()) {
            String amountStr = matcher2.group(1);
            long amount = Long.parseLong(amountStr);
            // If amount is too large (over 1 million), divide by 1000
            if (amount > 1000000) {
                amount = amount / 1000;
            }
            return String.valueOf(amount);
        }
        
        return null;
    }
}
