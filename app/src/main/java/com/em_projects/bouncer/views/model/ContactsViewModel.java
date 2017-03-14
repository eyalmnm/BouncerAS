package com.em_projects.bouncer.views.model;

import android.util.Log;

import com.em_projects.bouncer.BouncerApplication;
import com.em_projects.bouncer.BouncerProperties;
import com.em_projects.bouncer.R;
import com.em_projects.bouncer.model.Contact;
import com.em_projects.infra.views.model.TabViewModel;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

@SuppressWarnings("serial")
public class ContactsViewModel extends TabViewModel implements Serializable {
    private static final String TAG = "ContactsViewModel";

    private Vector<ContactViewModel> m_contactVMs;

    public ContactsViewModel(List<Contact> contacts) {
        super(BouncerProperties.TAB_TYPE.CONTACTS, R.drawable.tab_item, BouncerApplication.getApplication().getString(R.string.contacts_tab_label), true);
        Log.d(TAG, "ContactsViewModel");

        m_contactVMs = new Vector<ContactViewModel>();

        for (Contact c : contacts) {
            //protect from null entries in the contacts collection
            if (c != null)
                m_contactVMs.add(new ContactViewModel(c));
        }
    }

    public Vector<ContactViewModel> getContactsViewModels() {
        Log.d(TAG, "getContactsViewModels");
        return m_contactVMs;
    }
}
