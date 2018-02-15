package com.example.weatherproject2017.weatherapp.data;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * Class for utility methods related to converting data between various formats, and creating
 * fake test data.
 */

public class DataUtils {
    private final static String TAG = DataUtils.class.getSimpleName();

    private DataUtils(Context context) {
    }

    public static LineGraphSeries<DataPoint> getLineGraphSeries(ArrayList<WeatherDataObject> weatherData, String sensor, Long[] timeStamps) {

        DataPoint[] dataPoints = new DataPoint[timeStamps.length];
        switch (sensor) {
            case "Temperature":
                for (int i = 0; i < weatherData.size(); i++) dataPoints[i] = new DataPoint(timeStamps[i], weatherData.get(i).getTemp());
                break;
            case "Wind Speed":
                for (int i = 0; i < weatherData.size(); i++) dataPoints[i] = new DataPoint(timeStamps[i], weatherData.get(i).getWindSpeed());
                break;
            case "Rainfall":
                for (int i = 0; i < weatherData.size(); i++) dataPoints[i] = new DataPoint(timeStamps[i], weatherData.get(i).getRainfall());
                break;
            case "Pressure":
                for (int i = 0; i < weatherData.size(); i++) dataPoints[i] = new DataPoint(timeStamps[i], weatherData.get(i).getRainfall());
                break;
            case "Humidity":
                for (int i = 0; i < weatherData.size(); i++) dataPoints[i] = new DataPoint(timeStamps[i], weatherData.get(i).getHumidity());
                break;
            /*case "TemperatureOrdered":

                for (int i = 0; i < weatherData.size(); i++) dataPoints[i] = new DataPoint(unixToHHMM(timeStamps[i]), weatherData.get(i).getTemp());
                break;*/
        }

        return new LineGraphSeries<DataPoint>(dataPoints);
    }

    //turns 360 degrees into a string points on a compass
    public static String degreesToCompass(double degrees){
        String compass;
        if( (degrees >= 348.75) && (degrees <= 360) ||
                (degrees >= 0) && (degrees <= 11.25)    ){
            compass = "N";
        } else if( (degrees >= 11.25 ) && (degrees <= 33.75)){
            compass = "NNE";
        } else if( (degrees >= 33.75 ) &&(degrees <= 56.25)){
            compass = "NE";
        } else if( (degrees >= 56.25 ) && (degrees <= 78.75)){
            compass = "ENE";
        } else if( (degrees >= 78.75 ) && (degrees <= 101.25) ){
            compass = "E";
        } else if( (degrees >= 101.25) && (degrees <= 123.75) ){
            compass = "ESE";
        } else if( (degrees >= 123.75) && (degrees <= 146.25) ){
            compass = "SE";
        } else if( (degrees >= 146.25) && (degrees <= 168.75) ){
            compass = "SSE";
        } else if( (degrees >= 168.75) && (degrees <= 191.25) ){
            compass = "S";
        } else if( (degrees >= 191.25) && (degrees <= 213.75) ){
            compass = "SSW";
        } else if( (degrees >= 213.75) && (degrees <= 236.25) ){
            compass = "SW";
        } else if( (degrees >= 236.25) && (degrees <= 258.75) ){
            compass = "WSW";
        } else if( (degrees >= 258.75) && (degrees <= 281.25) ){
            compass = "W";
        } else if( (degrees >= 281.25) && (degrees <= 303.75) ){
            compass = "WNW";
        } else if( (degrees >= 303.75) && (degrees <= 326.25) ){
            compass = "NW";
        } else if( (degrees >= 326.25) && (degrees <= 348.75) ){
            compass = "NNW";
        } else {
            compass = "?";
        }

        return compass;
    }

    // Test method, includes rowID and isNewData so can see every variable in Log.
    public static JSONObject toJsonForTesting(WeatherDataObject wd) {
        try {
            final JSONObject jo = new JSONObject();
            jo.put(DatabaseHelper.WEATHER_COLUMN_ID, wd.getRowID());
            jo.put(DatabaseHelper.WEATHER_COLUMN_STATIONID, wd.getStationID());
            jo.put(DatabaseHelper.WEATHER_COLUMN_TEMP, wd.getTemp());
            jo.put(DatabaseHelper.WEATHER_COLUMN_PRESSURE, wd.getPressure());
            jo.put(DatabaseHelper.WEATHER_COLUMN_WINDSPEED, wd.getWindSpeed());
            jo.put(DatabaseHelper.WEATHER_COLUMN_WINDDIRECTION, wd.getWindDirection());
            jo.put(DatabaseHelper.WEATHER_COLUMN_RAINFALL, wd.getRainfall());
            jo.put(DatabaseHelper.WEATHER_COLUMN_HUMIDITY, wd.getHumidity());
            jo.put(DatabaseHelper.WEATHER_COLUMN_TIMESTAMP, wd.getTimeStamp());
            jo.put(DatabaseHelper.WEATHER_COLUMN_NEWDATA, wd.isNewData());
            return jo;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static int getRandomStationID() {
        Random r = new Random();
        return r.nextInt(50)+1;
    }

    /**
     * Returns random latitude in Rimutaka region
     * -41.236242 to -41.367523
     * @return
     */
    public static double getRandomLatitude() {
        Random r = new Random();
        double d = r.nextInt(367523-236242);
        d = d/1000000;
        d = 0-41-0.236242-d;
        return round(d,6);
    }

    /**
     * Returns random longitude in Rimutaka region
     * 174.932067 to 175.116774
     * @return
     */
    public static double getRandomLongitude() {
        Random r = new Random();
        double d = r.nextInt(1116774-932067);
        d = d/1000000;
        d = 174.932067+d;
        return round(d,6);
    }

    // For testing returns random date 2010-2017.
    public static Long getRandomDate() {
        Random r = new Random();
        Calendar c = Calendar.getInstance();
        c.set((r.nextInt(8) + 2010), r.nextInt(11), r.nextInt(28));
        return c.getTimeInMillis() / 1000;
    }

    // For testing, returns true/false
    public static boolean getRandomBoolean() {
        Random r = new Random();
        int i = r.nextInt(2);
        if (i == 1) return true;
        else return false;
    }

    // For testing, returns double[] with random values between min/max for each sensor.
    public static double[] getRandomSensors() {
        Random r = new Random();
        double[] sensors = new double[6];
        sensors[0] = DataUtils.round(r.nextInt(60) + r.nextDouble() - 40, 1);
        sensors[1] = DataUtils.round(r.nextInt(301) + r.nextDouble() + 850, 1);
        sensors[2] = DataUtils.round(r.nextInt(409) + r.nextDouble() + 0.5, 1);
        sensors[3] = DataUtils.round(r.nextInt(361), 1);
        sensors[4] = DataUtils.round(r.nextInt(409) + r.nextDouble() + 0.5, 1);
        sensors[5] = DataUtils.round(r.nextInt(100) + r.nextDouble(), 1);
        return sensors;
    }

    // Code from StackOverflow. Rounds decimal places.
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    //Converts time stamp to dd.MM.yy HH:mm
    public static String unixToDate(Long time) {
        //Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time*1000);
        Date d = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy HH:mm");
        return sdf.format(d).toString();
    }

    //Converts string unix timestamp to HHmm as an int
    public static int unixToHHMM(long l) {
        DateFormat format = null;
        Date date;
        int hhmm;
        date = new Date();
        date.setTime((long) l * 1000);
        format = new SimpleDateFormat("HHmm");
        hhmm = Integer.parseInt(format.format(date));
        return hhmm;
    }

    //Calculate the difference in time between the current time and a unix timestamp
    public static String calcTimeDiff(long timestamp){
        String timeDiff = null;
        long currentTime = System.currentTimeMillis() / 1000;
        int diff = (int) ((currentTime - timestamp) / 60);
        //Log.i(TAG, ""+diff);
        if(diff > 24 *  60){
            timeDiff = diff / 24 / 60 + " days ago";
        }
            else if(diff > 60)
                timeDiff = diff / 60 + " hours ago";
        else
            timeDiff = String.valueOf(diff) + " minutes ago";
        
        return timeDiff;
    }
}
