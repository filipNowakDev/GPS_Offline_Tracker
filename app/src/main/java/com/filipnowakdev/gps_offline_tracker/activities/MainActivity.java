package com.filipnowakdev.gps_offline_tracker.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.fragments.HomeFragment;
import com.filipnowakdev.gps_offline_tracker.interfaces.ToolbarTitleUpdater;
import com.filipnowakdev.gps_offline_tracker.services.LocationService;
import com.filipnowakdev.gps_offline_tracker.services.NotificationService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity implements HomeFragment.OnButtonClickListener, HomeFragment.RecordingStateHelper, ToolbarTitleUpdater
{

    private LocationService locationService;
    private boolean isLocationServiceBound;
    private ServiceConnection serviceConnection;
    Intent locationServiceIntent;

    private NotificationService notificationService;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    public boolean onSupportNavigateUp()
    {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

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
        isLocationServiceBound = false;
        if (!locationService.isRecordingActive())
        {
            this.stopService(locationServiceIntent);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        initNotificationService();
        initLocationService();
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
        navController = Navigation.findNavController(this, R.id.navigation_container);
        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigation, navController);

    }


    private void initLocationService()
    {
        locationServiceIntent = new Intent(this.getApplicationContext(), LocationService.class);
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

    private void initNotificationService()
    {
        this.notificationService = new NotificationService(this);
    }

    @Override
    public HomeFragment.BUTTON_STATE onStartClick()
    {
        if (isLocationServiceBound)
        {
            if (locationService.getLocation() != null)
            {
                locationService.startRecording();
                notificationService.displayRecordingNotification();
                return HomeFragment.BUTTON_STATE.RECORDING;
            } else
            {
                displayError();
                return HomeFragment.BUTTON_STATE.NOT_RECORDING;
            }
        }
        return HomeFragment.BUTTON_STATE.LOCATION_UNAVAILABLE;
    }

    @Override
    public HomeFragment.BUTTON_STATE onEndClick()
    {
        if (isLocationServiceBound)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Track name: ");

            Date currentTime = Calendar.getInstance().getTime();
            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yy_HH_mm_SS", Locale.US);
            String date = DATE_FORMAT.format(currentTime);

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setText(date);
            builder.setView(input);

            builder.setPositiveButton("OK", (dialog, which) ->
            {

                String fileName = input.getText().toString();
                locationService.saveRecording(fileName);
                notificationService.hideRecordingNotification();

            });
            builder.show();
        }
        return HomeFragment.BUTTON_STATE.NOT_RECORDING;
    }


    @Override
    public boolean isRecordingActive()
    {
        if (locationService != null)
            return locationService.isRecordingActive();
        return false;
    }

    @Override
    public boolean isLocationAvailable()
    {
        return locationService != null && locationService.getLocation() != null;
    }

    @Override
    public void updateToolbarTitle(String title)
    {
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
    }
}
