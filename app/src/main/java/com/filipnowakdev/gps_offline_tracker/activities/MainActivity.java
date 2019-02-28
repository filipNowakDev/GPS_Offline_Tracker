package com.filipnowakdev.gps_offline_tracker.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.fragments.HomeFragment;
import com.filipnowakdev.gps_offline_tracker.fragments.MapFragment;
import com.filipnowakdev.gps_offline_tracker.fragments.TracksFragment;
import com.filipnowakdev.gps_offline_tracker.services.LocationService;
import com.filipnowakdev.gps_offline_tracker.services.NotificationService;

public class MainActivity extends AppCompatActivity implements HomeFragment.OnButtonClickListener
{

    private LocationService locationService;
    private boolean isLocationServiceBound;
    private ServiceConnection serviceConnection;
    private LocalBroadcastManager localBroadcastManager;

    private BroadcastReceiver locationBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            updateFragmentsLocation();

        }
    };


    private NotificationService notificationService;

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener
            = item ->
    {
        switch (item.getItemId())
        {
            case R.id.navigation_home:
                return loadFragment(new HomeFragment());
            case R.id.navigation_tracks:
                return loadFragment(new TracksFragment());
            case R.id.navigation_map:
                return loadFragment(new MapFragment());
        }
        return false;
    };


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermissions();
        initNavigation();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        this.unbindService(serviceConnection);
        localBroadcastManager.unregisterReceiver(locationBroadcastReceiver);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        initBroadcastManager();
        initNotificationService();
        initLocationService();
        registerBroadcastReceiver();
        loadFragment(new HomeFragment());
    }

    private void getPermissions()
    {
        while (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]
                            {
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                            },
                    2);
        }

    }

    private void initNavigation()
    {
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
    }

    private boolean loadFragment(Fragment fragment)
    {
        if (fragment != null)
        {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commitAllowingStateLoss();
            return true;
        }
        return false;
    }


    private void initLocationService()
    {
        Intent locationServiceIntent = new Intent(this.getApplicationContext(), LocationService.class);
        this.startService(locationServiceIntent);


        if (serviceConnection == null)
        {
            serviceConnection = new ServiceConnection()
            {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder)
                {
                    LocationService.LocationServiceBinder locationServiceBinder = (LocationService.LocationServiceBinder) iBinder;
                    locationService = locationServiceBinder.getService();
                    isLocationServiceBound = true;
                    updateFragmentsLocation();
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName)
                {
                    isLocationServiceBound = false;
                }
            };
        }
        this.bindService(locationServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void displayError()
    {
        Toast toast = Toast.makeText(this.getApplicationContext(), "Location unavailable.", Toast.LENGTH_LONG);
        toast.show();
    }

    private void initBroadcastManager()
    {
        localBroadcastManager = LocalBroadcastManager.getInstance(this.getApplicationContext());
    }

    private void registerBroadcastReceiver()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(LocationService.BROADCAST_LOCATION_UPDATE);
        localBroadcastManager.registerReceiver(locationBroadcastReceiver, filter);
    }

    private void initNotificationService()
    {
        this.notificationService = new NotificationService(this);
    }

    @Override
    public boolean onStartClick()
    {
        if (isLocationServiceBound)
        {
            if (locationService.getLocation() != null)
            {
                locationService.startRecording();
                notificationService.displayRecordingNotification();
            } else
            {
                displayError();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onEndClick()
    {
        if (isLocationServiceBound)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Track name: ");

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("OK", (dialog, which) ->
            {

                String fileName = input.getText().toString();
                locationService.saveRecording(fileName);
                notificationService.hideRecordingNotification();

            });
            builder.show();
        }
        return false;
    }

    @Override
    public void onAttachFragment(Fragment fragment)
    {
        if (fragment instanceof HomeFragment)
        {
            HomeFragment homeFragment = (HomeFragment) fragment;
            homeFragment.setOnButtonClickListener(this);
            if (locationService != null)
            {
                homeFragment.setRecordingState(locationService.isRecordingActive());
                homeFragment.setLastLocation(locationService.getLocation());
            }
        } else if (fragment instanceof MapFragment)
        {
            MapFragment mapFragment = (MapFragment) fragment;
            if (locationService != null)
            {
                mapFragment.setLastLocation(locationService.getLocation());
            }
        }
    }


    private void updateFragmentsLocation()
    {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (fragment instanceof HomeFragment)
        {
            ((HomeFragment) fragment).updateLocation(locationService.getLocation());
            ((HomeFragment) fragment).setRecordingButtonsActivated(locationService.isRecordingActive());
        } else if (fragment instanceof MapFragment)
        {
            ((MapFragment) fragment).updateLocation(locationService.getLocation());
        }
    }
}
