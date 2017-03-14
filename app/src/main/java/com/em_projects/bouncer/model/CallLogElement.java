package com.em_projects.bouncer.model;

import android.util.Log;

import com.cellebrite.ota.socialphonebook.repositories.handsetdata.CallElement;
import com.em_projects.infra.model.Entity;

import java.util.Comparator;

@SuppressWarnings("serial")
public class CallLogElement extends Entity<String> {

    private static final String TAG = "CallLogElement";

    //Comparator for call-logs list sorting
    public static Comparator<CallLogElement> COMPARE_BY_CALL_TIME = new Comparator<CallLogElement>() {
        public int compare(CallLogElement one, CallLogElement other) {
            if (one != null && other != null)
                return String.valueOf(other.CallTime).compareTo(String.valueOf(one.CallTime));
            else
                return 0;
        }
    };
    //holds call id
    public final String UID;
    //holds caller phone number
    public final String CallerNumber;
    //holds call time (the time the call occurred)
    public final long CallTime;

    //holds call type (incoming, outgoing, missed)
    public final int CallType;

    //holds call time duration
    public final long CallDuration;
    //holds caller name
    public String CallerName;

    /**
     * Call Element.
     */
    public CallLogElement(CallElement ce) {
        super(ce.getId());
        Log.d(TAG, "CallLogElement");

        UID = ce.getId();
        CallDuration = ce.getCallAbsDuration();
        CallerName = ce.getCallerName();
        CallerNumber = ce.getCallerNumber();
        CallTime = ce.getCallAbsTime();
        CallType = ce.getCallType();
    }

    /**
     * Call Element.
     */
    public CallLogElement(String id, String name, String number, int type, long duration, long time) {
        super(id);
        Log.d(TAG, "CallLogElement");

        UID = id;
        CallDuration = duration;
        CallerName = name;
        CallerNumber = number;
        CallTime = time;
        CallType = type;
    }

    public static final class CALL_TYPE {
        public static final int INCOMING = 1;
        public static final int OUTGOING = 2;
        public static final int MISSED = 3;
    }
}
