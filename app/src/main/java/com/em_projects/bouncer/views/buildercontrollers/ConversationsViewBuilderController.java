package com.em_projects.bouncer.views.buildercontrollers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.em_projects.bouncer.BouncerApplication;
import com.em_projects.bouncer.BouncerChatActivity;
import com.em_projects.bouncer.R;
import com.em_projects.bouncer.model.Conversation;
import com.em_projects.bouncer.repositories.SmsRepository;
import com.em_projects.bouncer.views.model.ConversationViewModel;
import com.em_projects.bouncer.views.model.ConversationsListViewModel;
import com.em_projects.infra.views.buildercontrollers.TabViewBuilderController;
import com.em_projects.infra.views.controllers.ViewController;

import java.util.Date;
import java.util.Vector;

public class ConversationsViewBuilderController extends TabViewBuilderController<ConversationsListViewModel> {
    private static final String TAG = "ConversatnsVwBldrCntrlr";

    ConversationsListViewAdapter m_adapter = new ConversationsListViewAdapter();

    public ConversationsViewBuilderController(ConversationsListViewModel model) {
        super(model);
        Log.d(TAG, "ConversationsViewBuilderController");

        m_adapter.setNewModel(model);
    }

    @Override
    public void attachController(final View view) {
        Log.d(TAG, "attachController");
        //get the search textfield and attach its listener
        EditText searchTxt = (EditText) view.findViewById(R.id.search_txt);
        searchTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged");
                //get the filtered conversations
                Vector<Conversation> conversations = SmsRepository.getInstance().getFilteredConversations(s.toString());

                //sort the conversations
                SmsRepository.getInstance().sortLazyConversations(conversations);

                //refresh the screen with filtered conversations
                refreshView(view, new ConversationsListViewModel(conversations));
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //get conversations list view and set its adapter and item-click listener
        ListView lv = (ListView) view.findViewById(R.id.list_view);
        lv.setAdapter(m_adapter);
        lv.setOnItemClickListener(m_adapter);
    }

    @Override
    public View getMainLayout(LayoutInflater inflater) {
        Log.d(TAG, "getMainLayout");
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.tab_content_container, null);
        return layout;
    }

    @Override
    public void refreshView(View mainlayout, Object newModel) {
        Log.d(TAG, "refreshView");
        //set new model to the adapter
        m_adapter.setNewModel((ConversationsListViewModel) newModel);

        //notify that data has changed - triggers repaint
        m_adapter.notifyDataSetChanged();
    }

    @Override
    public ViewController getViewController() {
        Log.d(TAG, "getViewController");
        return this;
    }

    @Override
    public boolean onBackKeyPressed() {
        Log.d(TAG, "onBackKeyPressed");
        return false;
    }

    private static class ConversationsListViewAdapter extends BaseAdapter implements OnItemClickListener {
        ConversationsListViewModel m_model;

        public void setNewModel(ConversationsListViewModel model) {
            m_model = model;
        }

        @Override
        public int getCount() {
            return m_model.getConversationsViewModels().size();
        }

        @Override
        public Object getItem(int position) {
            return m_model.getConversationsViewModels().elementAt(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //get layout inflater
            LayoutInflater inflater = BouncerApplication.getApplication().getActivityInForeground().getLayoutInflater();

            // init the object that holds list-view item's data
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.conversation_list_item, null);
            }

            //holds conversations view models
            Vector<ConversationViewModel> conversationsVMs = m_model.getConversationsViewModels();

            //get the current conversation view model
            ConversationViewModel convVM = conversationsVMs.elementAt(position);

            //get the current conversation
            Conversation conversation = SmsRepository.getInstance().getConversationByConvId(convVM.ConvID);

            //set contact's photo view params
            ImageView thumb = (ImageView) convertView.findViewById(R.id.photo_bg);
            byte[] thumbData = conversation.getThumbData();
            if (thumbData != null) {
                Bitmap thumbBitmap = BitmapFactory.decodeByteArray(thumbData, 0, thumbData.length);
                BitmapDrawable drawable = new BitmapDrawable(BouncerApplication.getApplication().getResources(), thumbBitmap);
                thumb.setBackgroundDrawable(drawable);
            } else {
                thumb.setBackgroundResource(R.drawable.default_photo);
            }

            //set display name
            TextView displayName = (TextView) convertView.findViewById(R.id.display_name);
            displayName.setText(conversation.getDisplayName());

            //set snippet
            TextView snippet = (TextView) convertView.findViewById(R.id.snippet);
            snippet.setText(conversation.getSnippet());

            //set date
            TextView date = (TextView) convertView.findViewById(R.id.date);
            date.setText(new Date(convVM.LastMsgDateTime).toLocaleString());

            //set read/unread icon
            ImageView msgStateIndicator = (ImageView) convertView.findViewById(R.id.msg_state);
            if (conversation.getReadState() == Conversation.READ_STATE.READ)
                msgStateIndicator.setBackgroundResource(R.drawable.read_msg);
            else
                msgStateIndicator.setBackgroundResource(R.drawable.unread_msg);

            return convertView;
        }

        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int index, long arg3) {
            //get context
            Context m_context = BouncerApplication.getApplication().getActivityInForeground().getBaseContext();

            //set intent for opening the conversation in chat activity.
            Intent intent = new Intent(m_context, BouncerChatActivity.class);
            intent.putExtra("address", m_model.getConversationsViewModels().get(index).PhoneNumber);

            //start chat activity
            BouncerApplication.getApplication().getActivityInForeground().startNewActivity(intent, false);
        }
    }
}
