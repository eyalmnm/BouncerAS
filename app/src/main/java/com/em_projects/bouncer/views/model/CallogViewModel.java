package com.em_projects.bouncer.views.model;

import android.util.Log;

import com.em_projects.bouncer.model.CallLogElement;

import java.io.Serializable;

@SuppressWarnings("serial")
public class CallogViewModel implements Serializable {
    private static final String TAG = "CallogViewModel";

    //holds callog id
    public final String UID;

    //holds caller phone number
    public final String CallerNumber;
    //holds call time (the time the call occurred)
    public final long CallTime;
    //holds caller name
    public String CallerName;

    public CallogViewModel(CallLogElement ce) {
        Log.d(TAG, "CallogViewModel");
        UID = ce.UID;
        CallerName = ce.CallerName;
        CallerNumber = ce.CallerNumber;
        CallTime = ce.CallTime;
    }
}
