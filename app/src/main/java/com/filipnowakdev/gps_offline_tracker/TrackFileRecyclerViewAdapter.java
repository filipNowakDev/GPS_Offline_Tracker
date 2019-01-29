package com.filipnowakdev.gps_offline_tracker;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.filipnowakdev.gps_offline_tracker.TracksFragment.OnListFragmentInteractionListener;
import com.filipnowakdev.gps_offline_tracker.dummy.DummyContent.DummyItem;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class TrackFileRecyclerViewAdapter extends RecyclerView.Adapter<TrackFileRecyclerViewAdapter.ViewHolder>
{

	private final List<File> fileList;
	private final OnListFragmentInteractionListener mListener;

	public TrackFileRecyclerViewAdapter(List<File> items, OnListFragmentInteractionListener listener)
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

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);
		holder.creationDateView.setText(simpleDateFormat.format(fileList.get(position).lastModified()));

		holder.view.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (null != mListener)
				{
					// Notify the active callbacks interface (the activity, if the
					// fragment is attached to one) that an item has been selected.
					mListener.onListFragmentInteraction(holder.file);
				}
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
