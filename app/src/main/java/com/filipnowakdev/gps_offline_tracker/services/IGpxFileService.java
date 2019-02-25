package com.filipnowakdev.gps_offline_tracker.services;


import android.location.Location;

import java.io.File;
import java.util.List;

public interface IGpxFileService
{
    void createNewTrack();

    void addNewTrackpoint(Location location);

    void saveTrackAsFile(String filename);

    List<File> getListOfFiles();

}
