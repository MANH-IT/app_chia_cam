package com.chupchia.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.chupchia.widget.BillWidgetProvider;

public class WidgetUpdateWorker extends Worker {

    public WidgetUpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // TODO: Fetch latest bill from API/Firebase if needed
        // For now, we'll just trigger widget update with existing cached data
        BillWidgetProvider.updateAllWidgets(getApplicationContext());
        return Result.success();
    }
}
