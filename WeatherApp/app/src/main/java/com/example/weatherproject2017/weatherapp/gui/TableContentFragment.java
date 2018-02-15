package com.example.weatherproject2017.weatherapp.gui;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.weatherproject2017.weatherapp.R;
import com.example.weatherproject2017.weatherapp.data.DataUtils;
import com.example.weatherproject2017.weatherapp.data.DatabaseHelper;
import com.example.weatherproject2017.weatherapp.data.WeatherDataObject;

import java.util.ArrayList;

/**
 * Provides UI for the view with a WeatherDataTable.
 */
public class TableContentFragment extends Fragment {
    private static final String[] headerNames = new String[]{"Station", "Temperature", "Pressure", "Wind Speed", "Wind Direction", "Rainfall", "Humidity", "Time"};
    private static final String[] unitNames = new String[]{"ID", "°C", "hPa", "km/h", " ", "mm", "%", " "};
    private ArrayList<WeatherDataObject> weatherData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(
                R.layout.recycler_view, container, false);
        ContentAdapter adapter = new ContentAdapter(recyclerView.getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(container.getContext());
        weatherData = dbHelper.getMostRecentRowForAllStations();

        return recyclerView;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_table, parent, false));

            Context c = parent.getContext();

            TableLayout tableLayout = (TableLayout) itemView.findViewById(R.id.table_tableLayout);
            TableRow tableRow = new TableRow(c);
            for (String s : headerNames) {
                tableRow.addView(getHeaderViewColumn(s, c));
            }
            tableLayout.addView(tableRow);

             tableRow = new TableRow(c);
            for (String s : unitNames) {
                tableRow.addView(getHeaderViewColumn(s, c));
            }
            tableLayout.addView(tableRow);

            if (weatherData!=null&&weatherData.size()>0) {
                for (WeatherDataObject wd : weatherData) {
                    tableRow = new TableRow(c);
                    String[] s = wd.getValuesAsString();
                    for (int i = 0; i < s.length-1; i++) {
                        if(i == 4){
                            tableRow.addView(getTextViewColumn(DataUtils.degreesToCompass(wd.getWindDirection()), c));
                        }
                        else
                            tableRow.addView(getTextViewColumn(s[i], c));
                    }
                    tableRow.addView(getTextViewColumn(DataUtils.unixToDate(wd.getTimeStamp()), c));
                    tableLayout.addView(tableRow);
                }
            }
        }
    }

    private TextView getHeaderViewColumn(String name, Context c) {
        TextView textView = new TextView(c);
        textView.setText(name);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(10, 1, 10, 1);
        textView.setTypeface(null, Typeface.BOLD);
        //textView.setTextColor(Color.BLACK);
        return textView;
    }

    private TextView getTextViewColumn(String name, Context c) {
        TextView textView = new TextView(c);
        textView.setText(name);
        textView.setGravity(Gravity.RIGHT);
        textView.setPadding(10, 1, 10, 1);
        //textView.setTextColor(Color.BLACK);
        return textView;
    }

    public class ContentAdapter extends RecyclerView.Adapter<ViewHolder> {

        public ContentAdapter(Context context) {

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 1;
        }
    }


}