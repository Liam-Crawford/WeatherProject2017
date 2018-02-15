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

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.weatherproject2017.weatherapp.R;
import com.example.weatherproject2017.weatherapp.data.DataUtils;
import com.example.weatherproject2017.weatherapp.data.DatabaseHelper;
import com.example.weatherproject2017.weatherapp.data.WeatherDataObject;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Provides UI for the view with Graph.
 */
public class LineGraphContentFragment extends Fragment {

    private final static String TAG = LineGraphContentFragment.class.getSimpleName();
    private ArrayList<WeatherDataObject> weatherData;
    private Long[] timeStamps;
    private int[] timeStampsDefault = new int[]{1, 2, 3, 4}; // For testing
    private boolean useDefaultTimeStamps = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(
                R.layout.recycler_view, container, false);

        ContentAdapter adapter = new ContentAdapter(recyclerView.getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Pull data from SQLite and store in local variable for easy access.
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(container.getContext());
        weatherData = dbHelper.getAllData();

        // Make local array for time stamps as they will be the same for each of the 5 graphs.
        if (weatherData!=null&&weatherData.size()>0) {
            timeStamps = new Long[weatherData.size()];
            for (int i = 0; i < weatherData.size(); i++) {
                timeStamps[i] = weatherData.get(i).getTimeStamp();
            }
        }
        else useDefaultTimeStamps = true;

        return recyclerView;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public GraphView mGraph;

        public ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_linegraph, parent, false));

            name = (TextView) itemView.findViewById(R.id.linegraph_title);
            mGraph = (GraphView) itemView.findViewById(R.id.linegraph);

            if (useDefaultTimeStamps) {
                mGraph.getViewport().setMinX(timeStampsDefault[0]);
                mGraph.getViewport().setMaxX(timeStampsDefault[2]);
            } else {
                mGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity()));
                mGraph.getGridLabelRenderer().setNumHorizontalLabels(3);
                mGraph.getGridLabelRenderer().setHumanRounding(false);
                mGraph.getViewport().setXAxisBoundsManual(true);

                mGraph.getViewport().setMinX(timeStamps[0]);
                mGraph.getViewport().setMaxX(timeStamps[2]);
                mGraph.getViewport().setScrollable(true);
            }
        }
    }

    /**
     * Adapter to display recycler view.
     */
    public class ContentAdapter extends RecyclerView.Adapter<ViewHolder> {
        // This number defines exactly how many ViewHolders will be added. It will need to be
        // <= the amount of values in the String Arrays it is using otherwise you will get
        // array out of bounds errors.
        private static final int LENGTH = 5;

        private final String[] graphTitles;
        //private HashMap<String, GraphView> graphRef = new HashMap<String, GraphView>();

        public ContentAdapter(Context context) {
            Resources resources = context.getResources();
            graphTitles = resources.getStringArray(R.array.graphTitles);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String graphTitle = graphTitles[position % graphTitles.length];
            holder.name.setText(graphTitle);
            GraphView mGraph = holder.mGraph;
            if (!useDefaultTimeStamps) mGraph.addSeries(DataUtils.getLineGraphSeries(weatherData, graphTitle, timeStamps));
        }

        @Override
        public int getItemCount() {
            return LENGTH;
        }
    }
}
