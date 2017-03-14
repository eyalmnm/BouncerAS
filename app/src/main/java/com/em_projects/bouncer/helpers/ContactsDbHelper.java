/*
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.em_projects.bouncer.helpers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

import com.cellebrite.ota.socialphonebook.repositories.contacts.ContactDataKinds;
import com.cellebrite.ota.socialphonebook.repositories.contacts.ContactGroup;
import com.cellebrite.ota.socialphonebook.repositories.contacts.DataArgs;
import com.cellebrite.ota.socialphonebook.repositories.contacts.DeviceContact;
import com.cellebrite.ota.socialphonebook.repositories.contacts.VCardParser;
import com.cellebrite.ota.socialphonebook.repositories.contacts.VCardParser.VCardParserException;
import com.cellebrite.ota.socialphonebook.repositories.handsetdata.DeviceContactsDataRepository;
import com.em_projects.bouncer.BouncerApplication;
import com.em_projects.bouncer.model.Contact;
import com.em_projects.bouncer.model.Phone;
import com.em_projects.infra.application.BasicApplication;
import com.em_projects.infra.helpers.DatabaseHelper;
import com.em_projects.utils.StringUtil;

import java.util.ArrayList;
import java.util.UUID;
import java.util.Vector;

public class ContactsDbHelper extends DatabaseHelper {

    public static final String KEY_CONTACT_ID = "contact_id";
    public static final String KEY_VCARD = "vcard";
    private static final String TAG = "ContactsDbHelper";
    private static final String DATABASE_NAME = "hidden_contacts.db";
    private static final String CONTACTS_TABLE_NAME = "contacts_vcard";
    private static final int DATABASE_VERSION = 1;
    private static final String[] DATABASE_CREATE = new String[]{
            new StringBuffer()
                    .append("CREATE TABLE ").append(CONTACTS_TABLE_NAME)
                    .append(" ( ")
                    .append(KEY_CONTACT_ID).append(" TEXT PRIMARY KEY, ")
                    .append(KEY_VCARD).append(" TEXT NOT NULL")
                    .append(")")
                    .toString()
    };
    private static ContactsDbHelper s_instance;

    private ContactsDbHelper() {
        super(BasicApplication.getApplication(),
                DATABASE_NAME, DATABASE_VERSION, DATABASE_CREATE);
        Log.d(TAG, "ContactsDbHelper");
    }

    public static synchronized ContactsDbHelper getInstance() {
        Log.d(TAG, "getInstance");
        if (s_instance == null)
            s_instance = new ContactsDbHelper();

        return s_instance;
    }

    public String saveHiddenContact(String vcard) {
        Log.d(TAG, "saveHiddenContact");
        //generate random contact id
        String contactId = UUID.randomUUID().toString();

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CONTACT_ID, contactId);
        initialValues.put(KEY_VCARD, vcard);

        if (m_db.insert(CONTACTS_TABLE_NAME, null, initialValues) != -1)
            return contactId;
        else
            return null;
    }

    public boolean deleteHiddenContact(String contactId) {
        Log.d(TAG, "deleteHiddenContact");
        return m_db.delete(CONTACTS_TABLE_NAME, KEY_CONTACT_ID + "='" + contactId + "'", null) > 0;
    }

    public Cursor getAllHiddenContacts() {
        Log.d(TAG, "getAllHiddenContacts");
        Cursor cursor = m_db.query(CONTACTS_TABLE_NAME, new String[]{KEY_CONTACT_ID,
                KEY_VCARD}, null, null, null, null, null);

        if (cursor != null) {
            if (cursor.getCount() == 0) {
                cursor.close();

                return null;
            }

            cursor.moveToFirst();
        }

        return cursor;
    }

    public Cursor getHiddenContactById(String contactId) throws SQLException {
        Log.d(TAG, "getHiddenContactById");
        Cursor cursor = m_db.query(true, CONTACTS_TABLE_NAME, new String[]{KEY_CONTACT_ID,
                        KEY_VCARD}, KEY_CONTACT_ID + "='" + contactId + "'", null,
                null, null, null, null);

        if (cursor != null) {
            if (cursor.getCount() == 0) {
                cursor.close();

                return null;
            }

            cursor.moveToFirst();
        }

        return cursor;

    }

    public boolean updateHiddenContact(String contactId, String vcard) {
        Log.d(TAG, "updateHiddenContact");
        ContentValues args = new ContentValues();
        args.put(KEY_CONTACT_ID, contactId);
        args.put(KEY_VCARD, vcard);

        return m_db.update(CONTACTS_TABLE_NAME, args, KEY_CONTACT_ID + "=" + contactId, null) > 0;
    }

    public ArrayList<Phone> getPhoneNumbersFromVcard(String vCard) {
        Log.d(TAG, "getPhoneNumbersFromVcard");
        if (vCard == null) {
            Log.d(getClass().getSimpleName(), "getPhoneNumbersFromVcard() - vCard is null!!! ");

            return null;
        }

        //holds contact group that will hold vcard's data
        //TODO - use groups not group for OSes with groups
        ContactGroup cg = null;

        try {
            //get contact group details from vcard
            cg = VCardParser.getContactGroupFromVCard(vCard);
        } catch (VCardParserException vce) {
            Log.e(getClass().getSimpleName(), "getPhoneNumbersFromVcard() - failed getting contact group from VCard.", vce);

            return null;
        }

        //get phone numbers data from the contact group
        Vector<DataArgs> data = cg.getData(ContactDataKinds.Phone.KIND);

        //in case we have no data of the 'Phone' kind - the contact has no phones.
        if (data == null) {
            Log.d(getClass().getSimpleName(), "getPhoneNumbersFromVcard() - contact has no phone numbers.");

            return null;
        }

        //holds the phones array
        ArrayList<Phone> phones = new ArrayList<Phone>();

        //collect the phones from vcard data
        for (DataArgs args : data) {
            String phoneNumber = args.getValue(ContactDataKinds.Phone.NUMBER);
            int phoneType = args.getValue(ContactDataKinds.Phone.TYPE);

            phones.add(new Phone(phoneNumber, phoneType));
        }

        //return phones list
        return phones;
    }

    public ArrayList<Phone> getHiddenPhoneNumbersById(String uid) {
        Log.d(TAG, "getHiddenPhoneNumbersById");
        //holds the vcard
        String vCard = null;

        //get cursor to hidden contacts table
        Cursor hiddenContacts = ContactsDbHelper.getInstance().getHiddenContactById(uid);

        //if table exists, try to get contact's vcard
        if (hiddenContacts != null) {
            try {
                vCard = hiddenContacts.getString(hiddenContacts.getColumnIndex(ContactsDbHelper.KEY_VCARD));
            } finally {
                hiddenContacts.close();
            }
        }

        //in case we failed to get a vcard
        if (vCard == null) {
            Log.d(getClass().getSimpleName(), "getHiddenPhoneNumbersById() - failed getting VCard on uid:" + uid);

            return null;
        }

        return getPhoneNumbersFromVcard(vCard);
    }

    /**
     * Gets contact's display name by phone number.
     * In case there are more then one contact with the given number,
     * the contact with the highest contact id value will be used.
     *
     * @param phoneNumber (String != null) Contact's phone number to find it's display name.
     * @return (String) Contact's display name. In case display name not found, phone number is returned.
     */
    public String getDisplayNameByPhoneNumber(String phoneNumber) {
        Log.d(TAG, "getDisplayNameByPhoneNumber");
        //get content resolver
        ContentResolver cr = BouncerApplication.getApplication().getContentResolver();

        //holds cursor for display names
        Cursor displayNameCursor = null;

        //holds contact's display name
        String displayName = phoneNumber;

        try {
            //get cursor with display names that have the given phone number
            displayNameCursor = cr.query(
                    Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber)),
                    new String[]{PhoneLookup.DISPLAY_NAME},
                    null,
                    null,
                    null);

            //in case we have data
            if (displayNameCursor != null && displayNameCursor.moveToLast()) {
                //get contact's display name
                displayName = displayNameCursor.getString(displayNameCursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
            }
            //try getting display name from hidden db
            else {
                //get cursor to all hidden contacts
                displayNameCursor = m_db.query(CONTACTS_TABLE_NAME, null, null, null, null, null, null);

                //in case we have data
                if (displayNameCursor != null && displayNameCursor.moveToFirst()) {
                    do {
                        //get the display name from contact's vcard
                        String vcard = displayNameCursor.getString(displayNameCursor.getColumnIndex(KEY_VCARD));
                        ArrayList<Phone> phones = getPhoneNumbersFromVcard(vcard);
                        for (Phone p : phones) {
//							if (Utils.cleanPhoneNumber(p.getNumber()).equals(Utils.cleanPhoneNumber(phoneNumber)))
                            if (p.getNumber().equals(phoneNumber) ||
                                    DeviceContactsDataRepository.getInstance().cleanPhoneNumber(p.getNumber()).equals(DeviceContactsDataRepository.getInstance().cleanPhoneNumber(phoneNumber))) {
                                String dispName = VCardParser.getContactFromVCard(vcard).getDisplayName();
                                if (!StringUtil.isNullOrEmpty(dispName))
                                    displayName = dispName;
                            }
                        }
                    }
                    while (displayNameCursor.moveToNext());
                }
            }
        } catch (Throwable t) {
            Log.e(getClass().getSimpleName(), "getDisplayNameByPhoneNumberFromDevice() - Failed to get " +
                    "display name for phone number: " + phoneNumber, t);
        } finally {
            if (displayNameCursor != null)
                displayNameCursor.close();
        }

        return displayName;
    }

    /**
     * Gets contact's id by phone number.
     * In case there are more then one contact with the given number,
     * the contact with the highest contact id value will be used.
     *
     * @param phoneNumber (String != null) Contact's phone number to find it's id.
     * @return (String) Contact's id. May be null.
     */
    public String getContactIdByPhoneNumber(String phoneNumber) {
        Log.d(TAG, "getContactIdByPhoneNumber");
        //get content resolver
        ContentResolver cr = BouncerApplication.getApplication().getContentResolver();

        //holds cursor for ids
        Cursor contactIdCursor = null;

        //holds contact's id
        String contactId = null;

        try {
            //get cursor with ids that have the given phone number
            contactIdCursor = cr.query(
                    Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber)),
                    new String[]{PhoneLookup._ID},
                    null,
                    null,
                    null);

            //in case we have data
            if (contactIdCursor != null && contactIdCursor.moveToLast()) {
                //get contact's id
                contactId = contactIdCursor.getString(contactIdCursor.getColumnIndex(PhoneLookup._ID));
            }
            //try getting id from hidden db
            else {
                //get cursor to all hidden contacts
                contactIdCursor = getAllHiddenContacts();

                //in case we have data
                if (contactIdCursor != null && contactIdCursor.moveToFirst()) {
                    do {
                        //get hidden contact's id
                        String hiddenId = contactIdCursor.getString(contactIdCursor.getColumnIndex(KEY_CONTACT_ID));
                        ArrayList<Phone> phones = getHiddenPhoneNumbersById(hiddenId);

                        for (Phone p : phones) {
//							if (Utils.cleanPhoneNumber(p.getNumber()).equals(Utils.cleanPhoneNumber(phoneNumber)))
                            if (p.getNumber().equals(phoneNumber) ||
                                    DeviceContactsDataRepository.getInstance().cleanPhoneNumber(p.getNumber()).equals(DeviceContactsDataRepository.getInstance().cleanPhoneNumber(phoneNumber))) {
                                contactId = hiddenId;

                                break;
                            }
                        }
                    }
                    while (contactIdCursor.moveToNext());
                }
            }
        } catch (Throwable t) {
            Log.e(getClass().getSimpleName(), "getContactIdByPhoneNumberFromDevice() - Failed to get " +
                    "display name for phone number: " + phoneNumber, t);
        } finally {
            if (contactIdCursor != null)
                contactIdCursor.close();
        }

        return contactId;
    }

    public Vector<Contact> getDeviceLazyContacts() {
        Log.d(TAG, "getDeviceLazyContacts");
        //get content resolver
        ContentResolver cr = BouncerApplication.getApplication().getContentResolver();

        //holds the cursor for contact
        Cursor cursor = null;

        //holds the contacts collection
        Vector<Contact> lazyContacts = new Vector<Contact>();

        try {
            //get cursor for the contact
            cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME},
                    ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1",
                    null,
                    "UPPER(" + ContactsContract.Contacts.DISPLAY_NAME + ")"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    //create contact from cursor
                    Contact contact = new Contact(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)));
                    contact.setDetails(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));

                    //add the contact to contacts collection
                    lazyContacts.add(contact);
                }
                while (cursor.moveToNext());
            }
        } catch (Throwable t) {
            Log.e(getClass().getSimpleName(), "getDeviceLazyContacts() - failed to get lazy contacts from device!", t);
        } finally {
            if (cursor != null)
                cursor.close();
        }

        return lazyContacts;
    }

    public Vector<Contact> getHiddenLazyContacts() {
        Log.d(TAG, "getHiddenLazyContacts");
        //holds the cursor for contact
        Cursor cursor = null;

        //holds the contacts collection
        Vector<Contact> lazyContacts = new Vector<Contact>();

        try {
            //get cursor for the contact
            cursor = m_db.query(CONTACTS_TABLE_NAME, new String[]{KEY_CONTACT_ID, KEY_VCARD},
                    null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    //get id and vcard from cursor
                    String id = cursor.getString(cursor.getColumnIndex(KEY_CONTACT_ID));
                    String vcard = cursor.getString(cursor.getColumnIndex(KEY_VCARD));

                    //get display name from vcard
                    DeviceContact dc = VCardParser.getContactFromVCard(vcard);
                    String displayName = dc.getDisplayName();

                    //create lazy contact
                    Contact contact = new Contact(id);
                    contact.setDetails(displayName);

                    //add the contact to contacts collection
                    lazyContacts.add(contact);
                }
                while (cursor.moveToNext());
            }
        } catch (Throwable t) {
            Log.e(getClass().getSimpleName(), "getDeviceLazyContacts() - failed to get lazy contacts from device!", t);
        } finally {
            if (cursor != null)
                cursor.close();
        }

        return lazyContacts;
    }
}