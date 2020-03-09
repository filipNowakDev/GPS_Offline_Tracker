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
    private String xAxisMode;
    private String yAxisMode;

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

    public void setPlotMode(String xAxisMode, String yAxisMode)
    {
        this.xAxisMode = xAxisMode;
        this.yAxisMode = yAxisMode;
    }

    public List<Entry> getEntries()
    {
        LinkedList<Entry> entries = new LinkedList<>();
        for (Trackpoint trackpoint : trackpoints)
        {
            float xValue = getXValue(trackpoint);
            float yValue = getYValue(trackpoint);
            entries.add(new Entry(xValue, yValue));
        }
        return entries;
    }

    private float getXValue(Trackpoint trackpoint)
    {
        float xValue;
        switch (xAxisMode)
        {
            case "distance":
                xValue = (float)trackpoint.distanceFromStart;
                break;
            case "time":
                xValue = trackpoint.timeFromStart / 1000.0f;
                break;
            default:
                xValue = 0;
        }
        return xValue;
    }

    private float getYValue(Trackpoint trackpoint)
    {
        float yValue;
        switch (yAxisMode)
        {
            case "speed":
                yValue = (float)trackpoint.speed;
                break;
            case "bpm":
                yValue = trackpoint.bpm;
                break;
            default:
                yValue = 0;
        }
        return yValue;
    }
}
