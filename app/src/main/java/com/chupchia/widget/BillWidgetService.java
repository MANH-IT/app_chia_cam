package com.chupchia.widget;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class BillWidgetService extends Service {
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Update widget immediately when service starts
        BillWidgetProvider.updateAllWidgets(this);
        return START_NOT_STICKY;
    }
}
