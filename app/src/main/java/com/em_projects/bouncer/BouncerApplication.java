package com.em_projects.bouncer;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.em_projects.bouncer.helpers.CallLogsDBHelper;
import com.em_projects.bouncer.helpers.ContactsDbHelper;
import com.em_projects.bouncer.helpers.SmsDbHelper;
import com.em_projects.infra.application.BasicApplication;

import java.lang.reflect.Method;

import io.paperdb.Paper;

public class BouncerApplication extends BasicApplication<BouncerUserSession> {

    private static final String TAG = "BouncerApplication";

    public static TelephonyManager TM;
    public static ITelephony IT;

    @Override
    protected BouncerUserSession createUserSession() {
        Log.d(TAG, "createUserSession");
        return new BouncerUserSession();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        // Init Paper
        Paper.init(this);

        //open/create application databases
        ContactsDbHelper.getInstance().open();
        SmsDbHelper.getInstance().open();
        CallLogsDBHelper.getInstance().open();

        //restore user session
        restoreUserSession();

        //init telephony manager class that enables call blocking
        try {
            TM = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

            Log.d(getClass().getSimpleName(), "Getting Telephony Service...");
            Class<?> c = Class.forName(TM.getClass().getName());
            Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            IT = (ITelephony) m.invoke(TM);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to get Telephony Service!");
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "onTerminate");
        ContactsDbHelper.getInstance().close();
        SmsDbHelper.getInstance().close();
        CallLogsDBHelper.getInstance().close();

        storeUserSession();
    }
}
