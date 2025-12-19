package com.example.majordesign_master_v1.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DrawingDao {
    @Insert
    long insert(DrawingEntity entity);

    @Update
    void update(DrawingEntity entity);

    @Query("DELETE FROM drawings WHERE id IN (:ids)")
    void deleteByIds(List<Long> ids);

    @Query("SELECT * FROM drawings ORDER BY updatedAt DESC")
    LiveData<List<DrawingEntity>> observeAll();

    @Query("SELECT * FROM drawings WHERE id = :id LIMIT 1")
    DrawingEntity findByIdSync(long id);

    @Query("SELECT * FROM drawings ORDER BY updatedAt DESC LIMIT 1")
    DrawingEntity latestSync();

    @Query("SELECT * FROM drawings WHERE name LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    LiveData<List<DrawingEntity>> search(String query);

    @Query("SELECT * FROM drawings WHERE id = :id LIMIT 1")
    LiveData<DrawingEntity> observeById(long id);

    @Query("SELECT * FROM drawings WHERE id IN (:ids)")
    List<DrawingEntity> findByIdsSync(List<Long> ids);
}
