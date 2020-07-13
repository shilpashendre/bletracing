package com.androidtest;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class BLEModule extends ReactContextBaseJavaModule {
    private final ReactApplicationContext applicationContext;
    private BluetoothAdapter bluetoothAdapter, mBluetoothAdapter;
    private boolean mScanning;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @SuppressLint("ServiceCast")
    public BLEModule(ReactApplicationContext reactContext) {
        super(reactContext);
        applicationContext = reactContext;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!applicationContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            System.out.println("R.string.ble_not_supported");
            Toast.makeText(reactContext.getApplicationContext(), "R.string.ble_not_supported", Toast.LENGTH_SHORT).show();
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) applicationContext.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

    }

    @NonNull
    @Override
    public String getName() {
        return "BLEModule";
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @ReactMethod
    public void getBleDevices(Callback callback) {

        WritableArray array = new WritableNativeArray();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                bluetoothAdapter.stopLeScan(leScanCallback);
//                for (BluetoothDevice bt : mBTDevices) {
//                    WritableMap arrayMap = new WritableNativeMap();
//                    arrayMap.putString("address", bt.getAddress().toString());
//                    array.pushMap(arrayMap);
//                }
                callback.invoke(array);
            }
        }, 10000); // 300 is the delay in millis
        bluetoothAdapter.startLeScan(leScanCallback);
    }

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {
            System.out.println("===============================" + bluetoothDevice);
            boolean flag = false;
//            for (int i = 0; i < mBTDevices.size(); i++) {
//                if (bluetoothDevice.getAddress().equals(mBTDevices.get(i).getAddress())) {
//                    flag = true;
//                }
//            }

//            if(!flag){
//                mBTDevices.add(bluetoothDevice);
//            }
        }
    };


    @ReactMethod
    public void getDiscoverDevices(Callback callback) {
        mBTDevices.clear();
        System.out.println("1. hieeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeisDiscoveringeeee " + mBluetoothAdapter.isDiscovering());
        WritableArray array = new WritableNativeArray();
        if (mBluetoothAdapter.isDiscovering()) {

            System.out.println("2 true********************************************");
            mBluetoothAdapter.cancelDiscovery();
            Log.d("sh", "getDiscoverDevices: cancelling discovery");
//             checkBTPermission();
            mBluetoothAdapter.startDiscovery();
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            // getReactApplicationContext().registerReceiver(mReceiver, filter);
            applicationContext.registerReceiver(mReceiver, filter);
            // registerReceiver(mReceiver, filter);
        }

        if (!mBluetoothAdapter.isDiscovering()) {

            System.out.println("2 false********************************************");
//             checkBTPermission();
            System.out.println("false discovering " + mBluetoothAdapter.isDiscovering());
            mBluetoothAdapter.startDiscovery();
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            // getReactApplicationContext().registerReceiver(mReceiver, filter);
            applicationContext.registerReceiver(mReceiver, filter);
            // registerReceiver(mReceiver, filter);
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                System.out.println("7 ****timer***************mBTDevices* " + mBTDevices);
                for (BluetoothDevice bt : mBTDevices) {
                    System.out.println("7 ****forloop***************mBTDevices* " + bt.getName() + "/" + bt.getAddress());
                    WritableMap arrayMap = new WritableNativeMap();
                    Date date = new Date();
                    long time = date.getTime();
                    Timestamp ts = new Timestamp(time);
                    arrayMap.putString("timestamp", ts.toString());
                    arrayMap.putString("name", bt.getName());
                    arrayMap.putString("address", bt.getAddress().toString());
                    array.pushMap(arrayMap);
                    // array.pushString(bt.toString());
                }
                System.out.println("8 ************************ " + array);
                callback.invoke(null, array);
            }
        }, 10000); // 300 is the delay in millis

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("3 intent=============================================" + intent);
            String action = intent.getAction();
            Log.d("sh", "onReceive: Action found");
            System.out.println("4 intent=============================================" + action);

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                boolean valueContain = false;
                System.out.println("5 true hieeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee " + device);
                for (int i = 0; i < mBTDevices.size(); i++) {
                    if (device.getAddress().equals(mBTDevices.get(i).getAddress())) {
                        valueContain = true;
                    }
                }
                if (!valueContain) {
                    mBTDevices.add(device);
                }
                System.out.println("6 hieeeeeeeeeeeeeeeeeeeeeeeeeeeeemBTDeviceseeeeee " + mBTDevices);
                // Log.d(TAG, "onReceieve: " + device.getName() + ": " + device.getAddress());
                //// foundDevice(mBTDevices);
                // WritableMap arrayMap = new WritableNativeMap();
                // arrayMap.putString("name", device.getName());
                // arrayMap.putString("address", device.getAddress().toString());
                // array.pushMap(arrayMap);
            }
        }
    };
}