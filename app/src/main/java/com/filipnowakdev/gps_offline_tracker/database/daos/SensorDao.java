package com.filipnowakdev.gps_offline_tracker.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.filipnowakdev.gps_offline_tracker.database.entities.Sensor;

import java.util.List;

@Dao
public interface SensorDao
{
    @Query("SELECT * FROM sensor")
    List<Sensor> getAll();

    @Query("SELECT * FROM sensor LIMIT 1")
    List<Sensor> getFirst();

    @Query("SELECT * FROM sensor WHERE isDefault == 1")
    Sensor getDefault();

    @Insert
    void insert(Sensor sensor);

    @Delete
    void delete(Sensor sensor);

    @Update
    void update(Sensor sensor);
}
