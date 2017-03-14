package com.em_projects.bouncer.services;

import android.util.Log;

import com.em_projects.bouncer.model.Contact;
import com.em_projects.bouncer.repositories.ContactsRepository;
import com.em_projects.bouncer.views.model.ContactsViewModel;
import com.em_projects.infra.services.AsyncService;

import java.util.Collections;
import java.util.Vector;

public class LoadContactsService extends AsyncService<ContactsViewModel> {
    private static final String TAG = "LoadContactsService";

    @Override
    protected ContactsViewModel execute() {
        Log.d(TAG, "execute");
        //get lazy contacts
        Vector<Contact> lazyContacts = ContactsRepository.getInstance().getLazyContacts();

        //sort the contacts by display name
        Collections.sort(lazyContacts, Contact.COMPARE_BY_DISPLAY_NAME);

        return new ContactsViewModel(lazyContacts);
    }
}
