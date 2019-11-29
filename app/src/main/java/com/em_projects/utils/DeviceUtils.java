package com.em_projects.utils;

/**
 * Created by eyal muchtar on 15/03/2017.
 */

import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;


public class DeviceUtils {

    /**
     * Retrieve device's IMEI
     *
     * @param context application or base context
     * @return device's IMEI
     */
    public static String getIMEI(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    /**
     * Retrieve device's unique Id
     *
     * @param context application or base context
     * @return device's UID
     */
    public static String getDeviceUniqueID(Context context) {
        String device_unique_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return device_unique_id;
    }

    /**
     * Retrieve device's IMSI
     *
     * @param context application or base context
     * @return device's IMSI
     */
    public static String getDeviceImsi(Context context) {
//        String myIMSI = SystemProperties.get(android.telephony.TelephonyProperties.PROPERTY_IMSI);
//        String myIMEI = SystemProperties.get(android.telephony.TelephonyProperties.PROPERTY_IMEI);
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getSubscriberId();
    }
}
