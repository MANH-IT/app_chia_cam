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
        // TODO: Lấy hóa đơn mới nhất từ API/Firebase nếu cần
        // Hiện tại, chỉ kích hoạt cập nhật widget với dữ liệu cache hiện có
        BillWidgetProvider.updateAllWidgets(getApplicationContext());
        return Result.success();
    }
}
