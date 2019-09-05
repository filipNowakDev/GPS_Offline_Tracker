package com.filipnowakdev.gps_offline_tracker.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.filipnowakdev.gps_offline_tracker.database.entities.Track;
import com.filipnowakdev.gps_offline_tracker.database.entities.Trackpoint;
import com.filipnowakdev.gps_offline_tracker.database.repositories.TrackRepository;
import com.filipnowakdev.gps_offline_tracker.database.repositories.TrackpointRepository;

import org.osmdroid.util.GeoPoint;

import java.util.LinkedList;
import java.util.List;

public class MapViewModel extends AndroidViewModel
{
    private final TrackpointRepository trackpointRepository;
    private final TrackRepository trackRepository;
    private Track track;
    private List<Trackpoint> trackpoints;


    public MapViewModel(@NonNull Application application)
    {
        super(application);
        trackRepository = new TrackRepository(application);
        trackpointRepository = new TrackpointRepository(application);
    }

    public void setTrackById(long id)
    {
        track = trackRepository.getByIdPOJO(id);
        trackpoints = trackpointRepository.getAllByTrackIdPOJO(id);
    }

    public List<Trackpoint> getTrackpoints()
    {
        return trackpoints;
    }

    public String getTrackName()
    {
        return track.getName();
    }

    public List<GeoPoint> getGeoPoints()
    {
        if (trackpoints != null)
        {
            LinkedList<GeoPoint> trackpointList = new LinkedList<>();
            for (Trackpoint trackpoint : trackpoints)
            {
                GeoPoint location = new GeoPoint(trackpoint.latitude, trackpoint.longitude, trackpoint.elevation);
                trackpointList.addLast(location);
            }
            return trackpointList;
        }
        return null;
    }
}
