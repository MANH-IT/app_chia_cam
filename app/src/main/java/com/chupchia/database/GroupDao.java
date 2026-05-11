package com.chupchia.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.chupchia.models.Group;

import java.util.List;

@Dao
public interface GroupDao {
    @Query("SELECT * FROM groups ORDER BY createdAt DESC")
    List<Group> getAllGroups();

    @Query("SELECT * FROM groups WHERE id = :groupId LIMIT 1")
    Group getGroupById(String groupId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Group group);

    @Update
    void update(Group group);

    @Delete
    void delete(Group group);

    @Query("SELECT COUNT(*) FROM groups")
    int getGroupCount();

    @Query("SELECT * FROM groups WHERE inviteCode = :code LIMIT 1")
    Group getGroupByInviteCode(String code);

    @Query("DELETE FROM groups")
    void deleteAll();
}
