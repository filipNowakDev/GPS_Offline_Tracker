package com.filipnowakdev.gps_offline_tracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.filipnowakdev.gps_offline_tracker.R;

public class TrackPagerFragment extends Fragment
{
    static final String TRACK_ID = "track_id";
    private static final int NUM_PAGES = 2;
    private long trackId;
    private ViewPager viewPager;


    public TrackPagerFragment()
    {
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            trackId = getArguments().getLong(TRACK_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.track_pager_fragment, container, false);
        initViewPager(v);
        return v;

    }

    private void initViewPager(View v)
    {
        viewPager = v.findViewById(R.id.pager);
        FragmentStatePagerAdapter pagerAdapter = new TrackPagerAdapter(this.getChildFragmentManager(), this.trackId);
        viewPager.setAdapter(pagerAdapter);
    }

    private class TrackPagerAdapter extends FragmentStatePagerAdapter
    {

        private long trackId;

        public TrackPagerAdapter(@NonNull FragmentManager fm, long trackId)
        {
            super(fm);
            this.trackId = trackId;
        }


        @NonNull
        @Override
        public Fragment getItem(int position)
        {
            if (position == 0)
                return TrackDetailsFragment.newInstance(trackId);
            else
                return TrackPlotFragment.newInstance(trackId);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position)
        {
            if (position == 0)
                return getString(R.string.track_details);
            else
                return getString(R.string.track_plot);
        }

        @Override
        public int getCount()
        {
            return NUM_PAGES;
        }
    }

}
