package com.example.weatherproject2017.weatherapp.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.example.weatherproject2017.weatherapp.R;

/**
 * Class for displaying and responding to preference changes. Currently it doesn't do any responding
 * as the only pref that works for now is whether the user allows data upload over mobile or just wifi
 * connection. This is checked in SyncAdapter which handles the uploading.
 */

public class PreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final static String TAG = PreferenceFragment.class.getSimpleName();
    public final static String KEY_PREF_AUTOSCAN = "pref_autoscan";
    public final static String KEY_PREF_MOBILEDATA = "pref_mobiledata";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * This is where any changes to preferences would be detected and then any code that needs to
     * run in response to that would be executed.
     *
     * @param sharedPreferences The variable containing all prefs for the app.
     * @param key The specific key for the preference that was changed triggering this method.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // TODO: 16/10/2017  Not sure if anything needs to go here..
    }
}

