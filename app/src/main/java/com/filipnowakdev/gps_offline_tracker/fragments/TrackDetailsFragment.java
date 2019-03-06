package com.filipnowakdev.gps_offline_tracker.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.gpx_utils.DOMGpxReader;
import com.filipnowakdev.gps_offline_tracker.gpx_utils.IGpxFileReader;
import com.filipnowakdev.gps_offline_tracker.interfaces.ToolbarTitleUpdater;

import java.util.List;
import java.util.Objects;


public class TrackDetailsFragment extends Fragment
{
    static final String TRACK_NAME = "track_name";
    private static final double MIN_MOVEMENT_SPEED = 3.0;
    private static final double MPS_TO_KPH = 3.6;

    private String track;
    private IGpxFileReader gpxFileReader;
    private ToolbarTitleUpdater toolbarTitleUpdater;
    private TextView distanceView;
    private TextView durationView;
    private TextView avgSpeedView;
    private TextView maxSpeedView;
    private TextView avgMovSpeedView;
    private SharedPreferences sharedPreferences;

    public TrackDetailsFragment()
    {
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        toolbarTitleUpdater = (ToolbarTitleUpdater) context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

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
        calculateData();
        return v;
    }

    private void calculateData()
    {

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


        setFieldsValues(distance, avgMoveSpeed, maxSpeed, hours, minutes, seconds, avgMetersPerSecond);
    }

    private void setFieldsValues(double distance, double avgMoveSpeed, double maxSpeed, long hours, long minutes, long seconds, double avgMetersPerSecond)
    {


        if (distance >= 1000.0)
            distanceView.setText(getString(R.string.kilometers, distance / 1000));
        else
            distanceView.setText(getString(R.string.meters, distance));

        String speedFormat = sharedPreferences.getString("unit_speed", getString(R.string.kilometers_per_hour));
        if (Objects.equals(speedFormat, getString(R.string.kilometers_per_hour)))
        {
            avgMetersPerSecond *= MPS_TO_KPH;
            maxSpeed *= MPS_TO_KPH;
            avgMoveSpeed *= MPS_TO_KPH;
            avgSpeedView.setText(getString(R.string.kilometers_per_hour, avgMetersPerSecond));
            maxSpeedView.setText(getString(R.string.kilometers_per_hour, maxSpeed));
            avgMovSpeedView.setText(getString(R.string.kilometers_per_hour, avgMoveSpeed));
        }
        else if (Objects.equals(speedFormat, getString(R.string.meters_per_second)))
        {
            avgSpeedView.setText(getString(R.string.meters_per_second, avgMetersPerSecond));
            maxSpeedView.setText(getString(R.string.meters_per_second, maxSpeed));
            avgMovSpeedView.setText(getString(R.string.meters_per_second, avgMoveSpeed));
        }

        durationView.setText(getString(R.string.duration_field, hours, minutes, seconds));

    }

    private void initFields(View v)
    {
        distanceView = v.findViewById(R.id.distance_field);
        durationView = v.findViewById(R.id.duration_field);
        avgSpeedView = v.findViewById(R.id.avg_speed_field);
        maxSpeedView = v.findViewById(R.id.max_speed_field);
        avgMovSpeedView = v.findViewById(R.id.avg_mov_speed_field);
    }

}
