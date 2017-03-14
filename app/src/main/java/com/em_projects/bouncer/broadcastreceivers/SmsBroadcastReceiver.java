package com.em_projects.bouncer.broadcastreceivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.cellebrite.ota.socialphonebook.repositories.handsetdata.DeviceContactsDataRepository;
import com.em_projects.bouncer.BouncerActivity;
import com.em_projects.bouncer.BouncerApplication;
import com.em_projects.bouncer.BouncerChatActivity;
import com.em_projects.bouncer.BouncerUserSession;
import com.em_projects.bouncer.model.SmsElement;
import com.em_projects.bouncer.repositories.ContactsRepository;
import com.em_projects.bouncer.repositories.SmsRepository;
import com.em_projects.bouncer.views.model.ChatScreenViewModel;

import java.util.Vector;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "SmsBroadcastReceiver.onReceive() - SMS has arrived!");

        //this stops notifications to others
        this.abortBroadcast();

        //get the SMS message passed in
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        if (bundle != null) {
            //---retrieve the SMS message received---
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            for (int i = 0; i < msgs.length; i++) {
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            }

            //hold the current sms
            SmsMessage recievedSms = msgs[0];

            //get phone number
            String phoneNumber = recievedSms.getDisplayOriginatingAddress();
            String cleanPhoneNumber = DeviceContactsDataRepository.getInstance().cleanPhoneNumber(phoneNumber);

            //get message recipient id
            String contactId = ContactsRepository.getInstance().getContactIdByPhoneNumber(cleanPhoneNumber);

            //hold user session
            BouncerUserSession userSession = BouncerApplication.getApplication().getUserSession();

            //restore marked-as-hidden contacts ids, if needed
            if (userSession.MarkedAsHiddenContactIds.isEmpty())
                userSession.restoreMarkedContactIds();

            //hold the current activity
            Activity activity = BouncerApplication.getApplication().getActivityInForeground();

            //in case the message is from a non-contact or the contact is not blocked or privacy is off
            if (contactId == null || !userSession.IsPrivacyOn || !userSession.MarkedAsHiddenContactIds.contains(contactId)) {
                //continue the normal process of sms
                this.clearAbortBroadcast();

                if (activity != null) {
                    Intent refreshIntent = new Intent(BouncerChatActivity.REFRESH_CHAT_SCREEN_ACTION);
                    refreshIntent.putExtra("adress", cleanPhoneNumber);
                    activity.sendBroadcast(refreshIntent);
                }

                return;
            }

            //get new sms id
            int id = SmsRepository.getInstance().generateNewSmsId(true);

            //get conversation id
            String convId = SmsRepository.getInstance().getOrCreateHiddenConvId(recievedSms.getOriginatingAddress());

            //create SmsElement
            SmsElement sms = new SmsElement(id,
                    Integer.valueOf(convId).intValue(),
                    phoneNumber,
                    recievedSms.getTimestampMillis(),
                    SmsElement.READ_STATE.UNREAD,
                    recievedSms.getStatus(),
                    SmsElement.SMS_TYPE.INCOMING,
                    recievedSms.getPseudoSubject(),
                    recievedSms.getMessageBody(),
                    recievedSms.getServiceCenterAddress(),
                    0);

            //insert the sms into the hidden database
            SmsRepository.getInstance().insertSmsToDB(sms, true);

            //removed the conversation from cache so it will reload
            SmsRepository.getInstance().removeFromCache(convId);

            if (activity != null) {
                //reload conversations
                BouncerActivity.s_conversationsService.startService();

                //if we are in chat screen, refresh the screen
                if (activity.getClass().equals(BouncerChatActivity.class)) {
                    //get sms messages
                    Vector<SmsElement> smsMessages = SmsRepository.getInstance().getSmsListByPhoneNumber(sms.PhoneNumber);

                    //refresh the screen
                    ChatScreenViewModel model = new ChatScreenViewModel(sms.PhoneNumber, smsMessages);
                    BouncerApplication.getApplication().getActivityInForeground().refreshView(model);

                    //refresh the conversations tab
                    SmsRepository.getInstance().setConvAsReadByConvId(convId);
                }
            }
        }
    }
}
