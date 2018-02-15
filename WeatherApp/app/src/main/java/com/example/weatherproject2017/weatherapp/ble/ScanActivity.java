package com.example.weatherproject2017.weatherapp.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.weatherproject2017.weatherapp.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Handles scanning for Bluetooth servers. It will only respond to those broadcasting the UART service.
 */
public class ScanActivity extends Activity {
    private static final String TAG = ScanActivity.class.getSimpleName();

    // For displaying some comments to UI in regards to scan process.
    private TextView textView;

    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private static final long SCAN_PERIOD = 5000; //scanning for 5 seconds

    private BluetoothDevice mDevice = null;
    private ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
    private HashMap<String, Integer> devRssiValues = new HashMap<String, Integer>();

    // The callback variable which will receive server data when the scan finds one.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = (device, rssi, scanRecord) -> runOnUiThread(new Runnable() {
        @Override
        public void run() {
            // Filters by UART service id only.
            if (BleUtils.decodeScanRecords(scanRecord)) {
                textView.append("\nServer discovered.");
                Log.i(TAG, "Server discovered.");
                addDevice(device, rssi);
                scanLeDevice(false);
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        textView = (TextView) findViewById(R.id.scanactivity_textview);

        mHandler = new Handler();

        mBluetoothAdapter = BleUtils.getBluetoothAdapter(this);
        if (!BleUtils.checkBluetoothAdapter(mBluetoothAdapter)) Log.d(TAG, "Unable to obtain bluetooth adapter.");

        scanLeDevice(true);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            textView.append("\nLooking for UART servers..");
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    Log.i(TAG, "Scan stopped.");
                    if (deviceList.size()<1) Log.i(TAG, "Nothing found.");
                    finish();
                }
            }, SCAN_PERIOD);

            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            Log.i(TAG, "Exiting ScanActivity..");
            mBluetoothAdapter.stopLeScan(mLeScanCallback);

            if (mDevice!=null) {
                // Server found, send address data back to MainActivity so it can connect.
                Bundle b = new Bundle();
                b.putString(BluetoothDevice.EXTRA_DEVICE, mDevice.getAddress());
                Intent result = new Intent();
                result.putExtras(b);
                setResult(Activity.RESULT_OK, result);

                finish();
            }
        }
    }

    /**
     * Add Bluetooth server to list of devices.
     *
     * @param device The bluetooth server device
     * @param rssi The rssi of the bluetooth server.
     */
    private void addDevice(BluetoothDevice device, int rssi) {
        mDevice = device;
        devRssiValues.put(device.getAddress(), rssi);
        if (!deviceList.contains(device)) {
            deviceList.add(device);
        }
    }
}
