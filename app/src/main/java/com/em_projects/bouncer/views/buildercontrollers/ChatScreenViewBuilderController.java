package com.em_projects.bouncer.views.buildercontrollers;

import android.app.PendingIntent;
import android.content.Context;
import android.database.DataSetObserver;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.em_projects.bouncer.BouncerActivity;
import com.em_projects.bouncer.BouncerApplication;
import com.em_projects.bouncer.R;
import com.em_projects.bouncer.model.Contact;
import com.em_projects.bouncer.model.SmsElement;
import com.em_projects.bouncer.repositories.ContactsRepository;
import com.em_projects.bouncer.repositories.SmsRepository;
import com.em_projects.bouncer.views.model.ChatBubbleViewModel;
import com.em_projects.bouncer.views.model.ChatScreenViewModel;
import com.em_projects.infra.activity.BasicActivity;
import com.em_projects.infra.views.buildercontrollers.ViewBuilderController;
import com.em_projects.infra.views.controllers.ViewController;
import com.em_projects.utils.StringUtil;

import java.util.ArrayList;
import java.util.Vector;

public class ChatScreenViewBuilderController extends ViewBuilderController<ChatScreenViewModel> {
    private static final String TAG = "ChatScreenViewBldrCntlr";
    ChatScreenViewAdapter m_adapter = new ChatScreenViewAdapter();

    private Context m_context;

    public ChatScreenViewBuilderController(ChatScreenViewModel model) {
        super(model);
        Log.d(TAG, "ChatScreenViewBuilderController");

        m_adapter.setNewModel(model);
    }

    @Override
    public View getMainLayout(LayoutInflater inflater) {
        Log.d(TAG, "getMainLayout");
        // get chat screen layout
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.chat_screen_layout, null);

        // set name on chat screen's title
        ((TextView) layout.findViewById(R.id.title)).setText(getModel().ContactName);

        return layout;
    }

    @Override
    public ViewController getViewController() {
        Log.d(TAG, "getViewController");
        return this;
    }

    @Override
    public void attachController(final View view) {
        Log.d(TAG, "attachController");
        m_context = BouncerApplication.getApplication().getActivityInForeground();

        final EditText editNewSms = (EditText) view.findViewById(R.id.editSms);

        //set send button on-click listener
        Button sendButton = (Button) view.findViewById(R.id.sendSmsButton);
        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //get entered text
                final String smsBody = editNewSms.getText().toString();

                //in case there is no text, do nothing
                if (StringUtil.isNullOrEmpty(smsBody.trim()))
                    return;

                //get recipient's phone number
                String phoneNumber = getModel().ContactPhoneNumber;

                //send the SMS message
                SmsManager sms = SmsManager.getDefault();

                //dividing the SMS
                ArrayList<String> messages = sms.divideMessage(smsBody);

                //hold pending intent for each message
                ArrayList<PendingIntent> listOfIntents = new ArrayList<PendingIntent>();
                for (int i = 0; i < messages.size(); i++) {
                    PendingIntent piSendSms = PendingIntent.getActivity(m_context, 0, null, 0);
                    listOfIntents.add(piSendSms);
                }

                //send the message
                sms.sendMultipartTextMessage(phoneNumber, null, messages, listOfIntents, null);

                // clean edit text
                editNewSms.setText("");

                //get whether the conversation is hidden
                Contact contact = ContactsRepository.getInstance().getContactByPhoneNumber(phoneNumber);
                boolean isHidden = false;
                if (contact != null)
                    isHidden = contact.isHidden();

                //get conversation's id
                int convId;
                if (isHidden)
                    convId = Integer.valueOf(SmsRepository.getInstance().getOrCreateHiddenConvId(phoneNumber)).intValue();
                else
                    convId = (int) SmsRepository.getInstance().getOrCreateThreadId(phoneNumber);

                //creates the SMS element
                SmsElement smsElement = new SmsElement(SmsRepository.getInstance().generateNewSmsId(isHidden),
                        convId,
                        phoneNumber,
                        System.currentTimeMillis(),
                        SmsElement.READ_STATE.READ,
                        0,
                        SmsElement.SMS_TYPE.OUTGOING,
                        "",
                        smsBody,
                        "",
                        0);

                //insert SMS element into DB
                SmsRepository.getInstance().insertSmsToDB(smsElement, isHidden);

                //get sms messages
                Vector<SmsElement> smsMessages = SmsRepository.getInstance().getSmsListByPhoneNumber(phoneNumber);

                //refresh the view to show the added message
                refreshView(view, new ChatScreenViewModel(getModel().ContactPhoneNumber, smsMessages));

                //remove the conversation from cache so it will reload
                SmsRepository.getInstance().removeFromCache(String.valueOf(convId));

                //reload conversations list in tabs screen
                BouncerActivity.s_conversationsService.startService();
            }
        });

        //get chat screen's list-view and set its params
        final ListView lv = (ListView) view.findViewById(R.id.scrollBubbles);
        lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        lv.setDivider(null);
        lv.setDividerHeight(0);

        //set listener to scroll the list view to the buttom
        m_adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                lv.setSelection(m_adapter.getCount() - 1);
            }
        });

        //set list-view's adapter
        lv.setAdapter(m_adapter);
    }

    @Override
    public void refreshView(View mainlayout, Object newModel) {
        Log.d(TAG, "refreshView");
        //set focus on the text field
        mainlayout.findViewById(R.id.editSms).requestFocus();

        //set new model to the adapter
        m_adapter.setNewModel((ChatScreenViewModel) newModel);

        //notify that data has changed - triggers repaint
        m_adapter.notifyDataSetChanged();

        BasicActivity.stopGauge();
    }

    @Override
    public boolean onBackKeyPressed() {
        Log.d(TAG, "onBackKeyPressed");
        BouncerApplication.getApplication().getActivityInForeground().finish();

        return true;
    }

    private static class ChatScreenViewAdapter extends BaseAdapter {
        ChatScreenViewModel m_model;

        public void setNewModel(ChatScreenViewModel model) {
            m_model = model;
        }

        @Override
        public int getCount() {
            return m_model.getMessages().size();
        }

        @Override
        public Object getItem(int position) {
            return m_model.getMessages().elementAt(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = BouncerApplication.getApplication().getActivityInForeground().getLayoutInflater();

            Vector<ChatBubbleViewModel> messages = m_model.getMessages();

            // init the object that holds list-view item's data
            if (convertView == null) {
                convertView = (LinearLayout) inflater.inflate(R.layout.chat_item_layout, null);
            }

            //get the current chat bubble view model
            ChatBubbleViewModel bubbleVM = messages.elementAt(position);

            //get the chat bubble layout
            LinearLayout bubble = (LinearLayout) convertView.findViewById(R.id.bubble);

            // hold layout params for bubble
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) bubble.getLayoutParams();

            // check sms type (incoming or outgoing) and add matching background, name and margins
            if (bubbleVM.IsIncoming) {
                bubble.setBackgroundResource(R.drawable.incoming_bubble);
                layoutParams.setMargins(10, 10, 120, 10);
                ((TextView) convertView.findViewById(R.id.name)).setText(bubbleVM.Name);
            } else {
                bubble.setBackgroundResource(R.drawable.outgoing_bubble);
                layoutParams.setMargins(120, 10, 10, 10);
                ((TextView) convertView.findViewById(R.id.name)).setText(R.string.me);
            }

            // add layout params to the bubble
            bubble.setLayoutParams(layoutParams);

            // add message body
            ((TextView) convertView.findViewById(R.id.content)).setText(bubbleVM.Body);

            //add message date
            ((TextView) convertView.findViewById(R.id.time)).setText(bubbleVM.Date);

            return convertView;
        }
    }
}
