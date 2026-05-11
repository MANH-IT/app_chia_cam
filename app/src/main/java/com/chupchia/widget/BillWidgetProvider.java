package com.chupchia.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.chupchia.R;
import com.chupchia.activities.MainActivity;
import com.chupchia.models.Bill;
import com.chupchia.utils.DateTimeUtils;

import java.text.NumberFormat;
import java.util.Locale;

public class BillWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_UPDATE_WIDGET = "com.chupchia.UPDATE_WIDGET";
    private static final String PREFS_WIDGET = "widget_prefs";
    private static final String KEY_LATEST_BILL = "latest_bill";
    private static final String KEY_LAST_UPDATE = "last_update";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (ACTION_UPDATE_WIDGET.equals(intent.getAction())) {
            updateAllWidgets(context);
        }
    }

    public static void updateAllWidgets(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, BillWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
        
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_bill_widget);

        // Lấy hóa đơn mới nhất từ SharedPreferences
        Bill latestBill = getLatestBill(context);

        if (latestBill != null) {
            updateWidgetContent(context, appWidgetManager, appWidgetId, views, latestBill);
        } else {
            showEmptyState(context, views);
        }

        // Gán sự kiện nhấp để mở ứng dụng
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static void updateWidgetContent(Context context, AppWidgetManager appWidgetManager,
                                            int appWidgetId, RemoteViews views, Bill bill) {
        // Tên nhóm (Bill không có groupName, dùng groupId hoặc giá trị mặc định)
        String groupDisplay = bill.getGroupId() != null ? "🏠 Nhóm" : "🏠 Chia Cam";
        views.setTextViewText(R.id.tv_group_name, groupDisplay);

        // Nội dung
        String actorName = bill.getActorName() != null ? bill.getActorName() : "Thành viên";
        views.setTextViewText(R.id.tv_content, actorName + " vừa mua " + bill.getProductName());

        // Số tiền
        views.setTextViewText(R.id.tv_amount, formatCurrency(bill.getAmount()));

        // Thời gian
        views.setTextViewText(R.id.tv_time, getTimeAgo(bill.getTimestamp()));

        // Tải ảnh bằng Glide cho RemoteViews
        if (bill.getImageUrl() != null && !bill.getImageUrl().isEmpty()) {
            try {
                Glide.with(context.getApplicationContext())
                        .asBitmap()
                        .load(bill.getImageUrl())
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                views.setImageViewBitmap(R.id.iv_product_image, resource);
                                appWidgetManager.updateAppWidget(appWidgetId, views);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                views.setImageViewResource(R.id.iv_product_image, R.drawable.ic_placeholder);
                                appWidgetManager.updateAppWidget(appWidgetId, views);
                            }
                        });
            } catch (Exception e) {
                views.setImageViewResource(R.id.iv_product_image, R.drawable.ic_placeholder);
            }
        } else {
            views.setImageViewResource(R.id.iv_product_image, R.drawable.ic_placeholder);
        }
    }

    private static void showEmptyState(Context context, RemoteViews views) {
        views.setTextViewText(R.id.tv_group_name, context.getString(R.string.widget_empty_title));
        views.setTextViewText(R.id.tv_content, context.getString(R.string.widget_empty_content));
        views.setTextViewText(R.id.tv_amount, context.getString(R.string.widget_empty_amount));
        views.setTextViewText(R.id.tv_time, context.getString(R.string.widget_empty_time));
        views.setImageViewResource(R.id.iv_product_image, R.drawable.ic_placeholder);
    }

    public static void saveLatestBill(Context context, Bill bill) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_LATEST_BILL, new Gson().toJson(bill));
        editor.putLong(KEY_LAST_UPDATE, System.currentTimeMillis());
        editor.apply();
    }

    public static Bill getLatestBill(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_LATEST_BILL, null);
        if (json != null) {
            try {
                return new Gson().fromJson(json, Bill.class);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private static String formatCurrency(long amount) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(amount) + "đ";
    }

    private static String getTimeAgo(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        if (diff < 60000) return "Vừa mới";
        if (diff < 3600000) return (diff / 60000) + " phút trước";
        if (diff < 86400000) return (diff / 3600000) + " giờ trước";
        if (diff < 604800000) return (diff / 86400000) + " ngày trước";
        return DateTimeUtils.formatDate(timestamp);
    }
}
