package com.filipnowakdev.gps_offline_tracker.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.database.entities.Track;
import com.filipnowakdev.gps_offline_tracker.viewmodels.TrackListViewModel;

import java.util.Objects;

public class TracksFragment extends Fragment
{
    private OnListFragmentInteractionListener listInteractionListener;
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
        trackAdapter = new TrackFileRecyclerViewAdapter(listInteractionListener);
        View view = inflater.inflate(R.layout.fragment_file_list, container, false);
        initRecyclerView(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        assignViewModel();
    }

    private void assignViewModel()
    {
        tracksViewModel = new ViewModelProvider(this).get(TrackListViewModel.class);
        tracksViewModel.getTracks().observe(this, tracks ->
                trackAdapter.submitList(tracks));
    }

    private void initRecyclerView(View view)
    {
        if (view instanceof RecyclerView)
        {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(trackAdapter);
        }
    }

    @Override
    public void onAttach(@NonNull final Context context)
    {
        super.onAttach(context);
        assignItemActionListener(context);
    }

    private void assignItemActionListener(@NonNull Context context)
    {
        if (context instanceof OnListFragmentInteractionListener)
            listInteractionListener = (OnListFragmentInteractionListener) context;
        else
            listInteractionListener = this::showActionsMenu;
    }

    private void showActionsMenu(Track track, View v)
    {
        PopupMenu popup = prepareActionsMenu(track, v);
        popup.show();
    }

    @NonNull
    private PopupMenu prepareActionsMenu(Track track, View v)
    {
        PopupMenu popup = new PopupMenu(v.getContext(), v);
        popup.getMenuInflater().inflate(R.menu.track_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item ->
        {
            switch (item.getItemId())
            {
                case R.id.track_map:
                    navigateToFragment(track, MapFragment.TRACK_ID, R.id.action_tracks_to_map);
                    break;
                case R.id.track_details:
                    navigateToFragment(track, TrackPagerFragment.TRACK_ID, R.id.action_tracks_to_pager);
                    break;
                case R.id.track_delete:
                    showDeleteDialog(track);
                    break;
            }
            return true;
        });
        return popup;
    }

    private void navigateToFragment(Track track, String argKey, int fragmentId)
    {
        Bundle args = new Bundle();
        args.putLong(argKey, track.getId());
        Navigation.findNavController(Objects.requireNonNull(getActivity()), R.id.navigation_container)
                .navigate(fragmentId, args);
    }

    private void showDeleteDialog(Track track)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(this.getContext()));
        builder.setTitle(track.getName());
        builder.setMessage(getString(R.string.delete_confirm_header_string));
        builder.setPositiveButton(getString(R.string.ok_string), (dialog, which) ->
                tracksViewModel.deleteTrack(track));
        builder.setNegativeButton(getString(R.string.cancel_string), (dialog, which) ->
        {
        });
        builder.show();
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        listInteractionListener = null;
    }


    public interface OnListFragmentInteractionListener
    {
        void onListFragmentInteraction(Track track, View v);
    }


}
