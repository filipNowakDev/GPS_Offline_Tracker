package com.filipnowakdev.gps_offline_tracker.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.filipnowakdev.gps_offline_tracker.R;

public class HomeFragment extends Fragment
{


    private TextView latView;
    private TextView lonView;
    private TextView accView;
    private TextView speedView;
    private Button startRecordingButton;
    private Button endRecordingButton;
    private boolean recordingState;

    private OnButtonClickListener onButtonClickCallback;
    private Location lastLocation;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {


        return inflater.inflate(R.layout.fragment_home, null);
    }

    public void setOnButtonClickListener(OnButtonClickListener onButtonClickCallback)
    {
        this.onButtonClickCallback = onButtonClickCallback;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        initCoordsBoxes();
        initRecordingButtons();
        restoreUI();
    }

    private void restoreUI()
    {
        setRecordingButtonsActivated();
        updateLocation();
    }


    private void initRecordingButtons()
    {

        startRecordingButton = getView().findViewById(R.id.start_recording_button);
        startRecordingButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                setRecordingButtonsActivated(onButtonClickCallback.onStartClick());
            }
        });

        endRecordingButton = getView().findViewById(R.id.end_recording_button);
        endRecordingButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                setRecordingButtonsActivated(onButtonClickCallback.onEndClick());
            }
        });
    }


    public void setRecordingButtonsActivated(boolean isRecording)
    {
        recordingState = isRecording;
        if (isRecording)
        {
            startRecordingButton.setEnabled(false);
            endRecordingButton.setEnabled(true);
        } else
        {
            startRecordingButton.setEnabled(true);
            endRecordingButton.setEnabled(false);
        }
    }

    public void setRecordingButtonsActivated()
    {
        setRecordingButtonsActivated(recordingState);
    }


    public void updateLocation(Location location)
    {
        this.lastLocation = location;
        if(location != null)
        {
            latView.setText(getString(R.string.latitude_label, location.getLatitude()));
            lonView.setText(getString(R.string.longitude_label, location.getLongitude()));
            accView.setText(getString(R.string.accuracy_label, location.getAccuracy()));
            speedView.setText(getString(R.string.speed_label, location.getSpeed()));
        }
    }

    public void updateLocation()
    {
        if(lastLocation != null)
        {
            updateLocation(lastLocation);
        }
    }

    private void initCoordsBoxes()
    {
        latView = getView().findViewById(R.id.latitude_box);
        lonView = getView().findViewById(R.id.longitude_box);
        accView = getView().findViewById(R.id.accuracy_box);
        speedView = getView().findViewById(R.id.speed_box);
    }

    public void setLastLocation(Location lastLocation)
    {
        this.lastLocation = lastLocation;
    }

    public interface OnButtonClickListener
    {
        boolean onStartClick();
        boolean onEndClick();
    }

    public void setRecordingState(boolean recordingState)
    {
        this.recordingState = recordingState;
    }
}
