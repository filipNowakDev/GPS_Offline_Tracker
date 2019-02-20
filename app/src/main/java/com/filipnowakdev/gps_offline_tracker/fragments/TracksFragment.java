package com.filipnowakdev.gps_offline_tracker.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.services.FileWriterGpxFileService;
import com.filipnowakdev.gps_offline_tracker.services.IGpxFileService;

import java.io.File;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
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
	public void onAttach(Context context)
	{
		super.onAttach(context);
		gpxFileService = new FileWriterGpxFileService(context);

		if (context instanceof OnListFragmentInteractionListener)
		{
			mListener = (OnListFragmentInteractionListener) context;
		} else
		{
			mListener = new OnListFragmentInteractionListener()
			{
				@Override
				public void onListFragmentInteraction(File item)
				{

				}
			};
		}
	}

	@Override
	public void onDetach()
	{
		super.onDetach();
		mListener = null;
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnListFragmentInteractionListener
	{
		// TODO: Update argument type and name
		void onListFragmentInteraction(File item);
	}
}
