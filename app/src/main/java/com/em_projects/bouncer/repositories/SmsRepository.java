package com.em_projects.bouncer.repositories;

import android.util.Log;

import com.cellebrite.ota.socialphonebook.repositories.handsetdata.DeviceContactsDataRepository;
import com.cellebrite.ota.socialphonebook.repositories.handsetdata.DeviceContactsDataRepository.HandsetDataKinds;
import com.cellebrite.ota.socialphonebook.repositories.handsetdata.DeviceContactsDataRepository.HandsetDataRepositoryObserver;
import com.cellebrite.ota.socialphonebook.repositories.handsetdata.SmsObserver;
import com.em_projects.bouncer.BouncerActivity;
import com.em_projects.bouncer.helpers.SmsDbHelper;
import com.em_projects.bouncer.model.Conversation;
import com.em_projects.bouncer.model.SmsElement;
import com.em_projects.infra.repositories.EntityRepositoryWithCache;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class SmsRepository extends EntityRepositoryWithCache<Conversation, String> {
    private static final String TAG = "SmsRepository";

    //holds class instance
    private static SmsRepository s_instance = null;

    /**
     * Private Ctor.
     */
    private SmsRepository() {
        Log.d(TAG, "SmsRepository");
        DeviceContactsDataRepository.getInstance().registerContactObserver(new HandsetDataRepositoryObserver() {
            @Override
            public void onHandsetDataChange(HandsetDataKinds kind) {
                //data has changed - clear cache
                if (kind == HandsetDataKinds.SMS)
                    clearCache();
            }
        });
    }

    /**
     * Singletone method
     *
     * @return (SmsRepository != null) Class single instance.
     */
    public static synchronized SmsRepository getInstance() {
        Log.d(TAG, "getInstance");
        if (s_instance == null) {
            s_instance = new SmsRepository();

            //create new SMSs observer and start it
            SmsObserver observer = new SmsObserver();
            observer.start();
        }

        return s_instance;
    }

    public Vector<Conversation> getLazyConversations() {
        Log.d(TAG, "getLazyConversations");
        //get conversations from device
        Vector<Conversation> lazyConversations = SmsDbHelper.getInstance().getDeviceLazyConversations();

        //get hidden conversations
        lazyConversations.addAll(SmsDbHelper.getInstance().getHiddenLazyConversations());

        return lazyConversations;
    }

//	/**
//	 * <p>Returns all conversations from device and from application's databases.</p>
//	 * <p>Note: the conversations are retrieved with default sorting order. 
//	 * 	  Device's conversations are retrieved first followed by the application's conversations.</P>
//	 * 
//	 * @return (List<Conversation>) a list of Conversation elements.
//	 */
//	public List<Conversation> getAllConversations()
//	{
//		//holds all conversations from device's and application's databases
//		Vector<Conversation> conversations = new Vector<Conversation>();
//		
//		//add device conversations
//		conversations.addAll(SmsDbHelper.getInstance().getAllDeviceConversations());
//		
//		//add application conversations
//		conversations.addAll(SmsDbHelper.getInstance().getAllHiddenConversations());
//		
//		//return the conversations
//		return conversations;
//	}
//	
//	public List<String> getAllConversationIDs()
//	{
//		//holds all conversation ids from device and from hidden db
//		Vector<String> convIDs = new Vector<String>();
//		
//		//get all conversation ids from device
//		convIDs.addAll(SmsDbHelper.getInstance().getAllDeviceConvIDs());
//		
//		//get all conversation ids from hidden db
//		convIDs.addAll(SmsDbHelper.getInstance().getAllHiddenConvIDs());
//		
//		//return ids
//		return convIDs;
//	}

    public Conversation getConversationByConvId(String convId) {
        Log.d(TAG, "getConversationByConvId");
        //try to get the conversation from cache
        Conversation conversation = getFromCache(convId);

        //in case conversation is not in cache
        if (conversation == null) {
            //get the conversation from database
            conversation = SmsDbHelper.getInstance().getConversationByConvId(convId);

            //cache the conversation
            if (conversation != null)
                cache(conversation);
        }

        //return conversation
        return conversation;
    }


    /**
     * Retrieves Conversation by phone number.
     *
     * @param phoneNumber (String != null) Phone number to retrieve.
     * @return (Conversation) The requested Conversation element. May be null.
     */
    public Conversation getConversationByPhoneNumber(String phoneNumber) {
        Log.d(TAG, "getConversationByPhoneNumber");
        // get all conversations
        List<Conversation> conversations = getLazyConversations();

        // go over all conversation's phone number
        for (Conversation conversation : conversations) {
            // if conversation's phone number equal to phone number
            if (DeviceContactsDataRepository.getInstance().cleanPhoneNumber(conversation.getPhoneNumber())
                    .equals(DeviceContactsDataRepository.getInstance().cleanPhoneNumber(phoneNumber)))
                return conversation;
        }

        // if there is no conversation with this phone number
        return null;
    }

    /**
     * <p>Hides all SMS messages that are related to the specified contact id.</p>
     * <p>
     * <p>Hiding is done by backing up the messages on application's sms database
     * and deleting them from device's database.</p>
     *
     * @param contactId (String != null) The id of the contact whose messages we want to delete.
     * @return (boolean) true in case SMSs where hidden successfully, false otherwise.
     */
    public boolean hideSMSs(String contactId) {
        Log.d(TAG, "hideSMSs");
        //backup hidden SMSs in application's DB
        return SmsDbHelper.getInstance().hideSmsList(contactId);
    }

    /**
     * <p>Reveals all SMS messages that are related to the specified contact id.</p>
     * <p>
     * <p>Revealing is done by copying the messages from application's sms database
     * to device's sms database and then deleting them from application's sms database.</p>
     *
     * @param contactId (String != null) The id of the contact whose messages we want to reveal.
     * @return (boolean) true in case SMSs where revealed successfully, false otherwise.
     */
    public boolean revealSMSs(String contactId) {
        Log.d(TAG, "revealSMSs");
        return SmsDbHelper.getInstance().restoreHiddenSmsList(contactId);
    }

    public Vector<SmsElement> getSmsListByConversationId(String convId) {
        Log.d(TAG, "getSmsListByConversationId");
        //get from device
        Vector<SmsElement> smsList = SmsDbHelper.getInstance().getDeviceSmsListByConvId(convId);

        //if empty (maybe hidden) get from hidden db
        if (smsList.isEmpty()) {
            smsList = SmsDbHelper.getInstance().getHiddenSmsListByConvId(convId);
        }

        return smsList;
    }

    public Vector<SmsElement> getSmsListByContactId(String contactId) {
        Log.d(TAG, "getSmsListByContactId");
        //get from device
        Vector<SmsElement> smsList = SmsDbHelper.getInstance().getDeviceSmsElementsByContactId(contactId);

        //if empty (maybe hidden) get from hidden db
        if (smsList.isEmpty()) {
            smsList = SmsDbHelper.getInstance().getHiddenSmsElementsByContactId(contactId);
        }

        return smsList;
    }

    /**
     * Retreives SmsElements from all databases by given phone number.
     *
     * @param phoneNumber (String != null) The phone number to search by.
     * @return (Vector<SmsElement>) A collection of Sms Elements related to the given number. Might be empty.
     */
    public Vector<SmsElement> getSmsListByPhoneNumber(String phoneNumber) {
        Log.d(TAG, "getSmsListByPhoneNumber");
        //get from device
        Vector<SmsElement> smsList = SmsDbHelper.getInstance().getDeviceSmsElementsByPhoneNumber(phoneNumber);

        //if empty (maybe hidden) get from hidden db
        if (smsList.isEmpty()) {
            smsList = SmsDbHelper.getInstance().getHiddenSmsElementsByPhoneNumber(phoneNumber);
        }

        return smsList;
    }

    /**
     * @see DeviceContactsDataRepository#registerContactObserver(HandsetDataRepositoryObserver);
     */
    public void setConversationsObserver(HandsetDataRepositoryObserver observer) {
        Log.d(TAG, "setConversationsObserver");
        DeviceContactsDataRepository.getInstance().registerContactObserver(observer);
    }

    /**
     * @see SmsDbHelper#insertToHiddenSMSsDB(SmsElement)
     * @see SmsDbHelper#insertToDeviceSMSsDB(SmsElement)
     */
    public boolean insertSmsToDB(SmsElement sms, boolean isHidden) {
        Log.d(TAG, "insertSmsToDB");
        if (isHidden)
            return SmsDbHelper.getInstance().insertToHiddenSMSsDB(sms);
        else
            return SmsDbHelper.getInstance().insertToDeviceSMSsDB(sms);
    }

    /**
     * @see SmsDbHelper#getOrCreateThreadId(String)
     */
    public long getOrCreateThreadId(String phoneNumber) {
        Log.d(TAG, "getOrCreateThreadId");
        return SmsDbHelper.getInstance().getOrCreateThreadId(phoneNumber);
    }

    /**
     * Returns a collection of conversations that match the filter criteria.
     *
     * @param filterStr (String) The string by which to filter the conversations.
     * @return (Vector<Conversations>) a collection of Conversations objects that match the filter criteria.
     */
    public Vector<Conversation> getFilteredConversations(String filterStr) {
        Log.d(TAG, "getFilteredConversations");
        //get lazy conversations
        List<Conversation> lazyConversations = getLazyConversations();

        //holds the filtered conversations
        Vector<Conversation> filteredConvs = new Vector<Conversation>();

        //find conversations that match the filter criteria
        for (Conversation conv : lazyConversations) {
            String displayName = ContactsRepository.getInstance().getDisplayNameByPhoneNumber(conv.getPhoneNumber());

            //in case conversation's recipient names contain the filter string
            if (displayName.toLowerCase().contains(filterStr.trim().toLowerCase()))
                //add the conversation to filtered conversations collection
                filteredConvs.add(conv);
        }

        return filteredConvs;
    }

    /**
     * @see SmsDbHelper#generateNewSmsId(boolean)
     */
    public int generateNewSmsId(boolean isHidden) {
        Log.d(TAG, "generateNewSmsId");
        return SmsDbHelper.getInstance().generateNewSmsId(isHidden);
    }

    /**
     * Retreives hidden conversation's id according to the given phone number.
     * In case conversation with the given number does not exist,
     * a new conversations is created and a new conversations id is returned.
     *
     * @param phoneNumber (String != null) The phone number that the conversation is conducted with.
     * @return (String != null) Conversation's id.
     */
    public String getOrCreateHiddenConvId(String phoneNumber) {
        Log.d(TAG, "getOrCreateHiddenConvId");
        return SmsDbHelper.getInstance().getOrCreateHiddenConvId(phoneNumber);
    }

    /**
     * Change SMS's status from unread to read by phone number.
     *
     * @param phoneNumber (String != null) The conversation's phone number.
     */
    public void setConversationAsRead(String phoneNumber) {
        Log.d(TAG, "setConversationAsRead");
        //get conversation by phone number
        Conversation conversation = getConversationByPhoneNumber(phoneNumber);

        //if we didn't find the conversation, log error
        if (conversation == null) {
            Log.e(getClass().getSimpleName(), "setConversationAsRead() - Failed to find conversation by phone number: " + phoneNumber);

            return;
        }

        setConvAsReadByConvId(conversation.getUID());
    }

    /**
     * Change SMS's status from unread to read by conversation id.
     *
     * @param convId (String != null) The conversation id.
     */
    public void setConvAsReadByConvId(String convId) {
        Log.d(TAG, "setConvAsReadByConvId");
        // update DB
        SmsDbHelper.getInstance().setConversationAsRead(convId);

        //remove the conversation from cache so it will reload
        removeFromCache(convId);

        // update conversation tab
        BouncerActivity.s_conversationsService.startService();
    }

    public void sortLazyConversations(Vector<Conversation> lazyConnversations) {
        Log.d(TAG, "sortLazyConversations");
        //holds read and unread conversations collections
        Vector<Conversation> lazyReadConversations = new Vector<Conversation>(1, 1);
        Vector<Conversation> lazyUnreadConversations = new Vector<Conversation>(1, 1);

        //put conversations into appropriate collection according to its read state
        for (Conversation conv : lazyConnversations) {
            if (conv.getReadState() == Conversation.READ_STATE.READ)
                lazyReadConversations.add(conv);
            else
                lazyUnreadConversations.add(conv);
        }

        //sort both collections
        Collections.sort(lazyReadConversations, Conversation.COMPARE_BY_DATE);
        Collections.sort(lazyUnreadConversations, Conversation.COMPARE_BY_DATE);

        //clear the collection and add unread conversations followed by the read once
        lazyConnversations.clear();
        lazyConnversations.addAll(lazyUnreadConversations);
        lazyConnversations.addAll(lazyReadConversations);
    }
}
