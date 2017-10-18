package com.hacksygarage.paperplane;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    private static final UUID PROPELLER_SERVICE_UUID = UUID.fromString("86c3810e-f171-40d9-a117-26b300768cd6");
    private static final UUID PROPELLER_CHARACTERISTIC_UUID = UUID.fromString("86c3810e-0010-40d9-a117-26b300768cd6");
    private static final UUID RUDDER_SERVICE_UUID =  UUID.fromString("86c3810e-f171-40d9-a117-26b300768cd6");
    private static final UUID RUDDER_CHARACTERISTIC_UUID =  UUID.fromString("86c3810e-0021-40d9-a117-26b300768cd6");
    private SeekBar seekSpeed;
    private TextView txtSpeed;
    private SeekBar seekRudderAngle;
    private TextView txtRudderAngle;
    private Button btnConnect;
    private BluetoothAdapter mBluetoothAdapter;
    private static final long SCAN_PERIOD = 10000;
    private Handler mHandler = new Handler();
    ;
    private boolean mScanning = true;
    private BluetoothDevice device;
    private boolean mConnected = false;
    private BluetoothGatt mGatt;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private BluetoothManager bluetoothManager;
    private BluetoothGattServer mGattServer;
    private BluetoothGatt mActiveGatt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initViews();
        updateSpeed();
        updateRudder();
          bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        connectToDevice();
    }

    private void updateRudder() {
        //Update the screen value
        seekRudderAngle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtRudderAngle.setText(Integer.toString(progress));
                //Send to bluetooth , if connected
                if(mActiveGatt != null){
                    //We have a device
                    BluetoothGattService service = mActiveGatt.getService(RUDDER_SERVICE_UUID);
                    for (BluetoothGattCharacteristic chracteristics :  service.getCharacteristics()){
                        android.util.Log.w("chracteristics", chracteristics.getUuid().toString());
                    }
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(RUDDER_CHARACTERISTIC_UUID);
                    characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                    byte[] messageBytes = new byte[] { (byte) progress};
                    characteristic.setValue(messageBytes);
                    mActiveGatt.writeCharacteristic(characteristic);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    private void connectToDevice() {
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Enable Bluetooth if disabled
                if (!mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.enable();
                }
                //Search for Devices
                scanLeDevice(true);
            }
        });

    }

    private void updateSpeed() {
        //Update the screen value
        seekSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtSpeed.setText(Integer.toString(progress));
                //Send to bluetooth , if connected
                if(mActiveGatt != null){
                    //We have a device
                    BluetoothGattService service = mActiveGatt.getService(PROPELLER_SERVICE_UUID);
                    for (BluetoothGattCharacteristic chracteristics :  service.getCharacteristics()){
                        android.util.Log.w("chracteristics", chracteristics.getUuid().toString());
                    }
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(PROPELLER_CHARACTERISTIC_UUID);
                    characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                    byte[] messageBytes = new byte[] { (byte) progress};
                    characteristic.setValue(messageBytes);
                    mActiveGatt.writeCharacteristic(characteristic);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    private void initViews() {
        seekSpeed = (SeekBar) findViewById(R.id.seekSpeed);
        txtSpeed = (TextView) findViewById(R.id.txtSpeed);

        seekRudderAngle = (SeekBar) findViewById(R.id.seekRudderAngle);
        txtRudderAngle = (TextView) findViewById(R.id.txtRudderAngle);
        btnConnect = (Button) findViewById(R.id.btnConnect);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.getBluetoothLeScanner().stopScan(bleScanCallback);
                }
            }, SCAN_PERIOD);
            mScanning = true;
            mBluetoothAdapter.getBluetoothLeScanner().startScan(bleScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(bleScanCallback);
        }
    }

    private ScanCallback bleScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            addScanResult(result);

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results) {
                addScanResult(result);
            }
        }
    };

    private void addScanResult(ScanResult result) {
        BluetoothDevice tempDevice = result.getDevice();
        if (tempDevice.getName() != null && tempDevice.getName().equals("TailorToys PowerUp")) {
            Snackbar.make(findViewById(android.R.id.content), "Device Found", Snackbar.LENGTH_SHORT).show();
            GattClientCallback gattClientCallback = new GattClientCallback();
            mGatt = tempDevice.connectGatt(this, false, gattClientCallback);
            mGatt.connect();
        }
    }

    private class GattClientCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (status == BluetoothGatt.GATT_FAILURE) {
                disconnectGattServer();
                return;
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                disconnectGattServer();
                return;
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnected = true;
                mActiveGatt = gatt;
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        boolean ans = gatt.discoverServices();
                        Log.d("TAG", "Discover Services started: " + ans);
                    }
                },1000);
                //GattServerCallback gattServerCallback = new GattServerCallback();
                //mGattServer = bluetoothManager.openGattServer(MainActivity.this, gattServerCallback);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                disconnectGattServer();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            android.util.Log.w("OnSErvicesDiscovered","called");
            for (BluetoothGattService serviceT :  gatt.getServices()){
                android.util.Log.w("ServiceN", serviceT.getUuid().toString());
            }
            if (status != BluetoothGatt.GATT_SUCCESS) {
                return;
            }
        }
    }

    private void disconnectGattServer() {
        mConnected = false;
        if (mGatt != null) {
            mGatt.disconnect();
            mGatt.close();
            android.util.Log.d("DEBUGBT", "Disconnect");
        }
    }


}


