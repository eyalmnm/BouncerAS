package com.em_projects.bouncer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.em_projects.infra.application.UserSession;
import com.em_projects.utils.StringUtil;

import java.util.Vector;

public class BouncerUserSession extends UserSession {

    private static final String TAG = "BouncerUserSession";

    //holds shared preferences name
    private static final String s_sharedPreferencesName = "BouncerPref";

    //holds constants for shared preferences editor keys
    private static final String USER_PASSWORD = "userp";
    private static final String IS_PRIVACY_ON = "isPrivacyOn";
    private static final String IS_AUTO_LOCK_ON = "isAutomaticLock";
    private static final String IS_REQUEST_PASSWORD_TIMER_ON = "isRequestPasswordTimer";
    private static final String REQUEST_PASSWORD_TIME = "requestPasswordTime";
    private static final String MARKED_CONTACTS_IDS = "markedContactsIds";

    //user session properties
    public String Password;
    public boolean IsPrivacyOn;
    public boolean IsAutomaticLock;
    public boolean IsRequestPasswordTimer;
    public int RequestPasswordTime;

    //holds ids of contacts that are marked as hidden
    public Vector<String> MarkedAsHiddenContactIds = new Vector<String>();

    @Override
    protected void persist() {
        Log.d(TAG, "persist");
        SharedPreferences prefs = BouncerApplication.getApplication().getSharedPreferences(s_sharedPreferencesName, Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putString(USER_PASSWORD, Password);

        editor.putBoolean(IS_PRIVACY_ON, IsPrivacyOn);
        editor.putBoolean(IS_AUTO_LOCK_ON, IsAutomaticLock);
        editor.putBoolean(IS_REQUEST_PASSWORD_TIMER_ON, IsRequestPasswordTimer);
        editor.putInt(REQUEST_PASSWORD_TIME, RequestPasswordTime);

        editor.commit();
    }

    @Override
    protected void restore() {
        Log.d(TAG, "restore");
        SharedPreferences prefs = BouncerApplication.getApplication().getSharedPreferences(s_sharedPreferencesName, Context.MODE_PRIVATE);

        Password = prefs.getString(USER_PASSWORD, "");
        IsPrivacyOn = prefs.getBoolean(IS_PRIVACY_ON, true);
        IsAutomaticLock = prefs.getBoolean(IS_AUTO_LOCK_ON, true);
        IsRequestPasswordTimer = prefs.getBoolean(IS_REQUEST_PASSWORD_TIMER_ON, false);
        RequestPasswordTime = prefs.getInt(REQUEST_PASSWORD_TIME, 5);
    }

    public synchronized void storeMarkedContactIds() {
        Log.d(TAG, "storeMarkedContactIds");
        //holds the ids in string format
        String markedIdsString = "";

//		for (String id : MarkedAsHiddenContactIds)
//		{
//			if (!StringUtil.isNullOrEmpty(id))
//			{
//				
//			}
//				
//		}

        int size = MarkedAsHiddenContactIds.size();
        if (size > 0)
            markedIdsString = StringUtil.join(MarkedAsHiddenContactIds.toArray(new String[size]), ",");

        //get the preferences
        SharedPreferences prefs = BouncerApplication.getApplication().getSharedPreferences(s_sharedPreferencesName, Context.MODE_PRIVATE);

        //save the ids
        Editor editor = prefs.edit();
        editor.putString(MARKED_CONTACTS_IDS, markedIdsString);
        editor.commit();
    }

    public synchronized void restoreMarkedContactIds() {
        Log.d(TAG, "restoreMarkedContactIds");
        //clear marked-as-hidden collection
        MarkedAsHiddenContactIds.clear();

        //get the ids string from preferences
        SharedPreferences prefs = BouncerApplication.getApplication().getSharedPreferences(s_sharedPreferencesName, Context.MODE_PRIVATE);
        String markedIdsString = prefs.getString(MARKED_CONTACTS_IDS, null);

        //in case there are no values in the preferences, nothing else to do.
        if (StringUtil.isNullOrEmpty(markedIdsString))
            return;

        //convert string to string array
        String[] idsArray = StringUtil.split(markedIdsString, ",");

        //add the ids to ids collection
        for (String id : idsArray) {
            MarkedAsHiddenContactIds.add(id);
        }
    }
}
