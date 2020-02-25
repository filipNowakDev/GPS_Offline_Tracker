package com.filipnowakdev.gps_offline_tracker.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class LocationServiceBoundViewModel extends AndroidViewModel
{
    private MutableLiveData<Boolean> isBound;

    public MutableLiveData<Boolean> getIsBound()
    {
        return isBound;
    }

    public void setIsBound(Boolean isBound)
    {
        this.isBound.postValue(isBound);
    }

    public LocationServiceBoundViewModel(@NonNull Application application)
    {
        super(application);
        isBound = new MutableLiveData<Boolean>();
    }
}
