package com.filipnowakdev.gps_offline_tracker.database.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.filipnowakdev.gps_offline_tracker.database.daos.TrackDao;
import com.filipnowakdev.gps_offline_tracker.database.daos.TrackpointDao;
import com.filipnowakdev.gps_offline_tracker.database.db.TrackDatabase;
import com.filipnowakdev.gps_offline_tracker.database.entities.Track;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TrackRepository
{
    private final TrackDao trackDao;
    private final TrackpointDao trackpointDao;
    private final LiveData<List<Track>> allTracks;


    public TrackRepository(Application app)
    {
        this.trackDao = TrackDatabase.getInstance(app).trackDao();
        this.trackpointDao = TrackDatabase.getInstance(app).trackpointDao();
        allTracks = trackDao.getAll();
    }

    public LiveData<List<Track>> getAll()
    {
        return allTracks;
    }

    public void insert(Track category)
    {
        Executors.newSingleThreadExecutor().execute(() -> trackDao.insert(category));
    }

    public LiveData<Track> getById(long id)
    {
        return trackDao.findById(id);
    }

    public Track getByIdPOJO(long id)
    {
        Callable<Track> getCallable = () -> trackDao.findByIdPOJO(id);
        Track track = null;

        Future<Track> future = Executors.newSingleThreadExecutor().submit(getCallable);
        try
        {
            track = future.get();
        } catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }
        return track;
    }

    public void delete(Track track)
    {
        Executors.newSingleThreadExecutor().execute(() -> {
            trackDao.delete(track);
            trackpointDao.deleteByTrackId(track.id);
        });
    }

    public void update(Track track)
    {
        Executors.newSingleThreadExecutor().execute(() -> trackDao.update(track));
    }
}
