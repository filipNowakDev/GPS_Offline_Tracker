package com.filipnowakdev.gps_offline_tracker.fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.filipnowakdev.gps_offline_tracker.BuildConfig;
import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.gpx_utils.DOMGpxReader;
import com.filipnowakdev.gps_offline_tracker.gpx_utils.IGpxFileReader;
import com.filipnowakdev.gps_offline_tracker.interfaces.ToolbarTitleUpdater;
import com.filipnowakdev.gps_offline_tracker.viewmodels.MapViewModel;
import com.filipnowakdev.gps_offline_tracker.viewmodels.TrackListViewModel;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.List;


public class MapFragment extends Fragment implements LocationListener
{
    static final String TRACK_ID = "TRACK_ID";
    private boolean trackOverlayMode;
    private boolean followMode;
    private boolean positionInitialised = false;
    private long trackId;

    private MapView map;
    private IMapController mapController;
    private IGpxFileReader gpxFileReader;
    private ImageButton followModeButton;

    private Marker currentPositionMarker;
    private LocationManager locationManager;

    private ToolbarTitleUpdater toolbarTitleUpdater;
    private MapViewModel viewModel;

    public MapFragment()
    {
        // Required empty public constructor
    }


    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        toolbarTitleUpdater = (ToolbarTitleUpdater) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        gpxFileReader = new DOMGpxReader(getContext());
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
    }


    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View v = inflater.inflate(R.layout.fragment_map, container, false);
        if (getArguments() != null)
        {
            trackOverlayMode = true;
            followMode = false;
            positionInitialised = true;
            trackId = getArguments().getLong(TRACK_ID);
        } else
        {
            trackOverlayMode = false;
            followMode = true;
        }
        initFollowButton(v);
        initMap(v);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(MapViewModel.class);
        viewModel.setTrackById(trackId);
        initTrackOverlay();
        initLocationMarker();

    }


    private void initFollowButton(View v)
    {
        followModeButton = v.findViewById(R.id.follow_mode_button);
        followModeButton.setOnClickListener(view ->
        {
            followMode = !followMode;
            if (followMode)
            {
                mapController.animateTo(currentPositionMarker.getPosition());
                followModeButton.setImageResource(R.drawable.ic_gps_not_fixed_black_24dp);
            } else
                followModeButton.setImageResource(R.drawable.ic_gps_fixed_black_24dp);

        });
        if (followMode)
            followModeButton.setImageResource(R.drawable.ic_gps_not_fixed_black_24dp);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        locationManager.removeUpdates(this);

    }

    @Override
    public void onPause()
    {
        super.onPause();
        map.onPause();
    }


    private void initTrackOverlay()
    {
        if (trackOverlayMode)
        {
            List<GeoPoint> track = viewModel.getGeoPoints();
            Polyline trackLine = new Polyline(map);
            trackLine.setTitle(viewModel.getTrackName());
            trackLine.setPoints(track);
            map.getOverlays().add(trackLine);
            mapController.setCenter(track.get(track.size() / 2));
            toolbarTitleUpdater.updateToolbarTitle(getString(R.string.title_track_details, viewModel.getTrackName()));
        }
    }

    private void initLocationMarker()
    {
        currentPositionMarker = new Marker(map);
        currentPositionMarker.setTitle("You are here.");
        GeoPoint startPoint = new GeoPoint(0.0, 0.0);
        currentPositionMarker.setPosition(startPoint);
        currentPositionMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(currentPositionMarker);
        map.invalidate();
    }

    private void initMap(View v)
    {
        map = v.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(15.0);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        map.onResume();
    }


    private void updateLocation(Location location)
    {
        if (location != null)
        {
            GeoPoint curPos = new GeoPoint(location.getLatitude(), location.getLongitude());
            if (followMode)
            {
                if (positionInitialised)
                    mapController.animateTo(curPos);
                else
                {
                    positionInitialised = true;
                    mapController.setCenter(curPos);
                }
            }
            currentPositionMarker.setPosition(curPos);
            map.invalidate();
        }
    }


    @Override
    public void onLocationChanged(Location location)
    {
        updateLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {

    }

    @Override
    public void onProviderEnabled(String provider)
    {

    }

    @Override
    public void onProviderDisabled(String provider)
    {

    }
}