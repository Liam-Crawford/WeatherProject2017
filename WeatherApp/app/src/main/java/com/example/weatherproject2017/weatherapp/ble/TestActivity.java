package com.example.weatherproject2017.weatherapp.ble;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.weatherproject2017.weatherapp.R;
import com.example.weatherproject2017.weatherapp.data.DataUtils;
import com.example.weatherproject2017.weatherapp.data.DatabaseHelper;
import com.example.weatherproject2017.weatherapp.data.WeatherDataObject;
import com.example.weatherproject2017.weatherapp.network.SyncUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class TestActivity extends Activity {
    private final static String TAG = TestActivity.class.getSimpleName();
    private DatabaseHelper dbHelper;
    private RequestQueue requestQueue;
    private TextView textView;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        textView = (TextView) findViewById(R.id.textView_test);
        scrollView = (ScrollView) findViewById(R.id.scrollView_test);

        dbHelper = new DatabaseHelper(this);
        requestQueue = Volley.newRequestQueue(this);

        appendToOutput(""+dbHelper.getNumberOfRows());
    }

    private void appendToOutput(String outputText) {
        textView.append("\n"+outputText);
    }

    //**********************************************

    // Region Testing

    // To receive input from UI button
    public void dbTest(View view) {

    }

    public void postTest(View view) {
        ArrayList<WeatherDataObject> sensors = dbHelper.getAllData();
        SyncUtils.postJsonToWeb(sensors, requestQueue);
    }

    public void getTest(View view) {
        JSONObject jo = new JSONObject();
        Calendar date = Calendar.getInstance();
        date.set(2016, 5, 13);
        try {
            jo.put("dateFrom", date.getTimeInMillis() / 1000);
            date.set(2017, 7, 25);
            jo.put("dateTo", date.getTimeInMillis() / 1000);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SyncUtils.getJsonFromWeb(jo, requestQueue, dbHelper);
    }
}
