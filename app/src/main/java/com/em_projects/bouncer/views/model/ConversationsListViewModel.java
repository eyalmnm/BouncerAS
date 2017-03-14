package com.em_projects.bouncer.views.model;

import android.util.Log;

import com.em_projects.bouncer.BouncerApplication;
import com.em_projects.bouncer.BouncerProperties;
import com.em_projects.bouncer.R;
import com.em_projects.bouncer.model.Conversation;
import com.em_projects.infra.views.model.TabViewModel;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

@SuppressWarnings("serial")
public class ConversationsListViewModel extends TabViewModel implements Serializable {
    private static final String TAG = "ConversationsLstVwMdl";

    private Vector<ConversationViewModel> m_conversationsVMs;

    public ConversationsListViewModel(List<Conversation> conversations) {
        super(BouncerProperties.TAB_TYPE.CONVERSATIONS, R.drawable.tab_item, BouncerApplication.getApplication().getString(R.string.conversations_tab_label), false);
        Log.d(TAG, "ConversationsListViewModel");

        m_conversationsVMs = new Vector<ConversationViewModel>();

        for (Conversation conversation : conversations) {
            //in case we have conversation, add it to conversations collection
            if (conversation != null)
                m_conversationsVMs.add(new ConversationViewModel(conversation));
        }
    }

    public Vector<ConversationViewModel> getConversationsViewModels() {
        Log.d(TAG, "getConversationsViewModels");
        return m_conversationsVMs;
    }
}
