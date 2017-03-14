package com.em_projects.bouncer.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.em_projects.bouncer.BouncerApplication;
import com.em_projects.bouncer.BouncerUserSession;
import com.em_projects.bouncer.repositories.CallLogsRepository;
import com.em_projects.bouncer.repositories.ContactsRepository;

public class CallBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "CallBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "CallBroadcastReceiver.onReceive() - incoming call!!!");

        //get the intent bundle
        Bundle bundle = intent.getExtras();

        //in case this is not incoming call, abort call handling.
        if (BouncerApplication.TM.getCallState() != TelephonyManager.CALL_STATE_RINGING)
            return;

        //get caller's number
        String callerNumber = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

        //get message recipient id
        String contactId = ContactsRepository.getInstance().getContactIdByPhoneNumber(callerNumber);

        //hold user session
        BouncerUserSession userSession = BouncerApplication.getApplication().getUserSession();

        //restore marked-as-hidden contacts ids, if needed
        if (userSession.MarkedAsHiddenContactIds.isEmpty())
            userSession.restoreMarkedContactIds();

        //in case the call is from a hidden contact and the privacy is ON
        if (contactId != null && userSession.IsPrivacyOn && userSession.MarkedAsHiddenContactIds.contains(contactId)) {
            //cancel the call
            cancelCall();

            Log.d(getClass().getSimpleName(), "CallBroadcastReceiver.onReceive() - Call canceled!");

            try {
                //allow the call to be registered on the device's db
                Thread.sleep(1000);
            } catch (Throwable t) {
                Log.e(getClass().getSimpleName(), "CallBroadcastReceiver.onReceive() throws exception", t);
            }

            //hide the callogs from blocked contact
            if (!CallLogsRepository.getInstance().hideCallLogs(contactId, callerNumber))
                Log.d(getClass().getSimpleName(), "CallBroadcastReceiver.onReceive() - Failed to hide callog!");
        }
    }

    /**
     * Cancells all call notifications
     */
    private void cancelCall() {
        Log.d(TAG, "cancelCall");
        try {
            //hang-up
            BouncerApplication.IT.endCall();

            //aborts the display of the call dialpad screen
            BouncerApplication.IT.showCallScreenWithDialpad(false);

            //cancells missed calls notification
            BouncerApplication.IT.cancelMissedCallsNotification();
        } catch (Exception e) {
            Log.e(TAG, "Error in accessing Telephony Manager", e);
        }
    }
}
