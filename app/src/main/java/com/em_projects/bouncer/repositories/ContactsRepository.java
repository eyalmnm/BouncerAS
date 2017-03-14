package com.em_projects.bouncer.repositories;

import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.cellebrite.ota.socialphonebook.repositories.contacts.ContactDataKinds;
import com.cellebrite.ota.socialphonebook.repositories.contacts.ContactGroup;
import com.cellebrite.ota.socialphonebook.repositories.contacts.DataArgs;
import com.cellebrite.ota.socialphonebook.repositories.contacts.DeviceContact;
import com.cellebrite.ota.socialphonebook.repositories.contacts.DeviceContactsRepository;
import com.cellebrite.ota.socialphonebook.repositories.contacts.DeviceContactsRepository.ContactRepositoryObserver;
import com.cellebrite.ota.socialphonebook.repositories.contacts.VCardParser;
import com.em_projects.bouncer.BouncerApplication;
import com.em_projects.bouncer.R;
import com.em_projects.bouncer.helpers.ContactsDbHelper;
import com.em_projects.bouncer.model.Contact;
import com.em_projects.bouncer.model.Phone;
import com.em_projects.bouncer.utils.Utils;
import com.em_projects.infra.application.BasicApplication;
import com.em_projects.infra.repositories.EntityRepositoryWithCache;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

public class ContactsRepository extends EntityRepositoryWithCache<Contact, String> {
    private static final String TAG = "ContactsRepository";

    //holds class instance
    private static ContactsRepository s_instance = null;

    /**
     * Ctor
     */
    private ContactsRepository() {
        Log.d(TAG, "ContactsRepository");
        //register contacts observer
        DeviceContactsRepository.getInstance().registerContactObserver(new ContactRepositoryObserver() {
            @Override
            public void onContactChange() {
                ContactsRepository.this.clearCache();
            }
        });

        //set default contact photo images
        DeviceContact.DEFAULT_LARGE_CONTACT_PHOTO = DeviceContact.DEFAULT_SMALL_CONTACT_PHOTO =
                BitmapFactory.decodeResource(BasicApplication.getApplication().getResources(), R.drawable.default_photo);
    }

    /**
     * Singleton method
     *
     * @return (ContactsRepository != null) Class single instance.
     */
    public static synchronized ContactsRepository getInstance() {
        Log.d(TAG, "getInstance");
        if (s_instance == null)
            s_instance = new ContactsRepository();

        return s_instance;
    }

    /**
     * @see DeviceContactsRepository#registerContactObserver(ContactRepositoryObserver);
     */
    public void setContactsObserver(ContactRepositoryObserver observer) {
        Log.d(TAG, "setContactsObserver");
        DeviceContactsRepository.getInstance().registerContactObserver(observer);
    }

    public Contact getByUID(String uid) {
        Log.d(TAG, "getByUID");
        Contact contact = getFromCache(uid);

        if (contact == null) {
            DeviceContact dc = DeviceContactsRepository.getInstance().getContactById(uid);
            if (dc != null)
                contact = convertDeviceContactToContact(dc, false);
            else {
                Cursor c = ContactsDbHelper.getInstance().getHiddenContactById(uid);
                if (c != null) {
                    String vcard = c.getString(c.getColumnIndex(ContactsDbHelper.KEY_VCARD));
                    try {
                        dc = VCardParser.getContactFromVCard(vcard);
                        dc.setID(uid);
                        byte[] photoData = VCardParser.getPhotoDataFromVCard(vcard);
                        if (photoData != null)
                            dc.setPhotoData(new ByteArrayInputStream(photoData));
                        contact = convertDeviceContactToContact(dc, true);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    } finally {
                        c.close();
                    }
                }
            }

            if (contact != null)
                cache(contact);
        }

        return contact;

    }

    public Vector<Contact> getLazyContacts() {
        Log.d(TAG, "getLazyContacts");
        //get lazy contacts from device
        Vector<Contact> lazyContacts = ContactsDbHelper.getInstance().getDeviceLazyContacts();

        //get hidden lazy contacts
        lazyContacts.addAll(ContactsDbHelper.getInstance().getHiddenLazyContacts());

        return lazyContacts;
    }

    /**
     * Get the contact by phone number.
     *
     * @param phoneNumber (Sting != null) The phone number of the contact.
     * @return (Contact) The contact with this phone number. May be null in case it is not contact.
     */
    public Contact getContactByPhoneNumber(String phoneNumber) {
        Log.d(TAG, "getContactByPhoneNumber");
        String contactId = ContactsRepository.getInstance().getContactIdByPhoneNumber(phoneNumber);
        if (contactId != null)
            return getByUID(contactId);
        else
            return null;
    }

    /**
     * Hides the contact. The contact's data is registered on application's hidden database
     * and the contact is deleted from device's database.
     *
     * @return (String != null) new contact's UID in the hidden database.
     */
    public String hide(String contactId) {
        Log.d(TAG, "hide");
        //hide contact's SMS messages.
        SmsRepository.getInstance().hideSMSs(contactId);

        //hide the contact and get its hidden id
        String hiddenContactId = ContactsRepository.getInstance().hideContact(contactId);

        if (hiddenContactId != null) {
            //hide contact's call-logs by new contact's id
            Vector<Phone> phoneNumbers = ContactsRepository.getInstance().getPhones(hiddenContactId);
            for (Phone phone : phoneNumbers) {
                if (!CallLogsRepository.getInstance().hideCallLogs(hiddenContactId, phone.getNumber()))
                    Log.e(getClass().getSimpleName(), "hide() - Filed to hide callogs!");
            }
        }

        return hiddenContactId;
    }

    public synchronized String hideContact(String uid) {
        Log.d(TAG, "hideContact");
        String vcard = DeviceContactsRepository.getInstance().getContactVCard(uid, false);

        String contactID = null;

        if (vcard != null) {
            contactID = ContactsDbHelper.getInstance().saveHiddenContact(vcard);

            if (contactID != null && DeviceContactsRepository.getInstance().deleteContact(uid))
                return contactID;
        }

        return null;
    }

    /**
     * Reveals the contact. The contact's data is restored from application's hidden database
     * to the device's database and then deleted from application's database.
     *
     * @return (String) new contact's id in device's database. May be null in case revealing is failed.
     */
    public String reveal(String contactId) {
        Log.d(TAG, "reveal");
        //reveal contact's SMS messages.
        if (!SmsRepository.getInstance().revealSMSs(contactId)) {
            //display error message
            Utils.showMessage(R.string.error, R.string.error_reveal_contact, BouncerApplication.getApplication().getActivityInForeground());

            //stop the "revealing" process
            return null;
        }

        //reveal call-logs
        HashSet<String> phoneNumbers = CallLogsRepository.getInstance().getHiddenPhoneNumbersBy(contactId);
        for (String phone : phoneNumbers) {
            CallLogsRepository.getInstance().revealCallLogsByPhoneNumber(phone);
        }

        //reveal contact
        return ContactsRepository.getInstance().revealContact(contactId);
    }

    public synchronized String revealContact(String uid) {
        Log.d(TAG, "revealContact");
        Cursor c = ContactsDbHelper.getInstance().getHiddenContactById(uid);
        if (c != null) {
            try {
                String vcard = c.getString(c.getColumnIndex(ContactsDbHelper.KEY_VCARD));

                String newContactId = DeviceContactsRepository.getInstance().saveContact(vcard);

                if (newContactId != null)
                    ContactsDbHelper.getInstance().deleteHiddenContact(uid);

                return newContactId;
            } finally {
                c.close();
            }
        } else
            return null;
    }

    public Contact convertDeviceContactToContact(DeviceContact dc, boolean isHidden) {
        Log.d(TAG, "convertDeviceContactToContact");
        //convert device contact to contact
        Contact c = new Contact(dc.getId());

        //set contact's display name
        String fullName = dc.getDisplayName();
        if (fullName.equals(""))
            c.setDetails(dc.getFirstName() + " " + dc.getLastName());
        else
            c.setDetails(fullName);

        //set contact's hidden state
        c.setHidden(isHidden);

        //set contact's photo data
        c.setPhotoData(dc.getPhotoData());

        return c;
    }

    /**
     * Returns a collection of contact's Phone objects that hold phone type and number.
     *
     * @param contactId (String != null) The id of the contact whose phones to retreive.
     * @return (Vector<Phone>) A collection of contact's Phone objects that hold phone type and number.
     */
    public Vector<Phone> getPhones(String contactId) {
        Log.d(TAG, "getPhones");
        //holds the contact's phones
        Vector<Phone> phones = new Vector<Phone>();

        //get device contact details by groups {facebook, gmail...}
        Vector<ContactGroup> groups = DeviceContactsRepository.getInstance().getContactDetailsById(contactId);

        //in case we have any data from device db
        if (groups != null && groups.size() > 0) {
            //for each group get it's phones data
            for (ContactGroup cg : groups) {
                Vector<DataArgs> phoneArgs = cg.getData(ContactDataKinds.Phone.KIND);
                for (DataArgs da : phoneArgs) {
                    //get the number and type
                    String phoneValue = da.getValue(ContactDataKinds.Phone.NUMBER);
                    int phoneType = da.getValue(ContactDataKinds.Phone.TYPE);

                    //create a phone object and add it
                    Phone phone = new Phone(phoneValue, phoneType);
                    phones.add(phone);
                }
            }
        }
        //in case we have no data on device's db
        else {
            //get contact's phones from application's db
            ArrayList<Phone> hiddenPhoneNumbers = ContactsDbHelper.getInstance().getHiddenPhoneNumbersById(contactId);

            //in case we have any data, add it to phones collection
            if (hiddenPhoneNumbers != null && hiddenPhoneNumbers.size() > 0)
                phones.addAll(hiddenPhoneNumbers);
        }

        return phones;
    }

    public String getDisplayNameByPhoneNumber(String phoneNumber) {
        Log.d(TAG, "getDisplayNameByPhoneNumber");
        return ContactsDbHelper.getInstance().getDisplayNameByPhoneNumber(phoneNumber);
    }

    public String getContactIdByPhoneNumber(String phoneNumber) {
        Log.d(TAG, "getContactIdByPhoneNumber");
        return ContactsDbHelper.getInstance().getContactIdByPhoneNumber(phoneNumber);
    }

    /**
     * Returns a collection of contacts that match the filter criteria.
     *
     * @param filterStr (String) The string by which to filter the contacts.
     * @return (Vector<Contact>) a collection of Contact objects that match the filter criteria.
     */
    public Vector<Contact> getFilteredContacts(String filterStr) {
        Log.d(TAG, "getFilteredContacts");
        //get all contacts
        List<Contact> contacts = getLazyContacts();

        //holds the filtered contacts
        Vector<Contact> filteredContacts = new Vector<Contact>();

        for (Contact contact : contacts) {
            //get contact's display name
            String displayName = contact.getDisplayName();

            //if contact's display name contains the filter string
            if (displayName.toLowerCase().contains(filterStr.trim().toLowerCase()))
                //add the contact to filtered contacts collection
                filteredContacts.add(contact);
        }

        return filteredContacts;
    }
}
