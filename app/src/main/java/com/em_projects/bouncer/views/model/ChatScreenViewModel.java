package com.em_projects.bouncer.views.model;

import android.util.Log;

import com.em_projects.bouncer.model.SmsElement;
import com.em_projects.bouncer.repositories.ContactsRepository;

import java.util.Vector;


public class ChatScreenViewModel {
    private static final String TAG = "ChatScreenViewModel";

    //	// hold object properties
    public final String ContactPhoneNumber;
    public final String ContactName;

    private Vector<ChatBubbleViewModel> m_bubbles;

    /**
     * Ctor.
     */
    public ChatScreenViewModel(String contactPhoneNumber, Vector<SmsElement> messages) {
        Log.d(TAG, "ChatScreenViewModel");
        ContactPhoneNumber = contactPhoneNumber;
        ContactName = ContactsRepository.getInstance().getDisplayNameByPhoneNumber(contactPhoneNumber);

        m_bubbles = new Vector<ChatBubbleViewModel>();

        for (SmsElement sms : messages) {
            if (sms != null)
                m_bubbles.add(new ChatBubbleViewModel(sms));
        }
    }

    public Vector<ChatBubbleViewModel> getMessages() {
        return m_bubbles;
    }
}
