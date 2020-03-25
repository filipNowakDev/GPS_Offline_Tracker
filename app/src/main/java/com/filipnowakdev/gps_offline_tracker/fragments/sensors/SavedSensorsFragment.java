package com.filipnowakdev.gps_offline_tracker.fragments.sensors;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.database.entities.Sensor;
import com.filipnowakdev.gps_offline_tracker.viewmodels.SavedSensorsViewModel;

public class SavedSensorsFragment extends Fragment
{

    private OnListFragmentInteractionListener onListFragmentInteractionListener;
    private SavedSensorsViewModel viewModel;
    private SavedSensorsRecyclerViewAdapter adapter;

    public SavedSensorsFragment()
    {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        assignViewModel();
        adapter.addSensors(viewModel.getAllSensors());
        adapter.notifyDataSetChanged();
    }

    private void assignViewModel()
    {
        viewModel = new ViewModelProvider(this).get(SavedSensorsViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_saved_sensors, container, false);
        adapter = new SavedSensorsRecyclerViewAdapter(onListFragmentInteractionListener);
        initRecyclerView(view);
        return view;
    }

    private void initRecyclerView(View view)
    {
        RecyclerView recyclerView = view.findViewById(R.id.saved_sensors_list);
        Context context = view.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener)
            onListFragmentInteractionListener = (OnListFragmentInteractionListener) context;
        else
            onListFragmentInteractionListener = this::setDefaultSensor;
        System.out.println("Attach");
    }

    private void setDefaultSensor(Sensor sensor)
    {
        //viewModel.setDefaultSensor(sensor);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        onListFragmentInteractionListener = null;
    }

    public interface OnListFragmentInteractionListener
    {
        void onListFragmentInteraction(Sensor sensor);
    }
}
