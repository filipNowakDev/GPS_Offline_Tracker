package com.filipnowakdev.gps_offline_tracker.database.daos;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.filipnowakdev.gps_offline_tracker.database.entities.Track;

import java.util.List;

@Dao
public interface TrackDao
{
    @Query("SELECT * FROM track WHERE name <> \"com.filipnowakdev.__temp\"")
    LiveData<List<Track>> getAll();

    @Query("SELECT * FROM track WHERE id IN (:trackIds)")
    LiveData<List<Track>> loadAllByIds(int[] trackIds);

    @Query("SELECT * FROM track WHERE id = :id")
    LiveData<Track> findById(long id);

    @Query("SELECT * FROM track WHERE id = :id")
    Track findByIdPOJO(long id);

    @Insert
    void insertAll(Track... tracks);

    @Insert
    long insert(Track track);

    @Delete
    void delete(Track track);

    @Update
    void update(Track track);
}
