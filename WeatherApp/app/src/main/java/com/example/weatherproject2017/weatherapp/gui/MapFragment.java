package com.example.weatherproject2017.weatherapp.gui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.weatherproject2017.weatherapp.R;
import com.example.weatherproject2017.weatherapp.data.DataUtils;
import com.example.weatherproject2017.weatherapp.data.DatabaseHelper;
import com.example.weatherproject2017.weatherapp.data.WeatherDataObject;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.InfoWindowAdapter {

    public static MapFragment newInstance() {

        Bundle args = new Bundle();

        MapFragment fragment = new MapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        googleMap.setInfoWindowAdapter(this);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(mContext);

        // Start map in Rimutaka region.
        LatLng centreLatLng = new LatLng(-41.300313, 175.029270);

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        ArrayList<WeatherDataObject> weatherData = dbHelper.getMostRecentRowForAllStations();

        if (weatherData!=null&&weatherData.size()>0) for (WeatherDataObject wd : weatherData) addStationMarkers(wd);

        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centreLatLng, 13));
    }

    //Populate map with markers for each weather station in sqlite database
    public void addStationMarkers(WeatherDataObject wd) {
        LatLng latlng = new LatLng(wd.getLatitude(), wd.getLongitude());
        //LatLng latlng = new LatLng(-41.284544, 174.963720);

        //place markers with custom infomation
        mMap.addMarker(new MarkerOptions()
                .title("Weather Station ID: " + String.valueOf(wd.getStationID()))
                .snippet("Temperature: " + String.valueOf(wd.getTemp()) + "°C" +
                        "\n" + "Wind Speed: " + String.valueOf(wd.getWindSpeed()) + "km/h" +
                                "\n" + "Wind Direction: " + String.valueOf(wd.getWindDirection()) +
                        "\n" + "Rainfall: " + String.valueOf(wd.getRainfall()) + "mm"  +
                                "\n" + "Humidity: " + String.valueOf(wd.getHumidity() + "%" +
                        "\n" + "Pressure: " + String.valueOf(wd.getPressure()) + "hPa" +
                        "\n" + "Time of reading: " + DataUtils.calcTimeDiff(wd.getTimeStamp()))
                        )
                .position(latlng));
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null)
            getChildFragmentManager().beginTransaction().remove(mapFragment).commitAllowingStateLoss();
    }


    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    //display Info window
    @Override
    public View getInfoContents(Marker marker) {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.info_window_layout, null);

        TextView titleTextView = (TextView) view.findViewById(R.id.title);
        TextView snippetTextView = (TextView) view.findViewById(R.id.snippet);

        titleTextView.setText(marker.getTitle());
        snippetTextView.setText(marker.getSnippet());

        return view;
    }
}