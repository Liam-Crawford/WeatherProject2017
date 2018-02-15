package com.example.weatherproject2017.weatherapp.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by crawf_000 on 17/08/2017.
 *
 *  Contains sections of code which have been open sourced from Bluefruit_LE_Connect_Android
 *  as per the conditions of the following license
 The MIT License (MIT)

 Copyright (c) 2015 Adafruit Industries

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.

        */

public class BleUtils {
    private final static String TAG = BleUtils.class.getSimpleName();

    private static final int STATUS_BLE_ENABLED = 0;
    private static final int STATUS_BLUETOOTH_NOT_AVAILABLE = 1;
    private static final int STATUS_BLE_NOT_AVAILABLE = 2;
    private static final int STATUS_BLUETOOTH_DISABLED = 3;
    private static int currentBluetoothStatus = 1;

    // Service Constants
    private static final String UUID_SERVICE = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static final int kTxMaxCharacters = 20;
    private static final int kType_Unknown = 0;
    private static final int kType_Uart = 1;
    private static final int kType_Beacon = 2;
    private static final int kType_UriBeacon = 3;

    //char array for bytes to hex methods
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    // Determines what state the bluetooth is in.
    public static boolean isBleReady(Context context) {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            currentBluetoothStatus = STATUS_BLE_NOT_AVAILABLE;
            return false;
        }

        final BluetoothAdapter adapter = getBluetoothAdapter(context);
        // Checks if Bluetooth is supported on the device.
        if (adapter == null) {
            currentBluetoothStatus = STATUS_BLUETOOTH_NOT_AVAILABLE;
            return false;
        }

        if (!adapter.isEnabled()) {
            currentBluetoothStatus = STATUS_BLUETOOTH_DISABLED;
            return false;
        }

        currentBluetoothStatus = STATUS_BLE_ENABLED;
        return true;
    }

    //Returns string regarding current bluetooth status
    public static String getBleStatusMessage() {
        String status = "";
        switch (currentBluetoothStatus) {
            case STATUS_BLE_ENABLED: status = "Bluetooth Enabled"; break;
            case STATUS_BLUETOOTH_NOT_AVAILABLE: status = "Bluetooth not available on this device"; break;
            case STATUS_BLE_NOT_AVAILABLE: status = "Ble not available on this device"; break;
            case STATUS_BLUETOOTH_DISABLED: status = "Please enable bluetooth to use this feature."; break;
        }
        return status;
    }

    public static boolean checkBluetoothAdapter(BluetoothAdapter bta) {
        if (bta != null) return true;
        return false;
    }

    // Initializes a Bluetooth adapter.
    public static BluetoothAdapter getBluetoothAdapter(Context context) {
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            return null;
        } else {
            return bluetoothManager.getAdapter();
        }
    }

    //Enables Wifi
    public static void enableWifi(boolean enable, Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(enable);
    }

    //Checks if Wifi is enabled
    public static boolean isWifiEnabled(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    /*public static String[] getMACFilter() {
        return new String[]{"6A:E6:8C:09:75:10"};
    }*/

    //Code open sourced from Bluefruit_LE_Connect_Android
    private static String bytesToHex(byte[] bytes) {
        if (bytes != null) {
            char[] hexChars = new char[bytes.length * 2];
            for (int j = 0; j < bytes.length; j++) {
                int v = bytes[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        } else return null;
    }

    //Code open sourced from Bluefruit_LE_Connect_Android
    public static String byteToHex(byte value) {
        if (value > 0x0f) {
            char[] hexChars = new char[2];
            hexChars[0] = hexArray[value >>> 4];
            hexChars[1] = hexArray[value & 0x0F];
            return new String(hexChars);
        } else {
            return "" + hexArray[value & 0x0F];
        }
    }

    //Code open sourced from Bluefruit_LE_Connect_Android
    public static String stringToHex(String string) {
        return bytesToHex(string.getBytes());
    }

    //Open sourced from Bluefruit_LE_Connect_Android
    public static String bytesToHexWithSpaces(byte[] bytes) {
        StringBuilder newString = new StringBuilder();
        for (byte aByte : bytes) {
            String byteHex = String.format("%02X", (byte) aByte);
            newString.append(byteHex).append(" ");

        }
        return newString.toString().trim();
    }

    //Open sourced from Bluefruit_LE_Connect_Android
    public static String getUuidStringFromByteArray(byte[] bytes) {
        StringBuilder buffer = new StringBuilder();
        for (byte aByte : bytes) {
            buffer.append(String.format("%02x", aByte));
        }
        return buffer.toString();
    }

    //Code open sourced from Bluefruit_LE_Connect_Android
    public static UUID getUuidFromByteArrayBigEndian(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long high = bb.getLong();
        long low = bb.getLong();
        UUID uuid = new UUID(high, low);
        return uuid;
    }

    //Code open sourced from Bluefruit_LE_Connect_Android
    public static UUID getUuidFromByteArraLittleEndian(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        long high = bb.getLong();
        long low = bb.getLong();
        UUID uuid = new UUID(low, high);
        return uuid;
    }

    //Open sourced from Bluefruit_LE_Connect_Android
    public static boolean decodeScanRecords(final byte[] scanRecord) {
        // based on http://stackoverflow.com/questions/24003777/read-advertisement-packet-in-android
        //final byte[] scanRecord = deviceData.scanRecord;

        ArrayList<UUID> uuids = new ArrayList<>();
        byte[] advertisedData = Arrays.copyOf(scanRecord, scanRecord.length);
        int offset = 0;
        int type = kType_Unknown;
        boolean isUart = false;

        // Check if is an iBeacon ( 0x02, 0x0x1, a flag byte, 0x1A, 0xFF, manufacturer (2bytes), 0x02, 0x15)
        final boolean isBeacon = advertisedData[0] == 0x02 && advertisedData[1] == 0x01 && advertisedData[3] == 0x1A && advertisedData[4] == (byte) 0xFF && advertisedData[7] == 0x02 && advertisedData[8] == 0x15;

        // Check if is an URIBeacon
        final byte[] kUriBeaconPrefix = {0x03, 0x03, (byte) 0xD8, (byte) 0xFE};
        final boolean isUriBeacon = Arrays.equals(Arrays.copyOf(scanRecord, kUriBeaconPrefix.length), kUriBeaconPrefix) && advertisedData[5] == 0x16 && advertisedData[6] == kUriBeaconPrefix[2] && advertisedData[7] == kUriBeaconPrefix[3];

        if (isBeacon) {
            type = kType_Beacon;

            // Read uuid
            offset = 9;
            UUID uuid = BleUtils.getUuidFromByteArrayBigEndian(Arrays.copyOfRange(scanRecord, offset, offset + 16));
            uuids.add(uuid);
            offset += 16;

            // Skip major minor
            offset += 2 * 2;   // major, minor

            // Read txpower
            final int txPower = advertisedData[offset++];
            //deviceData.txPower = txPower;
        } else if (isUriBeacon) {
            type = kType_UriBeacon;

            // Read txpower
            final int txPower = advertisedData[9];
            //deviceData.txPower = txPower;
        } else {
            // Read standard advertising packet
            while (offset < advertisedData.length - 2) {
                // Length
                int len = advertisedData[offset++];
                if (len == 0) break;

                // Type
                type = advertisedData[offset++];
                if (type == 0) break;

                // Data
//            Log.d(TAG, "record -> lenght: " + length + " type:" + type + " data" + data);

                switch (type) {
                    case 0x02:          // Partial list of 16-bit UUIDs
                    case 0x03: {        // Complete list of 16-bit UUIDs
                        while (len > 1) {
                            int uuid16 = advertisedData[offset++] & 0xFF;
                            uuid16 |= (advertisedData[offset++] << 8);
                            len -= 2;
                            uuids.add(UUID.fromString(String.format("%08x-0000-1000-8000-00805f9b34fb", uuid16)));
                        }
                        break;
                    }

                    case 0x06:          // Partial list of 128-bit UUIDs
                    case 0x07: {        // Complete list of 128-bit UUIDs
                        while (len >= 16) {
                            try {
                                // Wrap the advertised bits and order them.
                                UUID uuid = BleUtils.getUuidFromByteArraLittleEndian(Arrays.copyOfRange(advertisedData, offset, offset + 16));
                                uuids.add(uuid);

                            } catch (IndexOutOfBoundsException e) {
                                Log.e(TAG, "BlueToothDeviceFilter.parseUUID: " + e.toString());
                            } finally {
                                // Move the offset to read the next uuid.
                                offset += 16;
                                len -= 16;
                            }
                        }
                        break;
                    }

                    case 0x09: {
                        byte[] nameBytes = new byte[len - 1];
                        for (int i = 0; i < len - 1; i++) {
                            nameBytes[i] = advertisedData[offset++];
                        }

                        String name = null;
                        try {
                            name = new String(nameBytes, "utf-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        //deviceData.advertisedName = name;
                        break;
                    }

                    case 0x0A: {        // TX Power
                        final int txPower = advertisedData[offset++];
                        //deviceData.txPower = txPower;
                        break;
                    }

                    default: {
                        offset += (len - 1);
                        break;
                    }
                }
            }

            // Check if Uart is contained in the uuids
            for (UUID uuid : uuids) {
                if (uuid.toString().equalsIgnoreCase(UUID_SERVICE)) {
                    isUart = true;
                    break;
                }
            }
            if (isUart) {
                type = kType_Uart;
            }
        }

        return isUart;
    }

    //Display message as popup notification
    public void showMessage(Context c, String msg) {
        Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
    }
}
