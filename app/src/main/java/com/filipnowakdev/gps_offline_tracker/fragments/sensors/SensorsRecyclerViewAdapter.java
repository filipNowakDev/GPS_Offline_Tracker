package com.filipnowakdev.gps_offline_tracker.fragments.sensors;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.filipnowakdev.gps_offline_tracker.R;

import java.util.LinkedList;

public class SensorsRecyclerViewAdapter extends RecyclerView.Adapter<SensorsRecyclerViewAdapter.ViewHolder>
{


    private final SensorsFragment.OnListFragmentInteractionListener mListener;
    private LinkedList<BluetoothDevice> devices;

    SensorsRecyclerViewAdapter(SensorsFragment.OnListFragmentInteractionListener listener)
    {
        super();
        mListener = listener;
        devices = new LinkedList<>();
    }

    public void addDevice(BluetoothDevice device)
    {
        devices.add(device);
    }

    public boolean exists(BluetoothDevice device)
    {
        return devices.contains(device);
    }

    @NonNull
    @Override
    public SensorsRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_sensor_item, parent, false);
        return new SensorsRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {

        BluetoothDevice device = devices.get(position);
        initViewHolderData(holder, device);
        initViewHolderClickListener(holder, device);
    }

    @Override
    public int getItemCount()
    {
        return devices.size();
    }

    private void initViewHolderClickListener(@NonNull SensorsRecyclerViewAdapter.ViewHolder holder, BluetoothDevice device)
    {
        holder.saveSensorButton.setOnClickListener(v ->
        {
            if (null != mListener)
                mListener.onListFragmentInteraction(device, v);
        });
    }

    private void initViewHolderData(@NonNull SensorsRecyclerViewAdapter.ViewHolder holder, BluetoothDevice device)
    {
        holder.sensorNameView.setText(device.getName());
        holder.sensorAddressView.setText(device.getAddress());
    }


    public class ViewHolder extends RecyclerView.ViewHolder
    {
        final View view;
        TextView sensorNameView;
        TextView sensorAddressView;
        Button saveSensorButton;

        ViewHolder(View view)
        {
            super(view);
            this.view = view;
            initFields(view);
        }

        private void initFields(View view)
        {
            sensorNameView = view.findViewById(R.id.sensor_name);
            sensorAddressView = view.findViewById(R.id.sensor_address);
            saveSensorButton = view.findViewById(R.id.save_sensor_button);
        }

        @NonNull
        @Override
        public String toString()
        {
            return super.toString() + " '" + sensorNameView.getText() + "'";
        }

    }
}
