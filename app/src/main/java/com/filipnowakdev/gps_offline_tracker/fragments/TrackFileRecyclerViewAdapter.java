package com.filipnowakdev.gps_offline_tracker.fragments;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.fragments.TracksFragment.OnListFragmentInteractionListener;
import com.filipnowakdev.gps_offline_tracker.gpx_utils.DOMGpxReader;

import java.io.File;
import java.util.List;


public class TrackFileRecyclerViewAdapter extends RecyclerView.Adapter<TrackFileRecyclerViewAdapter.ViewHolder>
{

    private final List<File> fileList;
    private final OnListFragmentInteractionListener mListener;

    TrackFileRecyclerViewAdapter(List<File> items, OnListFragmentInteractionListener listener)
    {
        fileList = items;
        mListener = listener;
    }

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
        holder.file = fileList.get(position);
        holder.filenameView.setText(fileList.get(position).getName().replaceFirst("[.][^.]+$", ""));

        DOMGpxReader reader = new DOMGpxReader();
        holder.creationDateView.setText(reader.getFormattedFileCreationTime(fileList.get(position)));

        holder.view.setOnClickListener(v ->
        {
            if (null != mListener)
            {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onListFragmentInteraction(holder.file, v);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return fileList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        final View view;
        final TextView filenameView;
        final TextView creationDateView;
        File file;

        ViewHolder(View view)
        {
            super(view);
            this.view = view;
            filenameView = view.findViewById(R.id.file_name);
            creationDateView = view.findViewById(R.id.creation_date);
        }

        @Override
        public String toString()
        {
            return super.toString() + " '" + filenameView.getText() + "'";
        }

    }
}
