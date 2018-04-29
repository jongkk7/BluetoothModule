/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.nainfox.bluetoothmodule.bluetooth;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    public static HashMap<String, String> attributes = new HashMap();

    public static String SMARTPHONE;
    public static String HEART_RATE_MEASUREMENT;
    public static String CLIENT_CHARACTERISTIC_CONFIG;
    public static String FBL770_ADC0_NOTIFY;
    public static String FBL770_ADC1_NOTIFY;
    public static String FBL770_INIT_SETTING;
    public static String FBL770_PIOREAD_NOTIFY;
    public static String FBL770_SPP_NOTIFY;
    public static String FBL770_SPP_WRITE;
    public static String FIRMWARE_VERSION;
    public static String HARDWARE_PARTNO;
    public static String MANUFACTURE_NAME;
    public static String NEXUS6_READXML_CHARA;
    public static String NEXUS6_WRITEXML_CHARA;
    public static String NEXUS6_XML_SERVICE;
    public static String SERIAL_NUMBER;

    static {
        SMARTPHONE = "00001101-0000-1000-8000-00805F9B34FB";
        HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
        CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
        NEXUS6_WRITEXML_CHARA = "f1a42261-aa44-11e2-9e96-0800200c9a66";
        NEXUS6_READXML_CHARA = "f1a42262-aa44-11e2-9e96-0800200c9a66";
        NEXUS6_XML_SERVICE = "f1a42260-aa44-11e2-9e96-0800200c9a66";
        HARDWARE_PARTNO = "003b002c-002d-2800-2a05-180a2a232a24";
        SERIAL_NUMBER = "002c002d-2800-2a05-180a-2a232a242a25";
        FIRMWARE_VERSION = "002d2800-2a05-180a-2a23-2a242a252a26";
        MANUFACTURE_NAME = "28002a05-180a-2a23-2a24-2a252a262a29";
        FBL770_PIOREAD_NOTIFY = "0000ffe1-0000-1000-8000-00805f9b34fb";
        FBL770_ADC0_NOTIFY = "0000ffd1-0000-1000-8000-00805f9b34fb";
        FBL770_ADC1_NOTIFY = "0000ffd2-0000-1000-8000-00805f9b34fb";
        FBL770_INIT_SETTING = "0000ffc1-0000-1000-8000-00805f9b34fb";
        FBL770_SPP_WRITE = "0000fff1-0000-1000-8000-00805f9b34fb";
        FBL770_SPP_NOTIFY = "0000fff2-0000-1000-8000-00805f9b34fb";
        attributes.put(SMARTPHONE,"Smart phone");
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put(NEXUS6_XML_SERVICE, "Nexus6 XML Service");
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put(NEXUS6_WRITEXML_CHARA, "Write XML");
        attributes.put(NEXUS6_READXML_CHARA, "Read XML");
        attributes.put(HARDWARE_PARTNO, "PCB Version");
        attributes.put(SERIAL_NUMBER, "Serial Number");
        attributes.put(FIRMWARE_VERSION, "Firmware Version");
        attributes.put(MANUFACTURE_NAME, "Manufacture");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
