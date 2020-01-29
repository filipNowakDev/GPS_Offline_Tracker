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
    private static final double MIN_MOVEMENT_SPEED = 3.0;

    private final TrackpointRepository trackpointRepository;
    private final TrackRepository trackRepository;
    private Track track;
    private List<Trackpoint> trackpoints;
    private double distance;
    private double avgMoveSpeed;
    private double maxSpeed = 666;
    private double avgMetersPerSecond;
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

        distance = 0;

        double moveDistance = 0;
        double moveDuration = 0;
        maxSpeed = 0;

        if (trackpoints.size() >= 2)
        {
            for (int i = 0; i < trackpoints.size() - 1; i++)
            {


                double distanceDiff = trackpoints.get(i).distanceTo(trackpoints.get(i + 1));
                double timeDiff = (trackpoints.get(i + 1).time - trackpoints.get(i).time) / 1000.0;
                System.out.println("distdiff: " + distanceDiff + " timediff: " + timeDiff + " time: " + trackpoints.get(i).time);
                double momentSpeed = distanceDiff / timeDiff;

                if (momentSpeed > maxSpeed && momentSpeed != Double.POSITIVE_INFINITY)
                    maxSpeed = momentSpeed;

                if (momentSpeed > MIN_MOVEMENT_SPEED && momentSpeed != Double.POSITIVE_INFINITY)
                {
                    moveDuration += timeDiff;
                    moveDistance += distanceDiff;
                }
                distance += distanceDiff;
            }
            long duration = trackpoints.get(trackpoints.size() - 1).time - trackpoints.get(0).time;
            long allSecondsDuration = duration / 1000;
            hours = (duration / 3600000);
            duration -= hours * 3600000;
            minutes = duration / 60000;
            duration -= minutes * 60000;
            seconds = duration / 1000;

            avgMetersPerSecond = distance / allSecondsDuration;
            avgMoveSpeed = Double.valueOf(moveDistance / moveDuration).isNaN() ? 0 : moveDistance / moveDuration;

            System.out.println("duration: "+ duration + " maxspeed: " + maxSpeed);
        }
        else
        {
            hours = minutes = seconds = 0;
            maxSpeed = 0;
            distance = 0;
            avgMetersPerSecond = 0;
            avgMoveSpeed = 0;
        }
    }

    public double getDistance()
    {
        return distance;
    }

    public double getAvgMoveSpeed()
    {
        return avgMoveSpeed;
    }

    public double getMaxSpeed()
    {
        return maxSpeed;
    }

    public double getAvgMetersPerSecond()
    {
        return avgMetersPerSecond;
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
        return track.getName();
    }
}
