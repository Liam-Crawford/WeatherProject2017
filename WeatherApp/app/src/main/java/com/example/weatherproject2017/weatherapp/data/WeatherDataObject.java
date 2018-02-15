package com.example.weatherproject2017.weatherapp.data;

/**
 * Created by crawf_000 on 6/09/2017.
 */

/**
 * Class to store weather sensor information. May not be necessary at this stage.
 */
public class WeatherDataObject {
    private int rowID;      // For checking which row it was in database.
    private int stationID;
                                    //  min     max     units
    private double temp;            //  -30.0   125.0   Celcius
    private double pressure;        //  300.0   1110.0  HPA
    private double windSpeed;       //  0       409.5   km/h
    private double windDirection;   //  0       360     Degrees
    private double rainfall;        //  0       409.5   mm/h ??
    private double humidity;        //  0       100.0   Percentage

    private Long timeStamp; // Unix Time.
    private boolean newData;

    private double latitude = 0;
    private double longitude = 0;

    private String[] allValuesAsString;

    public WeatherDataObject(int stationID, double[] sensorValues, Long timeStamp, boolean newData) {
        this.stationID = stationID;
        this.temp = sensorValues[0];
        this.pressure = sensorValues[1];
        this.windSpeed = sensorValues[2];
        this.windDirection = sensorValues[3];
        this.rainfall = sensorValues[4];
        this.humidity = sensorValues[5];
        this.timeStamp = timeStamp;
        this.newData = newData;

        allValuesAsString = getValuesAsString();
    }

    public void setRowID(int id) { this.rowID = id; }
    public int getRowID() { return this.rowID; }

    public int getStationID() { return this.stationID; }

    public double getTemp() { return this.temp; }
    public double getPressure() { return this.pressure; }
    public double getWindSpeed() { return this.windSpeed; }
    public double getWindDirection() { return this.windDirection; }
    public double getRainfall() { return this.rainfall; }
    public double getHumidity() { return this.humidity; }

    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public double getLatitude() { return this.latitude; }
    public double getLongitude() { return this.longitude; }

    public Long getTimeStamp() {
        return this.timeStamp;
    }

    public boolean isNewData() { return this.newData; }

    public String[] getValuesAsString() {
        return new String[]{
                String.valueOf(this.stationID),
                String.valueOf(this.temp),
                String.valueOf(this.pressure),
                String.valueOf(this.windSpeed),
                String.valueOf(this.windDirection),
                String.valueOf(this.rainfall),
                String.valueOf(this.humidity),
                String.valueOf(this.timeStamp),
        };
    }

    public double[] getAllSensorValues() { return new double[]{this.temp, this.pressure,
            this.windSpeed, this.windDirection, this.rainfall, this.humidity}; }

    // Region return String values.

    public String getTemperatureAsString() {
        return this.allValuesAsString[1];
    }

    public String getPressureAsString() {
        return this.allValuesAsString[2];
    }

    public String getWindSpeedAsString() {
        return this.allValuesAsString[3];
    }

    public String getWindDirectionAsString() {
        return this.allValuesAsString[4];
    }

    public String getRainFallAsString() {
        return this.allValuesAsString[5];
    }

    public String getHumidityAsString() {
        return this.allValuesAsString[6];
    }

    public String getTimeStampAsString() {
        return this.allValuesAsString[7];
    }

    // End Region.

    public String toString() {
        return ("WeatherDataObject: "+
        this.stationID+", "+this.temp+", "+this.pressure+", "+this.windSpeed+", "+
        this.windDirection+", "+this.rainfall+", "+this.humidity+", "+this.latitude+", "+
        this.longitude+", "+this.newData+", "+this.timeStamp+" - "+DataUtils.unixToDate(this.timeStamp));
    }
}
