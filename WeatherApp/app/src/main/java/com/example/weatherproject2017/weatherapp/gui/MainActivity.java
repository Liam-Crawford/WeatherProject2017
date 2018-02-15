/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.weatherproject2017.weatherapp.gui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.weatherproject2017.weatherapp.R;
import com.example.weatherproject2017.weatherapp.ble.BleUtils;
import com.example.weatherproject2017.weatherapp.ble.BluetoothLeService;
import com.example.weatherproject2017.weatherapp.ble.DataManager;
import com.example.weatherproject2017.weatherapp.ble.ScanActivity;
import com.example.weatherproject2017.weatherapp.ble.TestActivity;
import com.example.weatherproject2017.weatherapp.ble.Unpacker;
import com.example.weatherproject2017.weatherapp.data.DatabaseHelper;
import com.example.weatherproject2017.weatherapp.data.WeatherDataObject;
import com.example.weatherproject2017.weatherapp.network.SyncUtils;
import com.example.weatherproject2017.weatherapp.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * Provides UI for the main screen.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final long CHAR_READ_DELAY = 6000; //Delay for when to assume character reading has stopped.

    // Connection
    private BluetoothLeService mBluetoothLeService = null;
    private boolean isConnected = false;
    private boolean isScanning = false;

    // Data
    private ArrayList<Byte> mCharacteristicData = new ArrayList<Byte>();
    private DatabaseHelper dbHelper;
    private Calendar timeStampForReading = Calendar.getInstance();
    private Handler mHandler;

    // Activity request codes (used for onActivityResult)
    private static final int rActivityCode_EnableBluetooth = 1;
    private static final int rActivityCode_Settings = 2;
    private static final int rActivityCode_RequestScan = 3;
    private static final int rActivityCode_TestOther = 4;

    // Permission codes
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;

    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Adding Toolbar to Main screen
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Setting ViewPager for each Tabs
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        // Set Tabs inside Toolbar
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        // Create Navigation drawer and inflate layout
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        // Adding menu icon to Toolbar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            VectorDrawableCompat indicator
                    = VectorDrawableCompat.create(getResources(), R.drawable.ic_menu, getTheme());
            indicator.setTint(ResourcesCompat.getColor(getResources(), R.color.white, getTheme()));
            supportActionBar.setHomeAsUpIndicator(indicator);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        // Set behavior of Navigation drawer
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    // This method will trigger on item Click of navigation menu
                    public boolean onNavigationItemSelected(MenuItem item) {
                        int id = item.getItemId();

                        if (id == R.id.nav_table)
                        {
                            viewPager.setCurrentItem(0);
                        }
                        else if (id == R.id.nav_graph)
                        {
                            viewPager.setCurrentItem(1);
                        }
                        else if (id == R.id.nav_map)
                        {
                            viewPager.setCurrentItem(2);
                        }

                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    }
                });

        // Sets the preferences to default values the first time this app is run after being installed.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Setup backend
        dbHelper = DatabaseHelper.getInstance(this);
        // Setup some initial test data.
        if (dbHelper.getNumberOfRows()<1) {
            if (dbHelper.getNumStationRows()<1) dbHelper.populateStationTable();
            dbHelper.populateWithOrderedData();
        }
        //Log.i(TAG, "Rows in DB: " + dbHelper.getNumberOfRows());

        mHandler = new Handler();

        // Setup SyncAdapter which will check every hour if new data has been added to the database
        // and try to upload it to the online server.
        SyncUtils.CreateSyncAccount(this);

        requestLocationPermissionIfNeeded();
    }

    // Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new TableContentFragment(), "Table");
        adapter.addFragment(new LineGraphContentFragment(), "Graph");
        adapter.addFragment(new MapFragment(), "Map");
        viewPager.setAdapter(adapter);
    }

    /**
     * Internal class to handle storage of the various UI fragments.
     */
    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }


        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
    //************************ END REGION UI ***********************************

    //************************ START REGION FUNCTIONS **************************

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;

        switch (id) {
            case R.id.action_settings:
                intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(intent, rActivityCode_Settings);
                return true;
            case R.id.action_testscan:
                if (isConnected) {
                    mBluetoothLeService.disconnect();
                }
                scan();
                return true;
            // Activity for test purposes.
            case R.id.action_testother:
                intent = new Intent(MainActivity.this, TestActivity.class);
                startActivityForResult(intent, rActivityCode_TestOther);
                return true;
            case R.id.action_resetdb:
                dbHelper.resetDB();
                Log.i(TAG, "Rows in DB: " + dbHelper.getNumberOfRows());
                dbHelper.close();
                return true;
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case rActivityCode_EnableBluetooth:
                if (resultCode == Activity.RESULT_OK) {
                    scan();

                } else if (resultCode == Activity.RESULT_CANCELED) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    AlertDialog dialog = builder.setMessage(R.string.dialog_error_no_bluetooth)
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                    //DialogUtils.keepDialogOnOrientationChanges(dialog);

                }
                break;
            case rActivityCode_RequestScan:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    showMessage("Scan successful.");
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);

                    Log.i(TAG, "Attempting connection to device: " + deviceAddress + "..");
                    mBluetoothLeService.connect(deviceAddress);
                    isScanning = false;
                } else {
                    showMessage("Scan unsuccessful.");
                    disconnectServices();
                }
                break;
            case rActivityCode_Settings:
                showMessage("Settings finished.");
                break;
            case rActivityCode_TestOther:
                showMessage("Testing Finished with status code: " + resultCode);
        }
    }

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        super.onResume();
    }

    @Override
    protected void onPause() {
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mGattUpdateReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        disconnectServices();

        dbHelper.close();
        super.onDestroy();
    }

    /**
     * Handles removing references/resources associated with bluetooth scanning/reading.
     */
    private void disconnectServices() {
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mGattUpdateReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }

        if (mBluetoothLeService != null) {
            mBluetoothLeService.stopSelf();
            try {
                unbindService(mServiceConnection);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mBluetoothLeService = null;
        }
        isScanning = false;
        isConnected = false;
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.i(TAG, "BleService connected.");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();

            // If we have successfully connected to the BleService then attempt to scan
            // for GATT servers.
            isScanning = true;
            Intent scanIntent = new Intent(MainActivity.this, ScanActivity.class);
            Log.i(TAG, "Scan starting..");
            startActivityForResult(scanIntent, rActivityCode_RequestScan);

            if (!mBluetoothLeService.initialize(getApplicationContext())) {
                Log.w(TAG, "Unable to initialise Bluetooth.");
                //Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "BleService disconnected");
            disconnectServices();
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            switch (action) {
                case BluetoothLeService.ACTION_GATT_CONNECTED:
                    isConnected = true;
                    isScanning = false;
                    Log.i(TAG, "Connected to server.");
                    showMessage("Connected to server.");
                    break;
                case BluetoothLeService.ACTION_GATT_DISCONNECTED:
                    isConnected = false;
                    isScanning = false;
                    Log.i(TAG, "Disconnected from server.");
                    showMessage("Disconnected from server.");
                    disconnectServices();
                    break;
                case BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED:
                    Log.i(TAG, "Services discovered.");
                    showMessage("Services discovered, enabling TX notification.");
                    mBluetoothLeService.enableTXNotification();
                    break;
                case BluetoothLeService.ACTION_DATA_AVAILABLE:
                    final byte[] txValue = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    readCharacteristic(txValue);
                    break;
                case BluetoothLeService.DEVICE_DOES_NOT_SUPPORT_UART:
                    Log.i(TAG, "UART not supported, disconnecting.");
                    showMessage("UART not supported, disconnecting.");
                    isConnected = false;
                    mBluetoothLeService.disconnect();
                    break;
            }
        }
    };

    // Region Scanning

    private void scan() {
        if (isScanning) {
            Log.i(TAG, "Still scanning!");
            return;
        }
        if (isConnected) {
            Log.i(TAG, "Still connected to device!");
            return;
        }

        // If bluetooth isn't available then attempt to turn it on.
        if (!BleUtils.isBleReady(this)) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, rActivityCode_EnableBluetooth);
            return;
        }

        initiateServiceConnection();
    }

    /**
     * This function is called when the user wants to scan for bluetooth servers.
     * It makes sure that we are connected to the BleService and BroadcastReceiver to enable
     * communication with a server once it is found.
     */
    private void initiateServiceConnection() {
        // If we previously scanned for something, make sure BleService and BroadcastReceiver are
        // refreshed for a new scan.
        if (mBluetoothLeService!=null) disconnectServices();

        // Attempt connection to BleService.
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        if (bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE)) {
            Log.i(TAG, "Bind successful.");
        } else Log.i(TAG, "Bind unsuccessful.");

        LocalBroadcastManager.getInstance(this).registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    // End Region.

    // Region Permissions
    @TargetApi(Build.VERSION_CODES.M)
    private void requestLocationPermissionIfNeeded() {
        // Certain Android version require location permissions for Bluetooth scanning.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can scan for Bluetooth peripherals");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
                    }
                });
                builder.show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_FINE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Location permission granted");

                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Bluetooth Scanning not available");
                    builder.setMessage("Since location access has not been granted, the app will not be able to scan for Bluetooth peripherals");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                break;
            }
            default:
                break;
        }
    }
    // End region

    // Sets up the broadcast receiver with the intents we care about receiving updates for.
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    // Region Network



    // End Region

    // Region Data Parsing

    /**
     * Gets called each time the server tells us that it's got new data. We then save a timestamp
     * and write the bytes to an ArrayList. After 5 seconds has passed a thread will make a new
     * time stamp and check whether it is more than 3 seconds newer than the last saved timestap.
     * If it is then we assume all data reading is finished and call addReadDataToDB method.
     *
     * @param txValue
     */
    private void readCharacteristic(byte[] txValue) {
        Log.i(TAG, "Read characteristic..");
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(() -> {
            Calendar cal = Calendar.getInstance();
            if ((cal.getTimeInMillis() - timeStampForReading.getTimeInMillis()) > 4000)
                addReadDataToDB();
        }, CHAR_READ_DELAY);

        timeStampForReading = Calendar.getInstance();
        for (Byte b : txValue) mCharacteristicData.add(b);

        //printCharacteristic(txValue);
    }

    /**
     * Makes use of Jesse's Unpacker and DataManager classes. This essentially reads our local ArrayList
     * of bytes which should be fully popoulated when this method is called. It decodes the data which
     * allows us to populate the SQLite database.
     */
    private void addReadDataToDB() {
        showMessage("Characteristics read, adding data to database.");
        Log.i(TAG, "addReadDataTDB..");
        // Two byte arrays. One with header information, one with the rest of the information.
        byte[] informationBytes = new byte[6];
        byte[] dataBytes = new byte[mCharacteristicData.size() - 6];
        // Populate the byte arrays using our local ArrayList.
        for (int i = 0; i < 6; i++) informationBytes[i] = mCharacteristicData.get(i);
        for (int i = 6; i < mCharacteristicData.size(); i++)
            dataBytes[i - 6] = mCharacteristicData.get(i);
        // DEBUG*******
        for (int i = 0; i < informationBytes.length; i++)
            Log.i(TAG, "Info: " + Byte.toString(informationBytes[i]));
        for (int i = 0; i < dataBytes.length; i++)
            Log.i(TAG, "Data: " + Byte.toString(dataBytes[i]));
        // ************
        // Setup classes for decoding data.
        DataManager d = new DataManager(dataBytes);
        Unpacker p = new Unpacker(d);
        p.setTransmissionDetails(informationBytes);
        // Reads byte data and decodes it to make ArrayList of records
        ArrayList<Unpacker.Record> records = new ArrayList<Unpacker.Record>();
        while (d.getDataCounter() < dataBytes.length - 1) {
            System.out.println(d.getDataCounter() + "START");
            p.readHeader();
            records.add(p.decode());
            System.out.println(d.getDataCounter() + "END");
        }
        p.clearPreviousRecord();
        // Creates weather objects from records and stores in Array.
        ArrayList<WeatherDataObject> weatherData = new ArrayList<WeatherDataObject>();
        for (Unpacker.Record r : records)
            weatherData.add(new WeatherDataObject(
                    r.getStationID(),
                    new double[]{
                            r.getTemperature(true).doubleValue(),
                            r.getPressure(true).doubleValue(),
                            r.getWindSpeed(true).doubleValue(),
                            r.getWindDirection(),
                            r.getRainfall(true).doubleValue(),
                            r.getHumidity(true).doubleValue()
                    },
                    r.getTime(),
                    true
            ));
        // Passes weather object array to sql helper for adding to database.
        dbHelper.insertRowList(weatherData);
    }

    // Helper method to display the contents of a byte array as a String. Useful for testing.
    private void printCharacteristic(byte[] txValue) {
        try {
            String text = new String(txValue, "UTF-8");
            Log.i(TAG, text);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
    // End Region

    // For easy showing of toasts throughout the application code.
    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
