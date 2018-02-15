package com.example.weatherproject2017.weatherapp.network;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Service for binding SyncAdapter
 *
 * See https://developer.android.com/training/sync-adapters/creating-sync-adapter.html
 */

public class SyncService extends Service {
    private static final String TAG = SyncService.class.getSimpleName();

    private static SyncAdapter sSyncAdapter = null;
    // Object to use as a thread safe lock
    private static final Object sSyncAdapterLock = new Object();

    @Override
    public void onCreate() {
        super.onCreate();
        /*
         * Create the sync adapter as a singleton.
         * Set the sync adapter as syncable
         * Disallow parallel syncs
         */
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
        Log.i(TAG, "SyncService started.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        /*
         * Get the object that allows external processes
         * to call onPerformSync(). The object is created
         * in the base class code when the SyncAdapter
         * constructors call super()
         */
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
