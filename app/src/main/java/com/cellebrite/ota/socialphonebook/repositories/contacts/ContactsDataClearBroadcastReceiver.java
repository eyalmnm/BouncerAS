package com.cellebrite.ota.socialphonebook.repositories.contacts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.em_projects.utils.Utils;

public class ContactsDataClearBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //holds the package name
        String intentString = intent.getDataString();

        //#ifdef DEBUG
        Utils.debug("ContactsDataClearBroadcastReceiver.onReceive() - pacakge data cleared or package removed for " + intentString);
        //#endif

        //TODO - package name needs to be verified in other OSes. The package name may be different.

//		//check if its the contacts package 
//		if (intentString.contains("com.android.providers.contacts"))
//		{
//			//clear all SN persistence data 
//			Hashtable<String, ?> map = ModelManager.SharedModelComponents.getContactsIDs2SNMapping();
//			for (Enumeration<String> en = map.keys(); en.hasMoreElements();)
//			{
//				String contactID = en.nextElement();
//				ContactsRepository.getInstance().deleteContactSNContentById(contactID);
//			}
//			
//			//clear mapping 
//			ModelManager.SharedModelComponents.getContactsIDs2SNMapping().clear();
//			
//			//save empty mapping
//			CLApplicationManager.getUserSession().saveContactsIDs2SNMapping(ModelManager.SharedModelComponents.getContactsIDs2SNMapping());
//			
//			//reset application run
//			CLApplicationManager.getUserSession().saveIsFirstRun(true);
//			
//			//hint the contact repository for contacts DB wipe 
//			ContactsRepository.getInstance().hintForPossibleAction(ContactsRepository.ExtrenalActionHints.CONTACTS_DB_WIPE, null);
//		}
    }
}
