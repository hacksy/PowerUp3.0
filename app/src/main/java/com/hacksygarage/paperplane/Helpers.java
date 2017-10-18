package com.hacksygarage.paperplane;

import android.bluetooth.BluetoothAdapter;

/**
 * Created by hacksy on 18/10/2017.
 */

public class Helpers {
    public static boolean isBluetoothEnabled()
    {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            // Bluetooth is not enable :)
            return false;
        }
        else{
            return true;
        }

    }
}
