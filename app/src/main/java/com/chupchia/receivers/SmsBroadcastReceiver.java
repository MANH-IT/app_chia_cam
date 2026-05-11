package com.chupchia.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

public class SmsBroadcastReceiver extends BroadcastReceiver {
    
    private OTPReceivedListener otpReceivedListener;
    
    public interface OTPReceivedListener {
        void onOTPReceived(String otp);
        void onOTPTimeout();
    }
    
    public void setOTPReceivedListener(OTPReceivedListener listener) {
        this.otpReceivedListener = listener;
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
            
            if (status != null && status.getStatusCode() == CommonStatusCodes.SUCCESS) {
                // Lấy tin nhắn SMS
                String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                if (message != null && otpReceivedListener != null) {
                    String otp = extractOTP(message);
                    if (otp != null && otp.length() == 6) {
                        otpReceivedListener.onOTPReceived(otp);
                    }
                }
            } else if (status != null && status.getStatusCode() == CommonStatusCodes.TIMEOUT) {
                if (otpReceivedListener != null) {
                    otpReceivedListener.onOTPTimeout();
                }
            }
        }
    }
    
    /**
     * Trích xuất mã OTP 6 chữ số từ tin nhắn SMS
     */
    private String extractOTP(String message) {
        // Tìm 6 chữ số liên tiếp
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b\\d{6}\\b");
        java.util.regex.Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}
