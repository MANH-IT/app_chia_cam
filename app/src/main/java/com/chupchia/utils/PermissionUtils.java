package com.chupchia.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtils {
    
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 101;
    
    /**
     * Kiểm tra quyền camera đã được cấp chưa
     */
    public static boolean hasCameraPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Kiểm tra quyền lưu trữ đã được cấp chưa (Đọc ảnh cho Camera/Thư viện)
     */
    public static boolean hasStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }
    
    /**
     * Yêu cầu quyền camera
     */
    public static void requestCameraPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }
    
    /**
     * Yêu cầu quyền lưu trữ (cách hiện đại)
     */
    public static void requestStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(activity, 
                new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 
                STORAGE_PERMISSION_REQUEST_CODE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10, 11, 12 chỉ cần READ_EXTERNAL_STORAGE cho thư viện
            ActivityCompat.requestPermissions(activity, 
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 
                STORAGE_PERMISSION_REQUEST_CODE);
        } else {
            // Android 9 và thấp hơn cần cả hai quyền
            ActivityCompat.requestPermissions(activity, 
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 
                STORAGE_PERMISSION_REQUEST_CODE);
        }
    }
    
    /**
     * Kiểm tra quyền danh bạ đã được cấp chưa
     */
    public static boolean hasContactPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Yêu cầu quyền danh bạ
     */
    public static void requestContactPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, 
                new String[]{Manifest.permission.READ_CONTACTS}, 
                102); // 102 khớp với mã trong CreateGroupActivity
    }

    /**
     * Kiểm tra tất cả quyền cần thiết cho Camera đã được cấp chưa
     */
    public static boolean hasAllPermissions(Context context) {
        return hasCameraPermission(context);
    }

    /**
     * Lấy tất cả quyền cần thiết dưới dạng mảng String theo phiên bản SDK
     */
    public static String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES
            };
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            };
        } else {
            return new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }
    }

    /**
     * Hiển thị hộp thoại hướng dẫn người dùng vào cài đặt để bật quyền
     */
    public static void openAppSettings(Activity activity) {
        android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        android.net.Uri uri = android.net.Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }
}
