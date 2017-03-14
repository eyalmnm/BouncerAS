package com.em_projects.bouncer.services;

import android.util.Log;

import com.em_projects.bouncer.model.Conversation;
import com.em_projects.bouncer.repositories.SmsRepository;
import com.em_projects.bouncer.views.model.ConversationsListViewModel;
import com.em_projects.infra.services.AsyncService;

import java.util.Vector;

public class LoadConversationsService extends AsyncService<ConversationsListViewModel> {
    private static final String TAG = "LoadConversationsServ";

    @Override
    protected ConversationsListViewModel execute() {
        Log.d(TAG, "execute");
        //holds lazy conversations
        Vector<Conversation> lazyConversations = SmsRepository.getInstance().getLazyConversations();

        //sort the conversations collection
        SmsRepository.getInstance().sortLazyConversations(lazyConversations);

        //construct new conversations list model with the collected conversations and return it.
        return new ConversationsListViewModel(lazyConversations);
    }
}
