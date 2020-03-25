package com.filipnowakdev.gps_offline_tracker.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.filipnowakdev.gps_offline_tracker.database.entities.Sensor;
import com.filipnowakdev.gps_offline_tracker.database.repositories.SensorRepository;

import java.util.List;

public class SavedSensorsViewModel extends AndroidViewModel
{

    SensorRepository sensorRepository;
    public SavedSensorsViewModel(@NonNull Application application)
    {
        super(application);
        sensorRepository = new SensorRepository(application);
    }

    public List<Sensor> getAllSensors()
    {
        return sensorRepository.getAll();
    }
}
