package com.filipnowakdev.gps_offline_tracker.fragments;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.services.DOMGpxReader;
import com.filipnowakdev.gps_offline_tracker.services.IGpxFileReader;

import org.osmdroid.util.GeoPoint;

import java.util.List;


public class TrackDetailsFragment extends Fragment
{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TRACK_NAME = "track_name";

    private String track;
    private IGpxFileReader gpxFileReader;
    private TextView distanceView;
    private TextView durationView;
    private TextView avgSpeedView;

    public TrackDetailsFragment()
    {
    }

    public static TrackDetailsFragment newInstance(String filename)
    {
        TrackDetailsFragment fragment = new TrackDetailsFragment();
        Bundle args = new Bundle();
        args.putString(TRACK_NAME, filename);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            track = getArguments().getString(TRACK_NAME);
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
        distanceView = v.findViewById(R.id.distance_field);
        durationView = v.findViewById(R.id.duration_field);
        avgSpeedView = v.findViewById(R.id.avg_speed_field);
        double distance = 0;
        long duration;
        List<Location> trackpoints = gpxFileReader.getLocationList(track);

        for (int i = 0; i < trackpoints.size() - 1; i++)
        {
            distance += trackpoints.get(i).distanceTo(trackpoints.get(i + 1));
        }

        duration = trackpoints.get(trackpoints.size() - 1).getTime() - trackpoints.get(0).getTime();
        long allSecondsDuration = duration / 1000;
        long hours = (duration / 3600000);
        duration -= hours * 3600000;
        long minutes = duration / 60000;
        duration -= minutes * 60000;
        long seconds = duration / 1000;

        Double avgMetersPerSecond = distance / allSecondsDuration;

        if (distance >= 1000.0)
            distanceView.setText(getString(R.string.kilometers, distance / 1000));
        else
            distanceView.setText(getString(R.string.meters, distance));

        avgSpeedView.setText(getString(R.string.meters_per_second, avgMetersPerSecond));
        durationView.setText(getString(R.string.duration_field, hours, minutes, seconds));
    }


}
