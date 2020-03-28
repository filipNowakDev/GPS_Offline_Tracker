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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.filipnowakdev.gps_offline_tracker.ble_utils.SensorManager;
import com.filipnowakdev.gps_offline_tracker.database.db.TrackDatabase;
import com.filipnowakdev.gps_offline_tracker.track_utils.TrackRecordingManager;

public class LocationService extends Service implements LocationListener, SensorManager.OnSensorInteractionCallback
{
    private LocationManager locationManager;

    private boolean isRecording;
    private IBinder binder = new LocationServiceBinder();
    private int minDistanceChange;
    private int minUpdateInterval;
    private boolean useBpmSensor;
    private TrackDatabase db;
    private TrackRecordingManager trackRecordingManager;
    private SensorManager sensorManager;
    private int currentBpm;

    public boolean isRecordingActive()
    {
        return isRecording;
    }

    @SuppressLint("MissingPermission")
    public void startRecording()
    {
        if (db != null)
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

            if(sharedPreferences.getBoolean("use_bpm_sensor", false) != useBpmSensor)
                useBpmSensor = sharedPreferences.getBoolean("use_bpm_sensor", false);
            currentBpm = 0;
            if (useBpmSensor == false)
            {
                trackRecordingManager.createNewTrack();
                isRecording = true;
                Toast.makeText(this, "Recording started.", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "Scanning for sensor.", Toast.LENGTH_SHORT).show();
                sensorManager.connectToDefaultSensor();
            }
        }
    }

    public void saveRecording(String name)
    {
        if (isRecording)
        {
            isRecording = false;
            trackRecordingManager.stopRecording(name);
            sensorManager.disconnectDefaultSensor();
            Toast.makeText(this, "Recording saved as " + name + ".", Toast.LENGTH_SHORT).show();
        }
    }

    private void initLocationManager()
    {
        if (locationManager == null)
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
                    useBpmSensor = sharedPreferences.getBoolean("use_bpm_sensor", false);
                } else
                {
                    displayError();
                }
            } catch (SecurityException e)
            {
                displayError();
            }
        }
    }

    private void initTrackRecordingService()
    {
        if (db == null)
            db = TrackDatabase.getLocationServiceInstance(getApplicationContext());
        if (trackRecordingManager == null)
            trackRecordingManager = new TrackRecordingManager(db);
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
        initTrackRecordingService();
        initSensorManager();
        return START_REDELIVER_INTENT;
    }

    private void initSensorManager()
    {
        sensorManager = new SensorManager(getApplicationContext(), db, this);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        locationManager.removeUpdates(this);
        sensorManager.disconnectDefaultSensor();
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
        {
            trackRecordingManager.addTrackpoint(location, currentBpm);
        }
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

    @Override
    public void onSensorConnected()
    {
        trackRecordingManager.createNewTrack();
        isRecording = true;
        System.out.println("Default Sensor Connected.\nRecording started.");
    }

    @Override
    public void onSensorDisconnected()
    {
        System.out.println("Sensor Disconnected.\nRecording started.");
    }

    @Override
    public void onBpmUpdate(int bpm)
    {
        this.currentBpm = bpm;
    }

    public class LocationServiceBinder extends Binder
    {
        public LocationService getService()
        {
            return LocationService.this;
        }
    }
}
