package com.androidtest;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.ArrayAdapter;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

//import androidx.localbroadcastmanager.content.LocalBroadcastManager

public class MyLibraryModule extends ReactContextBaseJavaModule implements ActivityEventListener {


    private static int REQUEST_ENABLE_BT = 1001;
    Promise tryToTurnBluetoothOn;
    private final ReactApplicationContext reactContext;
    private static final String TAG = "MainActivity";
    BluetoothAdapter bleAdapter;
    ArrayAdapter mArrayAdapter;
    Context context;
    public ArrayList<JSONObject> mBTDevices = new ArrayList<>();
    public WritableArray array = new WritableNativeArray();
    public Callback activityCallback;
    private static final int ENABLE_REQUEST = 1001;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public MyLibraryModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        reactContext.addActivityEventListener(this);
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) reactContext.getSystemService(Context.BLUETOOTH_SERVICE);
        bleAdapter = bluetoothManager.getAdapter();
    }


    @Override
    public String getName() {
        return "MyLibraryBle";
    }


    @ReactMethod
    public void sampleMethod(String stringArgument, int numberArgument, Callback callback) {
        // TODO: Implement some actually useful functionality
        callback.invoke("Received numberArgument: " + numberArgument + " stringArgument: " + stringArgument);
    }

    /**
     * List of paired devices.
     */
    @ReactMethod
    public void getListOfPairedDevices(Callback cb) {
        System.out.println("called method");
        WritableArray pairedArray = new WritableNativeArray();
        // getting list of devies which are already paired with our device
        Set<BluetoothDevice> pairedDevice = bleAdapter.getBondedDevices();
        if (pairedDevice.size() > 0) {
            for (BluetoothDevice device : pairedDevice) {
                // device name
                WritableMap arrayMap = new WritableNativeMap();
                arrayMap.putString("name", device.getName());
                arrayMap.putString("address", device.getAddress().toString());
                pairedArray.pushMap(arrayMap);
            }
        }
        try {
            cb.invoke(null, pairedArray);
        } catch (Exception e) {
            cb.invoke(e.getMessage().toString(), null);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @ReactMethod
    public void getBleDevices(Callback callback) {
        mBTDevices.clear();
        WritableArray array = new WritableNativeArray();
        new Timer().schedule(new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void run() {
                bleAdapter.stopLeScan(leScanCallback);
                try {
                    for (JSONObject object : mBTDevices) {
                        WritableMap arrayMap = new WritableNativeMap();
                        Date date = new Date();
                        long time = date.getTime();
                        Timestamp ts = new Timestamp(time);
                        arrayMap.putString("timestamp", ts.toString());
                        arrayMap.putString("name", String.valueOf(object.get("name")));
                        arrayMap.putString("btAddr", String.valueOf(object.get("btAddr")));
                        arrayMap.putString("rssi", String.valueOf(object.get("rssi")));
                        arrayMap.putString("txPower", String.valueOf(object.get("txPower")));
                        arrayMap.putString("distanceBle", String.valueOf(object.get("distanceBle")));
                        array.pushMap(arrayMap);
                    }


                    callback.invoke(null, array);
                } catch (Exception e) {
                    callback.invoke(e.getMessage(), null);
                }
            }
        }, 10000); // 300 is the delay in millis
        bleAdapter.startLeScan(leScanCallback);
    }

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            System.out.println("________________________________"+device.getName());
            boolean valueContain = false;

            System.out.println(device);
            for (int index = 0; index < mBTDevices.size(); index++) {
                try {
                    if (device.getAddress().equals(mBTDevices.get(index).get("btAddr"))) {
                        valueContain = true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (!valueContain) {
                JSONObject jsonObject = new JSONObject();
                double distance = getBLEDistance(rssi, scanRecord[29]);
                System.out.println("______________"+distance);
                try {
                    if (device.getName() != null) {

                        jsonObject.put("name", device.getName());
                    } else {
                        jsonObject.put("name", "unavailable");
                    }
                    jsonObject.put("btAddr", device.getAddress());
                    jsonObject.put("rssi", rssi);
                    jsonObject.put("txPower", scanRecord[29]);
                    jsonObject.put("distanceBle", distance);
                    mBTDevices.add(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }
    };

    private double getBLEDistance(int rssi, byte scanValue) {
        int txPower = (int) scanValue;
        if (rssi == 0) {
            return -1.0; // if we cannot determine distance, return -1.
        }
        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
    }

    // Activity Result

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            tryToTurnBluetoothOn.resolve(_isBluetoothTurnedOn());
        }
    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    private boolean _isBluetoothTurnedOn() {
        if (bleAdapter == null || !bleAdapter.isEnabled())
            return false;
        return true;
    }

    @ReactMethod
    public void isBluetoothTurnedOn(final Promise promise) {
        promise.resolve(_isBluetoothTurnedOn());
    }

    @ReactMethod
    public void tryToTurnBluetoothOn(final Promise promise) {
        tryToTurnBluetoothOn = promise;
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        getReactApplicationContext().getCurrentActivity().startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }


}
