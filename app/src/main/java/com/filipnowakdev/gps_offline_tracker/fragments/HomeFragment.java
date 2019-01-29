package com.filipnowakdev.gps_offline_tracker.fragments;

import android.app.AlertDialog;
import android.content.*;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.services.LocationService;
import com.filipnowakdev.gps_offline_tracker.services.NotificationService;

public class HomeFragment extends Fragment
{


	private TextView latView;
	private TextView lonView;
	private TextView accView;
	private Button startRecordingButton;
	private Button endRecordingButton;


	private Intent locationServiceIntent;
	private LocationService locationService;
	private boolean isLocationServiceBound;
	private ServiceConnection serviceConnection;
	private LocalBroadcastManager localBroadcastManager;

	private BroadcastReceiver locationBroadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			updateLocation();
		}
	};


	private NotificationService notificationService;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
	{


		return inflater.inflate(R.layout.fragment_home, null);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		initCoordsBoxes();
		initRecordingButtons();
		initBroadcastManager();
		initNotificationService();
		initLocationService();
		registerBroadcastReceiver();
	}

	@Override
	public void onPause()
	{
		super.onPause();
		getContext().unbindService(serviceConnection);
		localBroadcastManager.unregisterReceiver(locationBroadcastReceiver);
	}

	private void initBroadcastManager()
	{
		localBroadcastManager = LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
	}

	private void initRecordingButtons()
	{
		startRecordingButton = getView().findViewById(R.id.start_recording_button);
		startRecordingButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if (isLocationServiceBound)
				{
					if (locationService.getLocation() != null)
					{
						locationService.startRecording();
						setRecordingButtonsActivated();
						notificationService.displayRecordingNotification();
					}
					else
					{
						displayError();
					}
				}
			}
		});

		endRecordingButton = getView().findViewById(R.id.end_recording_button);
		endRecordingButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if (isLocationServiceBound)
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
					builder.setTitle("Track name: ");

					final EditText input = new EditText(getContext());
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
		this.notificationService = new NotificationService(getContext());
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


	private void updateLocation()
	{
		Location location = locationService.getLocation();
		if (location != null)
		{
			latView.setText(getString(R.string.latitude_label, location.getLatitude()));
			lonView.setText(getString(R.string.longitude_label, location.getLongitude()));
			accView.setText(getString(R.string.accuracy_label, location.getAccuracy()));
		} else
			displayError();
	}

	private void initCoordsBoxes()
	{
		latView = getView().findViewById(R.id.latitude_box);
		lonView = getView().findViewById(R.id.longitude_box);
		accView = getView().findViewById(R.id.accuracy_box);
	}

	private void initLocationService()
	{
		locationServiceIntent = new Intent(getActivity().getApplicationContext(), LocationService.class);
		getActivity().startService(locationServiceIntent);


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
					updateLocation();
				}

				@Override
				public void onServiceDisconnected(ComponentName componentName)
				{
					isLocationServiceBound = false;
				}
			};
		}
		getContext().bindService(locationServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
	}

	private void displayError()
	{
		Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Location unavailable.", Toast.LENGTH_LONG);
		toast.show();
	}

}
