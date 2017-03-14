package com.em_projects.contactsApi;

import android.util.Log;

import java.util.ArrayList;

public class Contact {
    private static final String TAG = "Contact";

    private String id;
    private String displayName;
    private ArrayList<Phone> phone;
    private ArrayList<Email> email;
    private ArrayList<String> notes;
    private ArrayList<Address> addresses = new ArrayList<Address>();
    private ArrayList<IM> imAddresses;
    private Organization organization;

    public Organization getOrganization() {
        Log.d(TAG, "getOrganization");
        return organization;
    }

    public void setOrganization(Organization organization) {
        Log.d(TAG, "setOrganization");
        this.organization = organization;
    }

    public ArrayList<IM> getImAddresses() {
        Log.d(TAG, "getImAddresses");
        return imAddresses;
    }

    public void setImAddresses(ArrayList<IM> imAddresses) {
        Log.d(TAG, "setImAddresses");
        this.imAddresses = imAddresses;
    }

    public void addImAddresses(IM imAddr) {
        Log.d(TAG, "addImAddresses");
        this.imAddresses.add(imAddr);
    }

    public ArrayList<String> getNotes() {
        Log.d(TAG, "getNotes");
        return notes;
    }

    public void setNotes(ArrayList<String> notes) {
        Log.d(TAG, "setNotes");
        this.notes = notes;
    }

    public void addNote(String note) {
        Log.d(TAG, "addNote");
        this.notes.add(note);
    }

    public ArrayList<Address> getAddresses() {
        Log.d(TAG, "getAddresses");
        return addresses;
    }

    public void setAddresses(ArrayList<Address> addresses) {
        Log.d(TAG, "setAddresses");
        this.addresses = addresses;
    }

    public void addAddress(Address address) {
        Log.d(TAG, "addAddress");
        this.addresses.add(address);
    }

    public ArrayList<Email> getEmail() {
        Log.d(TAG, "getEmail");
        return email;
    }

    public void setEmail(ArrayList<Email> email) {
        Log.d(TAG, "setEmail");
        this.email = email;
    }

    public void addEmail(Email e) {
        Log.d(TAG, "addEmail");
        this.email.add(e);
    }

    public String getId() {
        Log.d(TAG, "getId");
        return id;
    }

    public void setId(String id) {
        Log.d(TAG, "setId");
        this.id = id;
    }

    public String getDisplayName() {
        Log.d(TAG, "getDisplayName");
        return displayName;
    }

    public void setDisplayName(String dName) {
        Log.d(TAG, "setDisplayName");
        this.displayName = dName;
    }

    public ArrayList<Phone> getPhone() {
        Log.d(TAG, "getPhone");
        return phone;
    }

    public void setPhone(ArrayList<Phone> phone) {
        Log.d(TAG, "setPhone");
        this.phone = phone;
    }

    public void addPhone(Phone phone) {
        Log.d(TAG, "addPhone");
        this.phone.add(phone);
    }
}