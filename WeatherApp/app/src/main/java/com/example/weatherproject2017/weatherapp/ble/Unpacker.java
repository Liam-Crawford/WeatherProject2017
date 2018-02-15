package com.example.weatherproject2017.weatherapp.ble;

import java.util.*;

//TODO document this class
public class Unpacker {

    //Constants for use in arrays managing bits and minimum values
    //Amount of bits of a pressure reading for a full record
    static final int HPABITS = 14;
    //Amount of bits of a temperature reading for a full record
    static final int TMPBITS = 11;
    //Amount of bits of a humidity reading for a full record
    static final int HMDBITS = 10;
    //Amount of bits of a wind speed reading for a full record
    static final int WNDSPDBITS = 12;
    //Amount of bits of a wind direction reading for a full record
    static final int WNDDIRBITS = 9;
    //Amount of bits of a rainfall reading for a full record
    static final int RNFBITS = 13;

    //Maximum bits of a pressure reading for an update
    static final int HPABITSUPD = 15;
    //Maximum bits of a temperature reading for an update
    static final int TMPBITSUPD = 12;
    //Maximum bits of a humidity reading for an update
    static final int HMDBITSUPD = 11;
    //Maximum bits of a wind speed reading for an update
    static final int WNDSPDBITSUPD = 13;
    //Maximum bits of a wind direction reading for an update
    static final int WNDDIRBITSUPD = 10;
    //Maximum bits of a rainfall reading for an update
    static final int RNFBITSUPD = 14;

    //Map that references a list of bits used by a value in a record with a numerical header
    HashMap<Integer, Header> headerReference = new HashMap<Integer, Header>();

    //Numbers that can be used to mask offset bits (bits overflowed into a byte from a previous value
    int[] offsetAnd = {1, 3, 7, 15, 31, 63, 127};

    //Is this record an update?
    boolean isUpdate = false;

    //Instance of data manager
    DataManager dManager;
    //Instance of record calss that holds previous record for referencing
    Record prevRecord;
    //Transmission details instance
    TransmissionDetails t = new TransmissionDetails();
    //Field that tracks the overflow (the amount that a previous piece of data itrudes into the next byte)
    private int overflow;

    /* Unpacker constructor that sets the data manager and populates the header map using populateMap();
     * failing to call this before decoding data will lead to null pointer errors when looking up header numbers
     * so probably just leave it here.
     */
    public Unpacker(DataManager dManager) {
        populateMap();
        this.dManager = dManager;
    }

    /* Clear the previous record, call this when an array of data has been finished.  Failing to do so will cause errors with time synchronisation
     * as the first record of the next transmission decoded will be treated as if it is similarly timed with the previous.  e.g. 3 hours worth of data
     * gathered at 10am means the oldest full record was 7am.  Gathering the same amount of data again at 5pm with a full record still intact will time
     * the oldest data at 4am.
     */
    public void clearPreviousRecord() {
        prevRecord = null;
    }

    /* Set the transmission details for the coming records.  This data is of fixed size (6byte) and doesn't overflow, it contains the ID of the sation
     * the current UNIX time and the syncTime (the time since the last timed record was taken).
     */
    public void setTransmissionDetails(byte[] informationBytes) {
        //ID is only one byte so just get the first byte as id
        int id = informationBytes[0];
        int time = 0;
        //Loop through the next 4 bytes storing them as the time
        for(int i = 1; i < 5; i++) {
            time = time << 8;
            time = time | informationBytes[i] & 255;
        }
        //The final byte contains the sync time information so store it as such
        int syncTime = informationBytes[5];

        t.setStationID(id);
        t.setCurrentTime(time);
        t.setSyncTime(syncTime);
    }
    /* Populate the map that determines which values are present in the following data and what amount of bits they occupy.
     * Because headers are byte numbers from 0 - 255, the loop index is placed the reference number and after some subtraction
     * is places the bits at the correct index.
     * Handles special cases first, then handles update headers and record headers
     */
    private void populateMap() {
        //The array containing the number of bits when a record is an update
        int[] updateBits = {TMPBITSUPD, HPABITSUPD, HMDBITSUPD, WNDSPDBITSUPD, WNDDIRBITSUPD, RNFBITSUPD};
        //The array containing the number of bits when a record is a full record
        int[] recordBits = {TMPBITS, HPABITS, HMDBITS, WNDSPDBITS, WNDDIRBITS, RNFBITS};

        //Special case 1: The number 2 (0000 0010) represents a header updating no sensors, so map all bits as 0
        int[] emptyBits = new int[6];
        for(int i = 0; i < emptyBits.length; i++) {
            emptyBits[i] = 0;
        }
        Header zeroHeader = new Header(false, emptyBits);
        headerReference.put(2, zeroHeader);

        //Special case 2: 0 (0000 0000) represents a reading of no sensors which is an invalid header so place it as such
        //Special case 3: 1 (0000 0001) represents a reading of no sensors with a time sync which is an invalid header so place it as such
        //Special case 4: 3 (0000 0011) represents an update of no sensors with a time sync which is an invalid header so place it as such
        //Special case 5: 254 (1111 1110) represents an update of all sensors, because this would be a transmission of more
        //				  bits than a regular read it should not occur, leading to an invalid header
        int[] invalidBits = new int[6];
        invalidBits[0] = -1;
        Header invalidHeader = new Header(false, emptyBits);
        headerReference.put(0, invalidHeader);
        headerReference.put(1, invalidHeader);
        headerReference.put(3, invalidHeader);
        headerReference.put(254, invalidHeader);

        //Loop through each valid header number and assign the bits from the appropriate array to the bit management array
        for(Integer i = 4; i <= 253; i++) {
            int[] bitList = new int[6];

            //if the last 2 bits of a index number evaluate to 2 the header is an update.
            if((i & 3) == 2) {
                //substract header bit so loop doesn't space out
                int headerKey = i - 2;

                //Copy the right bits from the update array into the bitlist array
                modifyArrays(headerKey, bitList, updateBits);

                //Create a header instance to store in the header map
                Header h = new Header(false, bitList);

                //put header on the map
                headerReference.put(i, h);

                //if the last 2 bits of a index number evaluate to 1 the header is a synchronised (timed) full record.
            }else if((i & 3) == 1){
                //subtract time update bit so loop doesn't space out
                int headerKey = i - 1;

                modifyArrays(headerKey, bitList, recordBits);

                Header h = new Header(true, bitList);
                headerReference.put(i, h);
                //if the last two bits evaluate to 0 then header represents an untimed record
            }else if((i & 3) == 0) {
                int headerKey = i;
                modifyArrays(headerKey, bitList, recordBits);

                Header h = new Header(false, bitList);
                headerReference.put(i, h);
            }
        }
    }

    /* A simple array copy function that puts bits in the header bitlist from the given array(recordBits or updateBits) based on the number
     * If a 1 is found in a particular position, the corresponding number of bits is stored in the appropriate place.
     */
    private void modifyArrays(int headerKey, int[] bitList, int[]recordBits) {
        while(headerKey != 0){
            if(headerKey >= 128) {
                bitList[0] = recordBits[0];
                headerKey -= 128;
            }
            if(headerKey >= 64) {
                bitList[1] = recordBits[1];
                headerKey -= 64;
            }
            if(headerKey >= 32) {
                bitList[2] = recordBits[2];
                headerKey -= 32;
            }
            if(headerKey >= 16) {
                bitList[3] = recordBits[3];
                headerKey -= 16;
            }
            if(headerKey >= 8) {
                bitList[4] = recordBits[4];
                headerKey -= 8;
            }
            if(headerKey >= 4) {
                bitList[5] = recordBits[5];
                headerKey -= 4;
            }
        }
    }

    /* Read the header byte, an index is not provided as the counter should always be at the header
     * when this function is called.  If there is no offset in the reading at the time of call (usually at the start of the bytes read),
     * simply set the appropriate bit array from the map and increase the counter, then check for a update header and set it appropriately.
     *
     * If offset is found, juggle the numbers a little before checking and setting (outlined below).  Note that when this occurs the offset
     * doesn't need to be recalculated as a header is a fixed size, meaning that the offset will be the same size as the previous.
     */
    public void readHeader() {
        //No offset found
        if(dManager.getOffset() == 0) {

            //Get the byte and use its numerical value to retrieve a header object from the map
            Header h = headerReference.get(dManager.getReceivedByteAtCounter());

            //Get the bitgroup from the header and set the datamanagers bit group
            dManager.setBitGroup(h.getBitArray());

            //Set weather the record is timed or not
            dManager.setTimed(h.isTimed);

            //If the the last 2 bits in the byte evaluate to 2 the record is an update so set as such
            if((dManager.getReceivedByteAtCounter() & 3) == 2)
                isUpdate = true;

            dManager.increaseDataCounter();
            //offset found
        }else {
            //Get the byte
            int header = dManager.getReceivedByteAtCounter();

            //mask it as needed. e.g. an offset of 2 would need a mask of 63 (XX?? ???? & 0011 1111 == 00?? ????)
            header = header & offsetAnd[8 - dManager.getOffset() - 1];
            dManager.increaseDataCounter();

            //move the header left by one byte ready for next byte
            header = header << 8;

            //store next byte into a separate variable so the above can be performed without damaging the previous data.
            //Uses an inverted mask to ensure that only the bits from the next byte needed are saved.
            //e.g. and offset of 2 means only 2 bits from the next byte are needed (overflow) (??XX XXXX & 1100 0000 == ??00 0000)
            int headerOverflow = dManager.getReceivedByteAtCounter();
            headerOverflow = headerOverflow & ~offsetAnd[8 - dManager.getOffset() - 1];

            //bitwise or the two together
            header = header | headerOverflow;

            //shift the header right so that it occupies the right space (the first byte)
            header = header >> 8 - dManager.getOffset();
            System.out.println(header + "oseth");
            if((header & 3) == 2)
                isUpdate = true;
            System.out.println(header + "osethead");
            Header h = headerReference.get(header);
            dManager.setBitGroup(h.getBitArray());
            dManager.setTimed(h.isTimed);
        }

    }

    /* Function that decodes the data in the record data array. Mention that it stores values in order tmp pressure etc
     *
     * Note that because the update values are able to be negative numbers a little extra checking is required before
     * storage to ensure a number is in the right condition to add/subtract with the previous full record.
     */
    public Record decode() {

        //Create a new record for storage
        Record record = new Record();
        //TODO error checking for invalid headers

        //set the ID of the weather station
        record.setStationID(t.getStationID());


        if(dManager.isTimed()) {
            //If the record is timed but the full record is null this is the newest record so subtract the time since the last record and set times
            if(prevRecord == null) {
                long time = t.getCurrentTime() - (t.getSyncTime() * 60);
                record.setTime(time);
                dManager.setTempTime(time);
            }else {
                //If the record is times and a full record is found this record is an hour older than the most recent one (as per design) so set as such
                long time = prevRecord.getTime() - 3600;
                record.setTime(time);
                dManager.setTempTime(time);
            }
        }else{
            //This record is an update so it's timing is 15 minutes after the last record time (As per design  doc)
            record.setTime(dManager.getTempTime() + (15 * 60));
            dManager.setTempTime(record.getTime());
        }

        //for each bit number in the array of bits (accessing the data in a set order)
        for(int i = 0; i < dManager.getBitGroup().length; i++) {

            //Get the appropriate number bits to read from the array
            int dataPointBits = dManager.getBitGroup()[i];

            //If the header detailed that there were actually bits to be read
            if(dataPointBits > 0) {
                System.out.println(dataPointBits + "dpb");
                //If an offset is found
                if(dManager.getOffset() > 0) {
                    //temporary storage for values
                    int unpackedValue = 0;

                    //Get the byte
                    int readByte = dManager.getReceivedByteAtCounter();
                    dManager.increaseDataCounter();

                    //Mask and shift the bits (same process as offset header)
                    unpackedValue = readByte & offsetAnd[8 - dManager.getOffset() - 1];

                    //reduce the number of bits left to read
                    dataPointBits -= 8 - dManager.getOffset();
                    System.out.println(dataPointBits + "rb");
                    //Get the unpacked, useful value
                    unpackedValue = getValue(dataPointBits, unpackedValue);


                    //The following code stores the data within the record
                    //If record is an update
                    if(isUpdate) {
                        //calculate the number whose binary representation would provide a mask appropriate to find a negative update
                        //i.e. a bit mask that only acts on the left-most bit of an update number
                        int bits = (int) Math.pow(2, dManager.getBitGroup()[i]-1);
                        if((unpackedValue & bits) == bits) {
                            //If update value is negative (decrase in value of read), descale, add negative to previous record value and store
                            record.populateRecord(prevRecord.orderedAccess(i) + descaleUpdate(unpackedValue, dManager.getBitGroup()[i]), i);
                        }
                        else {
                            //If update is positive (increase in value of read), simply add value to previous record value and store.
                            record.populateRecord(prevRecord.orderedAccess(i) + unpackedValue, i);
                        }

                        //If record is not an update, just store
                    }else {
                        record.populateRecord(unpackedValue, i);
                    }
                    //If no offset is present
                }else {
                    //Get the unpacked useful, value
                    int unpackedValue = getValue(dataPointBits, 0);

                    if(isUpdate) {
                        int bits = (int) Math.pow(2, dManager.getBitGroup()[i]-1);
                        if((unpackedValue & bits) == bits) {
                            System.out.println(descaleUpdate(unpackedValue, dManager.getBitGroup()[i]) + "upval1");
                            record.populateRecord(prevRecord.orderedAccess(i) + descaleUpdate(unpackedValue, dManager.getBitGroup()[i]), i);
                        }
                        else {
                            System.out.println(unpackedValue + "upval2");
                            record.populateRecord(prevRecord.orderedAccess(i) + unpackedValue, i);
                        }

                    }else
                        record.populateRecord(unpackedValue, i);
                }
            }else if (dataPointBits == 0 && isUpdate) {
                record.populateRecord(prevRecord.orderedAccess(i), i);
            }
            //If we're on the last byte of an update or record and the data afterwards is empty then we're at the send of a collection of records so reset offset
            if(i == dManager.getBitGroup().length -1) {
                int lastByte = dManager.getReceivedByteAtCounter();
                lastByte = lastByte & offsetAnd[(8 - overflow) - 1];
                int checkBit = (int) Math.pow(2, ((8 - overflow) -1));
                if((lastByte & checkBit) == checkBit) {
                    overflow = (overflow + 1) % 8;
                }else{
                    overflow = 0;
                    dManager.increaseDataCounter();
                }
            }
            //Set the overflow
            dManager.setOffset(overflow);
        }

        if(!isUpdate)
            prevRecord = record;

        System.out.println(record.getTemperature(true));
        System.out.println(record.getPressure(true));
        System.out.println(record.getHumidity(true));
        System.out.println(record.getWindSpeed(true));
        System.out.println(record.getWindDirection());
        System.out.println(record.getRainfall(true));

        System.out.println("DONE" + isUpdate);
        return record;
    }

    /*
     *
     *
     *
     */
    int getValue(int dataPointBits, int unpackedValue) {
        //calculate overflow
        overflow = dataPointBits % 8;

        //int unpackedValue = 0;

        //Loop through bits 'oring' and storing until only the overflow bits remain
        while(dataPointBits > overflow) {
            unpackedValue = unpackedValue << 8;
            unpackedValue = unpackedValue | dManager.getReceivedByteAtCounter();
            dManager.increaseDataCounter();
            dataPointBits -= 8;
        }
        //if a value overflows to the next byte
        if(overflow > 0) {
            //make space for the overflow in the value
            unpackedValue = unpackedValue << overflow;
            //get the overflow byte, mask it and shift the value.
            int overflowByte =  dManager.getReceivedByteAtCounter();
            System.out.println(overflowByte + "obyte");
            overflowByte = overflowByte & ~offsetAnd[(8 - overflow) - 1];
            overflowByte = overflowByte >> 8 - overflow;
            unpackedValue = unpackedValue | overflowByte;
        }
        System.out.println(unpackedValue + "retval");
        return unpackedValue;
    }

    /* Function to scale a negative update number appropriately.  Because numbers only occupy a certain number of bits during transport,
     * negative numbers are turned to positive numbers due to the loss of the sign bit and leading 1's.  This function reinstates that by 'oring'
     * the numbers with a mask that has all 1's except for where the valid data is. (the inverse of a number whose binary representation
     * is a single 1 in the position of the left most bit in the trimmed number.
     */
    int descaleUpdate(int value, int bits) {
        int orBits = -(int)Math.pow(2, bits);
        return (value | orBits);
    }

    public class Header{
        private boolean isTimed;
        private int[] bitArray;

        public Header(boolean isTimed, int[] bitArray) {
            this.isTimed = isTimed;
            this.bitArray = bitArray;
        }

        public boolean isTimed() {
            return isTimed;
        }
        public void setTimed(boolean isTimed) {
            this.isTimed = isTimed;
        }

        public int[] getBitArray() {
            return bitArray;
        }
        public void setBitArray(int[] bitArray) {
            this.bitArray = bitArray;
        }
    }

    public class TransmissionDetails {
        private int stationID;
        private long currentTime;
        private int syncTime;

        TransmissionDetails(){};

        public int getStationID() {
            return stationID;
        }

        public void setStationID(int stationID) {
            this.stationID = stationID;
        }

        public long getCurrentTime() {
            return currentTime;
        }

        public void setCurrentTime(long currentTime) {
            this.currentTime = currentTime;
        }

        public int getSyncTime() {
            return syncTime;
        }

        public void setSyncTime(int syncTime) {
            this.syncTime = syncTime;
        }
    }

    public class Record {
        private long time;
        private int temperature;
        private int humidity;
        private int pressure;
        private int rainfall;
        private int windSpeed;
        private int windDirection;
        private int stationID;


        private boolean isSet = false;

        static final int MINTMP = -40;
        static final int MINHPA = 200;
        static final int MINHMD = 0;
        static final double MINWNDSPD = 0;
        static final double MINWNDDIR = 0;
        static final double MINRNF = 0;

        public Record() {}

        public Record(int temperature, int humidity, int pressure, int rainfall,
                      int windSpeed, int windDirection, int time) {
            this.time = time;
            this.temperature = temperature;
            this.humidity = humidity;
            this.pressure = pressure;
            this.rainfall = rainfall;
            this.windSpeed = windSpeed;
            this.windDirection = windDirection;
        }

        public long getTime() {
            return time;
        }
        public void setTime(long time) {
            this.time = time;
        }

        public Number getTemperature(boolean descale) {
            if(descale)
                return descale(temperature, MINTMP);
            else
                return temperature;
        }
        public void setTemperature(int temperature) {
            this.temperature = temperature;
        }
        public Number getHumidity(boolean descale) {
            if(descale)
                return descale(humidity, MINHMD);
            else
                return humidity;
        }
        public void setHumidity(int humidity) {
            this.humidity = humidity;
        }
        public Number getPressure(boolean descale) {
            if(descale)
                return descale(pressure, MINHPA);
            else
                return pressure;
        }
        public void setPressure(int pressure) {
            this.pressure = pressure;
        }
        public Number getRainfall(boolean descale) {
            if(descale)
                return descale(rainfall, 0);
            else
                return rainfall;
        }
        public void setRainfall(int rainfall) {
            this.rainfall = rainfall;
        }
        public Number getWindSpeed(boolean descale) {
            if(descale)
                return descale(windSpeed, 0);
            else
                return windSpeed;
        }
        public void setWindSpeed(int windSpeed) {
            this.windSpeed = windSpeed;
        }

        public double getWindDirection() {
            return windDirection;
        }
        public void setWindDirection(int windDirection) {
            this.windDirection = windDirection;
        }

        public boolean isSet() {
            return isSet;
        }

        public void setSet(boolean isSet) {
            this.isSet = isSet;
        }

        public int getStationID() {
            return stationID;
        }

        public void setStationID(int stationID) {
            this.stationID = stationID;
        }

        public void populateRecord(int value, int type) {
            switch(type) {
                case 0:
                    setTemperature(value);
                    break;

                case 1:
                    setPressure(value);
                    break;

                case 2:
                    setHumidity(value);
                    break;

                case 3:
                    setWindSpeed(value);
                    break;

                case 4:
                    setWindDirection(value);
                    break;

                case 5:
                    setRainfall(value);
                    break;

                default:
                    break;
            }
        }

        public int orderedAccess(int type) {
            switch(type) {
                case 0:
                    return temperature;
                case 1:
                    return pressure;
                case 2:
                    return humidity;
                case 3:
                    return windSpeed;
                case 4:
                    return windDirection;
                case 5:
                    return rainfall;
                default:
                    return 0;
            }
        }

        double descale(int scaledValue, int minNum) {
            double realValue = (scaledValue / 10.0) + minNum;
            return realValue;
        }

    }
}

