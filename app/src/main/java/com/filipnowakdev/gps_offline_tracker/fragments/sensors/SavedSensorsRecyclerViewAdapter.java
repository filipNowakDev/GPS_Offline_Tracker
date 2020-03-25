package com.filipnowakdev.gps_offline_tracker.fragments.sensors;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.database.entities.Sensor;
import com.filipnowakdev.gps_offline_tracker.fragments.sensors.SavedSensorsFragment.OnListFragmentInteractionListener;

import java.util.LinkedList;
import java.util.List;

public class SavedSensorsRecyclerViewAdapter extends RecyclerView.Adapter<SavedSensorsRecyclerViewAdapter.ViewHolder>
{

    private final OnListFragmentInteractionListener listener;
    private List<Sensor> sensorList;


    public SavedSensorsRecyclerViewAdapter(OnListFragmentInteractionListener listener)
    {
        this.listener = listener;
        sensorList = new LinkedList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_saved_sensors_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position)
    {
        Sensor sensor = sensorList.get(position);
        setHolderData(holder, sensor);
        setOnClickListener(holder, sensor);
    }

    private void setHolderData(ViewHolder holder, Sensor sensor)
    {
        holder.sensorAddressView.setText(sensor.address);
        holder.sensorNameView.setText(sensor.name);
        //TODO make the radio buttons functional, only one sensor can be chosen...
        holder.sensorDefaultRadioButton.setChecked(sensor.isDefault);
    }

    private void setOnClickListener(ViewHolder holder, Sensor sensor)
    {
        holder.view.setOnClickListener(v ->
        {
            if (null != listener)
                listener.onListFragmentInteraction(sensor);
        });
    }

    @Override
    public int getItemCount()
    {
        return sensorList.size();
    }

    public void addSensors(List<Sensor> allSensors)
    {
        sensorList = allSensors;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public final View view;
        public final TextView sensorNameView;
        public final TextView sensorAddressView;
        public final RadioButton sensorDefaultRadioButton;

        public ViewHolder(View view)
        {
            super(view);
            this.view = view;
            sensorNameView = view.findViewById(R.id.saved_sensor_name);
            sensorAddressView = view.findViewById(R.id.saved_sensor_address);
            sensorDefaultRadioButton = view.findViewById(R.id.sensor_default_switch);
        }

        @Override
        public String toString()
        {
            return super.toString() + " '" + sensorAddressView.getText() + "'";
        }
    }
}
