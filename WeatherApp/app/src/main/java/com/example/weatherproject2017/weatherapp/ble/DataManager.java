package com.example.weatherproject2017.weatherapp.ble;

import java.util.Arrays;
//TODO docuement this class
//TODO sort functions used/not used but useful

public class DataManager {
    private int offset;
    private int dataCounter;
    private int recordBits;
    private int[] bitGroup;
    byte[] receivedData;
    byte[] recordData;
    private boolean isTimed;
    private long tempTime;

    public DataManager(byte[] bytes){
        receivedData = bytes;
    }

    //Setters and getters for numbers and counters
    public int getRecordBits() {
        return recordBits;
    }

    public void setRecordBits(int recordBits) {
        this.recordBits = recordBits;
    }

    public int getDataCounter() {
        return dataCounter;
    }

    public void increaseDataCounter() {
        dataCounter += 1;
    }

    public void increaseDataCounter(int byValue) {
        dataCounter += byValue;
    }

    public void setDataCounter(int dataCounter) {
        this.dataCounter = dataCounter;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        if(offset == 8)
            this.offset = 0;
        else
            this.offset = offset;
    }

    //setters and getters of array of all read data
    public byte[] getSpecificData(int startIndex, int endIndex) {
        return Arrays.copyOfRange(receivedData, startIndex, endIndex+1);
    }

    public int getReceivedByte(int index) {
        return receivedData[index] & 255;
    }

    public int getReceivedByteAtCounter() {
        return receivedData[dataCounter] & 255;
    }

    public void setDataReceived(byte[] data) {
        this.receivedData = data;
    }

    public byte[] getDataReceived() {
        return receivedData;
    }

    //setters and getters for data that makes up an individual record
    public void setRecordData(int startIndex, int endIndex, int number) {
        recordData = Arrays.copyOfRange(receivedData, startIndex, endIndex+1);
        for(byte b : recordData) {
            System.out.println(b + ":" + number + "data");
        }
    }

    public byte[] getRecordData() {
        return recordData;
    }

    public int getRecordByte(int index) {
        //System.out.println(recordData[index] + "data");
        return recordData[index] & 255;
    }

    //methods that manage the array of bits that determines which sensor data is being unpacked
    public int[] getBitGroup() {
        return bitGroup;
    }

    public void setBitGroup(int[] bitGroup) {
        int counter = 0;
        for(int bits : bitGroup) {
            counter += bits;
        }

        recordBits = (int) Math.ceil((counter + 8 + offset) / 8);
        this.bitGroup = bitGroup;
    }

    public boolean isTimed() {
        return isTimed;
    }

    public void setTimed(boolean isTimed) {
        this.isTimed = isTimed;
    }

    public long getTempTime() {
        return tempTime;
    }

    public void setTempTime(long tempTime) {
        this.tempTime = tempTime;
    }
}