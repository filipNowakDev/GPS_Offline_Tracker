package com.filipnowakdev.gps_offline_tracker.ble_utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.filipnowakdev.gps_offline_tracker.database.daos.SensorDao;
import com.filipnowakdev.gps_offline_tracker.database.db.TrackDatabase;
import com.filipnowakdev.gps_offline_tracker.database.entities.Sensor;

import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SensorManager
{
    private static final int SCAN_PERIOD = 10000;
    private final static String HEART_RATE_SERVICE_UUID = "0000180d-0000-1000-8000-00805f9b34fb";
    private final static String HEART_RATE_CHARACTERISTIC_UUID = "00002a37-0000-1000-8000-00805f9b34fb";
    private static final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private Context context;
    private TrackDatabase db;
    private SensorDao sensorDao;
    private BluetoothGatt defaultSensorGatt;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner leScanner;
    private Handler finishScanDelay;
    private LinkedList<ScanFilter> scanFilters;
    private ScanSettings scanSettings;
    private boolean sensorConnected;
    private OnSensorInteractionCallback sensorCallback;

    public SensorManager(Context context, TrackDatabase db, OnSensorInteractionCallback callback)
    {
        this.context = context;
        this.db = db;
        this.sensorDao = db.sensorDao();
        sensorConnected = false;
        this.sensorCallback = callback;
        initBleScanner();
        initScanSettings();
        initScanFilters();
        initFinishScanHandler();
    }

    private void initScanFilters()
    {
        scanFilters = new LinkedList<>();
    }

    private void initBleScanner()
    {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        leScanner = bluetoothAdapter.getBluetoothLeScanner();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled())
            Toast.makeText(context, "Please enable Bluetooth to use BPM sensor.", Toast.LENGTH_SHORT).show();
    }

    private void initFinishScanHandler()
    {
        finishScanDelay = new Handler();
    }

    private void initScanSettings()
    {
        scanSettings = new ScanSettings.Builder().build();
    }


    public void connectToDefaultSensor()
    {
        if (scanFilters.isEmpty())
        {
            Sensor defaultSensor = getDefaultSensor();
            ScanFilter scanFilter = new ScanFilter.Builder().setDeviceAddress(defaultSensor.address).build();
            scanFilters.add(scanFilter);
        }
        scanLeDevice(true);
    }

    private Sensor getDefaultSensor()
    {
        Callable<Sensor> getCallable = sensorDao::getDefault;
        Sensor sensor = null;
        Future<Sensor> future = Executors.newSingleThreadExecutor().submit(getCallable);
        try
        {
            sensor = future.get();
        } catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }
        return sensor;
    }


    private void scanLeDevice(final boolean enable)
    {
        if (enable)
        {
            // Stops scanning after a pre-defined scan period.
            finishScanDelay.postDelayed(() ->
                    leScanner.stopScan(leScanCallback), SCAN_PERIOD);

            leScanner.startScan(scanFilters, scanSettings, leScanCallback);
        } else
        {
            leScanner.stopScan(leScanCallback);
        }
    }


    private ScanCallback leScanCallback = new ScanCallback()
    {
        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {
            super.onScanResult(callbackType, result);
            if (!sensorConnected)
            {
                defaultSensorGatt = result.getDevice().connectGatt(context, false, new BluetoothGattCallback()
                {

                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                        int newState)
                    {
                        if (newState == BluetoothProfile.STATE_CONNECTED)
                        {
                            System.out.println("Connected to Our Sensor's server.");
                            System.out.println("Attempting to start service discovery:" +
                                    gatt.discoverServices());
                            sensorConnected = true;
                            scanLeDevice(false);
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED)
                        {
                            System.out.println("Disconnected from GATT server.");
                            sensorConnected = false;
                            sensorCallback.onSensorDisconnected();
                        }
                    }

                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, int status)
                    {
                        super.onServicesDiscovered(gatt, status);

                        BluetoothGattCharacteristic heartRateCharacteristic = gatt.getService(UUID.fromString(HEART_RATE_SERVICE_UUID))
                                .getCharacteristic(UUID.fromString(HEART_RATE_CHARACTERISTIC_UUID));

                        gatt.setCharacteristicNotification(heartRateCharacteristic, true);
                        BluetoothGattDescriptor descriptor = heartRateCharacteristic.getDescriptor(
                                CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);

                        sensorCallback.onSensorConnected();
                    }

                    @Override
                    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
                    {
                        super.onCharacteristicChanged(gatt, characteristic);

                        int flag = characteristic.getProperties();
                        int format = -1;
                        if ((flag & 0x01) != 0)
                        {
                            format = BluetoothGattCharacteristic.FORMAT_UINT16;
                            System.out.println("Heart rate format UINT16.");
                        } else
                        {
                            format = BluetoothGattCharacteristic.FORMAT_UINT8;
                            System.out.println("Heart rate format UINT8.");
                        }
                        final int heartRate = characteristic.getIntValue(format, 1);
                        System.out.println(String.format("Received heart rate: %d", heartRate));
                        sensorCallback.onBpmUpdate(heartRate);
                    }
                });
            }
        }

        @Override
        public void onScanFailed(int errorCode)
        {
            System.out.println("Scan Failed\nError Code: " + errorCode);
        }
    };

    public void disconnectDefaultSensor()
    {
        if (defaultSensorGatt != null)
        {
            defaultSensorGatt.disconnect();
            defaultSensorGatt.close();
        }
        sensorConnected = false;
        defaultSensorGatt = null;
    }

    public interface OnSensorInteractionCallback
    {
        void onSensorConnected();

        void onSensorDisconnected();

        void onBpmUpdate(int bpm);
    }
}
