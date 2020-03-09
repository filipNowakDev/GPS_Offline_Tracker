package com.filipnowakdev.gps_offline_tracker.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.filipnowakdev.gps_offline_tracker.database.entities.Track;
import com.filipnowakdev.gps_offline_tracker.database.entities.Trackpoint;
import com.filipnowakdev.gps_offline_tracker.database.repositories.TrackRepository;
import com.filipnowakdev.gps_offline_tracker.database.repositories.TrackpointRepository;

import java.util.List;

public class TrackDetailsViewModel extends AndroidViewModel
{
    private static final double MIN_MOVEMENT_SPEED = 1.5;

    private final TrackpointRepository trackpointRepository;
    private final TrackRepository trackRepository;
    private Track track;
    private List<Trackpoint> trackpoints;
    private double avgMoveSpeed;
    private long seconds;
    private long minutes;
    private long hours;

    public TrackDetailsViewModel(@NonNull Application application)
    {
        super(application);
        trackRepository = new TrackRepository(application);
        trackpointRepository = new TrackpointRepository(application);
    }

    public void setTrackById(long id)
    {
        track = trackRepository.getByIdPOJO(id);
        trackpoints = trackpointRepository.getAllByTrackIdPOJO(id);
        if (track != null && trackpoints != null)
            calculateData();
        else
            System.out.println("Error getting track.");
    }

    private void calculateData()
    {

        double moveDistance = 0;
        double moveDuration = 0;

        if (trackpoints.size() >= 2)
        {
            for (int i = 0; i < trackpoints.size() - 1; i++)
            {
                double distanceDiff = trackpoints.get(i).distanceTo(trackpoints.get(i + 1));
                double timeDiff = (trackpoints.get(i + 1).time - trackpoints.get(i).time) / 1000.0;

                double momentSpeed = trackpoints.get(i).speed;
                if (momentSpeed > MIN_MOVEMENT_SPEED && momentSpeed != Double.POSITIVE_INFINITY)
                {
                    moveDuration += timeDiff;
                    moveDistance += distanceDiff;
                }
            }

            hours = (track.duration / 3600000);
            track.duration -= hours * 3600000;
            minutes = track.duration / 60000;
            track.duration -= minutes * 60000;
            seconds = track.duration / 1000;
            avgMoveSpeed = Double.valueOf(moveDistance / moveDuration).isNaN() ? 0 : moveDistance / moveDuration;
        }
        else
        {
            hours = minutes = seconds = 0;
            avgMoveSpeed = 0;
        }
    }

    public double getDistance()
    {
        return track.distance;
    }

    public double getAvgMoveSpeed()
    {
        return avgMoveSpeed;
    }

    public double getMaxSpeed()
    {
        return track.maxSpeed;
    }

    public double getAvgMetersPerSecond()
    {
        return track.avgSpeed;
    }

    public long getSeconds()
    {
        return seconds;
    }

    public long getMinutes()
    {
        return minutes;
    }

    public long getHours()
    {
        return hours;
    }

    public String getTrackName()
    {
        return track.name;
    }
}
