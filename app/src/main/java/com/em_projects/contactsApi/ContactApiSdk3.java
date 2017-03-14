package com.em_projects.contactsApi;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.provider.ContactsContract;
import android.util.Log;

import com.em_projects.infra.application.BasicApplication;

import java.util.ArrayList;

public class ContactApiSdk3 extends ContactAPI {
    private static final String TAG = "ContactApiSdk3";

    private ContentResolver cr = BasicApplication.getApplication().getContentResolver();
    private Cursor cur = cr.query(People.CONTENT_URI,
            null, null, null, null);

    public Cursor getCur() {
        Log.d(TAG, "getCur");
        return cur;
    }

    public void setCur(Cursor cur) {
        Log.d(TAG, "setCur");
        this.cur = cur;
    }

    public ContentResolver getCr() {
        Log.d(TAG, "getCr");
        return cr;
    }

    public void setCr(ContentResolver cr) {
        Log.d(TAG, "setCr");
        this.cr = cr;
    }

    public Intent getContactIntent() {
        Log.d(TAG, "getContactIntent");
        return (new Intent(Intent.ACTION_PICK, People.CONTENT_URI));
    }

    public ContactList newContactList() {
        Log.d(TAG, "newContactList");
        ContactList contacts = new ContactList();
        String id;

        this.cur = this.cr.query(People.CONTENT_URI, null, null, null, null);
        if (this.cur.getCount() > 0) {
            while (cur.moveToNext()) {
                Contact c = new Contact();
                id = cur.getString(cur.getColumnIndex(People._ID));
                c.setId(id);
                c.setDisplayName(cur.getString(cur.getColumnIndex(People.DISPLAY_NAME)));
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(People.PRIMARY_PHONE_ID))) > 0) {
                    c.setPhone(this.getPhoneNumbers(id));
                }
                c.setEmail(this.getEmailAddresses(id));
                ArrayList<String> notes = new ArrayList<String>();
                notes.add(cur.getString(cur.getColumnIndex(People.NOTES)));
                c.setNotes(notes);
                c.setAddresses(this.getContactAddresses(id));
                c.setImAddresses(this.getIM(id));
                c.setOrganization(this.getContactOrg(id));
                contacts.addContact(c);
            }
        }
        return (contacts);
    }

    public Contact getContact(String contactId) {
        Log.d(TAG, "getContact");
        String where = ContactsContract.Contacts._ID + "=?";
        String id;
        Contact contact = null;

        this.cur = this.cr.query(ContactsContract.Contacts.CONTENT_URI, null, where, new String[]{contactId}, null);
        if (this.cur != null && this.cur.moveToNext()) {
            contact = new Contact();
            id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
            contact.setId(id);
            contact.setDisplayName(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
            if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                contact.setPhone(this.getPhoneNumbers(id));
            }
            contact.setEmail(this.getEmailAddresses(id));
            ArrayList<String> notes = new ArrayList<String>();
            notes.add(cur.getString(cur.getColumnIndex(People.NOTES)));
            contact.setNotes(notes);
            contact.setAddresses(this.getContactAddresses(id));
            contact.setImAddresses(this.getIM(id));
            contact.setOrganization(this.getContactOrg(id));
        }

        return contact;
    }

    public ArrayList<Phone> getPhoneNumbers(String id) {
        Log.d(TAG, "getPhoneNumbers");
        ArrayList<Phone> phones = new ArrayList<Phone>();

        Cursor pCur = this.cr.query(Contacts.Phones.CONTENT_URI, null, Contacts.Phones.PERSON_ID + " = ?",
                new String[]{id}, null);
        while (pCur.moveToNext()) {
            phones.add(new Phone(pCur.getString(pCur.getColumnIndex(Contacts.Phones.NUMBER)), pCur.getString(pCur.getColumnIndex(Contacts.Phones.TYPE))));

        }
        pCur.close();
        return (phones);
    }

    public ArrayList<Email> getEmailAddresses(String id) {
        Log.d(TAG, "getEmailAddresses");
        ArrayList<Email> emails = new ArrayList<Email>();

        Cursor emailCur = this.cr.query(Contacts.ContactMethods.CONTENT_EMAIL_URI, null,
                Contacts.ContactMethods.PERSON_ID + " = ?", new String[]{id}, null);
        while (emailCur.moveToNext()) {
            // This would allow you get several email addresses
            Email e = new Email(emailCur.getString(emailCur.getColumnIndex(Contacts.ContactMethods.DATA)), emailCur.getString(emailCur.getColumnIndex(Contacts.ContactMethods.CONTENT_EMAIL_TYPE)));
            emails.add(e);
        }
        emailCur.close();
        return (emails);
    }

    public ArrayList<Address> getContactAddresses(String id) {
        Log.d(TAG, "getContactAddresses");
        ArrayList<Address> addrList = new ArrayList<Address>();

        String where = Contacts.ContactMethods.PERSON_ID + " = ? AND " + Contacts.ContactMethods.KIND + " = ?";
        String[] whereParameters = new String[]{id, Contacts.ContactMethods.CONTENT_POSTAL_ITEM_TYPE};

        Cursor addrCur = this.cr.query(Contacts.ContactMethods.CONTENT_URI, null, where, whereParameters, null);
        while (addrCur.moveToNext()) {
            String addr = addrCur.getString(addrCur.getColumnIndex(Contacts.ContactMethodsColumns.DATA));
            String type = addrCur.getString(addrCur.getColumnIndex(Contacts.ContactMethodsColumns.TYPE));
            Address a = new Address(addr, type);
            addrList.add(a);
        }
        addrCur.close();
        return (addrList);
    }

    public ArrayList<IM> getIM(String id) {
        Log.d(TAG, "getIM");
        ArrayList<IM> imList = new ArrayList<IM>();
        String where = Contacts.ContactMethods.PERSON_ID + " = ? AND " + Contacts.ContactMethods.KIND + " = ?";
        String[] whereParameters = new String[]{id, Contacts.ContactMethods.CONTENT_IM_ITEM_TYPE};

        Cursor imCur = this.cr.query(Contacts.ContactMethods.CONTENT_URI, null, where, whereParameters, null);
        if (imCur.moveToFirst()) {
            String imName = imCur.getString(imCur.getColumnIndex(Contacts.ContactMethodsColumns.DATA));
            String imType = imCur.getString(imCur.getColumnIndex(Contacts.ContactMethodsColumns.TYPE));
            if (imName.length() > 0) {
                IM im = new IM(imName, imType);
                imList.add(im);
            }
        }
        imCur.close();
        return (imList);
    }

    public Organization getContactOrg(String id) {
        Log.d(TAG, "getContactOrg");
        Organization org = new Organization();
        String where = Contacts.ContactMethods.PERSON_ID + " = ?";
        String[] whereParameters = new String[]{id};

        Cursor orgCur = this.cr.query(Contacts.Organizations.CONTENT_URI, null, where, whereParameters, null);

        if (orgCur.moveToFirst()) {
            String orgName = orgCur.getString(orgCur.getColumnIndex(Contacts.Organizations.COMPANY));
            String title = orgCur.getString(orgCur.getColumnIndex(Contacts.Organizations.TITLE));
            if (orgName.length() > 0) {
                org.setOrganization(orgName);
                org.setTitle(title);
            }
        }
        orgCur.close();
        return (org);
    }
}