package com.filipnowakdev.gps_offline_tracker.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.database.entities.Track;
import com.filipnowakdev.gps_offline_tracker.viewmodels.TrackListViewModel;

import java.util.Objects;

import androidx.navigation.Navigation;

public class TracksFragment extends Fragment
{
    private OnListFragmentInteractionListener mListener;
    private TrackListViewModel tracksViewModel;
    private TrackFileRecyclerViewAdapter trackAdapter;

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
        trackAdapter = new TrackFileRecyclerViewAdapter(mListener);
        // Set the adapter
        if (view instanceof RecyclerView)
        {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;

            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(trackAdapter);
        }
        return view;
    }


    @Override
    public void onAttach(@NonNull final Context context)
    {
        super.onAttach(context);

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
                        args.putLong(MapFragment.TRACK_ID, track.getId());
                        Navigation.findNavController(Objects.requireNonNull(getActivity()), R.id.navigation_container)
                                .navigate(R.id.action_tracks_to_map, args);
                    } else if (item.getItemId() == R.id.track_details)
                    {
                        Bundle args = new Bundle();
                        args.putLong(TrackDetailsFragment.TRACK_ID, track.getId());
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
        void onListFragmentInteraction(Track track, View v);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        tracksViewModel = ViewModelProviders.of(this).get(TrackListViewModel.class);
        tracksViewModel.getTracks().observe(this, tracks ->
                trackAdapter.submitList(tracks));
    }
}
