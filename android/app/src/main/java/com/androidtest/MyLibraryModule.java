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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.Activity.RESULT_OK;

//import androidx.localbroadcastmanager.content.LocalBroadcastManager

public class MyLibraryModule extends ReactContextBaseJavaModule implements ActivityEventListener {


    private static int REQUEST_ENABLE_BT = 1001;
    Promise tryToTurnBluetoothOn;
    private final ReactApplicationContext reactContext;
    private static final String TAG = "MainActivity";
    BluetoothAdapter bleAdapter;
    ArrayAdapter mArrayAdapter;
    Context context;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public ArrayList<BluetoothDevice> mBTPairedDevice = new ArrayList<>();
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

                for (BluetoothDevice bt : mBTDevices) {
                    WritableMap arrayMap = new WritableNativeMap();
                    Date date = new Date();
                    long time = date.getTime();
                    Timestamp ts = new Timestamp(time);
                    arrayMap.putString("timestamp", ts.toString());
                    arrayMap.putString("name", bt.getName());
                    arrayMap.putString("address", bt.getAddress().toString());
                    array.pushMap(arrayMap);
                }
                try {

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
        public void onLeScan(BluetoothDevice device, int rssi, byte[] bytes) {

            System.out.println("===============================" + device);
            System.out.println("===============================" + device.getName());
            System.out.println("===================rssi============" + rssi);
            boolean valueContain = false;
            System.out.println(device);
            for (int index = 0; index < mBTDevices.size(); index++) {
                if (device.getAddress().equals(mBTDevices.get(index).getAddress())) {
                    valueContain = true;
                }
            }
            if (!valueContain) {
                mBTDevices.add(device);
            }

        }
    };

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
