package com.filipnowakdev.gps_offline_tracker.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.interfaces.ToolbarTitleUpdater;
import com.filipnowakdev.gps_offline_tracker.viewmodels.TrackDetailsViewModel;

import java.util.Objects;


public class TrackDetailsFragment extends Fragment
{
    static final String TRACK_ID = "track_id";
    private static final double MPS_TO_KPH = 3.6;

    private ToolbarTitleUpdater toolbarTitleUpdater;
    private TextView distanceView;
    private TextView durationView;
    private TextView avgSpeedView;
    private TextView maxSpeedView;
    private TextView avgMovSpeedView;
    private SharedPreferences sharedPreferences;
    private TrackDetailsViewModel viewModel;
    private long trackId;

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

    public static Fragment newInstance(long trackId)
    {
        TrackDetailsFragment f = new TrackDetailsFragment();
        f.trackId = trackId;
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            trackId = getArguments().getLong(TRACK_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_track_details, container, false);
        initFields(v);
        return v;
    }

    private void setFieldsValues()
    {

        double distance = viewModel.getDistance();
        double avgMetersPerSecond = viewModel.getAvgMetersPerSecond();
        double maxSpeed = viewModel.getMaxSpeed();
        double avgMoveSpeed = viewModel.getAvgMoveSpeed();
        long hours = viewModel.getHours();
        long minutes = viewModel.getMinutes();
        long seconds = viewModel.getSeconds();

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
        } else if (Objects.equals(speedFormat, getString(R.string.meters_per_second)))
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(TrackDetailsViewModel.class);
        viewModel.setTrackById(trackId);
        toolbarTitleUpdater.updateToolbarTitle(getString(R.string.title_track_details, viewModel.getTrackName()));
        setFieldsValues();
    }

}
