package com.example.weatherproject2017.weatherapp.network;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.weatherproject2017.weatherapp.data.DataUtils;
import com.example.weatherproject2017.weatherapp.data.DatabaseHelper;
import com.example.weatherproject2017.weatherapp.data.WeatherDataObject;

import java.util.ArrayList;

/**
 * A SyncAdapter for performing background network communication with our online server.
 */

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = SyncAdapter.class.getSimpleName();

    ContentResolver contentResolver;
    Context context;
    RequestQueue q;

    public SyncAdapter(Context context, boolean autoInitialise) {
        super(context, autoInitialise);

        constructorSetup(context);
    }

    public SyncAdapter(Context context, boolean autoInitialise, boolean allowParallelSyncs) {
        super(context, autoInitialise, allowParallelSyncs);

        constructorSetup(context);
    }

    private void constructorSetup(Context c) {
        this.context = c;
        this.contentResolver = c.getContentResolver();
        this.q = Volley.newRequestQueue(c);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        if (!SyncUtils.canUseNetwork()) {
            Log.i(TAG, "Error, cannot use network at this time.");
            return;
        }

        this.q = Volley.newRequestQueue(context);

        Log.i(TAG, "SyncAdapter: Syncing...");
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this.context);

        if (dbHelper.isThereNewData()) {
            Log.i(TAG, "Sending new data.");
            ArrayList<WeatherDataObject> weatherData = dbHelper.getNewData();
            SyncUtils.postJsonToWeb(weatherData, q);
            Log.i(TAG, "Telling database to update flags.");
            dbHelper.updateNewDataFlag();
        }
        else Log.i(TAG, "No new data.");
    }
}
