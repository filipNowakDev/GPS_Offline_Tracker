package com.filipnowakdev.gps_offline_tracker.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.filipnowakdev.gps_offline_tracker.database.entities.Track;
import com.filipnowakdev.gps_offline_tracker.database.entities.Trackpoint;
import com.filipnowakdev.gps_offline_tracker.database.repositories.TrackRepository;
import com.filipnowakdev.gps_offline_tracker.database.repositories.TrackpointRepository;
import com.github.mikephil.charting.data.Entry;

import java.util.LinkedList;
import java.util.List;

public class TrackPlotViewModel extends AndroidViewModel
{
    private final TrackRepository trackRepository;
    private final TrackpointRepository trackpointRepository;
    private Track track;
    private List<Trackpoint> trackpoints;

    public TrackPlotViewModel(@NonNull Application application)
    {
        super(application);
        trackRepository = new TrackRepository(application);
        trackpointRepository = new TrackpointRepository(application);
    }

    public void setTrackById(long id)
    {
        track = trackRepository.getByIdPOJO(id);
        trackpoints = trackpointRepository.getAllByTrackIdPOJO(id);
        if (track == null || trackpoints == null)
            System.out.println("Error getting track.");
    }

    public List<Entry> getSpeedInTime()
    {
        LinkedList<Entry> vals = new LinkedList<>();
        float currentTime  = 0;
        for (int i = 0; i < trackpoints.size() - 1; i++)
        {

            double distanceDiff = trackpoints.get(i).distanceTo(trackpoints.get(i + 1));
            double timeDiff = (trackpoints.get(i + 1).time - trackpoints.get(i).time) / 1000.0;
            double momentSpeed = distanceDiff / timeDiff;
            vals.add(new Entry(currentTime, (float) momentSpeed));
            currentTime += timeDiff;
        }
        return vals;
    }

    // TODO: Implement the ViewModel
}
