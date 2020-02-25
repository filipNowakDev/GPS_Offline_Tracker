package com.filipnowakdev.gps_offline_tracker.fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.filipnowakdev.gps_offline_tracker.BuildConfig;
import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.interfaces.ToolbarTitleUpdater;
import com.filipnowakdev.gps_offline_tracker.viewmodels.MapViewModel;

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
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View v = inflater.inflate(R.layout.fragment_map, container, false);
        setOverlayMode();
        initFollowButton(v);
        initMap(v);
        initLocationManager();
        return v;
    }

    @SuppressLint("MissingPermission")
    private void initLocationManager()
    {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    private void setOverlayMode()
    {
        if (getArguments() != null)
            turnOnOverlayMode();
        else
            turnOffOverlayMode();
    }

    private void turnOffOverlayMode()
    {
        trackOverlayMode = false;
        followMode = true;
    }

    private void turnOnOverlayMode()
    {
        trackOverlayMode = true;
        followMode = false;
        positionInitialised = true;
        assert getArguments() != null;
        trackId = getArguments().getLong(TRACK_ID);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        initViewModel();
        initTrackOverlay();
        initLocationMarker();

    }

    private void initViewModel()
    {
        viewModel = new ViewModelProvider(this).get(MapViewModel.class);
        viewModel.setTrackById(trackId);
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
            if (!track.isEmpty())
                mapController.setCenter(track.get(track.size() / 2));
            else
                Toast.makeText(this.getContext(), getString(R.string.no_trackpoints_string), Toast.LENGTH_LONG).show();
            toolbarTitleUpdater.updateToolbarTitle(getString(R.string.title_track_details, viewModel.getTrackName()));
        }
    }


    private void initLocationMarker()
    {
        currentPositionMarker = new Marker(map);
        currentPositionMarker.setTitle(getString(R.string.marker_title_string));
        GeoPoint startPoint = new GeoPoint(0.0, 0.0);
        currentPositionMarker.setPosition(startPoint);
        currentPositionMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(currentPositionMarker);
        map.invalidate();
    }

    private void initFollowButton(View v)
    {
        followModeButton = v.findViewById(R.id.follow_mode_button);
        setButtonListener();
        setButtonIcon();
    }

    private void setButtonIcon()
    {
        if (followMode)
            setButtonNoFollowIcon();
        else
            setButtonFollowIcon();
    }

    private void setButtonListener()
    {
        followModeButton.setOnClickListener(view ->
        {
            toggleFollowMode();
            setButtonIcon();
            if (followMode)
                animateMapToCurrentLocation();
        });
    }



    private void setButtonFollowIcon()
    {
        followModeButton.setImageResource(R.drawable.ic_gps_fixed_black_24dp);
    }

    private void setButtonNoFollowIcon()
    {
        followModeButton.setImageResource(R.drawable.ic_gps_not_fixed_black_24dp);
    }

    private void toggleFollowMode()
    {
        followMode = !followMode;
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
            setCurrentPosition(location);
            setMapPosition();
        }
    }

    private void setMapPosition()
    {
        if (followMode)
        {
            if (positionInitialised)
                animateMapToCurrentLocation();
            else
            {
                positionInitialised = true;
                setMapToCurrentPosition();
            }
        }
        map.invalidate();
    }

    private void setCurrentPosition(Location location)
    {
        GeoPoint curPos = new GeoPoint(location.getLatitude(), location.getLongitude());
        currentPositionMarker.setPosition(curPos);
    }

    private void animateMapToCurrentLocation()
    {
        mapController.animateTo(currentPositionMarker.getPosition());
    }

    private void setMapToCurrentPosition()
    {
        mapController.setCenter(currentPositionMarker.getPosition());
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