package com.filipnowakdev.gps_offline_tracker.fragments;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.gpx_utils.DOMGpxReader;
import com.filipnowakdev.gps_offline_tracker.gpx_utils.IGpxFileReader;
import com.filipnowakdev.gps_offline_tracker.interfaces.ToolbarTitleUpdater;

import java.util.List;


public class TrackDetailsFragment extends Fragment
{
    public static final String TRACK_NAME = "track_name";
    private static final double MIN_MOVEMENT_SPEED = 3.0;

    private String track;
    private IGpxFileReader gpxFileReader;
    private ToolbarTitleUpdater toolbarTitleUpdater;

    public TrackDetailsFragment()
    {
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        toolbarTitleUpdater = (ToolbarTitleUpdater) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            track = getArguments().getString(TRACK_NAME);
            toolbarTitleUpdater.updateToolbarTitle(getString(R.string.title_track_details, track));
        }
        gpxFileReader = new DOMGpxReader(getContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_track_details, container, false);
        initFields(v);
        return v;
    }

    private void initFields(View v)
    {
        TextView distanceView = v.findViewById(R.id.distance_field);
        TextView durationView = v.findViewById(R.id.duration_field);
        TextView avgSpeedView = v.findViewById(R.id.avg_speed_field);
        TextView maxSpeedView = v.findViewById(R.id.max_speed_field);
        TextView avgMovSpeedView = v.findViewById(R.id.avg_mov_speed_field);
        double distance = 0;
        long duration;
        double avgMoveSpeed;

        double moveDistance = 0;
        double moveDuration = 0;
        double maxSpeed = 0;

        List<Location> trackpoints = gpxFileReader.getLocationList(track);

        for (int i = 0; i < trackpoints.size() - 1; i++)
        {
            double distanceDiff = trackpoints.get(i).distanceTo(trackpoints.get(i + 1));
            double timeDiff = (trackpoints.get(i + 1).getTime() - trackpoints.get(i).getTime()) / 1000.0;

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

        duration = trackpoints.get(trackpoints.size() - 1).getTime() - trackpoints.get(0).getTime();
        long allSecondsDuration = duration / 1000;
        long hours = (duration / 3600000);
        duration -= hours * 3600000;
        long minutes = duration / 60000;
        duration -= minutes * 60000;
        long seconds = duration / 1000;

        double avgMetersPerSecond = distance / allSecondsDuration;
        avgMoveSpeed = moveDistance / moveDuration;


        if (distance >= 1000.0)
            distanceView.setText(getString(R.string.kilometers, distance / 1000));
        else
            distanceView.setText(getString(R.string.meters, distance));

        avgSpeedView.setText(getString(R.string.kilometers_per_hour, avgMetersPerSecond * 3.6));
        maxSpeedView.setText(getString(R.string.kilometers_per_hour, maxSpeed * 3.6));
        durationView.setText(getString(R.string.duration_field, hours, minutes, seconds));
        avgMovSpeedView.setText(getString(R.string.kilometers_per_hour, avgMoveSpeed * 3.6));
    }

}
