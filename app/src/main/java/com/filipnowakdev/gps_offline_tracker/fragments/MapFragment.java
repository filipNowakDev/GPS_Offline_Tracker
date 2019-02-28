package com.filipnowakdev.gps_offline_tracker.fragments;


import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.services.DOMGpxReader;
import com.filipnowakdev.gps_offline_tracker.services.IGpxFileReader;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.List;


public class MapFragment extends Fragment
{
    private static final String TRACK_ARG = "TRACK_ARG";
    private boolean trackOverlayMode;
    private String trackOverlaid;

    private MapView map;
    private IMapController mapController;
    private Location lastLocation;
    private IGpxFileReader gpxFileReader;

    private Marker currentPositionMarker;

    public MapFragment()
    {
        // Required empty public constructor
    }

    public static MapFragment newInstance(String trackFilename)
    {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(TRACK_ARG, trackFilename);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        gpxFileReader = new DOMGpxReader(getContext());
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View v = inflater.inflate(R.layout.fragment_map, container, false);
        if (getArguments() != null)
        {
            trackOverlayMode = true;
            trackOverlaid = getArguments().getString(TRACK_ARG);
        }
        initMap(v);
        initTrackOverlay();
        initLocationMarker();
        return v;
    }


    @Override
    public void onPause()
    {
        super.onPause();
        map.onPause();
    }


    private void initTrackOverlay()
    {
        if(trackOverlayMode)
        {
            List<GeoPoint> track = gpxFileReader.getGeoPointsList(trackOverlaid);
            Polyline trackLine = new Polyline(map);
            trackLine.setTitle(trackOverlaid);
            trackLine.setPoints(track);
            map.getOverlays().add(trackLine);
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
        updateLocation();
    }


    public void updateLocation(Location location)
    {
        this.lastLocation = location;
        GeoPoint curPos = new GeoPoint(location.getLatitude(), location.getLongitude());
        mapController.setCenter(curPos);
        currentPositionMarker.setPosition(curPos);
        map.invalidate();
    }


    public void updateLocation()
    {
        if (lastLocation != null)
        {
            updateLocation(lastLocation);
        }
    }

    public void setLastLocation(Location lastLocation)
    {
        this.lastLocation = lastLocation;
    }

}
