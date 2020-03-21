package com.filipnowakdev.gps_offline_tracker.viewmodels;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.filipnowakdev.gps_offline_tracker.database.entities.Sensor;
import com.filipnowakdev.gps_offline_tracker.database.repositories.SensorRepository;
import com.filipnowakdev.gps_offline_tracker.database.repositories.TrackRepository;

public class SensorsViewModel extends ViewModel
{

    private SensorRepository sensorRepository;

    public SensorsViewModel(@NonNull Application application)
    {
        sensorRepository = new SensorRepository(application);
    }



    public void saveDevice(BluetoothDevice device)
    {
        Sensor sensor = new Sensor();
        sensor.address = device.getAddress();
        sensor.name = device.getName();
        sensor.isDefault = false;
        if (!sensorRepository.exists(sensor))
            sensorRepository.insert(sensor);
    }
}
