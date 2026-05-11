package com.chupchia.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.chupchia.models.PaymentHistory;

import java.util.List;

@Dao
public interface PaymentHistoryDao {
    @Query("SELECT * FROM payment_history ORDER BY timestamp DESC")
    List<PaymentHistory> getAll();

    @Query("SELECT * FROM payment_history WHERE fromUserId = :userId OR toUserId = :userId ORDER BY timestamp DESC")
    List<PaymentHistory> getByUser(String userId);

    @Query("SELECT * FROM payment_history WHERE (fromUserId = :userId1 AND toUserId = :userId2) OR (fromUserId = :userId2 AND toUserId = :userId1) ORDER BY timestamp DESC")
    List<PaymentHistory> getBetweenUsers(String userId1, String userId2);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PaymentHistory paymentHistory);

    @Update
    void update(PaymentHistory paymentHistory);

    @Delete
    void delete(PaymentHistory paymentHistory);

    @Query("DELETE FROM payment_history")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM payment_history")
    int getCount();
}
