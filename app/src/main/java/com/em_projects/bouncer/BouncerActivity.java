package com.em_projects.bouncer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.cellebrite.ota.socialphonebook.repositories.contacts.DeviceContactsRepository.ContactRepositoryObserver;
import com.cellebrite.ota.socialphonebook.repositories.handsetdata.DeviceContactsDataRepository.HandsetDataKinds;
import com.cellebrite.ota.socialphonebook.repositories.handsetdata.DeviceContactsDataRepository.HandsetDataRepositoryObserver;
import com.em_projects.bouncer.model.CallLogElement;
import com.em_projects.bouncer.model.Contact;
import com.em_projects.bouncer.model.Conversation;
import com.em_projects.bouncer.repositories.CallLogsRepository;
import com.em_projects.bouncer.repositories.ContactsRepository;
import com.em_projects.bouncer.repositories.SmsRepository;
import com.em_projects.bouncer.services.LoadCallLogService;
import com.em_projects.bouncer.services.LoadContactsService;
import com.em_projects.bouncer.services.LoadConversationsService;
import com.em_projects.bouncer.views.buildercontrollers.CallogViewBuilderController;
import com.em_projects.bouncer.views.buildercontrollers.ContactsViewBuilderController;
import com.em_projects.bouncer.views.buildercontrollers.ConversationsViewBuilderController;
import com.em_projects.bouncer.views.buildercontrollers.MainScreenViewBuilderController;
import com.em_projects.bouncer.views.builders.TextScreenViewBuilder;
import com.em_projects.bouncer.views.model.CallogListViewModel;
import com.em_projects.bouncer.views.model.ContactViewModel;
import com.em_projects.bouncer.views.model.ContactsViewModel;
import com.em_projects.bouncer.views.model.ConversationsListViewModel;
import com.em_projects.bouncer.views.model.MainScreenViewModel;
import com.em_projects.bouncer.views.model.TextScreenViewModel;
import com.em_projects.infra.services.OnServiceCompletedListener;
import com.em_projects.infra.services.ServiceErrorArgs;
import com.em_projects.infra.views.buildercontrollers.TabViewBuilderController;
import com.em_projects.infra.views.model.TabViewModel;

import java.util.Vector;

public class BouncerActivity extends BouncerBasicActivity {

    public final static LoadContactsService s_contactsService = new LoadContactsService();
    public final static LoadConversationsService s_conversationsService = new LoadConversationsService();
    public final static LoadCallLogService s_callogService = new LoadCallLogService();
    public static final int CALL_FROM_APP_REQUEST_CODE = 1;
    public static final int EDIT_CONTACT_REQUEST_CODE = 2;
    private static final String TAG = "BouncerActivity";
    //holds locks for data observers
    public static boolean s_contactsObserverLocked = false;
    public static boolean s_conversationsObserverLocked = false;
    public static boolean s_callogsObserverLocked = false;
    public static ContactViewModel s_lastEditContactVM = null;

    public static void startAllServices() {
        Log.d(TAG, "startAllServices");
        if (s_contactsService != null)
            s_contactsService.startService();
        if (s_conversationsService != null)
            s_conversationsService.startService();
        if (s_callogService != null)
            s_callogService.startService();
    }

    public static void lockObservers() {
        Log.d(TAG, "lockObservers");
        s_contactsObserverLocked = true;
        s_conversationsObserverLocked = true;
        s_callogsObserverLocked = true;
    }

    public static void unlockObservers() {
        Log.d(TAG, "unlockObservers");
        s_contactsObserverLocked = false;
        s_conversationsObserverLocked = false;
        s_callogsObserverLocked = false;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        startGauge(this);

        BouncerApplication.getApplication().restoreUserSession();
        ((BouncerUserSession) BouncerApplication.getApplication().getUserSession()).restoreMarkedContactIds();

        //create a vector that will hold the tabs for the tab-host on the main-screen
        Vector<TabViewBuilderController<? extends TabViewModel>> tabs = new Vector<TabViewBuilderController<? extends TabViewModel>>();

        //add contacts tab
        tabs.add(new ContactsViewBuilderController(new ContactsViewModel(new Vector<Contact>())));

        //add conversations tab
        tabs.add(new ConversationsViewBuilderController(new ConversationsListViewModel(new Vector<Conversation>())));

        //add callog tab
        tabs.add(new CallogViewBuilderController(new CallogListViewModel(new Vector<CallLogElement>())));

        //create main screen view model
        MainScreenViewModel model = new MainScreenViewModel(tabs);

        //show the main screen
        showView(new MainScreenViewBuilderController(model));

        //init services that load application's data
        initLoadContactsService();
        initLoadConversationsService();
        initLoadCallLogService();
    }

    /**
     * Loads Contacts.
     */
    private void initLoadContactsService() {
        Log.d(TAG, "initLoadContactsService");
        s_contactsService.setListener(new OnServiceCompletedListener<ContactsViewModel>() {
            @Override
            public void onServiceCompleted(ContactsViewModel contactsVM) {
                //refresh the view after all contacts were loaded
                refreshView(contactsVM);
            }

            @Override
            protected void onServiceError(ServiceErrorArgs args) {

            }

            @Override
            protected void onServiceProgressChanged(ContactsViewModel contactsVM) {
                //refresh view when notified on progress
                refreshView(contactsVM);
            }
        });

        //start load-contacts service
        s_contactsService.startService();

        //track changes in the device's contacts DB and update our contacts DB accordingly
        ContactsRepository.getInstance().setContactsObserver(new ContactRepositoryObserver() {
            @Override
            public void onContactChange() {
                //in case contacts observer should not be ignored
                if (!s_contactsObserverLocked)
                    //start load-contacts service on any change in the device's contacts DB
                    s_contactsService.startService();
            }
        });
    }

    /**
     * Loads Conversations.
     */
    private void initLoadConversationsService() {
        Log.d(TAG, "initLoadConversationsService");
        s_conversationsService.setListener(new OnServiceCompletedListener<ConversationsListViewModel>() {
            @Override
            public void onServiceCompleted(ConversationsListViewModel convListVM) {
                //refresh the view after all conversations were loaded
                refreshView(convListVM);
            }

            @Override
            protected void onServiceError(ServiceErrorArgs args) {

            }

            @Override
            protected void onServiceProgressChanged(ConversationsListViewModel convListVM) {
                //refresh view when notified on progress
                refreshView(convListVM);
            }
        });

        //start load-conversations service
        s_conversationsService.startService();

        //track changes in the device's SMSs DB and update our SMSs DB accordingly
        SmsRepository.getInstance().setConversationsObserver(new HandsetDataRepositoryObserver() {
            @Override
            public void onHandsetDataChange(HandsetDataKinds kind) {
                if (!s_conversationsObserverLocked && kind == HandsetDataKinds.SMS)
                    s_conversationsService.startService();
            }
        });
    }

    /**
     * Loads CallLogs.
     */
    private void initLoadCallLogService() {
        Log.d(TAG, "initLoadCallLogService");
        s_callogService.setListener(new OnServiceCompletedListener<CallogListViewModel>() {
            @Override
            public void onServiceCompleted(CallogListViewModel callogListVM) {
                //refresh the view after all callogs were loaded
                refreshView(callogListVM);
            }

            @Override
            protected void onServiceError(ServiceErrorArgs args) {

            }

            @Override
            protected void onServiceProgressChanged(CallogListViewModel callogListVM) {
                //refresh view when notified on progress
                refreshView(callogListVM);
            }
        });

        //start load-callogs service
        s_callogService.startService();

        //track changes in the device's callogs DB and update our callogs DB accordingly
        CallLogsRepository.getInstance().setCallogObserver(new HandsetDataRepositoryObserver() {
            @Override
            public void onHandsetDataChange(HandsetDataKinds kind) {
                if (!s_callogsObserverLocked && kind == HandsetDataKinds.CALLLOG)
                    s_callogService.startService();
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        Log.d(TAG, "onPrepareOptionsMenu");

        // inflate the menu layout
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * @see Activity#onOptionsItemSelected(MenuItem);
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        // handle menu items selected
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.settings: {
                // open settings screen from userPreference activity
                startActivity(new Intent(getBaseContext(), BouncerSettingsActivity.class));

                break;
            }

            case R.id.about: {
                //set about text
                String aboutText = new StringBuffer(getString(R.string.about_text_pre_version))
                        .append(BouncerProperties.APP_VERSION).append(getString(R.string.about_text_post_version))
                        .toString();

                //set about screen view model and builder
                TextScreenViewModel model = new TextScreenViewModel(getString(R.string.about), aboutText);
                TextScreenViewBuilder builder = new TextScreenViewBuilder(model);

                //show the about screen
                showView(builder);

                break;
            }

//			case R.id.in_app_purchase:
//			{
//				break;
//			}

            case R.id.help: {
                TextScreenViewModel model = new TextScreenViewModel(getString(R.string.help), getString(R.string.help_text));
                TextScreenViewBuilder builder = new TextScreenViewBuilder(model);
                showView(builder);

                break;
            }

            default:
                break;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

        BouncerApplication.getApplication().storeUserSession();
        ((BouncerUserSession) BouncerApplication.getApplication().getUserSession()).storeMarkedContactIds();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        BouncerApplication.getApplication().restoreUserSession();
        ((BouncerUserSession) BouncerApplication.getApplication().getUserSession()).restoreMarkedContactIds();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");

        if (requestCode == CALL_FROM_APP_REQUEST_CODE) {
            Log.d(getClass().getSimpleName(), "onActivityResult() - received result after call. result code is: " + resultCode);

            //hide call-logs that should be hidden
            CallLogsRepository.getInstance().rehideCalls();
        }

        if (requestCode == EDIT_CONTACT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            //TODO - handle conversations and callogs lists update after contact's modification

//			//get the contact that was edited
//			Contact contact = ContactsRepository.getInstance().getByUID(s_lastEditContactVM.UID);
//
//			//set new model according to the changed contact
//			s_lastEditContactVM = new ContactViewModel(contact);
//
//			//refresh the contacts list adapter
//			ContactsViewBuilderController.m_adapter.notifyDataSetChanged();
//
//			//update call-logs
//			Vector<CallogViewModel> clvms = CallogViewBuilderController.m_adapter.m_model.getCallLogsViewModels();
//
//			for (CallogViewModel clvm : clvms)
//			{
//				if (clvm.CallerName.equals(s_lastEditContactVM.FullName))
//				{
//					clvm.CallerName = contact.getDisplayName();
//				}
//			}
        }
    }
}