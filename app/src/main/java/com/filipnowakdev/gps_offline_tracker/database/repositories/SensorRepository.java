package com.filipnowakdev.gps_offline_tracker.database.repositories;

import android.app.Application;

import com.filipnowakdev.gps_offline_tracker.database.daos.SensorDao;
import com.filipnowakdev.gps_offline_tracker.database.db.TrackDatabase;
import com.filipnowakdev.gps_offline_tracker.database.entities.Sensor;

import java.util.LinkedList;
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

    public List<Sensor> getAll()
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
        return sensors == null ? new LinkedList<>() : sensors;
    }


    public void insert(Sensor sensor)
    {
        Executors.newSingleThreadExecutor().execute(() -> sensorDao.insert(sensor));
    }

    public boolean exists(Sensor sensor)
    {
        //TODO optimize this to get result in single query
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

    public boolean isEmpty()
    {
        Callable<List<Sensor>> getCallable = sensorDao::getFirst;
        List<Sensor> sensors = null;

        Future<List<Sensor>> future = Executors.newSingleThreadExecutor().submit(getCallable);
        try
        {
            sensors = future.get();
        } catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }

        return sensors == null || sensors.isEmpty();
    }
}
