package com.chupchia.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.chupchia.models.Bill;
import com.chupchia.models.Notification;
import com.chupchia.models.Group;
import com.chupchia.models.PaymentHistory;

@Database(entities = {Bill.class, Notification.class, Group.class, PaymentHistory.class}, version = 5, exportSchema = false)
@androidx.room.TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract BillDao billDao();
    public abstract NotificationDao notificationDao();
    public abstract GroupDao groupDao();
    public abstract PaymentHistoryDao paymentHistoryDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "chiacam_db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() // Chỉ dùng cho demo, sau này nên dùng background thread
                    .build();
        }
        return instance;
    }
}
