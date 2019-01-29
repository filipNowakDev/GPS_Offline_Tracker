package com.filipnowakdev.gps_offline_tracker.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

public class LocationService extends Service implements LocationListener
{
	public static final String BROADCAST_LOCATION_UPDATE = "com.filipnowakdev.gps_offline_tracker.LOCATION_UPDATE";

	private static final long MIN_DISTANCE_CHANGE = 10;//meters
	private static final long MIN_UPDATE_INTERVAL = 100; //milliseconds


	private LocationManager locationManager;
	private LocalBroadcastManager localBroadcastManager;

	private IGpxFileService gpxFileService;
	private boolean isRecording;
	private IBinder binder = new LocationServiceBinder();

	public boolean isRecordingActive()
	{
		return isRecording;
	}

	public void startRecording()
	{
		if (gpxFileService != null)
		{
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
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_UPDATE_INTERVAL, MIN_DISTANCE_CHANGE, this);
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
		initLocationManager();
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		super.onStartCommand(intent, flags, startId);
		initBroadcastManager();
		initLocationManager();
		initGpxManager();
		return START_REDELIVER_INTENT;
	}

	private void initBroadcastManager()
	{
		localBroadcastManager = LocalBroadcastManager.getInstance(this);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public void onCreate()
	{
		initLocationManager();
		super.onCreate();
	}

	@Override
	public void onLocationChanged(Location location)
	{
		localBroadcastManager.sendBroadcast(new Intent(BROADCAST_LOCATION_UPDATE));
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
