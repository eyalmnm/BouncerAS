package com.em_projects.bouncer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.cellebrite.ota.socialphonebook.repositories.handsetdata.DeviceContactsDataRepository;
import com.em_projects.bouncer.model.SmsElement;
import com.em_projects.bouncer.repositories.SmsRepository;
import com.em_projects.bouncer.services.LoadConversationMessagesService;
import com.em_projects.bouncer.views.buildercontrollers.ChatScreenViewBuilderController;
import com.em_projects.bouncer.views.model.ChatScreenViewModel;
import com.em_projects.infra.services.OnServiceCompletedListener;
import com.em_projects.infra.services.ServiceErrorArgs;

import java.util.Vector;

public class BouncerChatActivity extends BouncerBasicActivity {

    public static final String REFRESH_CHAT_SCREEN_ACTION = "refresh_chat_screen";
    private static final String TAG = "BouncerChatActivity";
    //holds the service that loads conversation's messages
    private static LoadConversationMessagesService s_loadConversationMessagesService;
    //holds current conversation's phone number
    private static String s_convPhoneNumber;
    // BroadcastReceiver for handle of receive SMS when chat screen is open,
    // and contact is not hidden.
    private BroadcastReceiver refreshChatBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            Log.d(TAG, "onReceive");
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    // create a delay to allow the device to process the SMS and to register it in the database
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        Log.e(getClass().getSimpleName(), "refreshChatBroadcast.onReceived() throws exception!");
                    }

                    // get SMS phone number
                    String phoneNumber = intent.getStringExtra("adress");

                    // if the received sms belongs to the currently opened conversation
                    if (DeviceContactsDataRepository.getInstance().cleanPhoneNumber(phoneNumber)
                            .equals(DeviceContactsDataRepository.getInstance().cleanPhoneNumber(s_convPhoneNumber))) {
                        // reload messages
                        s_loadConversationMessagesService.startService();

                        // refresh the conversations tab
                        SmsRepository.getInstance().setConversationAsRead(phoneNumber);
                    }
                }
            });
            t.start();
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        //start gauge
        startGauge(this);

        // holds contact phone number
        s_convPhoneNumber = (String) getIntent().getCharSequenceExtra("address");

        // show empty chat screen
        showView(new ChatScreenViewBuilderController(new ChatScreenViewModel(s_convPhoneNumber, new Vector<SmsElement>())));

        // start loading messages
        startConversationMessagesSerice(s_convPhoneNumber);

        // change SMS from unread to read in DB
        SmsRepository.getInstance().setConversationAsRead(s_convPhoneNumber);

        //hide the soft-keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    /**
     * Loads conversation's messages by phone number.
     *
     * @param convPhoneNumber (String != null) The phone number this conversation is conducted with.
     */
    private void startConversationMessagesSerice(final String convPhoneNumber) {
        Log.d(TAG, "startConversationMessagesSerice");
        // init load-chat-messages service
        s_loadConversationMessagesService = new LoadConversationMessagesService(convPhoneNumber);

        // set load-chat-messages service listener
        s_loadConversationMessagesService.setListener(new OnServiceCompletedListener<Vector<SmsElement>>() {
            @Override
            protected void onServiceProgressChanged(Vector<SmsElement> messages) {

            }

            @Override
            protected void onServiceError(ServiceErrorArgs args) {

            }

            @Override
            protected void onServiceCompleted(Vector<SmsElement> messages) {
                refreshView(new ChatScreenViewModel(convPhoneNumber, messages));
            }
        });

        // start the service
        s_loadConversationMessagesService.startService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

        // unregister refresh-chat-broadcast
        unregisterReceiver(refreshChatBroadcast);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        // register refresh-chat-broadcast
        registerReceiver(refreshChatBroadcast, new IntentFilter(REFRESH_CHAT_SCREEN_ACTION));
    }
}
