package com.filipnowakdev.gps_offline_tracker.viewmodels;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.filipnowakdev.gps_offline_tracker.database.entities.Sensor;
import com.filipnowakdev.gps_offline_tracker.database.repositories.SensorRepository;
import com.filipnowakdev.gps_offline_tracker.exceptions.SensorAlreadySavedException;

public class SensorsViewModel extends AndroidViewModel
{

    private SensorRepository sensorRepository;

    public SensorsViewModel(@NonNull Application application)
    {
        super(application);
        sensorRepository = new SensorRepository(application);
    }


    public void saveDevice(BluetoothDevice device)
    {
        Sensor sensor = new Sensor();
        sensor.address = device.getAddress();
        sensor.name = device.getName();
        sensor.isDefault = sensorRepository.isEmpty();
        if (!sensorRepository.exists(sensor))
            sensorRepository.insert(sensor);
        else
            throw new SensorAlreadySavedException();
    }
}
