package com.em_projects.bouncer.services;

import android.util.Log;

import com.em_projects.bouncer.model.SmsElement;
import com.em_projects.bouncer.repositories.SmsRepository;
import com.em_projects.infra.services.AsyncService;

import java.util.Vector;

public class LoadConversationMessagesService extends AsyncService<Vector<SmsElement>> {
    private static final String TAG = "LoadConversationMsgsSrv";

    //holds phone number for messages retreival
    private String m_phoneNumber = null;

    /**
     * Ctor.
     *
     * @param phoneNumber (String) Phone number to retreive messages by.
     */
    public LoadConversationMessagesService(String phoneNumber) {
        Log.d(TAG, "LoadConversationMessagesService");
        m_phoneNumber = phoneNumber;
    }

    @Override
    protected Vector<SmsElement> execute() {
        Log.d(TAG, "execute");
        //get sms messages
        return SmsRepository.getInstance().getSmsListByPhoneNumber(m_phoneNumber);
    }
}
