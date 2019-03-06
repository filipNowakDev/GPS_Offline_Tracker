package com.filipnowakdev.gps_offline_tracker.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.gpx_utils.FileWriterGpxFileService;
import com.filipnowakdev.gps_offline_tracker.gpx_utils.IGpxFileService;

import java.io.File;
import java.util.Objects;

import androidx.navigation.Navigation;

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
                        Bundle args = new Bundle();
                        args.putString(MapFragment.TRACK_NAME, track.getName());
                        Navigation.findNavController(Objects.requireNonNull(getActivity()), R.id.navigation_container)
                                .navigate(R.id.action_tracks_to_map, args);
                    } else if (item.getItemId() == R.id.track_details)
                    {
                        Bundle args = new Bundle();
                        args.putString(TrackDetailsFragment.TRACK_NAME, track.getName());
                        Navigation.findNavController(Objects.requireNonNull(getActivity()), R.id.navigation_container)
                                .navigate(R.id.action_tracks_to_details, args);

                    }
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
