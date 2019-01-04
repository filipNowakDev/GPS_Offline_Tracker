package com.filipnowakdev.gps_offline_tracker.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.services.LocationService;
import com.filipnowakdev.gps_offline_tracker.services.NotificationService;

public class MainActivity extends AppCompatActivity
{


	private TextView latView;
	private TextView lonView;
	private Button startRecordingButton;
	private Button endRecordingButton;


	private Intent locationServiceIntent;
	private LocationService locationService;
	private boolean isLocationServiceBound;
	private ServiceConnection serviceConnection;
	private LocalBroadcastManager localBroadcastManager;


	private NotificationService notificationService;

	private BroadcastReceiver locationBroadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			updateLocation();
		}
	};

	private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener
			= new BottomNavigationView.OnNavigationItemSelectedListener()
	{

		@Override
		public boolean onNavigationItemSelected(@NonNull MenuItem item)
		{
			switch (item.getItemId())
			{
				case R.id.navigation_home:
					return true;
				case R.id.navigation_dashboard:
					return true;
				case R.id.navigation_exit:
					return true;
			}
			return false;
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getPermissions();

		initCoordsBoxes();
		initRecordingButtons();

		initBroadcastManager();

		initNavigation();
		initNotificationService();
	}

	private void initBroadcastManager()
	{
		localBroadcastManager = LocalBroadcastManager.getInstance(this);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		initLocationService();
		registerBroadcastReceiver();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		unbindService(serviceConnection);
		localBroadcastManager.unregisterReceiver(locationBroadcastReceiver);
	}

	private void initRecordingButtons()
	{
		startRecordingButton = findViewById(R.id.start_recording_button);
		startRecordingButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if (isLocationServiceBound)
				{
					locationService.startRecording();
					setRecordingButtonsActivated();
					notificationService.displayRecordingNotification();
				}
			}
		});

		endRecordingButton = findViewById(R.id.end_recording_button);
		endRecordingButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if (isLocationServiceBound)
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					builder.setTitle("Track name: ");

					final EditText input = new EditText(MainActivity.this);
					input.setInputType(InputType.TYPE_CLASS_TEXT);
					builder.setView(input);

					builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{

							String fileName = input.getText().toString();
							locationService.saveRecording(fileName);
							setRecordingButtonsActivated();
							notificationService.hideRecordingNotification();

						}
					});


					builder.show();

				}

			}
		});
	}


	private void initNotificationService()
	{
		this.notificationService = new NotificationService(this);
	}

	private void setRecordingButtonsActivated()
	{
		if (locationService.isRecordingActive())
		{
			startRecordingButton.setEnabled(false);
			endRecordingButton.setEnabled(true);
		} else
		{
			startRecordingButton.setEnabled(true);
			endRecordingButton.setEnabled(false);
		}

	}

	private void registerBroadcastReceiver()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(LocationService.BROADCAST_LOCATION_UPDATE);
		localBroadcastManager.registerReceiver(locationBroadcastReceiver, filter);
	}

	private void initNavigation()
	{
		BottomNavigationView navigation = findViewById(R.id.navigation);
		navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
	}


	private void updateLocation()
	{
		Location location = locationService.getLocation();
		if (location != null)
		{
			latView.setText(getString(R.string.latitude_label, location.getLatitude()));
			lonView.setText(getString(R.string.longitude_label, location.getLongitude()));
		} else
			displayError();
	}

	private void initCoordsBoxes()
	{
		latView = findViewById(R.id.latitude_box);
		lonView = findViewById(R.id.longitude_box);
	}

	private void initLocationService()
	{
		locationServiceIntent = new Intent(this.getApplicationContext(), LocationService.class);
		startService(locationServiceIntent);


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
					setRecordingButtonsActivated();
				}

				@Override
				public void onServiceDisconnected(ComponentName componentName)
				{
					isLocationServiceBound = false;
				}
			};
		}
		bindService(locationServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
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

	private void displayError()
	{
		Toast toast = Toast.makeText(this, "Location unavailable.", Toast.LENGTH_LONG);
		toast.show();
	}


}
