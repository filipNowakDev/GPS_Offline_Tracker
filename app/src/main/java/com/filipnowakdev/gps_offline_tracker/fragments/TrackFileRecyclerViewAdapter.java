package com.filipnowakdev.gps_offline_tracker.fragments;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.database.entities.Track;
import com.filipnowakdev.gps_offline_tracker.fragments.TracksFragment.OnListFragmentInteractionListener;
import com.filipnowakdev.gps_offline_tracker.utils.DateUtils;


public class TrackFileRecyclerViewAdapter extends ListAdapter<Track, TrackFileRecyclerViewAdapter.ViewHolder>
{

    private final OnListFragmentInteractionListener mListener;

    TrackFileRecyclerViewAdapter(OnListFragmentInteractionListener listener)
    {
        super(DIFF_CALLBACK);
        mListener = listener;
    }

    private static DiffUtil.ItemCallback<Track> DIFF_CALLBACK = new DiffUtil.ItemCallback<Track>()
    {
        @Override
        public boolean areItemsTheSame(@NonNull Track oldItem, @NonNull Track newItem)
        {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Track oldItem, @NonNull Track newItem)
        {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position)
    {
        Track track = getItem(position);
        initViewHolderData(holder, track);
        initViewHolderClickListener(holder, track);
    }

    private void initViewHolderClickListener(@NonNull ViewHolder holder, Track track)
    {
        holder.view.setOnClickListener(v ->
        {
            if (null != mListener)
                mListener.onListFragmentInteraction(track, v);
        });
    }

    private void initViewHolderData(@NonNull ViewHolder holder, Track track)
    {
        holder.filenameView.setText(track.name);
        holder.creationDateView.setText(DateUtils.getFormattedDateString(track.creationDate));
    }


    public class ViewHolder extends RecyclerView.ViewHolder
    {
        final View view;
        TextView filenameView;
        TextView creationDateView;

        ViewHolder(View view)
        {
            super(view);
            this.view = view;
            initFields(view);
        }

        private void initFields(View view)
        {
            filenameView = view.findViewById(R.id.file_name);
            creationDateView = view.findViewById(R.id.creation_date);
        }

        @NonNull
        @Override
        public String toString()
        {
            return super.toString() + " '" + filenameView.getText() + "'";
        }

    }
}
