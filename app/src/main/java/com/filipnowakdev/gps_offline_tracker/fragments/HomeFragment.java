package com.filipnowakdev.gps_offline_tracker.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.viewmodels.LocationServiceBoundViewModel;

import java.util.Objects;

public class HomeFragment extends Fragment implements LocationListener
{
    public enum BUTTON_STATE
    {
        RECORDING,
        NOT_RECORDING,
        LOCATION_UNAVAILABLE
    }

    private TextView latView;
    private TextView lonView;
    private TextView accView;
    private TextView speedView;
    private Button startRecordingButton;
    private Button endRecordingButton;

    private OnButtonClickListener onButtonClickCallback;
    private RecordingStateHelper recordingStateHelper;
    private LocationManager locationManager;
    private SharedPreferences sharedPreferences;

    private LocationServiceBoundViewModel locationViewModel;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        locationViewModel = new ViewModelProvider(Objects.requireNonNull(getActivity())).get(LocationServiceBoundViewModel.class);
        System.out.println("[DEBUG] ODPALAMY Obserwowanko");
        locationViewModel.getIsBound().observe(this, serviceBound ->
        {
            if (serviceBound)
                HomeFragment.this.restoreUI();
            System.out.println("[DEBUG] ZMIANA JU AJA POWINNA NA " + serviceBound.toString());
        });
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        onButtonClickCallback = (OnButtonClickListener) context;
        recordingStateHelper = (RecordingStateHelper) context;
    }

    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        initRecordingButtons(v);
        initCoordinatesBoxes(v);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        updateLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        return v;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    private void restoreUI()
    {
        if (!recordingStateHelper.isLocationAvailable())
            setRecordingButtonsActivated(BUTTON_STATE.LOCATION_UNAVAILABLE);

        else if (recordingStateHelper.isRecordingActive())
            setRecordingButtonsActivated(BUTTON_STATE.RECORDING);

        else
            setRecordingButtonsActivated(BUTTON_STATE.NOT_RECORDING);
        System.out.println("[DEBUG] NO I NAGRYWANKO JEST TERA " + recordingStateHelper.isRecordingActive());

    }


    private void initRecordingButtons(View v)
    {

        startRecordingButton = v.findViewById(R.id.start_recording_button);
        startRecordingButton.setOnClickListener(view -> setRecordingButtonsActivated(onButtonClickCallback.onStartClick()));

        endRecordingButton = v.findViewById(R.id.end_recording_button);
        endRecordingButton.setOnClickListener(view -> setRecordingButtonsActivated(onButtonClickCallback.onEndClick()));
    }


    private void setRecordingButtonsActivated(BUTTON_STATE buttonState)
    {
        if (buttonState == BUTTON_STATE.RECORDING)
            setButtonsStateRecording();

        else if (buttonState == BUTTON_STATE.NOT_RECORDING)
            setButtonStateNotRecording();

        else if (buttonState == BUTTON_STATE.LOCATION_UNAVAILABLE)
            setButtonStateLocationUnavailable();
    }

    private void setButtonStateLocationUnavailable()
    {
        startRecordingButton.setEnabled(false);
        endRecordingButton.setEnabled(false);
    }

    private void setButtonStateNotRecording()
    {
        startRecordingButton.setEnabled(true);
        endRecordingButton.setEnabled(false);
    }

    private void setButtonsStateRecording()
    {
        startRecordingButton.setEnabled(false);
        endRecordingButton.setEnabled(true);
    }


    private void updateLocation(Location location)
    {
        if (location != null)
        {
            updateLocationBoxes(location);
            updateAccuracyBox(location);
            updateSpeedBox(location);
        }
    }

    private void updateLocationBoxes(Location location)
    {
        latView.setText(getString(R.string.latitude_label, location.getLatitude()));
        lonView.setText(getString(R.string.longitude_label, location.getLongitude()));
    }

    private void updateAccuracyBox(Location location)
    {
        accView.setText(getString(R.string.accuracy_label, location.getAccuracy()));
    }

    private void updateSpeedBox(Location location)
    {
        String speedFormat = sharedPreferences.getString("unit_speed", getString(R.string.kilometers_per_hour));

        if (Objects.equals(speedFormat, getString(R.string.kilometers_per_hour)))
            speedView.setText(getString(R.string.speed_label_kph, location.getSpeed() * 3.6));

        else if (Objects.equals(speedFormat, getString(R.string.meters_per_second)))
            speedView.setText(getString(R.string.speed_label_mps, location.getSpeed()));
    }


    private void initCoordinatesBoxes(View v)
    {
        findBoxesViews(v);
        setInitialData();
    }

    @SuppressLint("MissingPermission")
    private void setInitialData()
    {
        if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null)
        {
            updateLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        }
    }

    private void findBoxesViews(View v)
    {
        latView = v.findViewById(R.id.latitude_box);
        lonView = v.findViewById(R.id.longitude_box);
        accView = v.findViewById(R.id.accuracy_box);
        speedView = v.findViewById(R.id.speed_box);
    }

    @Override
    public void onLocationChanged(Location location)
    {
        updateLocation(location);
        if (recordingStateHelper.isRecordingActive())
            setRecordingButtonsActivated(BUTTON_STATE.RECORDING);
        else
            setRecordingButtonsActivated(BUTTON_STATE.NOT_RECORDING);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {

    }

    @Override
    public void onProviderEnabled(String provider)
    {

    }

    @Override
    public void onProviderDisabled(String provider)
    {
        if (provider.equals(LocationManager.GPS_PROVIDER))
            setRecordingButtonsActivated(BUTTON_STATE.LOCATION_UNAVAILABLE);
    }

    public interface OnButtonClickListener
    {
        BUTTON_STATE onStartClick();

        BUTTON_STATE onEndClick();
    }

    public interface RecordingStateHelper
    {
        boolean isRecordingActive();

        boolean isLocationAvailable();
    }
}
