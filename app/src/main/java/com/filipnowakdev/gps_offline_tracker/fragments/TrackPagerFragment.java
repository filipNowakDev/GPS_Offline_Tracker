package com.filipnowakdev.gps_offline_tracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.filipnowakdev.gps_offline_tracker.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class TrackPagerFragment extends Fragment
{
    static final String TRACK_ID = "track_id";
    private static final int NUM_PAGES = 2;
    private long trackId;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private FragmentStateAdapter pagerAdapter;

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
        viewPager = v.findViewById(R.id.pager);
        tabLayout = v.findViewById(R.id.tab_layout);
        pagerAdapter = new TrackPagerAdapter(this, this.trackId);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) ->
                {
                    String text;
                    if (position == 0)
                        text = getString(R.string.track_details);
                    else
                        text = getString(R.string.track_plot);
                    tab.setText(text);
                }).attach();

        return v;

    }

    private class TrackPagerAdapter extends FragmentStateAdapter
    {

        private long trackId;

        public TrackPagerAdapter(Fragment f, long trackId)
        {
            super(f);
            this.trackId = trackId;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position)
        {
            if (position == 0)
                return TrackDetailsFragment.newInstance(trackId);
            else
                return TrackPlotFragment.newInstance(trackId);
        }

        @Override
        public int getItemCount()
        {
            return NUM_PAGES;
        }
    }

}
