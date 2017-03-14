package com.em_projects.bouncer.views.model;

import android.util.Log;

import com.em_projects.bouncer.model.SmsElement;
import com.em_projects.bouncer.repositories.ContactsRepository;

import java.util.Date;

public class ChatBubbleViewModel {
    private static final String TAG = "ChatBubbleViewModel";
    public String Name;
    public String Body;
    public String Date;
    public boolean IsIncoming;

    public ChatBubbleViewModel(SmsElement sms) {
        Log.d(TAG, "ChatBubbleViewModel");
        Name = ContactsRepository.getInstance().getDisplayNameByPhoneNumber(sms.PhoneNumber);
        Body = sms.Body;
        Date = new Date(sms.Date).toLocaleString();
        IsIncoming = sms.Type == SmsElement.SMS_TYPE.INCOMING ? true : false;
    }
}
