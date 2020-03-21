package com.filipnowakdev.gps_offline_tracker.fragments;


import android.os.Bundle;

import androidx.navigation.Navigation;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.filipnowakdev.gps_offline_tracker.R;

import java.util.Objects;


public class SettingsFragment extends PreferenceFragmentCompat
{


    public SettingsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.settings, rootKey);

        Preference sensorsPreference = findPreference("sensors_pair");
        assert sensorsPreference != null;
        sensorsPreference.setOnPreferenceClickListener(preference ->
        {

            Navigation.findNavController(Objects.requireNonNull(SettingsFragment.this.getActivity()), R.id.navigation_container)
                    .navigate(R.id.action_settings_to_sensors);
            return true;
        });
    }


}
