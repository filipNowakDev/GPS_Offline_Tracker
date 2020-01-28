package com.filipnowakdev.gps_offline_tracker.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.fragments.HomeFragment;
import com.filipnowakdev.gps_offline_tracker.interfaces.ToolbarTitleUpdater;
import com.filipnowakdev.gps_offline_tracker.services.LocationService;
import com.filipnowakdev.gps_offline_tracker.services.NotificationService;
import com.filipnowakdev.gps_offline_tracker.viewmodels.LocationServiceBoundViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements HomeFragment.OnButtonClickListener, HomeFragment.RecordingStateHelper, ToolbarTitleUpdater
{

    private LocationService locationService;
    private boolean isLocationServiceBound;
    private ServiceConnection serviceConnection;
    private Intent locationServiceIntent;

    private NotificationService notificationService;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private LocationServiceBoundViewModel locationViewModel;

    @Override
    public boolean onSupportNavigateUp()
    {
        System.out.println("[DEBUG] : Navigate_up");
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermissions();
        initNavigation();
        locationViewModel = new ViewModelProvider(this).get(LocationServiceBoundViewModel.class);
        locationViewModel.setIsBound(false);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        this.unbindService(serviceConnection);
        isLocationServiceBound = false;
        locationViewModel.setIsBound(false);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.settings_menu_item)
            Navigation.findNavController(this, R.id.navigation_container)
                    .navigate(R.id.action_settings);
        else

            return super.onOptionsItemSelected(item);

        return true;
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
                    locationViewModel.setIsBound(true);

                }

                @Override
                public void onServiceDisconnected(ComponentName componentName)
                {
                    isLocationServiceBound = false;
                    locationViewModel.setIsBound(false);
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
                locationService.startForeground(NotificationService.getRecordingNotificationId(), notificationService.getRecordingNotification());
                locationService.startRecording();
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
            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyMMddHHmmSS", Locale.US);
            String date = DATE_FORMAT.format(currentTime);

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setText(date);
            builder.setView(input);

            builder.setPositiveButton("OK", (dialog, which) ->
            {

                String fileName = input.getText().toString();
                locationService.saveRecording(fileName);
                locationService.stopForeground(true);
            });
            builder.show();
        }
        return HomeFragment.BUTTON_STATE.NOT_RECORDING;
    }

    @Override
    public boolean isRecordingActive()
    {
        if (locationService != null)
        {
            System.out.println("[DEBUG] NO KURDE W serwisie jest na " + locationService.isRecordingActive());
            return locationService.isRecordingActive();

        }
        System.out.println("[DEBUG] XD SERWIS NADAL JEST NULLEM");
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
