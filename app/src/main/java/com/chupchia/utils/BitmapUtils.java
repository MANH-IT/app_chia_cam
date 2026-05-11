package com.chupchia.utils;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitmapUtils {
    
    /**
     * Giải mã bitmap từ URI với kích thước mẫu và xử lý đúng hướng xoay
     */
    public static Bitmap decodeBitmapFromUri(ContentResolver resolver, Uri uri, int reqWidth, int reqHeight) throws IOException {
        // 1. Lấy kích thước
        InputStream inputStream = resolver.openInputStream(uri);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        if (inputStream != null) inputStream.close();
        
        // 2. Tính kích thước mẫu
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        
        // 3. Giải mã bitmap
        inputStream = resolver.openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        if (inputStream != null) inputStream.close();
        
        if (bitmap == null) return null;

        // 4. Xử lý xoay dựa trên EXIF
        return handleRotation(resolver, uri, bitmap);
    }

    private static Bitmap handleRotation(ContentResolver resolver, Uri uri, Bitmap bitmap) {
        try (InputStream input = resolver.openInputStream(uri)) {
            ExifInterface exif;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                exif = new ExifInterface(input);
            } else {
                // Dự phòng cho các phiên bản cũ hơn nếu cần, nhưng minSdk là 26
                return bitmap;
            }

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotationDegrees = 0;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90: rotationDegrees = 90; break;
                case ExifInterface.ORIENTATION_ROTATE_180: rotationDegrees = 180; break;
                case ExifInterface.ORIENTATION_ROTATE_270: rotationDegrees = 270; break;
            }

            if (rotationDegrees != 0) {
                Bitmap rotated = rotateBitmap(bitmap, rotationDegrees);
                if (rotated != bitmap) {
                    bitmap.recycle();
                }
                return rotated;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    
    /**
     * Tính kích thước mẫu (inSampleSize)
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        
        return inSampleSize;
    }
    
    /**
     * Xoay bitmap
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        if (orientation == 0) return bitmap;
        Matrix matrix = new Matrix();
        matrix.postRotate(orientation);
        try {
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return bitmap;
        }
    }
    
    /**
     * Nén bitmap thành JPEG
     */
    public static byte[] compressBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        return baos.toByteArray();
    }
}
