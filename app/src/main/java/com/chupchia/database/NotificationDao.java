package com.chupchia.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.chupchia.models.Notification;

import java.util.List;

@Dao
public interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    List<Notification> getAll();

    @Query("SELECT * FROM notifications WHERE isRead = 0 ORDER BY timestamp DESC")
    List<Notification> getUnread();

    @Query("SELECT * FROM notifications WHERE isRead = 1 ORDER BY timestamp DESC")
    List<Notification> getRead();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Notification notification);

    @Update
    void update(Notification notification);

    @Delete
    void delete(Notification notification);

    @Query("DELETE FROM notifications")
    void deleteAll();
}
