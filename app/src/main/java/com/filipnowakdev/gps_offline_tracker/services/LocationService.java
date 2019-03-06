package com.filipnowakdev.gps_offline_tracker.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import android.widget.Toast;

import com.filipnowakdev.gps_offline_tracker.gpx_utils.FileWriterGpxFileService;
import com.filipnowakdev.gps_offline_tracker.gpx_utils.IGpxFileService;

public class LocationService extends Service implements LocationListener
{


    private LocationManager locationManager;

    private IGpxFileService gpxFileService;
    private boolean isRecording;
    private IBinder binder = new LocationServiceBinder();
    private int minDistanceChange;
    private int minUpdateInterval;

    public boolean isRecordingActive()
    {
        return isRecording;
    }

    @SuppressLint("MissingPermission")
    public void startRecording()
    {
        if (gpxFileService != null)
        {
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(this);
            if (sharedPreferences.getInt("gps_min_distance", 10) != minDistanceChange
                    || (sharedPreferences.getInt("gps_min_interval", 5) * 1000) != minUpdateInterval)
            {
                //meters
                minDistanceChange = sharedPreferences.getInt("gps_min_distance", 10);
                //milliseconds
                minUpdateInterval = sharedPreferences.getInt("gps_min_interval", 5) * 1000;
                locationManager.removeUpdates(this);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minUpdateInterval, minDistanceChange, this);
            }

            System.out.println("[DEBUG] : starting with interval " + minUpdateInterval + " ms");
            gpxFileService.createNewTrack();
            gpxFileService.addNewTrackpoint(getLocation());
            isRecording = true;
            Toast.makeText(this, "Recording started.", Toast.LENGTH_SHORT).show();
        }
    }

    public void saveRecording(String filename)
    {
        if (isRecording)
        {
            isRecording = false;
            gpxFileService.saveTrackAsFile(filename);
            Toast.makeText(this, "Recording saved as " + filename + ".gpx", Toast.LENGTH_SHORT).show();

        }
    }

    private void initLocationManager()
    {
        try
        {


            this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(this);
                //meters
                minDistanceChange = sharedPreferences.getInt("gps_min_distance", 10);
                //milliseconds
                minUpdateInterval = sharedPreferences.getInt("gps_min_interval", 5) * 1000;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minUpdateInterval, minDistanceChange, this);
            } else
            {
                displayError();
            }
        } catch (SecurityException e)
        {
            displayError();
        }
    }

    private void initGpxManager()
    {
        gpxFileService = new FileWriterGpxFileService(this);
    }

    private void displayError()
    {
        Toast toast = Toast.makeText(this, "Location unavailable.", Toast.LENGTH_LONG);
        toast.show();
    }

    public Location getLocation()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return null;
        }

        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        initLocationManager();
        initGpxManager();
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public void onLocationChanged(Location location)
    {
        if (isRecording)
            gpxFileService.addNewTrackpoint(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle)
    {
    }

    @Override
    public void onProviderEnabled(String s)
    {
    }

    @Override
    public void onProviderDisabled(String s)
    {
    }

    public class LocationServiceBinder extends Binder
    {
        public LocationService getService()
        {
            return LocationService.this;
        }
    }
}
