package com.em_projects.bouncer.views.model;

import android.util.Log;

import com.em_projects.bouncer.model.Conversation;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ConversationViewModel implements Serializable {
    private static final String TAG = "ConversationViewModel";

    // hold contact's id
    public final String ConvID;

    // hold contact's phone number
    public final String PhoneNumber;

    //holds last message date/time long value
    public final long LastMsgDateTime;

    public ConversationViewModel(Conversation conv) {
        Log.d(TAG, "ConversationViewModel");
        //init members
        this.ConvID = conv.getUID();
        this.PhoneNumber = conv.getPhoneNumber();
        this.LastMsgDateTime = conv.getDate();
    }
}