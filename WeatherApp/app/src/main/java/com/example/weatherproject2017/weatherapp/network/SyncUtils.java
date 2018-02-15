package com.example.weatherproject2017.weatherapp.network;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.example.weatherproject2017.weatherapp.data.DatabaseHelper;
import com.example.weatherproject2017.weatherapp.data.WeatherDataObject;
import com.example.weatherproject2017.weatherapp.settings.PreferencesFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Taken from download sample at https://developer.android.com/training/sync-adapters/creating-authenticator.html
 */

public class SyncUtils {
    private static final String TAG = SyncUtils.class.getSimpleName();

    private static final String URLPost = "http://wltcweathermonitor.com/postDataAndroid";
    private static final String URLGet = "http://wltcweathermonitor.com/getDataAndroid";

    private static final String AUTHORITY = "com.example.weatherproject2017.weatherapp";
    private static final String PREF_SETUP_COMPLETE = "setup_complete";
    private static final long SECONDS_PER_MINUTE = 60L;
    private static final long SYNC_INTERVAL_IN_MINUTES = 60L;
    private static final long SYNC_INTERVAL = SYNC_INTERVAL_IN_MINUTES * SECONDS_PER_MINUTE; //1 hour

    private static Context c;

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static void CreateSyncAccount(Context context) {
        Log.i(TAG, "CreateSyncAccount started.");
        c = context;
        boolean newAccount = false;
        boolean setupComplete = PreferenceManager
                .getDefaultSharedPreferences(context).getBoolean(PREF_SETUP_COMPLETE, false);
        // Create the account type and default account
        Account account = AuthenticatorService.getAccount();
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(account, null, null)) {
            Log.i(TAG, "Account created successfully.");
            ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
            ContentResolver.addPeriodicSync(account, AUTHORITY, new Bundle(), SYNC_INTERVAL);
            newAccount = true;
        }
        // For testing we force a sync on each app reboot to see it working.
        if (!newAccount) {
            Log.i(TAG, "Not a new account, triggering refresh.");
            TriggerRefresh();
        }

        // Schedule an initial sync if we detect problems with either our account or our local
        // data has been deleted. (Note that it's possible to clear app data WITHOUT affecting
        // the account list, so wee need to check both.)
        if (newAccount || !setupComplete) {
            Log.i(TAG, "New account, triggering refresh.");
            TriggerRefresh();
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean(PREF_SETUP_COMPLETE, true).apply();
        }
    }

    /**
     * Helper method to trigger an immediate sync ("refresh").
     *
     * This should only be used when we need to preempt the normal sync schedule. Typically, this
     * means the user has pressed the "refresh" button.
     *
     * Note that SYNC_EXTRAS_MANUAL will cause an immediate sync, without any optimization to
     * preserve battery life. If you know new data is available (perhaps via a GCM notification),
     * but the user is not actively waiting for that data, you should omit this flag; this will give
     * the OS additional freedom in scheduling your sync request.
     */
    public static void TriggerRefresh() {
        Bundle b = new Bundle();
        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(
                AuthenticatorService.getAccount(),      // Sync account
                AUTHORITY,                              // Content authority
                b);                                     // Extras
    }

    /**
     * Volley implementation for POST
     *
     * Takes an array of WeatherDataObjects and iterates through them sending 1 by 1
     * to the remote server. Uses a helper method to convert the WeatherDataObject to a JSONObject.
     */
    public static void postJsonToWeb(ArrayList<WeatherDataObject> sensors, RequestQueue q) {
        for (WeatherDataObject wd : sensors) {
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, URLPost, toJson(wd), response -> {
                Log.i(TAG, response.toString());
                Log.i(TAG, "POST success.");
            }, error -> {
                Log.w(TAG, error.toString());
                Log.w(TAG, "POST Error.");
            });

            // Add request to Volley queue for execution.
            q.add(req);
        }
    }

    /**
     * Volley implementation for GET.
     *
     * Receives a JsonObject of JSONObjects from the remote server. This object is converted to a
     * JSONArray of JSONObjects which can be iterated over. Each JSONObject is used to create
     * a WeatherDataObject which can then be passed to DatabaseHelper and inserted into the SQLite database.
     */
    public static void getJsonFromWeb(JSONObject jo, RequestQueue q, final DatabaseHelper dbHelper) {
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, URLGet, jo, response -> {
            Log.i(TAG, "Response length: "+response.length());
            try {
                // Convert response from JSONObject to JSONArray for iteration.
                JSONArray jay = response.getJSONArray("resultData");
                Log.i(TAG, "JSONArray length: "+jay.length());

                // Iterate over JSONArray.
                for (int i = 0; i < jay.length(); i++) {
                    // The JSONArray is made up of JSONObjects.
                    JSONObject jo1 = jay.getJSONObject(i);

                    // Instantiate a WeatherDataObject from the values in the JSONObject.
                    WeatherDataObject wd = new WeatherDataObject(jo1.getInt("station_id"), new double[]{
                            jo1.getDouble("temp"),
                            jo1.getDouble("pressure"),
                            jo1.getDouble("wind_speed"),
                            jo1.getDouble("wind_direction"),
                            jo1.getDouble("rainfall"),
                            jo1.getDouble("humidity")
                    }, jo1.getLong("date_received"), false);

                    // Pass WeatherDataObject to DatabaseHelper to be inserted into database.
                    dbHelper.insertRowSingle(wd);
                }
            } catch (JSONException e) { e.printStackTrace(); }

            Log.i(TAG, "GET success.");

        }, error -> {
            Log.w(TAG, error.toString());
            Log.w(TAG, "GET Error.");
        });

        // Add request to the Volley queue for execution.
        q.add(req);
    }

    // Converts a WeatherDataObject into a JSONObject.
    // Currently not using rowID and isNewData as the remote db doesn't need to know this.
    public static JSONObject toJson(WeatherDataObject wd) {
        try {
            final JSONObject jo = new JSONObject();
            //jo.put(DatabaseHelper.WEATHER_COLUMN_ID, wd.getRowID());
            jo.put(DatabaseHelper.WEATHER_COLUMN_STATIONID, wd.getStationID());
            jo.put(DatabaseHelper.WEATHER_COLUMN_TEMP, wd.getTemp());
            jo.put(DatabaseHelper.WEATHER_COLUMN_PRESSURE, wd.getPressure());
            jo.put(DatabaseHelper.WEATHER_COLUMN_WINDSPEED, wd.getWindSpeed());
            jo.put(DatabaseHelper.WEATHER_COLUMN_WINDDIRECTION, wd.getWindDirection());
            jo.put(DatabaseHelper.WEATHER_COLUMN_RAINFALL, wd.getRainfall());
            jo.put(DatabaseHelper.WEATHER_COLUMN_HUMIDITY, wd.getHumidity());
            jo.put("date_received", wd.getTimeStamp());
            //jo.put(DatabaseHelper.WEATHER_COLUMN_NEWDATA, wd.isNewData());
            return jo;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Checks user preferences for whether they allow uploading over mobile connection. Based on this
     * and what the device is currently connected to, it will return true or false.
     *
     * @return Boolean representing whether the device can upload data over internet.
     */
    public static boolean canUseNetwork() {
        boolean wifiConnected = false;
        boolean mobileConnected = false;

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
        boolean mobilePref = sharedPrefs.getBoolean(PreferencesFragment.KEY_PREF_MOBILEDATA, false);

        ConnectivityManager connMgr = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();

        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            if (mobileConnected&&mobilePref) return true;
            else if (mobileConnected&&!mobilePref) return false;
            if (wifiConnected) return true;
        }

        return false;
    }
}
