package com.filipnowakdev.gps_offline_tracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.viewmodels.TrackPlotViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.List;

public class TrackPlotFragment extends Fragment
{

    private TrackPlotViewModel viewModel;
    private LineChart plot;
    private long trackId;

    public static Fragment newInstance(long trackId)
    {
        TrackPlotFragment f = new TrackPlotFragment();
        f.trackId = trackId;
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.track_plot_fragment, container, false);
        plot = v.findViewById(R.id.track_plot);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TrackPlotViewModel.class);
        viewModel.setTrackById(trackId);

        List<Entry> speedSeries = viewModel.getSpeedInTime();
        LineDataSet dataSet = new LineDataSet(speedSeries, "Speed");
        LineData lineData = new LineData(dataSet);
        plot.setData(lineData);
    }

}
