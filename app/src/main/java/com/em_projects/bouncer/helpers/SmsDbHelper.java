package com.em_projects.bouncer.helpers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.cellebrite.ota.socialphonebook.repositories.handsetdata.DeviceContactsDataRepository;
import com.em_projects.bouncer.BouncerApplication;
import com.em_projects.bouncer.BouncerProperties;
import com.em_projects.bouncer.model.Conversation;
import com.em_projects.bouncer.model.Phone;
import com.em_projects.bouncer.model.SmsElement;
import com.em_projects.bouncer.repositories.ContactsRepository;
import com.em_projects.infra.application.BasicApplication;
import com.em_projects.infra.helpers.DatabaseHelper;
import com.em_projects.infra.helpers.SQLQueryHelper;
import com.em_projects.utils.StringUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class SmsDbHelper extends DatabaseHelper {

    //holds constants for database creation
    public static final String SMS_TABLE_NAME = "sms";
    private static final String TAG = "SmsDbHelper";
    private static final String CONVERSATIONS_TABLE_NAME = "threads";
    //holds the sql commands for database creation
    private static final String[] DATABASE_CREATE = new String[]{
            new StringBuffer()
                    .append("CREATE TABLE ").append(SMS_TABLE_NAME)
                    .append(" (")
                    .append(SmsTable._ID).append(" INTEGER PRIMARY KEY,")
                    .append(SmsTable.THREAD_ID).append(" INTEGER,")
                    .append(SmsTable.ADDRESS).append(" TEXT,")
                    .append(SmsTable.PERSON_ID).append(" INTEGER,")
                    .append(SmsTable.DATE).append(" INTEGER,")
                    .append(SmsTable.PROTOCOL).append(" INTEGER,")
                    .append(SmsTable.READ).append(" INTEGER DEFAULT 0,")
                    .append(SmsTable.STATUS).append(" INTEGER DEFAULT -1,")
                    .append(SmsTable.TYPE).append(" INTEGER,")
                    .append(SmsTable.REPLY_PATH_PRESENT).append(" INTEGER,")
                    .append(SmsTable.SUBJECT).append(" TEXT,")
                    .append(SmsTable.BODY).append(" TEXT,")
                    .append(SmsTable.SERVICE_CENTER).append(" TEXT,")
                    .append(SmsTable.LOCKED).append(" INTEGER DEFAULT 0")
                    .append(")")
                    .toString(),
            new StringBuffer()
                    .append("CREATE TABLE ").append(CONVERSATIONS_TABLE_NAME)
                    .append(" (")
                    .append(ConversationsTable.THREAD_ID).append(" TEXT PRIMARY KEY,")
                    .append(ConversationsTable.MESSAGE_COUNT).append(" INTEGER DEFAULT 0,")
                    .append(ConversationsTable.SNIPPET).append(" TEXT")
                    .append(")")
                    .toString()};
    //holds class instance
    private static SmsDbHelper s_instance;
    //holds content resolver
    private ContentResolver m_contentResolver = BasicApplication.getApplication().getContentResolver();

    /**
     * Ctor.
     */
    private SmsDbHelper() {
        super(BasicApplication.getApplication(),
                BouncerProperties.SMS_DATABASE_NAME, BouncerProperties.DATABASE_VERSION, DATABASE_CREATE);
        Log.d(TAG, "SmsDbHelper");
    }

    /**
     * Singletone method
     */
    public static synchronized SmsDbHelper getInstance() {
        Log.d(TAG, "getInstance");
        if (s_instance == null)
            s_instance = new SmsDbHelper();

        return s_instance;
    }

    public Vector<Conversation> getDeviceLazyConversations() {
        Log.d(TAG, "getDeviceLazyConversations");
        //holds lazy conversations collection
        Vector<Conversation> lazyConversations = new Vector<Conversation>();

        //get all device conversation IDs
        List<String> deviceConvIds = getAllDeviceConvIDs();

        for (String convId : deviceConvIds) {
            //protect from messages without conversation id
            if (StringUtil.isNullOrEmpty(convId))
                continue;

            //holds a cursor to device's conversation messages table
            Cursor c = null;

            //set uri for current conversation's messages
            Uri convMessagesUri = Uri.withAppendedPath(ConversationsTable.CONTENT_URI, convId);

            try {
                //get a cursor to conversation's messages
                c = m_contentResolver.query(convMessagesUri,
                        new String[]{SmsTable.THREAD_ID, SmsTable.ADDRESS, SmsTable.DATE, SmsTable.READ},
                        null, null, null);

                //if we have data, create a lazy conversation and add it to the collection
                if (c != null && c.moveToFirst())
                    lazyConversations.add(getLazyConversationFromCursor(c));
                else
                    Log.d(getClass().getSimpleName(), "getDeviceLazyConversations() - Failed to get device conversation with id: " + convId);
            } catch (Throwable t) {
                Log.e(getClass().getSimpleName(), "getDeviceLazyConversations() - Error occured on getting device's conversations!", t);
            } finally {
                //close the cursor
                if (c != null)
                    c.close();
            }
        }

        //return the conversations
        return lazyConversations;
    }

    public Vector<Conversation> getHiddenLazyConversations() {
        Log.d(TAG, "getHiddenLazyConversations");
        //holds lazy conversations collection
        Vector<Conversation> lazyConversations = new Vector<Conversation>();

        //holds a cursor to application's hidden sms table
        Cursor c = null;

        try {
            //get a cursor to application's hidden sms table
            c = m_db.query(SMS_TABLE_NAME,
                    new String[]{SmsTable.THREAD_ID, SmsTable.ADDRESS, SmsTable.DATE, SmsTable.READ},
                    null, null, null, null, null);

            //if there is data
            if (c != null && c.moveToLast()) {
                //add the thread to the collection
                lazyConversations.add(getLazyConversationFromCursor(c));
            } else {
                Log.d(getClass().getSimpleName(), "getHiddenLazyConversations() - No hidden conversations were found!");
            }
        } catch (Throwable t) {
            Log.e(getClass().getSimpleName(), "getHiddenLazyConversations() - Error occured on getting hidden conversations!");
        } finally {
            //close the cursor
            if (c != null)
                c.close();
        }

        //return the lazy conversations collection
        return lazyConversations;
    }

    /**
     * Retreives a list of device's conversation ids.
     *
     * @return (List<String> != null) A list containing device's conversation ids. The list may be empty.
     */
    public List<String> getAllDeviceConvIDs() {
        Log.d(TAG, "getAllDeviceConvIDs");
        //holds conversation ids
        Vector<String> convIDs = new Vector<String>();

        //holds cursor to device's conversations table
        Cursor convCursor = null;

        try {
            //init cursor
            convCursor = m_contentResolver.query(SmsTable.CONTENT_URI, new String[]{SmsTable.THREAD_ID}
                    , null, null, null);

            convIDs.addAll(SQLQueryHelper.getValuesFromColumn(convCursor, SmsTable.THREAD_ID, true));

            //in case there is no data
            if (convIDs.isEmpty())
                Log.d(getClass().getSimpleName(), "getAllDeviceConvIDs() - No Conversations were found in device DB!");
        } catch (Throwable t) {
            Log.e(getClass().getSimpleName(), "getAllDeviceConvIDs() - Error occured on getting conversations from device DB!");
        } finally {
            //close the cursor
            if (convCursor != null)
                convCursor.close();
        }

        //return ids collection
        return convIDs;
    }

    /**
     * Retrieves Conversation by conversation id.
     * The conversation is first being looked-up in device's db and then, if not found, in the application's db.
     *
     * @param convId (String != null) Conversation id to retrieve.
     * @return (Conversation) The requested Conversation element. May be null.
     */
    public Conversation getConversationByConvId(String convId) {
        Log.d(TAG, "getConversationByConvId");
        //holds a cursor to sms table
        Cursor smsCursor = null;

        //holds the conversation
        Conversation conv = null;

        try {
            //set uri to the requested conversation
            Uri convUri = Uri.withAppendedPath(ConversationsTable.CONTENT_URI, convId);

            //get a cursor to device's sms table
            smsCursor = m_contentResolver.query(convUri, getSmsTableQueryProjection()
                    , null, null, null);

            //if we have no results from the device's db, try the application's db
            if (smsCursor == null || smsCursor.getCount() == 0) {
                //set query params
                String where = SmsTable.THREAD_ID + "=" + convId;

                //get a cursor to application's sms threads table
                smsCursor = m_db.query(SMS_TABLE_NAME, getSmsTableQueryProjection()
                        , where, null, null, null, SmsTable.DATE + " DESC");
            }

            //in case we have a cursor, get its data
            if (smsCursor != null && smsCursor.moveToFirst()) {
                conv = getConversationFromCursor(smsCursor);
            }
        } catch (Throwable t) {
            Log.e(getClass().getSimpleName(), "getConversationByConvId() - Error on getting conversation with id: " + convId);
        } finally {
            //close the cursor
            if (smsCursor != null)
                smsCursor.close();
        }

        //return the thread
        return conv;
    }

//	/**
//	 * Get all Conversations from device's SMS database.
//	 * 
//	 * @return (Vector<Conversation>) Conversations collection from device's database. May be null.
//	 */
//    public List<Conversation> getAllDeviceConversations()
//	{
//		//holds conversations collection
//		Vector<Conversation> conversations = new Vector<Conversation>();
//		
//		//get all device conversation IDs
//		List<String> deviceConvIds = getAllDeviceConvIDs();
//		
//		for (String convId : deviceConvIds)
//		{
//			//protect from messages without conversation id
//			if (StringUtil.isNullOrEmpty(convId))
//				continue;
//			
//			//holds a cursor to device's conversation messages table
//			Cursor convMessagesCursor = null;
//			
//			//set uri for current conversation's messages
//			Uri convMessagesUri = Uri.withAppendedPath(ConversationsTable.CONTENT_URI, convId);
//			
//			try
//			{
//				//get a cursor to conversation's messages
//				convMessagesCursor = m_contentResolver.query(convMessagesUri ,getSmsTableQueryProjection() 
//					,null, null ,null);
//
//				//if we have data, create a Conversation object and add it to the collection
//				if (convMessagesCursor != null && convMessagesCursor.moveToFirst())
//					conversations.add(getConversationFromCursor(convMessagesCursor));										
//				else
//					Log.d(getClass().getSimpleName(), "getAllDeviceConversations() - Failed to get device conversation with id: " + convId);
//			}
//			catch (Throwable t) 
//			{
//				Log.e(getClass().getSimpleName(), "getAllDeviceConversations() - Error occured on getting device's conversations!", t);
//			}
//			finally
//			{
//				//close the cursor
//				if (convMessagesCursor != null)
//					convMessagesCursor.close();
//			}
//		}
//
//		//return the conversations
//		return conversations;
//	}


//	/**
//	 * Get all conversations from application's hidden SMS database.
//	 * 
//	 * @return (Vector<Conversation> != null) Conversations collection from device's database. May be null.
//	 */
//    public List<Conversation> getAllHiddenConversations()
//	{
//		//holds conversations collection
//		Vector<Conversation> conversations = new Vector<Conversation>();
//		
//		//holds a cursor to application's hidden sms table
//		Cursor smsCursor = null;
//		
//		try
//		{
//			//get a cursor to application's hidden sms table
//			smsCursor = m_db.query(SMS_TABLE_NAME ,getSmsTableQueryProjection() ,null, null ,null, null, null);
//		
//			//if there is data
//			if (smsCursor != null && smsCursor.moveToFirst())
//			{
//				do
//				{
//					//add the thread to the collection
//					conversations.add(getConversationFromCursor(smsCursor));
//				}
//				while (smsCursor.moveToNext());
//			}
//			else
//			{
//				Log.d(getClass().getSimpleName(), "getAllHiddenConversations() - No hidden conversations were found!");
//			}
//		}
//		catch (Throwable t) 
//		{
//			Log.e(getClass().getSimpleName(), "getAllHiddenConversations() - Error occured on getting hidden conversations!");
//		}
//		finally
//		{
//			//close the cursor
//			if (smsCursor != null)
//				smsCursor.close();
//		}
//
//		//return the conversations collection
//		return conversations;
//	}

    /**
     * <p>Backs up hidden SMSs in application's DB.</p>
     * <p>
     * The process is:</br>
     * 1. Get contact's SMS messages from device's DB.</br>
     * 2. Insert messages into application's DB.</br>
     * 3. Delete messages from device's DB.
     * </p>
     * <p>
     * <p>In case of any failure/error during the insertion/deletion process, changes are rolled back.</p>
     *
     * @param contactId (String != null) The id of the contact whose messages we want to back up.
     * @return (boolean) true in case SMSs where backed-up successfully, false otherwise.
     */
    public boolean hideSmsList(String contactId) {
        Log.d(TAG, "hideSmsList");
        //get contact's SMS messages from device's DB
        Vector<SmsElement> deviceSmsElements = getDeviceSmsElementsByContactId(contactId);

        //in case the contact has no messages, we have nothing to do.
        if (deviceSmsElements.size() <= 0) {
            Log.d(getClass().getSimpleName(), "hideSmsList() - no SMS messages to hide for contact id: " + contactId);

            //indicate hide operation was handled
            return true;
        }

        m_db.beginTransaction();

        //insert messages into hidden sms database and delete them from device
        if (!insertToHiddenSMSsDB(deviceSmsElements) || !deleteHiddenSMSsFromDevice(deviceSmsElements)) {
            //in case the insert failed or nothing was deleted, log error and roll-back transaction
            Log.e(getClass().getSimpleName(), "hideSmsList() - Failed to backup hidden SMSs. Rolling back transaction.");
            m_db.endTransaction();

            //indicate failure to hide SMSs
            return false;
        }

        m_db.setTransactionSuccessful();
        m_db.endTransaction();

        //indicate success
        return true;
    }

//    /**
//     * Retreives a list of hidden conversation ids.
//     * 
//     * @return (List<String> != null) A list containing hidden conversation ids. The list may be empty.
//     */
//    public List<String> getAllHiddenConvIDs()
//    {
//    	//holds conversation ids
//    	Vector<String> convIDs = new Vector<String>();
//    	
//    	//holds cursor to hidden conversations table
//    	Cursor convCursor = null;
//    	
//    	try
//		{
//	    	//init cursor
//			convCursor = m_db.query(true, SMS_TABLE_NAME, new String[]{ConversationsTable.THREAD_ID}, 
//					null, null, null, null, null, null);
//			
//			convIDs.addAll(SQLQueryHelper.getValuesFromColumn(convCursor, ConversationsTable.THREAD_ID, true));
//			
//			//in case there is no data
//			if (convIDs.isEmpty())
//				Log.d(getClass().getSimpleName(), "getAllHiddenConvIDs() - No Conversations were found in hidden DB!");
//		}
//		catch (Throwable t) 
//		{
//			Log.e(getClass().getSimpleName(), "getAllHiddenConvIDs() - Error occured on getting conversations from hidden DB!");
//		}
//		finally
//		{
//			//close the cursor
//			if (convCursor != null)
//				convCursor.close();
//		}
//		
//		//return ids collection
//		return convIDs;
//    }

    /**
     * Returns a list of SmsElement filtered by a given contact ID.
     *
     * @param contactId (String != null) The ID of the contact whose SMSs we want to get.
     * @return (Vector<SmsElement>) a list of SMS elements.
     */
    public Vector<SmsElement> getDeviceSmsElementsByContactId(String contactId) {
        Log.d(TAG, "getDeviceSmsElementsByContactId");
        //holds the cursor to the sms table
        Cursor smsCur = null;

        //holds the results
        Vector<SmsElement> results = new Vector<SmsElement>();

        try {
            //get a list of contact's phone numbers
            Vector<Phone> phones = ContactsRepository.getInstance().getPhones(contactId);

            //get cursor of all device's SMSs
            smsCur = m_contentResolver.query(SmsTable.CONTENT_URI, null, null, null, null);

            //if there is data
            if (smsCur != null && smsCur.moveToFirst()) {
                do {
                    //get phone number from cursor
                    String phoneNumber = smsCur.getString(smsCur.getColumnIndex(SmsTable.ADDRESS));

                    for (Phone p : phones) {
                        //if there is SMS with this contact
                        if (DeviceContactsDataRepository.getInstance().cleanPhoneNumber(p.getNumber())
                                .equals(DeviceContactsDataRepository.getInstance().cleanPhoneNumber(phoneNumber))) {
                            //add the SMS element to results vector
                            results.add(getSmsElementFromCursor(smsCur));

                            break;
                        }
                    }
                }
                while (smsCur.moveToNext());
            }
            //if there is no data
            else {
                Log.d(getClass().getSimpleName(), "getDeviceSmsElementsByContactId() - No SMSs were found for contact id (" + contactId + ")");
            }
        } catch (Throwable t) {
            Log.e(getClass().getSimpleName(), "getDeviceSmsElementsByContactId() - Error occured on getting device's SMS entries.");
        } finally {
            //close the cursor
            if (smsCur != null)
                smsCur.close();
        }

        //return the results
        return results;
    }

    /**
     * Inserts one passed SMS element into the hidden SMSs database.
     *
     * @param sms (SmsElement != null)The SMS element to insert to hidden SMSs database.
     * @return (boolean) true in case the element was inserted successfully, false otherwise.
     */
    public boolean insertToHiddenSMSsDB(SmsElement sms) {
        Log.d(TAG, "insertToHiddenSMSsDB");
        Vector<SmsElement> smsElements = new Vector<SmsElement>();
        smsElements.add(sms);

        return insertToHiddenSMSsDB(smsElements);
    }

    /**
     * Inserts the passed SMS elements into the hidden SMSs database.
     *
     * @param smsElements (Vector<SmsElement> != null) The SMS elements to insert to hidden SMSs database.
     * @return (boolean) true in case all elements were inserted successfully, false otherwise.
     */
    private boolean insertToHiddenSMSsDB(Vector<SmsElement> smsElements) {
        Log.d(TAG, "insertToHiddenSMSsDB");
        //holds the values to put in hidden sms table
        ContentValues values = new ContentValues();

        //insert elements into hidden sms database
        for (SmsElement sms : smsElements) {
            //clear the values collection from old values, if any
            values.clear();

            //set columns values
            values.put(SmsTable._ID, sms.MessageId);
            values.put(SmsTable.THREAD_ID, sms.ThreadId);
            values.put(SmsTable.ADDRESS, sms.PhoneNumber);
//			values.put(SmsTable.PERSON_ID, sms.PersonID);
            values.put(SmsTable.DATE, sms.Date);
//			values.put(SmsTable.PROTOCOL, sms.Protocol);
            values.put(SmsTable.READ, sms.Read);
            values.put(SmsTable.STATUS, sms.Status);
            values.put(SmsTable.TYPE, sms.Type);
//			values.put(SmsTable.REPLY_PATH_PRESENT, sms.ReplyPathPresent);
            values.put(SmsTable.SUBJECT, sms.Subject);
            values.put(SmsTable.BODY, sms.Body);
            values.put(SmsTable.SERVICE_CENTER, sms.ServiceCenter);
            values.put(SmsTable.LOCKED, sms.Locked);

            //insert the values into the db
            if (m_db.insert(SMS_TABLE_NAME, null, values) == -1) {
                //in case we fail to insert - throw error to rollback transaction
                Log.e(getClass().getSimpleName(), "insertToHiddenSMSsDB() - Failed to insert hidden sms with id: " + sms.MessageId);

                //indicate that insert was unsuccessful
                return false;
            }
        }

        //get conversation id
        String convId = values.getAsString(SmsTable.THREAD_ID);

        //get the conversation
        Conversation conv = getConversationByConvId(convId);

        if (conv == null) {
            //log error
            Log.e(getClass().getSimpleName(), "insertToHiddenSMSsDB() - Failed to find conversation with id: " + convId);

            //indicate failure
            return false;
        }

        //insert the conversation into the hidden database and return the result
        return insertHiddenConversation(conv.getUID(), conv.getMessageCount(), conv.getSnippet());
    }

    /**
     * Inserts the given parameters into application's hidden database.
     * The values are inserted into 'threads' table.
     *
     * @param convId   (String != null) Conversation id.
     * @param msgCount (int) number of messages within the conversation.
     * @param snippet  (String) Last conversation's message text.
     * @return (boolean) true, in case the insert was successful, false otherwise.
     */
    public boolean insertHiddenConversation(String convId, int msgCount, String snippet) {
        Log.d(TAG, "insertHiddenConversation");
        //set content values for insert operation
        ContentValues convValues = new ContentValues();
        convValues.put(ConversationsTable.THREAD_ID, convId);
        convValues.put(ConversationsTable.MESSAGE_COUNT, msgCount);
        convValues.put(ConversationsTable.SNIPPET, snippet);

        //try updating the database first
        if (m_db.update(CONVERSATIONS_TABLE_NAME, convValues, ConversationsTable.THREAD_ID + "=" + convId, null) <= 0) {
            //insert the conversation into hidden device's database
            if (m_db.insert(CONVERSATIONS_TABLE_NAME, null, convValues) == -1) {
                //in case we fail to insert - throw error to rollback transaction
                Log.e(getClass().getSimpleName(), "insertToHiddenSMSsDB() - Failed to insert hidden conversation with id: " + convId);

                //indicate that insert was unsuccessful
                return false;
            }
        }

        //indicate that insert was successful
        return true;
    }

    /**
     * Deletes the passed sms elements from device's SMS database.
     *
     * @param deviceSmsElements (Vector<SmsElement>) SMS elements to delete from device's SMS database.
     * @return (boolean) true in case all messages were deleted, false otherwise.
     */
    private boolean deleteHiddenSMSsFromDevice(Vector<SmsElement> deviceSmsElements) {
        Log.d(TAG, "deleteHiddenSMSsFromDevice");
        //holds message ids
        Vector<String> msgIDs = new Vector<String>();

        //collect all message ids
        for (SmsElement sms : deviceSmsElements)
            msgIDs.add(String.valueOf(sms.MessageId));

        //set query selection string
        String where = SQLQueryHelper.createINSelection(SmsTable._ID, msgIDs.toArray(new String[msgIDs.size()]));

        //delete coresponding sms entries from device's db
        //indicate failure in case nothing was deleted
        if (m_contentResolver.delete(SmsTable.CONTENT_URI, where, null) <= 0) {
            //log error
            Log.e(getClass().getSimpleName(), "deleteHiddenSMSsFromDevice() - Failed to delete all requested messages from device!");

            //indicate failure
            return false;
        }

        //indicate success
        return true;
    }

    /**
     * <p>Restores hidden SMSs into device's SMSs DB.</p>
     * <p>
     * The process is:</br>
     * 1. Get contact's SMS messages from application's DB.</br>
     * 2. Insert messages into device's DB.</br>
     * 3. Delete messages from application's DB.
     * </p>
     * <p>
     * <p>In case of any failure/error during the insertion/deletion process, changes are rolled back.</p>
     *
     * @param contactId (String != null) The id of the contact whose messages we want to restore.
     * @return (boolean) true in case SMSs where restored successfully, false otherwise.
     */
    public boolean restoreHiddenSmsList(String contactId) {
        Log.d(TAG, "restoreHiddenSmsList");
        //get sms elements from application's DB
        Vector<SmsElement> hiddenSmsElements = getHiddenSmsElementsByContactId(contactId);

        //in case we have no hidden SMSs
        if (hiddenSmsElements.size() <= 0) {
            Log.d(getClass().getSimpleName(), "restoreHiddenSmsList() - Contact has no SMSs. Contact id: " + contactId);

            return true;
        }

        //begin db transaction
        m_db.beginTransaction();

        //move the SMSs from hidden database to device's database.
        //roll-back transaction in case of failure.
        if (!insertToDeviceSMSsDB(hiddenSmsElements) || !deleteSMSsFromHiddenDb(hiddenSmsElements)) {
            //log error
            Log.e(getClass().getSimpleName(), "restoreHiddenSmsList() - Failed to restore hidden SMSs. Rolling back transaction.");

            //roll-back transaction
            m_db.endTransaction();

            //reflect failure
            return false;
        }

        //commit the transaction
        m_db.setTransactionSuccessful();
        m_db.endTransaction();

        //reflect success
        return true;
    }

    /**
     * Returns a list of SmsElement from hidden SMSs database filtered by a given contact ID.
     *
     * @param contactId (String != null) The ID of the contact whoes SMSs we want to get.
     * @return (Vector<SmsElement> != null) a list of SMS elements. The list might be empty.
     */
    public Vector<SmsElement> getHiddenSmsElementsByContactId(String contactId) {
        Log.d(TAG, "getHiddenSmsElementsByContactId");
        //holds the cursor to the hidden sms table
        Cursor smsCur = null;

        //holds the results
        Vector<SmsElement> results = new Vector<SmsElement>();

        try {
            //get a list of contact's phone numbers
            ArrayList<Phone> phones = ContactsDbHelper.getInstance().getHiddenPhoneNumbersById(contactId);

            //in case contact has no phone numbers
            if (phones == null) {
                Log.d(getClass().getSimpleName(), "getHiddenSmsElementsByContactId() - Contact has no phone numbers. Contact id: " + contactId);

                //return empty results list
                return results;
            }

            //get cursor to all hidden sms elements
            smsCur = m_db.query(SMS_TABLE_NAME, getSmsTableQueryProjection(), null, null, null, null, SmsTable.DATE + " ASC");

            //if there is data
            if (smsCur != null && smsCur.moveToFirst()) {
                do {
                    //get phone number from cursor
                    String phoneNumber = smsCur.getString(smsCur.getColumnIndex(SmsTable.ADDRESS));

                    for (Phone p : phones) {
                        //if there is SMS with this contact
                        if (DeviceContactsDataRepository.getInstance().cleanPhoneNumber(p.getNumber())
                                .equals(DeviceContactsDataRepository.getInstance().cleanPhoneNumber(phoneNumber))) {
                            //add the SMS element to results vector
                            results.add(getSmsElementFromCursor(smsCur));

                            break;
                        }
                    }
                }
                while (smsCur.moveToNext());
            } else {
                Log.d(getClass().getSimpleName(), "getHiddenSmsElementsByContactId() - No SMSs were found for contact id (" + contactId + ")");
            }
        } catch (Throwable t) {
            Log.e(getClass().getSimpleName(), "getHiddenSmsElementsByContactId() - Error occured on getting hidden SMS entries.");
        } finally {
            //close the cursor
            if (smsCur != null)
                smsCur.close();
        }

        //return the results
        return results;
    }

    /**
     * Inserts one passed SMS element into the device's SMSs database.
     *
     * @param sms (SmsElement != null)The SMS element to insert to device's SMSs database.
     * @return (boolean) true in case the element was inserted successfully, false otherwise.
     */
    public boolean insertToDeviceSMSsDB(SmsElement sms) {
        Log.d(TAG, "insertToDeviceSMSsDB");
        Vector<SmsElement> smsElements = new Vector<SmsElement>();
        smsElements.add(sms);

        return insertToDeviceSMSsDB(smsElements);
    }

    /**
     * Inserts the passed SMS elements into device's SMSs database.
     *
     * @param hiddenSmsElements (Vector<SmsElement> != null) The SMS elements to insert into device's SMSs database.
     * @return (boolean) true in case insertion was successful, false otherwise.
     */
    private boolean insertToDeviceSMSsDB(Vector<SmsElement> hiddenSmsElements) {
        Log.d(TAG, "insertToDeviceSMSsDB");
        //get sms elements collection size
        int size = hiddenSmsElements.size();

        //holds the values array to put in hidden sms table
        ContentValues[] valuesArray = new ContentValues[size];

        //insert elements into device's sms database
        for (int i = 0; i < size; ++i) {
            //get the current sms element
            SmsElement sms = (SmsElement) hiddenSmsElements.elementAt(i);

            //make sure the current message has a thread
            long threadId = getOrCreateThreadId(sms.PhoneNumber);

            //holds the values to put in device's sms table
            ContentValues values = new ContentValues();

            //set columns values
//			values.put(SmsTable._ID, sms.MessageId);
            values.put(SmsTable.THREAD_ID, threadId);
            values.put(SmsTable.ADDRESS, sms.PhoneNumber);
//			values.put(SmsTable.PERSON_ID, sms.PersonID == 0 ? "" : String.valueOf(sms.PersonID));
            values.put(SmsTable.DATE, sms.Date);
//			values.put(SmsTable.PROTOCOL, sms.Protocol == 0 ? "" : String.valueOf(sms.Protocol));
            values.put(SmsTable.READ, sms.Read);
            values.put(SmsTable.STATUS, -1);
            values.put(SmsTable.TYPE, sms.Type);
//			values.put(SmsTable.REPLY_PATH_PRESENT, sms.ReplyPathPresent == 0 ? "" : String.valueOf(sms.ReplyPathPresent));
            values.put(SmsTable.SUBJECT, sms.Subject);
            values.put(SmsTable.BODY, sms.Body);
            values.put(SmsTable.SERVICE_CENTER, sms.ServiceCenter);
            values.put(SmsTable.LOCKED, sms.Locked);

            //add the values to values array
            valuesArray[i] = values;
        }

        //insert the values into the db
        if (m_contentResolver.bulkInsert(SmsTable.INBOX_CONTENT_URI, valuesArray) <= 0) {
            //log error
            Log.e(getClass().getSimpleName(), "insertToDeviceSMSsDB() - Failed to insert hidden SMSs into device's sms database.");

            //reflect failure in case we fail to insert
            return false;
        }

        //reflect success
        return true;
    }

    /**
     * Deletes the passed sms elements from hidden SMS database.
     *
     * @param hiddenSmsElements (Vector<SmsElement>) SMS elements to delete from the hidden SMS database.
     */
    private boolean deleteSMSsFromHiddenDb(Vector<SmsElement> hiddenSmsElements) {
        Log.d(TAG, "deleteSMSsFromHiddenDb");
        //holds messages ids
        Vector<String> msgIDs = new Vector<String>();

        //collect message IDs
        for (SmsElement sms : hiddenSmsElements)
            msgIDs.add(String.valueOf(sms.MessageId));

        //set value for where clause
        String where = SQLQueryHelper.createINSelection(SmsTable._ID, msgIDs.toArray(new String[msgIDs.size()]));

        //delete coresponding sms entry
        if (m_db.delete(SMS_TABLE_NAME, where, null) <= 0) {
            //log error
            Log.e(getClass().getSimpleName(), "deleteSMSsFromHiddenDb() - Failed to delete SMSs from hidden DB.");

            //indicate failure in case we fail to delete
            return false;
        }

        //get conversation id
        int convId = ((SmsElement) hiddenSmsElements.elementAt(0)).ThreadId;

        //set where string for delete conversation query
        where = new StringBuffer(ConversationsTable.THREAD_ID).append("=")
                .append(convId).toString();

        //delete the conversation from hidden db
        if (m_db.delete(CONVERSATIONS_TABLE_NAME, where, null) <= 0) {
            //log error
            Log.e(getClass().getSimpleName(),
                    "deleteSMSsFromHiddenDb() - Failed to delete conversation from hidden DB. convId=" + convId);

            //indicate failure in case we fail to delete
            return false;
        }

        //indicate success
        return true;
    }

    /**
     * Returns the projection on a SMS table.
     */
    private String[] getSmsTableQueryProjection() {
        Log.d(TAG, "getSmsTableQueryProjection");
        //holds the table columns which need to retrieve by query
        return new String[]{
                SmsTable._ID,
                SmsTable.THREAD_ID,
                SmsTable.ADDRESS,
//				SmsTable.PERSON_ID,
                SmsTable.DATE,
//				SmsTable.PROTOCOL,
                SmsTable.READ,
                SmsTable.STATUS,
                SmsTable.TYPE,
//				SmsTable.REPLY_PATH_PRESENT,
                SmsTable.SUBJECT,
                SmsTable.BODY,
                SmsTable.SERVICE_CENTER,
                SmsTable.LOCKED
        };
    }

    /**
     * Returns an SmsElement object from a given cursor.
     *
     * @param smsTableCurr (Cursor != null) a cursor directed to a valid record.
     * @return (SmsElement != null) An SmsElement representing the data in the current record.
     */
    private SmsElement getSmsElementFromCursor(Cursor smsTableCurr) {
        Log.d(TAG, "getSmsElementFromCursor");
        //creates the sms element
        SmsElement smsElement = new SmsElement(smsTableCurr.getInt(smsTableCurr.getColumnIndex(SmsTable._ID)),
                smsTableCurr.getInt(smsTableCurr.getColumnIndex(SmsTable.THREAD_ID)),
                smsTableCurr.getString(smsTableCurr.getColumnIndex(SmsTable.ADDRESS)),
                smsTableCurr.getLong(smsTableCurr.getColumnIndex(SmsTable.DATE)),
                smsTableCurr.getInt(smsTableCurr.getColumnIndex(SmsTable.READ)),
                smsTableCurr.getInt(smsTableCurr.getColumnIndex(SmsTable.STATUS)),
                smsTableCurr.getInt(smsTableCurr.getColumnIndex(SmsTable.TYPE)),
                smsTableCurr.getString(smsTableCurr.getColumnIndex(SmsTable.SUBJECT)),
                smsTableCurr.getString(smsTableCurr.getColumnIndex(SmsTable.BODY)),
                smsTableCurr.getString(smsTableCurr.getColumnIndex(SmsTable.SERVICE_CENTER)),
                smsTableCurr.getInt(smsTableCurr.getColumnIndex(SmsTable.LOCKED)));

        //returns the sms element
        return smsElement;
    }

    /**
     * Returns an Conversation object from a given cursor.
     *
     * @param convMessagesCursor (Cursor != null) a cursor directed to a valid record.
     * @return (Conversation != null) Conversation object that incapsulates the given messages thread.
     */
    private Conversation getConversationFromCursor(Cursor convMessagesCursor) {
        Log.d(TAG, "getConversationFromCursor");
        String convId = null;
        long date = 0;
        int messageCount = 0;
        String phoneNumber = null;
        String snippet = null;
        int readState = Conversation.READ_STATE.READ;

        //holds conversation recipients ids
        Vector<String> recipientIds = new Vector<String>();

        //holds conversation's recipients names
        Vector<String> recipientNames = new Vector<String>();

        //holds conversation's phone numbers
        Vector<String> phones = new Vector<String>();

        //get conversation id
        convId = convMessagesCursor.getString(convMessagesCursor.getColumnIndex(SmsTable.THREAD_ID));

        //get last message snippet
        snippet = convMessagesCursor.getString(convMessagesCursor.getColumnIndex(SmsTable.BODY));

        //get last message date
        date = convMessagesCursor.getLong(convMessagesCursor.getColumnIndex(SmsTable.DATE));

        //collect other conversation data
        do {
            //increase messages counter
            messageCount++;

            //check for unread messages
            int currMsgReadState = convMessagesCursor.getInt(convMessagesCursor.getColumnIndex(SmsTable.READ));
            if (readState == Conversation.READ_STATE.READ && currMsgReadState == Conversation.READ_STATE.UNREAD)
                readState = currMsgReadState;

            //get phone number
            phoneNumber = convMessagesCursor.getString(convMessagesCursor.getColumnIndex(SmsTable.ADDRESS));

            //clear non-number chars
            String cleanPhoneNumber = DeviceContactsDataRepository.getInstance().cleanPhoneNumber(phoneNumber);

            //in case we already have this phone number, move to the next
            if (!phones.contains(cleanPhoneNumber)) {
                //add the number to phones collection
                phones.add(cleanPhoneNumber);

                //get contact's id
                String contactId = ContactsRepository.getInstance().getContactIdByPhoneNumber(phoneNumber);

                //add recipient's contact id
                if (!StringUtil.isNullOrEmpty(contactId)) {
                    recipientIds.add(contactId);

                    //get recipient's name
                    recipientNames.add(ContactsRepository.getInstance().getDisplayNameByPhoneNumber(phoneNumber));
                } else {
                    recipientNames.add(phoneNumber);
                }
            }
        }
        while (convMessagesCursor.moveToNext());

        //create Conversation object
        Conversation conversation = new Conversation(convId, date, messageCount, phoneNumber,
                recipientIds, recipientNames, snippet, readState);

        //returns the sms thread
        return conversation;
    }

//	/**
//	 * Returns the projection on a Threads table.
//	 */
//	private String[] getConversationsTableQueryProjection()
//	{
//		//holds the table columns which need to retrieve by query
//		return new String[]{
//				ConversationsTable.THREAD_ID,
//				ConversationsTable.MESSAGE_COUNT,
//				ConversationsTable.SNIPPET,
//				};
//	}

    /**
     * Returns a lazy Conversation object from a given cursor.
     *
     * @param convMessagesCursor (Cursor != null) a cursor directed to a valid record.
     * @return (Conversation != null) Lazy Conversation object that incapsulates the given messages thread.
     */
    private Conversation getLazyConversationFromCursor(Cursor convMessagesCursor) {
        Log.d(TAG, "getLazyConversationFromCursor");
        String convId = null;
        long date = 0;
        String phoneNumber = null;

        //get conversation id
        convId = convMessagesCursor.getString(convMessagesCursor.getColumnIndex(SmsTable.THREAD_ID));

        //get last message date
        date = convMessagesCursor.getLong(convMessagesCursor.getColumnIndex(SmsTable.DATE));

        //get phone number
        phoneNumber = convMessagesCursor.getString(convMessagesCursor.getColumnIndex(SmsTable.ADDRESS));

        //holds message's read state
        int readState = -1;

        do {
            readState = convMessagesCursor.getInt(convMessagesCursor.getColumnIndex(SmsTable.READ));

            //check for unread messages
            if (readState == Conversation.READ_STATE.UNREAD)
                break;
        }
        while (convMessagesCursor.moveToNext());

        //create Conversation object
        Conversation conversation = new Conversation(convId, date, phoneNumber, readState);

        //returns the sms thread
        return conversation;
    }

    /**
     * This method returns a thread id for the given phone number.
     * In case there is no thread for the given phone number, it is being created and a new unique thread id is returned.
     *
     * @param phoneNumber (String != null) Phone number to get its thread id.
     * @return (long) thread id of messages related to the given phone number. In case of failure, -1 is returned.
     */
    public long getOrCreateThreadId(String phoneNumber) {
        Log.d(TAG, "getOrCreateThreadId");
        //add phone number to collection
        Set<String> numbers = new HashSet<String>();
        numbers.add(phoneNumber);

        //use method that gets numbers collection
        return getOrCreateThreadId(numbers);
    }

    /**
     * This method returns a thread id for the given phone numbers collection.
     * In case there is no thread for the given phone numbers, it is being created and a new unique thread id is returned.
     *
     * @param phoneNumbers (Set<String> != null) Collection of phone numbers to get their thread id.
     * @return (long) thread id of messages related to the given phone numbers. In case of failure, -1 is returned.
     */
    @SuppressWarnings("rawtypes")
    private long getOrCreateThreadId(Set<String> phoneNumbers) {
        Log.d(TAG, "getOrCreateThreadId");
        //holds thread id
        long threadId = -1;

        try {
            Class clazz = Class.forName("android.provider.Telephony");
            Class[] innerClasses = clazz.getDeclaredClasses();
            for (Class inner : innerClasses) {
                if (inner.getSimpleName().equals("Threads")) {
                    Method met = inner.getMethod("getOrCreateThreadId", Context.class, Set.class);
                    met.setAccessible(true);
                    Context context = BouncerApplication.getApplication().getActivityInForeground().getBaseContext();
                    threadId = ((Long) met.invoke(null, context, phoneNumbers)).longValue();

                    break;
                }
            }
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "getOrCreateThreadId() - Failed to get/create thread for address: " +
                    phoneNumbers.toArray().toString(), e);
        }

        return threadId;
    }

    public Vector<SmsElement> getDeviceSmsListByConvId(String convId) {
        Log.d(TAG, "getDeviceSmsListByConvId");
        Cursor c = m_contentResolver.query(Uri.withAppendedPath(ConversationsTable.CONTENT_URI, convId),
                getSmsTableQueryProjection(), null, null, SmsTable.DATE + " ASC");

        return getSmsListFromCursor(c);
    }

    private Vector<SmsElement> getSmsListFromCursor(Cursor c) {
        Log.d(TAG, "getSmsListFromCursor");
        Vector<SmsElement> smsList = new Vector<SmsElement>();

        try {
            if (c != null && c.moveToFirst()) {
                do {
                    // add sms to the elements vector
                    smsList.add(getSmsElementFromCursor(c));
                }
                while (c.moveToNext());
            }
        } finally {
            // protected from null pointer exception
            if (c != null)
                c.close();
        }

        return smsList;
    }

    public Vector<SmsElement> getHiddenSmsListByConvId(String convId) {
        Log.d(TAG, "getHiddenSmsListByConvId");
        Cursor c = m_db.query(SMS_TABLE_NAME, getSmsTableQueryProjection(), SmsTable.THREAD_ID + " = " + convId,
                null, null, null, SmsTable.DATE + " ASC");

        return getSmsListFromCursor(c);
    }

    // generate sms id for new sms
    public int generateNewSmsId(boolean isHidden) {
        Log.d(TAG, "generateNewSmsId");
        //holds the id
        int id = 0;

        //holds the cursor
        Cursor smsCur = null;

        try {
            //get all SMSs id from db from highest to lowest
            if (isHidden)
                smsCur = m_db.query(SMS_TABLE_NAME, new String[]{SmsTable._ID}, null, null, null, null, SmsTable._ID + " DESC");
            else
                smsCur = m_contentResolver.query(SmsTable.CONTENT_URI, new String[]{SmsTable._ID}, null, null, SmsTable._ID + " DESC");

            if (smsCur != null && smsCur.moveToFirst()) {
                //get the first id (the highest)
                id = smsCur.getInt(smsCur.getColumnIndex(SmsTable._ID));
            }
        } catch (Throwable t) {
            Log.e(getClass().getSimpleName(), "generateNewSmsId() - Error on creating new sms id! ");
        } finally {
            if (smsCur != null)
                smsCur.close();
        }

        //return the next available id
        return ++id;
    }

    /**
     * Generates a new conversation id.
     *
     * @param isHidden (boolean) Sets whether to generate new id for device's database or for application's database.
     * @return (String) New generated conversation id.
     */
    public String generateNewConversationId(boolean isHidden) {
        Log.d(TAG, "generateNewConversationId");
        int id = 0;

        //holds the cursor
        Cursor cursor = null;

        try {
            if (isHidden)
                cursor = m_db.query(CONVERSATIONS_TABLE_NAME, new String[]{ConversationsTable.THREAD_ID},
                        null, null, null, null, ConversationsTable.THREAD_ID + " DESC");
            else
                cursor = m_contentResolver.query(ConversationsTable.CONTENT_URI, new String[]{ConversationsTable.THREAD_ID},
                        null, null, ConversationsTable.THREAD_ID + " DESC");

            //in case we have data
            if (cursor != null && cursor.moveToFirst()) {
                //get the first id (the highest)
                id = cursor.getInt(cursor.getColumnIndex(ConversationsTable.THREAD_ID));
            }
        } catch (Throwable t) {

        } finally {
            if (cursor != null)
                cursor.close();
        }

        //return new id
        return String.valueOf(++id);
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
        //holds conversation id
        String convId = null;

        Cursor cursor = null;

        try {
            //set query params
            String[] columns = new String[]{SmsTable.THREAD_ID, SmsTable.ADDRESS};

            //save the phone numbers in a string array
            Vector<String> phonesAsString = new Vector<String>();
            phonesAsString.add(phoneNumber);
            phonesAsString.add(DeviceContactsDataRepository.getInstance().cleanPhoneNumber(phoneNumber));

            String where = SQLQueryHelper.createINSelection(SmsTable.ADDRESS,
                    phonesAsString.toArray(new String[phonesAsString.size()]));
//			String sortBy = SmsTable.DATE + " ASC";

            //get cursor
            cursor = m_db.query(SMS_TABLE_NAME, columns, where, null, null, null, null);

            //in case we have data
            if (cursor != null && cursor.moveToFirst()) {
                convId = cursor.getString(cursor.getColumnIndex(SmsTable.THREAD_ID));
            }
            //in case the cursor is empty
            else {
                //---create new conversation---
                //generate new conversation id
                convId = generateNewConversationId(true);

                //insert new empty conversation into hidden conversations database
                insertHiddenConversation(convId, 0, "");
            }
        } catch (Throwable t) {
            Log.e(getClass().getSimpleName(), "getOrCreateHiddenConvId() - failed to get cursor. ");
        } finally {
            if (cursor != null)
                cursor.close();
        }

        return convId;
    }

    /**
     * Change SMS's status from unread to read.
     *
     * @param convId (String != null) The conversation id of conversation that need to change.
     * @return (boolean) true for successful update, else return false.
     */
    public boolean setConversationAsRead(String convId) {
        Log.d(TAG, "setConversationAsRead");
        // set uri and values for device's SMSs update
        Uri uri = Uri.withAppendedPath(ConversationsTable.CONTENT_URI, convId);
        ContentValues values = new ContentValues();
        values.put(SmsTable.READ, SmsElement.READ_STATE.READ);

        String where = SmsTable.READ + "=" + SmsElement.READ_STATE.UNREAD;

        // if the update failed, i.e the sms not found in device try update in hidden SMSs
        if (m_contentResolver.update(uri, values, where, null) <= 0) {
            // set values for hidden SMSs update
            StringBuffer whereSB = new StringBuffer(SmsTable.THREAD_ID).append("=").append(convId)
                    .append(" AND ").append(SmsTable.READ).append("=").append(SmsElement.READ_STATE.UNREAD);

            // update hidden db
            if (m_db.update(SMS_TABLE_NAME, values, whereSB.toString(), null) <= 0) {
                Log.d(getClass().getSimpleName(), "setConversationAsRead() - No unread messages in conversation with id: " + convId);

                return false;
            }
        }

        return true;
    }

    /**
     * Retreives SmsElements from device's database by given phone number.
     *
     * @param phoneNumber (String != null) The phone number to search by.
     * @return (Vector<SmsElement>) A collection of Sms Elements related to the given number. Might be empty.
     */
    public Vector<SmsElement> getDeviceSmsElementsByPhoneNumber(String phoneNumber) {
        Log.d(TAG, "getDeviceSmsElementsByPhoneNumber");
        //holds the cursor to the sms table
        Cursor smsCur = null;

        //holds the results
        Vector<SmsElement> results = new Vector<SmsElement>();

        try {
            //get all SMSs
            smsCur = m_contentResolver.query(SmsTable.CONTENT_URI, getSmsTableQueryProjection(), null, null, SmsTable.DATE + " ASC");

            //in case there is data
            if (smsCur != null && smsCur.moveToFirst()) {
                do {
                    //get current phone
                    String phone = smsCur.getString(smsCur.getColumnIndex(SmsTable.ADDRESS));

                    //clean both numbers and check for equality
                    if (DeviceContactsDataRepository.getInstance().cleanPhoneNumber(phone)
                            .equals(DeviceContactsDataRepository.getInstance().cleanPhoneNumber(phoneNumber))) {
                        //add the sms element to results vector
                        results.add(getSmsElementFromCursor(smsCur));
                    }
                }
                while (smsCur.moveToNext());
            } else {
                Log.d(getClass().getSimpleName(), "getDeviceSmsElementsByPhoneNumber() - No SMSs were found for phone number: " + phoneNumber);
            }
        } catch (Throwable t) {
            Log.e(getClass().getSimpleName(), "getDeviceSmsElementsByPhoneNumber() - Error occured on getting device's SMS entries.");
        } finally {
            //close the cursor
            if (smsCur != null)
                smsCur.close();
        }

        //return the results
        return results;
    }

    /**
     * Retreives SmsElements from hidden database by given phone number.
     *
     * @param phoneNumber (String != null) The phone number to search by.
     * @return (Vector<SmsElement>) A collection of Sms Elements related to the given number. Might be empty.
     */
    public Vector<SmsElement> getHiddenSmsElementsByPhoneNumber(String phoneNumber) {
        Log.d(TAG, "getHiddenSmsElementsByPhoneNumber");
        //holds the cursor to the sms table
        Cursor smsCur = null;

        //holds the results
        Vector<SmsElement> results = new Vector<SmsElement>();

        try {
            //get all SMSs
            smsCur = m_db.query(SMS_TABLE_NAME, getSmsTableQueryProjection(), null, null, null, null, SmsTable.DATE + " ASC");

            //in case there is data
            if (smsCur != null && smsCur.moveToFirst()) {
                do {
                    //get current phone
                    String phone = smsCur.getString(smsCur.getColumnIndex(SmsTable.ADDRESS));

                    //clean both numbers and check for equality
                    if (DeviceContactsDataRepository.getInstance().cleanPhoneNumber(phone)
                            .equals(DeviceContactsDataRepository.getInstance().cleanPhoneNumber(phoneNumber))) {
                        //add the sms element to results vector
                        results.add(getSmsElementFromCursor(smsCur));
                    }
                }
                while (smsCur.moveToNext());
            } else {
                Log.d(getClass().getSimpleName(), "getHiddenSmsElementsByPhoneNumber() - No SMSs were found for phone number: " + phoneNumber);
            }
        } catch (Throwable t) {
            Log.e(getClass().getSimpleName(), "getHiddenSmsElementsByPhoneNumber() - Error occured on getting device's SMS entries.");
        } finally {
            //close the cursor
            if (smsCur != null)
                smsCur.close();
        }

        //return the results
        return results;
    }

    /**
     * Holds the column names of device's messages table.
     */
    private static final class SmsTable {
        //hold's device's database event record column names
        private static final Uri CONTENT_URI = Uri.parse("content://sms");
        private static final Uri INBOX_CONTENT_URI = Uri.parse("content://sms/inbox");
        private static final String _ID = "_id";
        private static final String THREAD_ID = "thread_id";
        private static final String ADDRESS = "address";
        private static final String PERSON_ID = "person";
        private static final String DATE = "date";
        private static final String PROTOCOL = "protocol";
        private static final String READ = "read";
        private static final String STATUS = "status";
        private static final String TYPE = "type";
        private static final String REPLY_PATH_PRESENT = "reply_path_present";
        private static final String SUBJECT = "subject";
        private static final String BODY = "body";
        private static final String SERVICE_CENTER = "service_center";
        private static final String LOCKED = "locked";
    }

    /**
     * Holds the column names of device's sms threads table.
     */
    private static final class ConversationsTable {
        //hold's device's database event record column names
        private static final Uri CONTENT_URI = Uri.parse("content://sms/conversations");
        private static final String THREAD_ID = "thread_id";
        private static final String MESSAGE_COUNT = "msg_count";
        private static final String SNIPPET = "snippet";
    }
}
