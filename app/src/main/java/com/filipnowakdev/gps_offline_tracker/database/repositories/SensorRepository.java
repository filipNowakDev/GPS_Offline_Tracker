package com.filipnowakdev.gps_offline_tracker.database.repositories;

import android.app.Application;

import com.filipnowakdev.gps_offline_tracker.database.daos.SensorDao;
import com.filipnowakdev.gps_offline_tracker.database.db.TrackDatabase;
import com.filipnowakdev.gps_offline_tracker.database.entities.Sensor;
import com.filipnowakdev.gps_offline_tracker.database.entities.Track;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SensorRepository
{
    private final SensorDao sensorDao;

    public SensorRepository(Application app)
    {
        this.sensorDao = TrackDatabase.getInstance(app).sensorDao();
    }


    public void insert(Sensor sensor)
    {
        Executors.newSingleThreadExecutor().execute(() -> sensorDao.insert(sensor));
    }

    public boolean exists(Sensor sensor)
    {

        Callable<List<Sensor>> getCallable = sensorDao::getAll;
        List<Sensor> sensors = null;

        Future<List<Sensor>> future = Executors.newSingleThreadExecutor().submit(getCallable);
        try
        {
            sensors = future.get();
        } catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }
        return sensors != null && sensors.contains(sensor);
    }
}
