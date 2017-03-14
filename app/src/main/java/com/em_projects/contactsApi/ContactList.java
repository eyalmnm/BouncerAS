package com.em_projects.contactsApi;

import android.util.Log;

import java.util.ArrayList;

public class ContactList {
    private static final String TAG = "ContactList";

    private ArrayList<Contact> contacts = new ArrayList<Contact>();

    public ContactList() {

    }

    public ArrayList<Contact> getContacts() {
        Log.d(TAG, "getContacts");
        return contacts;
    }

    public void setContacts(ArrayList<Contact> contacts) {
        Log.d(TAG, "setContacts");
        this.contacts = contacts;
    }

    public void addContact(Contact contact) {
        Log.d(TAG, "addContacts");
        this.contacts.add(contact);
    }
}