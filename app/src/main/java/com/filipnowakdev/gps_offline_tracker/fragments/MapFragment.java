package com.filipnowakdev.gps_offline_tracker.fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.filipnowakdev.gps_offline_tracker.BuildConfig;
import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.gpx_utils.DOMGpxReader;
import com.filipnowakdev.gps_offline_tracker.gpx_utils.IGpxFileReader;
import com.filipnowakdev.gps_offline_tracker.interfaces.ToolbarTitleUpdater;

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
    public static final String TRACK_NAME = "TRACK_NAME";
    private boolean trackOverlayMode;
    private String trackOverlaid;

    private MapView map;
    private IMapController mapController;
    private IGpxFileReader gpxFileReader;

    private Marker currentPositionMarker;
    private LocationManager locationManager;

    private ToolbarTitleUpdater toolbarTitleUpdater;

    public MapFragment()
    {
        // Required empty public constructor
    }


    @Override
    public void onAttach(Context context)
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
            trackOverlaid = getArguments().getString(TRACK_NAME);
            toolbarTitleUpdater.updateToolbarTitle(trackOverlaid + " on map");
        }
        initMap(v);
        initTrackOverlay();
        initLocationMarker();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        updateLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        return v;
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
            List<GeoPoint> track = gpxFileReader.getGeoPointsList(trackOverlaid);
            Polyline trackLine = new Polyline(map);
            trackLine.setTitle(trackOverlaid);
            trackLine.setPoints(track);
            map.getOverlays().add(trackLine);
            mapController.setCenter(track.get(track.size()/2));
        }
    }

    private void initLocationMarker()
    {
        currentPositionMarker = new Marker(map);
        currentPositionMarker.setTitle("You are here.");
        currentPositionMarker.setPosition(new GeoPoint(0.0, 0.0));
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


    public void updateLocation(Location location)
    {
        if(location != null)
        {
            GeoPoint curPos = new GeoPoint(location.getLatitude(), location.getLongitude());
            if (!trackOverlayMode)
                mapController.setCenter(curPos);
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