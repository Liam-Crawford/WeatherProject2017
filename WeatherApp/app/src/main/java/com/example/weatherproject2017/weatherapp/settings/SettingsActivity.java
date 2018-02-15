package com.example.weatherproject2017.weatherapp.settings;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by crawf_000 on 16/09/2017.
 */

public class SettingsActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new PreferencesFragment()).commit();
    }

}
