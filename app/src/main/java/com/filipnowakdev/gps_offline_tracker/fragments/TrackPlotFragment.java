package com.filipnowakdev.gps_offline_tracker.fragments;

import androidx.lifecycle.ViewModelProvider;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.util.PixelUtils;
import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PanZoom;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.viewmodels.TrackPlotViewModel;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

public class TrackPlotFragment extends Fragment
{

    private TrackPlotViewModel viewModel;
    private XYPlot plot;
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

        XYSeries speedSeries = viewModel.getSpeedSeries();

        LineAndPointFormatter seriesFormat = new LineAndPointFormatter(Color.RED, Color.TRANSPARENT, Color.BLUE, null);
        // just for fun, add some smoothing to the lines:
        // see: http://androidplot.com/smooth-curves-and-androidplot/
        seriesFormat.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        // add a new series' to the xyplot:
        plot.addSeries(speedSeries, seriesFormat);
        PanZoom.attach(plot, PanZoom.Pan.HORIZONTAL, PanZoom.Zoom.STRETCH_HORIZONTAL);

        /*plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                int i = Math.round(((Number) obj).floatValue());
                return toAppendTo.append(domainLabels[i]);
            }
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        });*/
    }

}
