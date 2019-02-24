package com.filipnowakdev.gps_offline_tracker.fragments;


import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.filipnowakdev.gps_offline_tracker.R;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;


public class MapFragment extends Fragment
{
    private MapView map;
    private IMapController mapController;
    private Location lastLocation;
    private ArrayList<OverlayItem> overlayItems;
    private ItemizedIconOverlay<OverlayItem> mMyLocationOverlay;
    private Marker currentPositionMarker;

    public MapFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        Context ctx = getActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        return inflater.inflate(R.layout.fragment_map, container, false);
    }


    @Override
    public void onPause()
    {
        super.onPause();
        map.onPause();
    }

    @Override
    public void onStart()
    {
        map = getView().findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.HIKEBIKEMAP);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(15.0);

        currentPositionMarker = new Marker(map);
        currentPositionMarker.setTitle("You are here.");
        currentPositionMarker.setPosition(new GeoPoint(0.0, 0.0));
        currentPositionMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(currentPositionMarker);
        map.invalidate();

        super.onStart();
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
        if(lastLocation != null)
        {
            updateLocation(lastLocation);
        }
    }

    public void setLastLocation(Location lastLocation)
    {
        this.lastLocation = lastLocation;
    }

}
