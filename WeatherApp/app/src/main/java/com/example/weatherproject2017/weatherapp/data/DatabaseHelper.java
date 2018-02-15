package com.example.weatherproject2017.weatherapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private final static String TAG = DatabaseHelper.class.getSimpleName();
    private static DatabaseHelper instance;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "weatherdata.db";

    // Database table column names
    public static final String WEATHER_TABLE_NAME = "weatherdata";
    public static final String WEATHER_COLUMN_ID = "id";                        // Integer primary key
    public static final String WEATHER_COLUMN_STATIONID = "station_id";         // SQL Integer, Java int
    public static final String WEATHER_COLUMN_TEMP = "temp";                    // SQL Real, Java double
    public static final String WEATHER_COLUMN_PRESSURE = "pressure";            // SQL Real, Java double
    public static final String WEATHER_COLUMN_WINDSPEED = "wind_speed";         // SQL Real, Java double
    public static final String WEATHER_COLUMN_WINDDIRECTION = "wind_direction"; // SQL Real, Java double
    public static final String WEATHER_COLUMN_RAINFALL = "rainfall";            // SQL Real, Java double
    public static final String WEATHER_COLUMN_HUMIDITY = "humidity";            // SQL Real, Java double
    public static final String WEATHER_COLUMN_TIMESTAMP = "timestamp";          // SQL Integer, Java Long
    public static final String WEATHER_COLUMN_NEWDATA = "new_data";             // SQL Integer, Java boolean

    private static final String STATION_TABLE_NAME = "stationdata";
    private static final String STATION_COLUMN_ID = "id";                        // Integer primary key
    private static final String STATION_COLUMN_STATIONID = "station_id";
    private static final String STATION_COLUMN_LATITUDE = "latitude";            // SQL Real, Java double
    private static final String STATION_COLUMN_LONGITUDE = "longitude";          // SQL Real, Java double

    private static final String SQL_CREATE_WEATHERTABLE = "CREATE TABLE " + WEATHER_TABLE_NAME + " ("
            + WEATHER_COLUMN_ID + " INTEGER PRIMARY KEY, " + WEATHER_COLUMN_STATIONID + " INTEGER, "
            + WEATHER_COLUMN_TEMP + " REAL, " + WEATHER_COLUMN_PRESSURE + " REAL, "
            + WEATHER_COLUMN_WINDSPEED + " REAL, " + WEATHER_COLUMN_WINDDIRECTION + " REAL, "
            + WEATHER_COLUMN_RAINFALL + " REAL, " + WEATHER_COLUMN_HUMIDITY + " REAL, "
            + WEATHER_COLUMN_TIMESTAMP + " INTEGER, " + WEATHER_COLUMN_NEWDATA + " INTEGER, "
            + "FOREIGN KEY("+WEATHER_COLUMN_STATIONID+") REFERENCES "+STATION_TABLE_NAME+"("
            + STATION_COLUMN_ID+"));";

    private static final String SQL_CREATE_STATIONTABLE = "CREATE TABLE " + STATION_TABLE_NAME + " ("
            + STATION_COLUMN_ID + " INTEGER PRIMARY KEY, " + STATION_COLUMN_STATIONID + " INTEGER, "
            + STATION_COLUMN_LATITUDE + " REAL, "+ STATION_COLUMN_LONGITUDE + " REAL);";

    private static final String SQL_DELETE_WEATHERDATA = "DROP TABLE IF EXISTS " + WEATHER_TABLE_NAME;
    private static final String SQL_DELETE_STATIONDATA = "DROP TABLE IF EXISTS " + STATION_TABLE_NAME;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Code I found while reading stackoverflow. Apparently it's safer to return the same instance
     * instead of letting other parts of the app open new connections and worry about closing them.
     * @param context The context with which to get the database.
     * @return The database object.
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) instance = new DatabaseHelper(context.getApplicationContext());
        instance.setForeignKeyOn();
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "Creating new database.");
        db.execSQL(SQL_CREATE_STATIONTABLE);
        db.execSQL(SQL_CREATE_WEATHERTABLE);
        db.execSQL("PRAGMA foreign_keys=ON");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        onCreate(db); // Will add proper upgrade code later.
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: 28/09/2017  
    }

    public void resetDB() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(SQL_DELETE_WEATHERDATA);
        db.execSQL(SQL_DELETE_STATIONDATA);
        onCreate(db);
    }

    /**
     * According to what I read on stackoverflow, foreign key constraint needs to be set every time
     * the database is opened. This is code that will turn it on, and is called when the database is
     * asked for.
     */
    private void setForeignKeyOn() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("PRAGMA foreign_keys=ON");
    }

    /**
     * Selects all rows for a given station from the database using the StationID.
     */
    public ArrayList<WeatherDataObject> getAllRowsFromStation(int stationID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select * from weatherdata where " + WEATHER_COLUMN_STATIONID + " = " + stationID, null);

        return getWeatherDataList(c, db);
    }

    /**
     * Returns a WeatherDataObject of just the most recent row in the db for a given weather station id.
     *
     * Needs to be rewritten to be smarter so instead of querying all rows against the id and returning
     * just the most recent, it queries just the most recent.
     * Could be some good code @ https://stackoverflow.com/questions/40435326/select-the-most-recent-entry
     *
     * @param stationID Uses this parameter to query the database.
     * @return returns a WeatherDataObject representation of the row.
     */
    public WeatherDataObject getMostRecentRowFromStation(int stationID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+WEATHER_TABLE_NAME+" WHERE " + WEATHER_COLUMN_STATIONID + " = " + stationID
                + " ORDER BY "+WEATHER_COLUMN_TIMESTAMP+";", null);
        if (c.moveToFirst()) return createWeatherDataObject(c, db);
        else Log.i(TAG, "Error in getMostRecentRowFromStation, stationID does not exist");

        return null;
    }

    /**
     * Queries the database for all weather stations and returns 1 row for each where the row
     * has the latest time stamp.
     * @return returns this query as an ArrayList of WeatherDataObjects.
     */
    public ArrayList<WeatherDataObject> getMostRecentRowForAllStations() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT MAX("+WEATHER_COLUMN_TIMESTAMP+") as "+WEATHER_COLUMN_TIMESTAMP+", "
                +WEATHER_COLUMN_ID+", "
                +WEATHER_COLUMN_STATIONID+", "
                +WEATHER_COLUMN_TEMP+", "
                +WEATHER_COLUMN_PRESSURE+", "
                +WEATHER_COLUMN_WINDSPEED+", "
                +WEATHER_COLUMN_WINDDIRECTION+", "
                +WEATHER_COLUMN_RAINFALL+", "
                +WEATHER_COLUMN_HUMIDITY+", "
                +WEATHER_COLUMN_NEWDATA+" FROM "+WEATHER_TABLE_NAME+" GROUP BY "+WEATHER_COLUMN_STATIONID
                +" ORDER BY "+WEATHER_COLUMN_STATIONID+";", null);

        /*if (c.moveToFirst()) {
            Log.i(TAG, c.getInt(c.getColumnIndex(WEATHER_COLUMN_STATIONID))+" "
                    +DataUtils.unixToDate(c.getLong(c.getColumnIndex(WEATHER_COLUMN_TIMESTAMP))));
            while (c.moveToNext()) Log.i(TAG, c.getInt(c.getColumnIndex(WEATHER_COLUMN_STATIONID))
                    +" "+DataUtils.unixToDate(c.getLong(c.getColumnIndex(WEATHER_COLUMN_TIMESTAMP))));
        }*/

        return getWeatherDataList(c, db);
    }

    /**
     * Checks if there are any rows in the database with newData flag set to '1' which indicates true.
     * @return true if newData, otherwise false.
     */
    public boolean isThereNewData() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT "+WEATHER_COLUMN_ID+" FROM "+WEATHER_TABLE_NAME+
                " WHERE "+WEATHER_COLUMN_NEWDATA+" = '1';", null);

        if (c.moveToFirst()) {
            c.close();
            return true;
        }
        c.close();
        return false;
    }

    /**
     * Selects data from database with 'New' flag. This is data to be considered for uploading to remote db.
     * The idea is we don't waste resources sending data to remote db that it already contains.
     */
    public ArrayList<WeatherDataObject> getNewData() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+WEATHER_TABLE_NAME+" WHERE " + WEATHER_COLUMN_NEWDATA + " = '1' " +
                "ORDER BY " +WEATHER_COLUMN_STATIONID+", "+ WEATHER_COLUMN_TIMESTAMP+";", null);

        return getWeatherDataList(c, db);
    }

    /**
     * After uploading new data to online server call this method to set all new data to old data.
     */
    public void updateNewDataFlag() {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.i(TAG, "Setting newData flag to false");

        ContentValues values = new ContentValues();
        values.put(WEATHER_COLUMN_NEWDATA, 0);
        int rows = db.update(WEATHER_TABLE_NAME, values, WEATHER_COLUMN_NEWDATA+" = '1'", null);

        Log.i(TAG, rows+" rows updated.");
    }

    public ArrayList<WeatherDataObject> getAllData() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select * from weatherdata order by " + WEATHER_COLUMN_STATIONID
                +", "+WEATHER_COLUMN_TIMESTAMP + ";", null);

        return getWeatherDataList(c, db);
    }

    /**
     * Returns ArrayList of WeatherDataObjects that have date between given start and end date inclusive.
     * Date parameters must be in Unix time format (Java getTimeInMillis/1000)
     *
     * @param startDate The date to start the search from.
     * @param endDate The date to end the search with.
     * @return ArrayList<WeatherDAtaObject>
     */
    public ArrayList<WeatherDataObject> getDataDateRange(Long startDate, Long endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM weatherdata WHERE " + WEATHER_COLUMN_TIMESTAMP +
                " BETWEEN " + startDate + " AND " + endDate, null);

        return getWeatherDataList(c, db);
    }

    /**
     * Helper method to create an ArrayList of WeatherDataObjects from a given SQL query.
     * The SQL query must return whole rows (SELECT *), or the WeatherDataObject will not have enough variables.
     */
    private ArrayList<WeatherDataObject> getWeatherDataList(Cursor c, SQLiteDatabase db) {
        ArrayList<WeatherDataObject> sensors = new ArrayList<WeatherDataObject>();
        if (c.moveToFirst()) {
            sensors.add(createWeatherDataObject(c, db));
            while (c.moveToNext()) {
                sensors.add(createWeatherDataObject(c, db));
            }
            c.close();
            return sensors;
        } else {
            c.close();
            Log.i(TAG, "Error: ArrayList is empty or null.");
        }

        return null;
    }

    /**
     * Helper method to create WeatherDataObject from the current SQL row in the Cursor.
     * The last variable using == 1 will set a boolean true if 1, false if anything else.
     * This is because SQLite doesn't have a boolean datatype, they are stored as Integers (1 true, 0 false)
     */
    private WeatherDataObject createWeatherDataObject(Cursor c, SQLiteDatabase db) {
        int stationID = c.getInt(c.getColumnIndex(WEATHER_COLUMN_STATIONID));
        //Log.i(TAG, ""+stationID);
        WeatherDataObject wd = new WeatherDataObject(stationID, new double[]{
                c.getDouble(c.getColumnIndex(WEATHER_COLUMN_TEMP)),
                c.getDouble(c.getColumnIndex(WEATHER_COLUMN_PRESSURE)),
                c.getDouble(c.getColumnIndex(WEATHER_COLUMN_WINDSPEED)),
                c.getDouble(c.getColumnIndex(WEATHER_COLUMN_WINDDIRECTION)),
                c.getDouble(c.getColumnIndex(WEATHER_COLUMN_RAINFALL)),
                c.getDouble(c.getColumnIndex(WEATHER_COLUMN_HUMIDITY))
        }, c.getLong(c.getColumnIndex(WEATHER_COLUMN_TIMESTAMP)), (c.getInt(c.getColumnIndex(WEATHER_COLUMN_NEWDATA)) == 1));

        wd.setRowID(c.getInt(c.getColumnIndex(WEATHER_COLUMN_ID)));

        Cursor llCursor = db.rawQuery("SELECT "+STATION_COLUMN_LATITUDE+", "+STATION_COLUMN_LONGITUDE
                +" FROM "+STATION_TABLE_NAME+" WHERE "+STATION_COLUMN_STATIONID+" = '"+stationID+"';", null);
        if (llCursor.moveToFirst()) {
            wd.setLatitude(llCursor.getDouble(llCursor.getColumnIndex(STATION_COLUMN_LATITUDE)));
            wd.setLongitude(llCursor.getDouble(llCursor.getColumnIndex(STATION_COLUMN_LONGITUDE)));
        } else Log.i(TAG, "Error in createWeatherDataObject finding lat/long");
        llCursor.close();

        return wd;
    }

    /**
     * Returns the total number of rows in the database.
     */
    public int getNumberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, WEATHER_TABLE_NAME);
    }

    public int getNumStationRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, STATION_TABLE_NAME);
    }

    /**
     * Takes a list of WeatherDataObjects and iterates over it inserting into database.
     * Should be less expensive than iterating and calling insertRowSingle each time as that
     * will call getWritableDatabase every iteration, whereas this calls it only once per list.
     *
     * @param weatherData The ArrayList to pull the data out of for inserting into database.
     */
    public void insertRowList(ArrayList<WeatherDataObject> weatherData) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (WeatherDataObject wd : weatherData) db.insert(WEATHER_TABLE_NAME, null, createContentValuesForRowInsert(wd));
    }

    /**
     * Inserts a row into the db, WEATHER_COLUMN_ID is incremented automatically. Returns WEATHER_COLUMN_ID.
     * Takes a WeatherDataObject and pulls values from it to create new row.
     */
    public long insertRowSingle(WeatherDataObject wd) {
        SQLiteDatabase db = this.getWritableDatabase();

        return db.insert(WEATHER_TABLE_NAME, null, createContentValuesForRowInsert(wd));
    }

    private ContentValues createContentValuesForRowInsert(WeatherDataObject wd) {
        ContentValues values = new ContentValues();
        values.put(WEATHER_COLUMN_STATIONID, wd.getStationID());
        values.put(WEATHER_COLUMN_TEMP, wd.getTemp());
        values.put(WEATHER_COLUMN_PRESSURE, wd.getPressure());
        values.put(WEATHER_COLUMN_WINDSPEED, wd.getWindSpeed());
        values.put(WEATHER_COLUMN_WINDDIRECTION, wd.getWindDirection());
        values.put(WEATHER_COLUMN_RAINFALL, wd.getRainfall());
        values.put(WEATHER_COLUMN_HUMIDITY, wd.getHumidity());
        values.put(WEATHER_COLUMN_TIMESTAMP, wd.getTimeStamp());
        values.put(WEATHER_COLUMN_NEWDATA, (wd.isNewData() ? 1 : 0));
        return values;
    }

    /**
     * Deletes a row using the WEATHER_COLUMN_ID (primary key).
     */
    public long deleteRow(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(WEATHER_TABLE_NAME, WEATHER_COLUMN_ID + " = ? ", new String[]{Integer.toString(id)});
    }

    // Region Weather Station private SQL statements

    private void insertStationRows(int[] ids) {
        SQLiteDatabase db = this.getWritableDatabase();

        for (int i : ids) {
            ContentValues values = new ContentValues();
            values.put(STATION_COLUMN_STATIONID, i);
            values.put(STATION_COLUMN_LATITUDE, DataUtils.getRandomLatitude());
            values.put(STATION_COLUMN_LONGITUDE, DataUtils.getRandomLongitude());
            db.insert(STATION_TABLE_NAME, null, values);
        }
    }

    // To help debug
    private void printToLogStationRows() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery("select * from " + STATION_TABLE_NAME + ";", null);
        if (c.moveToFirst()) {
            Log.i(TAG, "Station Table: " + c.getInt(c.getColumnIndex(STATION_COLUMN_ID)) +
                    " " + c.getInt(c.getColumnIndex(STATION_COLUMN_STATIONID)) + " " + c.getDouble(c.getColumnIndex(STATION_COLUMN_LATITUDE)) +
                    " " + c.getDouble(c.getColumnIndex(STATION_COLUMN_LONGITUDE)));
            while (c.moveToNext()) {
                Log.i(TAG, "Station Table: " + c.getInt(c.getColumnIndex(STATION_COLUMN_ID)) +
                        " " + c.getInt(c.getColumnIndex(STATION_COLUMN_STATIONID)) + " " + c.getDouble(c.getColumnIndex(STATION_COLUMN_LATITUDE)) +
                        " " + c.getDouble(c.getColumnIndex(STATION_COLUMN_LONGITUDE)));
            }
        } else {
            Log.i(TAG, "Error.");
        }
        c.close();
    }

    private void printArrayList(ArrayList<WeatherDataObject> weatherData) {
        if (weatherData!=null) for (WeatherDataObject wd : weatherData) Log.i(TAG, wd.toString());
        else Log.i(TAG, "ArrayList is null.");
    }

    // End Region

    //**********************TESTING*************************************

    // For testing. Order of sensors = temp, pressure, wind speed, wind direction, rainfall, humidity.
    private void populateWithFakeData() {
        ArrayList<WeatherDataObject> weatherData = new ArrayList<WeatherDataObject>();
        for (int i = 0; i < 10; i++) {
            Random r = new Random();
            weatherData.add(new WeatherDataObject(r.nextInt(100) + 1, DataUtils.getRandomSensors(), DataUtils.getRandomDate(), DataUtils.getRandomBoolean()));
        }
        insertRowList(weatherData);
    }

    // For testing. Order of sensors = temp, pressure, wind speed, wind direction, rainfall, humidity.
    public void populateWithOrderedData() {
        Log.i(TAG, "Initialising database with initial test data.");
        ArrayList<WeatherDataObject> weatherData = new ArrayList<WeatherDataObject>();
        for (int i = 1; i < 6; i++) {
            weatherData.add(new WeatherDataObject(i, DataUtils.getRandomSensors(), (long) (1507248000 + (i * 900)), false));
            weatherData.add(new WeatherDataObject(i, DataUtils.getRandomSensors(), (long) (1507248000 + (i * 100)), false));
            weatherData.add(new WeatherDataObject(i, DataUtils.getRandomSensors(), (long) (1507248000 + (i * 500)), true));
        }
        insertRowList(weatherData);
    }

    public void populateStationTable() {
        int[] ids = new int[100];
        for (int i = 1; i < ids.length; i++) ids[i-1]=i;
        insertStationRows(ids);
    }
}
