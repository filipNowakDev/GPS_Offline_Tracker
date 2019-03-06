package com.filipnowakdev.gps_offline_tracker.fragments;


import android.os.Bundle;

import com.filipnowakdev.gps_offline_tracker.R;

import androidx.preference.PreferenceFragmentCompat;


public class SettingsFragment extends PreferenceFragmentCompat
{


    public SettingsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
    }


}
