package com.em_projects.contactsApi;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import com.em_projects.infra.application.BasicApplication;

import java.util.ArrayList;

public class ContactApiSdk5 extends ContactAPI {
    private static final String TAG = "ContactApiSdk5";

    private ContentResolver cr = BasicApplication.getApplication().getContentResolver();
    private Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
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
        return (new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI));
    }

    public ContactList newContactList() {
        Log.d(TAG, "newContactList");
        ContactList contacts = new ContactList();
        String id;

        this.cur = this.cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (this.cur.getCount() > 0) {
            while (cur.moveToNext()) {
                Contact c = new Contact();
                id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                c.setId(id);
                c.setDisplayName(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    c.setPhone(this.getPhoneNumbers(id));
                }
                c.setEmail(this.getEmailAddresses(id));
                c.setNotes(this.getContactNotes(id));
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
            contact.setNotes(this.getContactNotes(id));
            contact.setAddresses(this.getContactAddresses(id));
            contact.setImAddresses(this.getIM(id));
            contact.setOrganization(this.getContactOrg(id));
        }

        return contact;
    }

    public ArrayList<Phone> getPhoneNumbers(String id) {
        Log.d(TAG, "getPhoneNumbers");
        ArrayList<Phone> phones = new ArrayList<Phone>();

        Cursor pCur = this.cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
        while (pCur.moveToNext()) {
            phones.add(new Phone(pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)), pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))));

        }
        pCur.close();
        return (phones);
    }

    public ArrayList<Email> getEmailAddresses(String id) {
        Log.d(TAG, "getEmailAddresses");
        ArrayList<Email> emails = new ArrayList<Email>();

        Cursor emailCur = this.cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{id}, null);
        while (emailCur.moveToNext()) {
            // This would allow you get several email addresses
            Email e = new Email(emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)), emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE)));
            emails.add(e);
        }
        emailCur.close();
        return (emails);
    }

    public ArrayList<String> getContactNotes(String id) {
        Log.d(TAG, "getContactNotes");
        ArrayList<String> notes = new ArrayList<String>();
        String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] whereParameters = new String[]{id, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE};
        Cursor noteCur = this.cr.query(ContactsContract.Data.CONTENT_URI, null, where, whereParameters, null);
        if (noteCur.moveToFirst()) {
            String note = noteCur.getString(noteCur.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
            if (note.length() > 0) {
                notes.add(note);
            }
        }
        noteCur.close();
        return (notes);
    }

    public ArrayList<Address> getContactAddresses(String id) {
        Log.d(TAG, "getContactAddresses");
        ArrayList<Address> addrList = new ArrayList<Address>();

        String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] whereParameters = new String[]{id, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE};

        Cursor addrCur = this.cr.query(ContactsContract.Data.CONTENT_URI, null, where, whereParameters, null);
        while (addrCur.moveToNext()) {
            String poBox = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX));
            String street = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
            String city = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
            String state = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
            String postalCode = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
            String country = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
            String type = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
            Address a = new Address(poBox, street, city, state, postalCode, country, type);
            addrList.add(a);
        }
        addrCur.close();
        return (addrList);
    }

    public ArrayList<IM> getIM(String id) {
        Log.d(TAG, "getIM");
        ArrayList<IM> imList = new ArrayList<IM>();
        String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] whereParameters = new String[]{id, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE};

        Cursor imCur = this.cr.query(ContactsContract.Data.CONTENT_URI, null, where, whereParameters, null);
        if (imCur.moveToFirst()) {
            String imName = imCur.getString(imCur.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA));
            String imType;
            imType = imCur.getString(imCur.getColumnIndex(ContactsContract.CommonDataKinds.Im.TYPE));
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
        String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] whereParameters = new String[]{id, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE};

        Cursor orgCur = this.cr.query(ContactsContract.Data.CONTENT_URI, null, where, whereParameters, null);

        if (orgCur.moveToFirst()) {
            String orgName = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA));
            String title = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
            if (orgName.length() > 0) {
                org.setOrganization(orgName);
                org.setTitle(title);
            }
        }
        orgCur.close();
        return (org);
    }

}