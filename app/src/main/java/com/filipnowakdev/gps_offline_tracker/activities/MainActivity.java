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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.fragments.HomeFragment;
import com.filipnowakdev.gps_offline_tracker.interfaces.ToolbarTitleUpdater;
import com.filipnowakdev.gps_offline_tracker.services.LocationService;
import com.filipnowakdev.gps_offline_tracker.services.NotificationBuilder;
import com.filipnowakdev.gps_offline_tracker.viewmodels.LocationServiceBoundViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity implements HomeFragment.OnButtonClickListener, HomeFragment.RecordingStateHelper, ToolbarTitleUpdater
{

    private LocationService locationService;
    private boolean isLocationServiceBound;
    private ServiceConnection serviceConnection;
    private Intent locationServiceIntent;

    private NotificationBuilder notificationService;
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
        PreferenceManager.setDefaultValues(this, R.xml.settings, true);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unbindLocationService();
    }

    private void unbindLocationService()
    {
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
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]
                            {
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.BLUETOOTH,
                                    Manifest.permission.BLUETOOTH_ADMIN
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
        startLocationService();
        bindToLocationService();
    }

    private void startLocationService()
    {
        locationServiceIntent = new Intent(this.getApplicationContext(), LocationService.class);
        this.startService(locationServiceIntent);
    }

    private void bindToLocationService()
    {
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
        this.notificationService = new NotificationBuilder(this);
    }

    @Override
    public HomeFragment.BUTTON_STATE onStartClick()
    {
        if (isLocationServiceBound)
        {
            if (locationService.getLocation() != null)
            {
                locationService.startForeground(NotificationBuilder.getRecordingNotificationId(), notificationService.getRecordingNotification());
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
        boolean cancelledSaving = false;
        if (isLocationServiceBound)
        {
            cancelledSaving = showSaveDialog();
        }
        if (cancelledSaving)
            return HomeFragment.BUTTON_STATE.RECORDING;
        return HomeFragment.BUTTON_STATE.NOT_RECORDING;
    }

    private boolean showSaveDialog()
    {
        //TODO extract custom DialogBuilder
        AtomicBoolean cancelledSaving = new AtomicBoolean(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.track_name_string));

        final EditText input = getDialogInputBox();
        setInitialFilename(input);
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.ok_string), (dialog, which) ->
                saveRecording(input));
        builder.setNegativeButton(getString(R.string.cancel_string), (dialog, which) -> cancelledSaving.set(true));
        builder.show();

        return cancelledSaving.get();
    }

    private void saveRecording(EditText input)
    {
        String fileName = input.getText().toString();
        locationService.saveRecording(fileName);
        locationService.stopForeground(true);
    }

    @NonNull
    private EditText getDialogInputBox()
    {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        return input;
    }

    private void setInitialFilename(EditText input)
    {
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyMMddHHmmSS", Locale.US);
        String date = DATE_FORMAT.format(currentTime);
        input.setText(date);
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
