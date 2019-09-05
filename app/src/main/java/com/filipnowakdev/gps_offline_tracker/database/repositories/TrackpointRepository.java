package com.filipnowakdev.gps_offline_tracker.database.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.filipnowakdev.gps_offline_tracker.database.daos.TrackpointDao;
import com.filipnowakdev.gps_offline_tracker.database.db.TrackDatabase;
import com.filipnowakdev.gps_offline_tracker.database.entities.Trackpoint;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TrackpointRepository
{
    private final TrackpointDao trackpointDao;

    public TrackpointRepository(Application app)
    {
        this.trackpointDao = TrackDatabase.getInstance(app).trackpointDao();
    }

    public LiveData<List<Trackpoint>> getAll()
    {
        return trackpointDao.getAll();
    }

    public LiveData<List<Trackpoint>> getAllByTrackId(long id)
    {
        return trackpointDao.getByTrackId(id);
    }

    public void insert(Trackpoint trackpoint)
    {
        Executors.newSingleThreadExecutor().execute(() -> trackpointDao.insert(trackpoint));
    }

    public LiveData<Trackpoint> getById(int id)
    {
        return trackpointDao.findById(id);
    }

    public void delete(Trackpoint trackpoint)
    {
        Executors.newSingleThreadExecutor().execute(() -> trackpointDao.delete(trackpoint));
    }

    public void update(Trackpoint trackpoint)
    {
        Executors.newSingleThreadExecutor().execute(() -> trackpointDao.update(trackpoint));
    }

    public List<Trackpoint> getAllByTrackIdPOJO(long id)
    {
        Callable<List<Trackpoint>> getCallable = () -> trackpointDao.getByTrackIdPOJO(id);
        List<Trackpoint> trackpoints = null;

        Future<List<Trackpoint>> future = Executors.newSingleThreadExecutor().submit(getCallable);
        try
        {
            trackpoints = future.get();
        } catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }
        return trackpoints;
    }
}
