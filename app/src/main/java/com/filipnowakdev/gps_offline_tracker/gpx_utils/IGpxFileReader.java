package com.filipnowakdev.gps_offline_tracker.gpx_utils;

import android.location.Location;

import org.osmdroid.util.GeoPoint;

import java.util.List;

public interface IGpxFileReader
{
    List<GeoPoint> getGeoPointsList(String filename);

    List<Location> getLocationList(String filename);

}
