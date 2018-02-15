package com.example.weatherproject2017.weatherapp.test;

import com.example.weatherproject2017.weatherapp.data.DataUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by User on 12/10/2017.
 */
public class DataUtilsTest {

    @Test
    public void  testEx()throws Exception{
        assertEquals("Error", 1, 1);
    }

    @Test
    public void toJson() throws Exception {

    }

    @Test
    public void degreesToCompass() throws Exception {
        assertEquals(DataUtils.degreesToCompass(360), "N");
    }

    @Test
    public void round() throws Exception {

    }

    @Test
    public void unixToDate() throws Exception {

        assertEquals(DataUtils.unixToDate((long) 1507803900), "12.10.17 23:25");
    }

    @Test
    public void unixToHHMM() throws Exception {
        assertEquals(DataUtils.unixToHHMM((long) 1507803900), 2325);
    }

    @Test
    public void calcTimeDiff() throws Exception {
        long a = ((System.currentTimeMillis() / 1000) - 1507803900) / 60;
        String b = String.valueOf(a) + " minutes ago";
        assertEquals(DataUtils.calcTimeDiff((long) 1507803900), b);
    }

}
