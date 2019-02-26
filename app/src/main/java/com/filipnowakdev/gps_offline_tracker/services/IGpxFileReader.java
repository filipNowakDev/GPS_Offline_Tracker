package com.filipnowakdev.gps_offline_tracker.services;

import org.osmdroid.util.GeoPoint;

import java.util.List;

public interface IGpxFileReader
{
    List<GeoPoint> getListOfTrackpoints(String filename);
}
