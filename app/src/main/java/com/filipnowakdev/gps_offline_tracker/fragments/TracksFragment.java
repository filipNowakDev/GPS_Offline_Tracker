package com.filipnowakdev.gps_offline_tracker.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.services.FileWriterGpxFileService;
import com.filipnowakdev.gps_offline_tracker.services.IGpxFileService;

import java.io.File;
import java.util.Objects;

public class TracksFragment extends Fragment
{
    private OnListFragmentInteractionListener mListener;
    private IGpxFileService gpxFileService;

    public TracksFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume()
    {
        super.onResume();

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_file_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView)
        {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;

            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new TrackFileRecyclerViewAdapter(gpxFileService.getListOfFiles(), mListener));
        }
        return view;
    }


    @Override
    public void onAttach(final Context context)
    {
        super.onAttach(context);
        gpxFileService = new FileWriterGpxFileService(context);

        if (context instanceof OnListFragmentInteractionListener)
        {
            mListener = (OnListFragmentInteractionListener) context;
        } else
        {
            mListener = (track, v) ->
            {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.getMenuInflater().inflate(R.menu.track_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(item ->
                {
                    if (item.getItemId() == R.id.track_map)
                    {
                        MapFragment fragment = MapFragment.newInstance(track.getName());
                        Objects.requireNonNull(getActivity()).getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .commitAllowingStateLoss();
                    } else if (item.getItemId() == R.id.track_details)
                    {
                        TrackDetailsFragment fragment = TrackDetailsFragment.newInstance(track.getName());
                        Objects.requireNonNull(getActivity()).getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .commitAllowingStateLoss();
                    }
                    Toast.makeText(getContext(), "You selected the action : " + item.getTitle() + " - " + item.getItemId() + " filename " + track.getName(), Toast.LENGTH_SHORT).show();
                    return true;
                });
                popup.show();
            };
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }


    public interface OnListFragmentInteractionListener
    {
        void onListFragmentInteraction(File track, View v);
    }
}
