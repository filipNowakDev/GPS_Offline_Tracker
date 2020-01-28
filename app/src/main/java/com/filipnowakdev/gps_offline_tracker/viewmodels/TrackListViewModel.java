package com.filipnowakdev.gps_offline_tracker.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.filipnowakdev.gps_offline_tracker.database.entities.Track;
import com.filipnowakdev.gps_offline_tracker.database.repositories.TrackRepository;

import java.util.List;

public class TrackListViewModel extends AndroidViewModel
{
    private TrackRepository trackRepository;

    private LiveData<List<Track>> tracks;

    public TrackListViewModel(@NonNull Application application)
    {
        super(application);
        trackRepository = new TrackRepository(application);
        tracks = trackRepository.getAll();
    }

    public LiveData<List<Track>> getTracks()
    {
        return tracks;
    }

    public void deleteTrack(Track track)
    {
        trackRepository.delete(track);
    }
}
