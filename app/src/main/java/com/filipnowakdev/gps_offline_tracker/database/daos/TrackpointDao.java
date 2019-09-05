package com.filipnowakdev.gps_offline_tracker.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.filipnowakdev.gps_offline_tracker.database.entities.Trackpoint;

import java.util.List;

@Dao
public interface TrackpointDao
{

    @Query("SELECT * FROM trackpoint")
    LiveData<List<Trackpoint>> getAll();

    @Query("SELECT * FROM trackpoint WHERE id IN (:trackpointIds)")
    LiveData<List<Trackpoint>> loadAllByIds(int[] trackpointIds);

    @Query("SELECT trackpoint.* FROM trackpoint JOIN track ON trackId == track.id WHERE trackId == :id ORDER BY sequenceNumber ASC")
    LiveData<List<Trackpoint>> getByTrackId(long id);

    @Query("SELECT trackpoint.* FROM trackpoint JOIN track ON trackId == track.id WHERE trackId == :id ORDER BY sequenceNumber ASC")
    List<Trackpoint> getByTrackIdPOJO(long id);

    @Query("SELECT * FROM trackpoint WHERE id = :id")
    LiveData<Trackpoint> findById(int id);

    @Insert
    void insertAll(Trackpoint... trackpoints);

    @Insert
    void insert(Trackpoint trackpoint);

    @Delete
    void delete(Trackpoint trackpoint);

    @Update
    void update(Trackpoint trackpoint);
}
