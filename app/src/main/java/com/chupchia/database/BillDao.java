package com.chupchia.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.chupchia.models.Bill;

import java.util.List;

@Dao
public interface BillDao {
    @Query("SELECT * FROM bills ORDER BY timestamp DESC")
    List<Bill> getAllBills();

    @Query("SELECT * FROM bills WHERE groupId = :groupId ORDER BY timestamp DESC")
    List<Bill> getBillsByGroup(String groupId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBill(Bill bill);

    @Update
    void updateBill(Bill bill);

    @Delete
    void deleteBill(Bill bill);

    @Query("DELETE FROM bills")
    void deleteAll();

    @Query("SELECT SUM(amount) FROM bills")
    long getTotalSpent();

    @Query("SELECT COUNT(*) FROM bills")
    int getBillCount();
}
