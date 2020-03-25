package com.filipnowakdev.gps_offline_tracker.fragments.sensors;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.filipnowakdev.gps_offline_tracker.R;
import com.filipnowakdev.gps_offline_tracker.exceptions.SensorAlreadySavedException;
import com.filipnowakdev.gps_offline_tracker.viewmodels.SensorsViewModel;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class SensorsFragment extends Fragment
{
    private OnListFragmentInteractionListener listInteractionListener;

    private final static String HEART_RATE_UUID_STRING = "0000180d-0000-1000-8000-00805f9b34fb";
    private final static UUID HEART_RATE_MEASUREMENT = UUID.fromString(HEART_RATE_UUID_STRING);

    private static final int REQUEST_ENABLE_BT = 666;
    private SensorsViewModel viewModel;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner leScanner;
    private ScanFilter scanFilter;
    private List<ScanFilter> filters;

    private boolean mScanning;
    private Handler handler;
    private static final long SCAN_PERIOD = 20000;
    private SensorsRecyclerViewAdapter leDeviceListAdapter;
    private RecyclerView recyclerView;


    public static SensorsFragment newInstance()
    {
        return new SensorsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_sensors, container, false);
        leDeviceListAdapter = new SensorsRecyclerViewAdapter((device, view) ->
        {
            try
            {
                viewModel.saveDevice(device);
            } catch (SensorAlreadySavedException e)
            {
                Toast.makeText(getContext(), getString(R.string.sensor_already_saved), Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(getContext(), getString(R.string.sansor_saved), Toast.LENGTH_SHORT).show();
        });
        initRecyclerView(v);
        return v;

    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        scanLeDevice(false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SensorsViewModel.class);
        final BluetoothManager bluetoothManager =
                (BluetoothManager) Objects.requireNonNull(getContext()).getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        leScanner = bluetoothAdapter.getBluetoothLeScanner();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        scanFilter = new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(HEART_RATE_UUID_STRING)).build();
        filters = new LinkedList<>();
        filters.add(scanFilter);
        handler = new Handler();
        scanLeDevice(true);


    }

    private void scanLeDevice(final boolean enable)
    {
        if (enable)
        {
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(() ->
            {
                mScanning = false;
                if (getContext() != null)
                    Toast.makeText(getContext(), "Finished scanning.", Toast.LENGTH_SHORT).show();
                leScanner.stopScan(leScanCallback);
            }, SCAN_PERIOD);

            mScanning = true;
            leScanner.startScan(leScanCallback);
        } else
        {
            mScanning = false;
            leScanner.stopScan(leScanCallback);
        }
    }


    private ScanCallback leScanCallback = new ScanCallback()
    {
        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {
            super.onScanResult(callbackType, result);

            BluetoothGatt bluetoothGatt = result.getDevice().connectGatt(getContext(), false, new BluetoothGattCallback()
            {

                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState)
                {
                    if (newState == BluetoothProfile.STATE_CONNECTED)
                    {
                        System.out.println("Connected to GATT server.");
                        System.out.println("Attempting to start service discovery:" +
                                gatt.discoverServices());

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED)
                    {
                        System.out.println("Disconnected from GATT server.");
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status)
                {
                    System.out.println("!!! DONE onServicesDiscovered received: " + status);
                    System.out.println(gatt.getDevice().getName() + ": ");
                    StringBuilder uuids = new StringBuilder();
                    for (BluetoothGattService service : gatt.getServices())
                    {
                        uuids.append("\n");
                        uuids.append(service.getUuid());
                        if (service.getUuid().equals(HEART_RATE_MEASUREMENT))
                        {
                            System.out.println("HEART RATE SENSOR DISCOVERED - SHALL ADD ONLY THIS");
                            Objects.requireNonNull(getActivity()).runOnUiThread(() ->
                            {
                                if (!leDeviceListAdapter.exists(result.getDevice()))
                                {
                                    leDeviceListAdapter.addDevice(gatt.getDevice());
                                    leDeviceListAdapter.notifyItemInserted(leDeviceListAdapter.getItemCount() - 1);
                                }
                            });
                        }
                    }
                    System.out.println(uuids.toString());
                    gatt.discoverServices();
                    gatt.disconnect();
                    gatt.close();
                }
            });

        }

        @Override
        public void onScanFailed(int errorCode)
        {
            System.out.println("Scan Failed\nError Code: " + errorCode);
        }
    };

    private void initRecyclerView(View view)
    {
        recyclerView = view.findViewById(R.id.sensors_list);
        Context context = view.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(leDeviceListAdapter);
    }

    public interface OnListFragmentInteractionListener
    {
        void onListFragmentInteraction(BluetoothDevice device, View v);
    }


}
