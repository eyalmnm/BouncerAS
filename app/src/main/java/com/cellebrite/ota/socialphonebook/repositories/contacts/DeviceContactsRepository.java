package com.cellebrite.ota.socialphonebook.repositories.contacts;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import com.cellebrite.ota.socialphonebook.model.SocialNetworkActivity;
import com.cellebrite.ota.socialphonebook.repositories.CacheObject;
import com.cellebrite.ota.socialphonebook.repositories.CacheObject.CacheState;
import com.cellebrite.ota.socialphonebook.repositories.contacts.VCardParser.VCardParserException;
import com.em_projects.bouncer.BouncerApplication;
import com.em_projects.bouncer.BouncerUserSession;
import com.em_projects.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This class responsibility is to manage the actions : saving,
 * getting and removing contacts from the DB and from the application data (model).
 * TODO this class should be abstract!!
 */
public class DeviceContactsRepository {
    //holds empty list of activities
    private final static Vector<SocialNetworkActivity> m_emptyActivities = new Vector<SocialNetworkActivity>();
    //holds the single instance of this class
    private static DeviceContactsRepository s_instance;
    //holds  a cached contacts details mapped by their device ID  - map(contactId , contact details)
    private final Hashtable<String, Vector<ContactGroup>> m_cachedcontactsDetails = new Hashtable<String, Vector<ContactGroup>>();
    //holds  a cached contacts activities mapped by their device ID  - map(contactId , contact activities)
    private final Hashtable<String, CacheObject<Vector<SocialNetworkActivity>>> m_cachedcontactsActivities = new Hashtable<String, CacheObject<Vector<SocialNetworkActivity>>>();
    //holds  a cached contacts mapped by their device ID  - map(contactId , contact data)
    private Hashtable<String, CacheObject<DeviceContact>> m_cachedcontacts = new Hashtable<String, CacheObject<DeviceContact>>();
    //holds  a cached contacts mapped by their device ID  - map(contactId , contact data)
    private Vector<String> m_cachedSortedContactsIDs = new Vector<String>();
    //holds  an observers which will be called when a change in the repository will occur
    private HashSet<ContactRepositoryObserver> m_contactRepositoryObservers = new HashSet<ContactRepositoryObserver>();

    //holds whether all contacts have been cached
    private boolean b_isAllContactsCachedOnce = false;

    //holds the last given hint
    private byte m_lastGivenHint = ExtrenalActionHints.NO_ACTION;

    //holds the last given contact id for hint
    private String m_lastGivenContactIDForHint = null;

    /**
     * Gets the single instance of this class.
     *
     * @return (ContactsRepository != null) the single instance of this class.
     */
    public synchronized static DeviceContactsRepository getInstance() {

        //in case the single instance of this class doesn't exist
        if (s_instance == null) {
            //TODO reflection for different OS version
            //creates the single instance of this class
            s_instance = new DeviceContactsRepository();

            //create new contact observer and start it
            ContactObserver observer = new ContactObserver();
            observer.start();
        }
        //returns the single instance of this class
        return s_instance;
    }

    /**
     * get the contact from cache container
     *
     * @param contactId (String != null) the contact id to find in cache
     * @return the contact if exist otherwise return null.
     */
    private DeviceContact getContactByIdFromCache(String contactId) {
        //get contact from cache
        CacheObject<DeviceContact> cacheContact = m_cachedcontacts.get(contactId);
        DeviceContact contact = null;

        //get the contact from cache object
        if (cacheContact != null)
            contact = cacheContact.geData();

        return contact;
    }

    /**
     * Return a full (with trying to load the photo from DB) contact by given contact id
     *
     * @param contactId (String) the contact id
     * @return - Contact for the given id , in case the contact not exist return null.
     */
    public DeviceContact getContactById(String contactId) {
        //get the lazy contact
        DeviceContact contact = getLazyContactById(contactId);

        //return null incase the contact doesnt exists
        if (contact == null)
            return null;

        CacheObject<DeviceContact> cacheContact = m_cachedcontacts.get(contactId);

        //wait if currently being loaded by another thread
        //ContactsLoadingSynchronizer.waitIfCurrentlyLoading(contactId);

        //if a photo has never been loaded before
        if (cacheContact.getState() != CacheState.Fully_Loaded) {
//			//in case contact have no status try to set it from DB.
//			if(contact.getLastSocialNetworkStatus() == null)
//			{
//				//get cursor with status if exist
//				Cursor cursor = RepositoryHelperForSNContent.performQueryOnStatusTable(contactId);
//				
//				//get the conatc's status from DB
//				if(cursor != null && cursor.moveToFirst())
//				{
//					SocialNetworkStatus status = RepositoryHelperForSNContent.getContactStatusFromCursor(cursor);
//					contact.setLastStatusUpdate(status);
//				}
//				
//				//close the cursor
//				if(cursor != null )
//					cursor.close();
//			}
            //holds the input stream to the photo
            InputStream is = null;

            try {
                //get input stream for the photo
                is = RepositoryHelperForOS2_0.getISToContactPhoto(contactId);

                //if not null set the contacts photo
                if (is != null)
                    contact.setPhotoData(is);
                    //else set the default photo
                else
                    contact.setDefaultPhoto();

            } catch (Throwable t) {
                //#ifdef ERROR
                Utils.error("ContactsRepository.getContactById() - failed to load contact photo throwen:", t);
                //#endif
            } finally {
                try {
                    if (is != null)
                        is.close();
                } catch (IOException ioe) {
                    //#ifdef ERROR
                    Utils.error("ContactsRepository.getContactById() - failed to close the photo stream! exception throwen:", ioe);
                    //#endif
                }

            }
        }

        //notify all awaiting threads for this contact to be loaded
        //ContactsLoadingSynchronizer.notifyAllAwaitingFor(contactId);

        //set as full
        cacheContact.seState(CacheState.Fully_Loaded);

        //return the contact
        return contact;
    }

    /**
     * Return a lazy (without loading a photo from DB, will have the default photo) contact by given contact id.<br>
     * Note: if the contact has been fully loaded (with photo) before, then it will be returned only if any other changes has not been done on this contact.
     *
     * @param contactId (String != null) the contact id
     * @return - Contact for the given id , in case the contact not exist return null.
     */
    public DeviceContact getLazyContactById(String contactId) {
        //wait if already loading this contact
        //ContactsLoadingSynchronizer.waitIfCurrentlyLoading(contactId);

        //get cache object if exist
        CacheObject<DeviceContact> cacheContact = m_cachedcontacts.get(contactId);
        DeviceContact contact = null;

        if (cacheContact != null)
            //try to get contact from cached
            contact = cacheContact.geData();

        //if the contact is not null and a hint for EDIT was given
        if (contact != null && (m_lastGivenHint & ExtrenalActionHints.EDIT) > 0) {
            //if the given hint ID equals to this contact's ID
            if ((m_lastGivenContactIDForHint != null && contact.getId().equals(m_lastGivenContactIDForHint))) {
                //reset the EDIT hint
                m_lastGivenHint = (byte) (m_lastGivenHint ^ ExtrenalActionHints.EDIT);

                //reset the last given contact ID hint
                m_lastGivenContactIDForHint = null;

                //set the contact to null so it will be reloaded
                contact = null;
            }
        }

        try {
            //if contact not exist in cache, load it from DB
            if (contact == null) {
                //#ifdef DEBUG
                Utils.debug("ContactsRepository.getLazyContactById() - loading from DB contact with ID:" + contactId);
                //#endif

                //get a cursor to the contact
                Cursor cursor = RepositoryHelperForOS2_0.performQueryOnContactsTable(contactId);

                //if the contact exits
                if (cursor != null && cursor.moveToFirst()) {
                    //get a lazy contact object from the cursor
                    contact = RepositoryHelperForOS2_0.getLazyContactFromCursor(cursor);

                    //create cache object with contact
                    cacheContact = new CacheObject<DeviceContact>(CacheState.Lazy_Loaded, contact);

                    //cache it
                    m_cachedcontacts.put(contactId, cacheContact);

                    //close the cursor
                    cursor.close();
                } else {
                    //#ifdef ERROR
                    Utils.error("ContactsRepository.getLazyContactById() - contact with ID: " + contactId + " does not exist!");
                    //#endif
                }
            } else {
//				//#ifdef DEBUG
//				Utils.debug("ContactsRepository.getLazyContactById() - returning from cache contact with ID:"+contactId);
//				//#endif
            }
        } catch (Throwable t) {
            //#ifdef DEBUG
            Utils.debug("ContactsRepository.getLazyContactById() - contact with ID:" + contactId + " does not exist!");
            //#endif
        } finally {
            //notify all the waiting threads that this contact has been loaded
            //ContactsLoadingSynchronizer.notifyAllAwaitingFor(contactId);
        }

        return contact;

    }

    /**
     * This method gets a list of contact IDs and returns a map of IDs to VCards.
     *
     * @param contactsIDs (Vector<String> != null) the list of contacts.
     * @return (Hashtable<String, String>) a map of IDs to VCards.
     */
    public Hashtable<String, String> getContactsVCards(Vector<String> contactsIDs) {
        //create the contact to vcards table
        Hashtable<String, String> syncData = new Hashtable<String, String>(contactsIDs.size());

        //traverse over all contacts IDs
        for (String id : contactsIDs) {
            //#ifdef DEBUG
            Utils.debug("ContactsRepository.getContactsVCards() - creating VCard for contact ID:" + id);
            //#endif

            //create a VCard for this contact
            String vcard = getContactVCard(id);

            //set the VCard
            syncData.put(id, vcard);
        }

        //hint the system to clear data
        System.runFinalization();
        System.gc();

        //return the syncable data
        return syncData;
    }

    /**
     * Returns a photo data to a given Contact <u><b>only if the contact photo is not the default one<b><u>.
     *
     * @param contact (Contact != null)
     * @return (byte[]) stream of bytes of the contact photo or null if its the default one.
     */
    private byte[] getPhotoData(DeviceContact contact) {
        //holds the photo daat
        byte[] photoData = null;

        //get the large photo
        Bitmap photo = contact.getLargePhoto();

        //if its not the large photo
        if (photo != null && photo != DeviceContact.DEFAULT_LARGE_CONTACT_PHOTO) {
            //extract PNG in form of byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            photo.compress(CompressFormat.PNG, 0, bos);
            photoData = bos.toByteArray();
        }

        return photoData;
    }

    /**
     * return all contacts (Lazy contacts)
     *
     * @return - list of Contacts , in case there is no contacts return empty list.
     */
    public Vector<String> getAllContacts() {
        //TODO - do not load any contacts that are already cached and valid (not been changed externally)

        //if the repository got a hint for UNKNOWN_ACTION
        if (m_lastGivenHint == ExtrenalActionHints.UNKNOWN_ACTION) {
            //if there is at least one contact in the cache
            if (m_cachedSortedContactsIDs.size() > 0)
                //if the contact given doesn't exists
                if (!RepositoryHelperForOS2_0.isContactExists(m_cachedSortedContactsIDs.firstElement()))
                    //set to load all contacts
                    b_isAllContactsCachedOnce = false;

            //reset the hint
            m_lastGivenHint = ExtrenalActionHints.NO_ACTION;
        }

        //in case all contacts have not been cached yet or a hint for the action NEW was given
        if (!b_isAllContactsCachedOnce || (m_lastGivenHint & ExtrenalActionHints.NEW) > 0) {
            //#ifdef DEBUG
            Utils.debug("ContactsRepository.getAllContacts() - loading all contacts from DB");
            //#endif

            //create new containers for cached contacts and cached sorted ids
            Hashtable<String, CacheObject<DeviceContact>> cachedcontacts = new Hashtable<String, CacheObject<DeviceContact>>();

            Vector<String> cachedSortedContactsIDs = new Vector<String>();

            //load all contacts to the new containers
            loadAllContacts(cachedcontacts, cachedSortedContactsIDs);

            //replace the cacheS
            m_cachedcontacts = cachedcontacts;
            m_cachedSortedContactsIDs = cachedSortedContactsIDs;

            //set all the contacts have been cached
            b_isAllContactsCachedOnce = true;

        }
        //if a hint for the external action EDIT was given
        else if ((m_lastGivenHint & ExtrenalActionHints.EDIT) > 0) {
            //remove the edited contact details
            if (m_lastGivenContactIDForHint != null) {
                //reload only this contact and cache it
                DeviceContact newContact = getLazyContactById(m_lastGivenContactIDForHint);
                CacheObject<DeviceContact> newCacheContact = new CacheObject<DeviceContact>(CacheState.Lazy_Loaded, newContact);
                m_cachedcontacts.put(newContact.getId(), newCacheContact);

                //clear the contacts details
                m_cachedcontactsDetails.remove(newContact.getId());
            } else {
                //#ifdef ERROR
                Utils.error("ContactsRepository.getAllContacts() - a hint for EDIT was given but no contact ID was provided!");
                //#endif
            }
        } else {
            //#ifdef DEBUG
            Utils.debug("ContactsRepository.getAllContacts() - returning all contacts from cache");
            //#endif
        }

        //clear the hints
        m_lastGivenContactIDForHint = null;
        m_lastGivenHint = ExtrenalActionHints.NO_ACTION;

        //create a container for all contacts and fill the collection with all the contacts
        Vector<String> allContacts = new Vector<String>(m_cachedSortedContactsIDs);
        return allContacts;
    }

    /**
     * Load all contacts from Android contacts DB to the given cached contacts container.
     *
     * @param cachedcontactsContainer - a container for all the loaded contacts from DB.
     */
    private void loadAllContacts(Hashtable<String, CacheObject<DeviceContact>> cachedcontactsContainer, Vector<String> cachedSortedContactsIDs) {
        //get a cursor to all contacts
        Cursor cursor = RepositoryHelperForOS2_0.performQueryOnContactsTable(null);

        //if the contact exits
        if (cursor != null) {
            //set cursor to first position in table
            if (cursor.moveToFirst()) {
                //go over all contacts raws
                do {
                    //get a lazy contact object from the cursor
                    DeviceContact contact = RepositoryHelperForOS2_0.getLazyContactFromCursor(cursor);

                    if (contact != null) {
                        //try to find the old cache contact with same id
                        DeviceContact oldContact = getContactByIdFromCache(contact.getId());

                        //set last status from old status and old photos
                        if (oldContact != null) {
                            //set old photos
                            contact.copyPhotos(oldContact);

                            //set old status
                            contact.setLastStatusUpdate(oldContact.getLastSocialNetworkStatus());
                        }
                    }

                    //create new cache contact
                    CacheObject<DeviceContact> newCacheContact = new CacheObject<DeviceContact>(CacheState.Lazy_Loaded, contact);

                    //cache it - if the contact is already exist - it will replace it with the new contact
                    cachedcontactsContainer.put(contact.getId(), newCacheContact);

                    //cache in the sorted list
                    cachedSortedContactsIDs.add(contact.getId());
                }
                while (cursor.moveToNext());
            }

            //close the cursor
            cursor.close();
        } else {
            //#ifdef DEBUG
            Utils.debug("ContactsRepository.loadAllContacts() - no contacts were found");
            //#endif
        }

    }

    /**
     * Called when a change occurs in the contacts DB.
     */
    public void onChangeInContacts() {
        //#ifdef DEBUG
        Utils.debug("ContactRepositoryObserver.notifyChageInContacts !!");
        //#endif

        //clear the cached contact details because there was a change
        m_cachedcontactsDetails.clear();

        //create new containers for cached contacts and cached sorted ids
        Hashtable<String, CacheObject<DeviceContact>> cachedcontacts = new Hashtable<String, CacheObject<DeviceContact>>();
        Vector<String> cachedSortedContactsIDs = new Vector<String>();

        //load all contacts to the new containers
        loadAllContacts(cachedcontacts, cachedSortedContactsIDs);

        m_cachedcontacts = cachedcontacts;
        m_cachedSortedContactsIDs = cachedSortedContactsIDs;

        //notify for the change for all the observers
        for (ContactRepositoryObserver observer : m_contactRepositoryObservers) {
            observer.onContactChange();
        }
    }

    /**
     * return all contact's details from all the Aggregate groups (accounts).
     *
     * @param contactId (String) - the contact id
     * @return contact's details from all the Aggregate accounts , in case no details return null.
     * Note : Each contact can be connected to several groups .
     * need to return all the relevant details from all available groups.
     */
    public Vector<ContactGroup> getContactDetailsById(String contactId) {
        //wait if another thread is already loading this contact details
        //TODO - it syncs also with the basic contact loading (getContactById() && getLazyContactById()) which is wrong! should be handled separately
        //ContactsLoadingSynchronizer.waitIfCurrentlyLoading(contactId);

        //get the contact details
        Vector<ContactGroup> contactDetails = m_cachedcontactsDetails.get(contactId);

        try {
            //if not cached already it needs the be loaded from the data base
            if (contactDetails == null) {
                //#ifdef DEBUG
                Utils.debug("ContactsRepository.getContactDetailsById() - loading details for contact with ID " + contactId + " from DB");
                //#endif

                //add addresses to the contact's group
                contactDetails = RepositoryHelperForOS2_0.getContactDetails(contactId);

                //cache the contact if it has details
                if (contactDetails != null && contactDetails.size() > 0)
                    m_cachedcontactsDetails.put(contactId, contactDetails);
            } else {
                //#ifdef DEBUG
                Utils.debug("ContactsRepository.getContactDetailsById() - returning contact with ID" + contactId + " details from cache");
                //#endif
            }
        } catch (Throwable t) {
            //#ifdef ERROR
            Utils.error("ContactsRepository.getContactDetailsById() - exception was throwen:", t);
            //#endif
        } finally {
            //notify the waiting threads that its been loaded
            //ContactsLoadingSynchronizer.notifyAllAwaitingFor(contactId);
        }

        return contactDetails;
    }

//	/**
//	 * return all contact's activities .
//	 * @param contactId (String) - the contact id
//	 * @return contact's activities , in case no activities return null.
//	 */
//	public Vector<SocialNetworkActivity> getContactActivitiesById(String contactId)
//	{
//		//get the contact details 
//		CacheObject<Vector<SocialNetworkActivity>> cacheContactActivities = m_cachedcontactsActivities.get(contactId);
//		Vector<SocialNetworkActivity> contactActivities = null ;
//		
//		//there is no cache for the given contact - need to get it from DB
//		if(cacheContactActivities == null)
//		{
//			//get cursor with contact activities
//			Cursor cursor = RepositoryHelperForSNContent.performQueryOnActivitiesTable(contactId);
//			
//			//create empty cache object
//			cacheContactActivities = new CacheObject<Vector<SocialNetworkActivity>>(CacheState.Invalid,null);
//			m_cachedcontactsActivities.put(contactId, cacheContactActivities);
//			
//			//if the contact have any activities exits
//			if (cursor != null && cursor.moveToFirst())
//			{
//				//create container for contact's activities
//				contactActivities = new Vector<SocialNetworkActivity>();
//					
//				//go over all contact's activities raws
//				do
//				{
//					//the social network activity for contact
//					SocialNetworkActivity snActivity = RepositoryHelperForSNContent.getContactActivityFromCursor(cursor);
//					contactActivities.add(snActivity);
//				}
//				while (cursor.moveToNext());
//				
//				
//				//set the cache data with the contact's activities 
//				cacheContactActivities.setData(CacheState.Fully_Loaded, contactActivities);
//			}
//			
//			//close cursor after use
//			if(cursor != null)
//				cursor.close();
//		}
//		//get cached activities
//		else
//			contactActivities = cacheContactActivities.geData();
//		
//		return contactActivities ;
//	}


//	/**
//	 * delete the contact's activities and status by given contact id.
//	 * @param contactId (String != null) the contact id 
//	 */
//	public synchronized void deleteContactSNContentById(String contactId)
//	{
//		//set empty status to contact
//		Contact contact =  getContactByIdFromCache(contactId) ;
//		if(contact != null)
//		{
//			contact.setLastStatusUpdate(null);
//			
//			//remove activities from cache
//			m_cachedcontactsActivities.remove(contactId);
//		
//			//remove activities from db
//			RepositoryHelperForSNContent.deleteActivitiesByContactID(contactId);
//			
//			//remove status from db
//			RepositoryHelperForSNContent.deleteStatusByDeviceID(contactId);
//		}
//		else
//		{
//			//#ifdef ERROR
//			Utils.error("ContactsRepository.deleteContactSNContentById() - contact:" + contactId + " not exist in cache!!");
//			//#endif
//		}
//		
//		
//
//	}

//	/**
//	 * save the contact's activities by given contact id.
//	 * @param contactId (String != null) the contact id 
//	 */
//	public synchronized void saveContactActivitiesById(String contactId , String activityId, String socialNetworkName, String text, long time,boolean canComment)
//	{
//		Contact contact = getContactByIdFromCache(contactId) ;
//		
//		//save activities only to valid contact
//		if(contact != null)
//		{
//			Vector<SocialNetworkActivity> activities = null ;
//			SocialNetworkActivity activity = new SocialNetworkActivity(activityId, contactId, socialNetworkName, text, time,canComment);
//			
//			//save activity in DB .
//			RepositoryHelperForSNContent.saveActivityByContactID(contactId, activity);
//			
//			//check if there is an old cache object
//			CacheObject<Vector<SocialNetworkActivity>> cacheActivities = m_cachedcontactsActivities.get(contactId);
//			if(cacheActivities == null || cacheActivities.geData() == null)
//			{
//				//create new list of activities with the new activity
//				activities = new Vector<SocialNetworkActivity> ();
//				activities.add(activity);
//				
//				//create new cache with the new activity
//				cacheActivities = new CacheObject<Vector<SocialNetworkActivity>> (CacheState.Fully_Loaded,activities);				
//			}
//			else
//			{
//				//get last activities from cache
//				activities = cacheActivities.geData();
//				activities.add(activity);
//				
//				//set the new updated list of activities
//				cacheActivities.setData(CacheState.Fully_Loaded,activities);
//			}
//			
//			//put the updated activities in cache
//			m_cachedcontactsActivities.put(contactId, cacheActivities);
//		}
//		else
//		{
//			//#ifdef ERROR
//			Utils.error("ContactsRepository.saveContactActivitiesById() - contact:" + contactId + " not exist in cache!!");
//			//#endif
//		}
//	}
//	
//	/**
//	 * save the contact's activities by given contact id.
//	 * @param contactId (String != null) the contact id 
//	 */
//	public synchronized void saveContactStatusById(String contactId ,String statusId, String socialNetworkName, String text, long time)
//	{
//		
//		//get contact from cache
//		Contact contact = getContactByIdFromCache(contactId) ;
//		
//		//save status only to valid contact
//		if(contact != null)
//		{
//			//create status information
//			SocialNetworkStatus status = new SocialNetworkStatus(statusId, contactId, socialNetworkName,text,time);
//			
//			//save status in DB .
//			RepositoryHelperForSNContent.saveStatusbyContactid(contactId, status);
//			
//			//set status to contact model
//			contact.setLastStatusUpdate(status);
//		}
//		else
//		{
//			//#ifdef ERROR
//			Utils.error("ContactsRepository.saveContactStatusById() - contact:" + contactId + " not exist in cache!!");
//			//#endif
//		}
//
//			
//	}


    /**
     * Save a contact to the DB from a given valid VCard.
     *
     * @param vCard (String != null) - contact VCard to save.
     * @return (String) - contacts unique id if save successfully , otherwise return null.
     */
    public String saveContact(String vCard) {
        try {
            //parse from VCard the contact details
            DeviceContact c = VCardParser.getContactFromVCard(vCard);

            //parse the contact photo if exists
            byte[] photoData = VCardParser.getPhotoDataFromVCard(vCard);

            InputStream is = null;

            //if a photo exists
            if (photoData != null) {
                //create input stram to photo data
                is = new ByteArrayInputStream(photoData);

                //set the data
                c.setPhotoData(is);
            }
            //else set the default photo
            else
                c.setDefaultPhoto();

            //parse from VCard the contact group details
            //TODO - use groups not group for OSes with groups
            ContactGroup cg = VCardParser.getContactGroupFromVCard(vCard);

            //TODO Boris - move account type to configuration

            BouncerUserSession session = BouncerApplication.getApplication().getUserSession();

            //save to the data base
            String id = RepositoryHelperForOS2_0.saveContact(c, cg, photoData,
                    session.getAccountType(),
                    session.getAccountName());

            //if failed saving to DB
            if (id.equals(DeviceContact.NULL_UID)) {
                //#ifdef ERROR
                Utils.error("ContactRepository.saveContact() - saving contact to the DB has failed");
                //#endif

                return null;
            }

            //set the contact id
            c.m_uid = id;

            //create cache object for new contact
            CacheObject<DeviceContact> cacheContact = new CacheObject<DeviceContact>(CacheState.Fully_Loaded, c);

            //put it in the cache
            m_cachedcontacts.put(id, cacheContact);

            //create a dummy group list
            Vector<ContactGroup> groups = new Vector<ContactGroup>();
            groups.add(cg);

            //put the details in the cache
            m_cachedcontactsDetails.put(id, groups);

            try {
                //close the input stream
                if (is != null)
                    is.close();
            } catch (IOException e) {
                //#ifdef ERROR
                Utils.error("ContactsRepository.saveContact() - failed to close input stream:", e);
                //#endif
            }

            //return its id
            return id;
        } catch (VCardParserException vce) {
            //#ifdef ERROR
            Utils.error("ContactsRepository.saveContact() - failed saving contact exception throwen:", vce);
            //#endif

            return null;
        }
    }

    /**
     * Modifies a contact in the DB with the data given in a valid VCard.
     *
     * @param vCard (String != null) - contact VCard to save.
     * @param id    (String != null) - the contact's ID.
     * @return (String) - contacts unique id if save successfully , otherwise return null.
     */
    public boolean modifyContact(String vCard, String id, String accountType, String accountName) {
        try {
            //parse from VCard the contact details
            DeviceContact c = VCardParser.getContactFromVCard(vCard);

            //parse the contact photo if exists
            byte[] photoData = VCardParser.getPhotoDataFromVCard(vCard);

            InputStream is = null;

            //if a photo exists
            if (photoData != null) {
                //create input stram to photo data
                is = new ByteArrayInputStream(photoData);

                //set the data
                c.setPhotoData(is);
            }
            //else set the default photo
            else
                c.setDefaultPhoto();

            //parse from VCard the contact group details
            //TODO - use groups not group for OSes with groups
            ContactGroup cg = VCardParser.getContactGroupFromVCard(vCard);

            //set the given ID
            c.m_uid = id;

            //save to the data base
            boolean isModified = RepositoryHelperForOS2_0.modifyContact(id, c, cg, photoData, accountType, accountName);

            //if failed saving to DB
            if (!isModified) {
                //#ifdef ERROR
                Utils.error("ContactRepository.modifyContact() - modifing a contact in the DB has failed");
                //#endif

                return false;
            }

            //create cache object for new contact
            CacheObject<DeviceContact> cacheContact = new CacheObject<DeviceContact>(CacheState.Fully_Loaded, c);

            //put it in the cache
            m_cachedcontacts.put(id, cacheContact);

            //create a dummy group list
            Vector<ContactGroup> groups = new Vector<ContactGroup>();
            groups.add(cg);

            //put the details in the cache
            m_cachedcontactsDetails.put(id, groups);

            try {
                //close the input stream
                if (is != null)
                    is.close();
            } catch (IOException e) {
                //#ifdef ERROR
                Utils.error("ContactRepository.modifyContact() - failed to close input stream:", e);
                //#endif
            }

            //return success
            return true;
        } catch (Exception vce) {
            //#ifdef ERROR
            Utils.error("ContactRepository.modifyContact() - failed modifying saving contact exception throwen:", vce);
            //#endif

            return false;
        }
    }

    /**
     * remove contact from the repository.
     *
     * @param conatctId (String) - contact to remove from the repository.
     * @return (boolean) true if removed false otherwise.
     */
    public boolean deleteContact(String contactId) {
        //if the contact is cached remove it
        m_cachedcontacts.remove(contactId);
        m_cachedcontactsDetails.remove(contactId);
        m_cachedSortedContactsIDs.remove(contactId);

        //delete the SN content of this contact
        //deleteContactSNContentById(contactId);

        if (RepositoryHelperForOS2_0.deleteContactPhoto(contactId)) {
            //#ifdef DEBUG
            Utils.debug("ContactRepository.deleteContact() - contacts with ID:" + contactId + " has no photo");
            //#endif
        }

        //return the result of deleting
        return RepositoryHelperForOS2_0.deleteContact(contactId);
    }

//	/**
//	 * Helper for synchronizing contacts loading.
//	 */
//	static class ContactsLoadingSynchronizer
//	{
//		//holds the currently loading contacts
//		private final static HashSet<String> m_currentlyLoadingContacts = new HashSet<String>();
//		
//		/**
//		 * Notifies all those awaiting for this contact to be loaded.<br>
//		 * Should be called when the contact with the given ID has been loaded. 
//		 * 
//		 * @param contactId (String != null) a valid contact id.
//		 */
//		static void notifyAllAwaitingFor(String contactId) 
//		{
//			//sync on the currently loading contacts
//			synchronized (m_currentlyLoadingContacts) 
//			{
//				//if the contact has just been loaded by this thread
//				if (m_currentlyLoadingContacts.contains(contactId))
//				{
////					//#ifdef DEBUG
////					Utils.debug("ContactsLoadingSynchronizer.notifyAllAwaitingFor() - thread:"+Thread.currentThread()+" notified that contact with ID:"+contactId+" has been loaded");
////					//#endif
//
//					//remove it from the currently loading contacts
//					m_currentlyLoadingContacts.remove(contactId);
//					
//					//notify all threads that wait for this contact to load 
//					m_currentlyLoadingContacts.notifyAll();
//				}
//			}
//		}
//		
//		/**
//		 * Will cause the running thread to wait if the contact is currently being loaded by another thread.
//		 * 
//		 * @param contactId (String != null) a valid contact id.
//		 */
//		static void waitIfCurrentlyLoading(String contactId)
//		{
//			//sync on the currently loading contacts
//			synchronized (m_currentlyLoadingContacts) 
//			{
//				//check if its being loaded already by another thread
//				if (m_currentlyLoadingContacts.contains(contactId))
//				{
//					try 
//					{
////						//#ifdef DEBUG
////						Utils.debug("ContactsLoadingSynchronizer.waitIfCurrentlyLoading() - thread:"+Thread.currentThread()+" is waiting for contact with ID:"+contactId+" to be loaded");
////						//#endif
//						
//						m_currentlyLoadingContacts.wait();
//					} 
//					catch (Throwable t) 
//					{
//						//#ifdef ERROR
//						Utils.error("ContactsRepository.getLazyContactById() -  Failed to wait on the currently loading contacts. throwen", t);
//						//#endif
//					}
//				}
//				//else set that it will be loaded by this thread
//				else
//				{
////					//#ifdef DEBUG
////					Utils.debug("ContactsLoadingSynchronizer.waitIfCurrentlyLoading() - thread:"+Thread.currentThread()+" will try loading contact with ID:"+contactId);
////					//#endif
//					
//					m_currentlyLoadingContacts.add(contactId);
//				}
//			}
//		}
//	}

    /**
     * @return (Vector<String> != null) the list of contact IDs which are favorites
     */
    public Vector<String> getFavoriteContacts() {
        //will holds the sorted IDs of favorites contacts
        Vector<String> allFavoriteContacts = new Vector<String>();

        //holds all the contacts IDs in sorted collection
        Vector<String> allContacts = getAllContacts();

        //for each contact id in list
        for (String contactId : allContacts) {
            //get the real contact
            DeviceContact contact = getLazyContactById(contactId);

            //if the contact is favorite
            if (contact.isFavorite())
                //add the contact as sorted favorite contact id
                allFavoriteContacts.add(contactId);
        }

        //return the favorite contacts IDs
        return allFavoriteContacts;
    }

    /**
     * creates a sorted sub-list from the contacts DB according to a given filter string.
     *
     * @param filterContactName (String != null) the filter (search) string.
     * @return (Vector<String> != null) the list of sorted contacts which have display name that start with the given string
     */
    public Vector<String> getFilterContacts(String filterContactName) {
        //holds all the contacts IDs in sorted collection
        Vector<String> allContacts = getAllContacts();

        if (filterContactName == null)
            return allContacts;

        //will holds the sorted filter contacts
        Vector<String> filterContacts = new Vector<String>();

        //for each contact id in list
        for (String contactId : allContacts) {
            //get the real contact
            DeviceContact contact = getLazyContactById(contactId);

            String conatctDisplayname = contact.getDisplayName().toLowerCase();

            //if the contact matches the filter string
            if (conatctDisplayname.startsWith(filterContactName.trim().toLowerCase()) ||
                    conatctDisplayname.contains(" " + filterContactName.trim().toLowerCase()))
                //add the contact to filtered contact ids collection
                filterContacts.add(contactId);
        }

        return filterContacts;
    }

    /**
     * Save a photo for a contact with the given ID.
     *
     * @param deviceID  (String != null) a valid contact ID.
     * @param photoData (byte[]l) the photo data if null the photo data will be cleared.
     * @return (boolean) true if succeeded false otherwise.
     */
    public boolean updateContactPhoto(String contactID, byte[] photoData) {
        try {
            //get a new lazy contact for this contact and cache it
            DeviceContact contact = getLazyContactById(contactID);
            CacheObject<DeviceContact> cacheContact = m_cachedcontacts.get(contactID);

            //if the contact doesnt exists
            if (contact == null)
                throw new NullPointerException("getLazyContactById(" + contactID + ") - returnd null");

            //if not null
            if (photoData != null) {
                //get a clone for this contact
                DeviceContact newContact = contact.getClone();

                //save photo data
                if (RepositoryHelperForOS2_0.saveContactPhoto(contactID, photoData)) {
                    InputStream is = new ByteArrayInputStream(photoData);

                    //set the photo
                    newContact.setPhotoData(is);

                    //close the input stream
                    is.close();
                } else
                    //set the default photo
                    newContact.setDefaultPhoto();

                //create new cache object and set previous cache state
                CacheObject<DeviceContact> newCacheContact = new CacheObject<DeviceContact>(cacheContact.getState(), newContact);

                //set the new contact
                m_cachedcontacts.put(contact.getId(), newCacheContact);
            } else {
                //delete the contact photo
                RepositoryHelperForOS2_0.deleteContactPhoto(contactID);

                //get a clone for this contact
                DeviceContact newContact = contact.getClone();

                //set the default photo
                newContact.setDefaultPhoto();

                //create new cache object and set previous cache state
                CacheObject<DeviceContact> newCacheContact = new CacheObject<DeviceContact>(CacheState.Lazy_Loaded, newContact);

                //set the new contact
                m_cachedcontacts.put(contact.getId(), newCacheContact);
            }
        } catch (Throwable t) {
            //#ifdef ERROR
            Utils.error("ContactsRepository.saveContactPhoto() - failed saving photo for contact with ID:" + contactID, t);
            //#endif

            return false;
        }

        return true;
    }

    /**
     * This method sets the 'favorite' indication for a specific contact according to a URI that represents a specific contact row.
     *
     * @param uri           (Uri) The URL of the row of the contact to set its 'favorite' indication.
     * @param setAsFavorite (boolean) indication for setting or unsetting the contact's 'favorite' indication.
     * @return (boolean) true is succeeded false otherwise.
     */
    public boolean setContactAsFavorite(String contactID, boolean setAsFavorite) {
        try {
            //save this contact new photo to the DB
            if (RepositoryHelperForOS2_0.setContactFavoriteIndication(contactID, setAsFavorite)) {
                //get a new lazy contact for this contact and cache it
                DeviceContact contact = getLazyContactById(contactID);
                CacheObject<DeviceContact> cacheContact = m_cachedcontacts.get(contactID);

                //if the contact doesnt exists
                if (contact == null)
                    throw new NullPointerException("getLazyContactById(" + contactID + ") - returnd null");

                //get a clone for this contact
                DeviceContact newContact = contact.getClone();

                //set as favorite
                newContact.setFavorite(setAsFavorite);

                //create new cache object and set previous cache state
                CacheObject<DeviceContact> newCacheContact = new CacheObject<DeviceContact>(cacheContact.getState(), newContact);

                //set the new contact
                m_cachedcontacts.put(contact.getId(), newCacheContact);

                //TODO - notify for the change!
            }
        } catch (Throwable t) {
            //#ifdef ERROR
            Utils.error("ContactsRepository.setContactFavoriteIndication() - failed to set favorite(" + setAsFavorite + ") for contact with ID:" + contactID, t);
            //#endif

            return false;
        }

        return true;
    }

    /**
     * By using this method you can provide the repository with hints on external actions that may occur on contacts.<br>
     * This will help the repository to manage its resources in more efficient and fast way.
     *
     * @param action    (byte) a bitwise constructed using ExtrenalActionHints that holds the possible actions that may occur.
     * @param contactID (String) the contact ID that was provided to external tools or null in case the actions is more general.
     */
    public void hintForPossibleAction(byte action, String contactID) {
        //in case a hint for contacts DB wipe has been received simulate on change in contacts notification
        if (action == ExtrenalActionHints.CONTACTS_DB_WIPE) {
            onChangeInContacts();
            return;
        }

        m_lastGivenHint = (byte) (m_lastGivenHint | action);
        m_lastGivenContactIDForHint = contactID;
    }

    /**
     * Returns a contact VCard representation to the contact with the given ID.
     *
     * @param contactID (String != null) the contact's ID.
     * @return (String) the contact's VCard or null if not exists.
     */
    public String getContactVCard(String contactID) {
        return getContactVCard(contactID, true);
    }

    /**
     * Returns a contact VCard representation to the contact with the given ID.
     *
     * @param contactID         (String != null) the contact's ID.
     * @param encodeDisplayName (boolean) whether to encode display name in UTF-8 format or not.
     * @return (String) the contact's VCard or null if not exists.
     */
    public String getContactVCard(String contactID, boolean encodeDisplayName) {
        //holds the details
        Vector<ContactGroup> groups = null;

        //get the full contact from repository
        DeviceContact contact = getContactById(contactID);

        //if the contact exists
        if (contact != null)
            //set the groups
            groups = getContactDetailsById(contactID);
            //else return
        else return null;

        //return the VCard
        return VCardParser.createContactVCard(contact, groups, getPhotoData(contact), encodeDisplayName);
    }

    /**
     * !!! Not implemented !!!
     *
     * @return (Vector<String> != null) a list of contacts sorted by SN content. first contact will have the latest post.
     */
    public Vector<String> getContactsBySortedSNContent() {
//		//holds the vector of contacts IDs
//		Vector<String> contacts = new Vector<String>();
//		
//		//get the cursors for the tables
//		Cursor statusesCursor = RepositoryHelperForSNContent.performQueryOnStatusTable(null);
//		Cursor activitiesCursor = RepositoryHelperForSNContent.performQueryOnActivitiesTable(null);
//		
//		try
//		{
//			//get the 
//			statusesCursor = RepositoryHelperForSNContent.performQueryOnStatusTable(null);
//			activitiesCursor = RepositoryHelperForSNContent.performQueryOnActivitiesTable(null);
//			
//			//in case we have both activities and statuses 
//			if (statusesCursor != null && activitiesCursor != null && 
//					statusesCursor.moveToFirst() && activitiesCursor.moveToFirst())
//			{
//				boolean traversedAll = false;
//				
//				do
//				{
//					SocialNetworkStatus status = RepositoryHelperForSNContent.getContactStatusFromCursor(statusesCursor);
//					SocialNetworkActivity activity = RepositoryHelperForSNContent.getContactActivityFromCursor(activitiesCursor);
//					
//					if (status.getTime() > activity.getTime())
//					{
//						//contacts.add()
//					}
//						
//					
//					traversedAll = statusesCursor.isLast() && activitiesCursor.isLast();
//				}
//				while (traversedAll);
//				
//				
//			}
//		}
//		catch (Throwable t)
//		{
//			
//		}
//		
        return null;

    }

//	/**
//	 * @return (Vector<String> != null) a list of contacts sorted by SN activities content. first contact will have the latest post.
//	 */
//	public Vector<SocialNetworkActivity> getSNActivitiesSortedByTime() 
//	{
//		//holds the vector of contacts IDs
//		Vector<SocialNetworkActivity> activities = new Vector<SocialNetworkActivity>();
//		
//		//get the cursors for the tables
//		Cursor activitiesCursor = null;
//		
//		try
//		{
//			//get the activities  
//			activitiesCursor = RepositoryHelperForSNContent.performQueryOnActivitiesTableSortedBy(SocialNetworkActivitiesTable.TIME, false);
//			
//			//in case we have both activities and statuses 
//			if (activitiesCursor != null && activitiesCursor.moveToFirst())
//			{
//				do
//				{
//					//add the contact id
//					activities.add(RepositoryHelperForSNContent.getContactActivityFromCursor(activitiesCursor));
//				}
//				while (activitiesCursor.moveToNext());
//			}
//		}
//		catch (Throwable t)
//		{
//			//#ifdef ERROR
//			Utils.error("ContactsRepository.getContactsBySortedSNActivities() - exception throwen ", t);
//			//#endif
//		}
//		
//		finally
//		{
//			if (activitiesCursor != null)
//				activitiesCursor.close();
//		}
//		
//		return activities;
//	}


    /**
     * Register an observer class that gets call backs when contacts will be change in the repository.
     *
     * @param observer (ContactRepositoryObserver != null)-The object that receives call backs when changes occur.
     */
    public void registerContactObserver(ContactRepositoryObserver observer) {
        m_contactRepositoryObservers.add(observer);
    }

    /**
     * Unregisters a change observer.
     *
     * @param observer (ContactRepositoryObserver != null) - The previously registered observer that is no longer needed.
     */
    public void unregisterContactObserver(ContactRepositoryObserver observer) {
        m_contactRepositoryObservers.remove(observer);
    }

    /**
     * This class Receives call backs for changes in contacts.
     * Each class that want notifications about change in contacts need to implements it.
     */
    public interface ContactRepositoryObserver {
        /**
         * This method is called when a change occurs in the contacts repository.
         */
        public void onContactChange();
    }

    /**
     * Holds constants for possible actions that may be executed on contacts and not by using the repository.<br>
     * e.g calling the Edit/Add activities, OS Sync process and so forth.<br>
     * The actions are bitwise and can be combined.
     */
    public static final class ExtrenalActionHints {
        public static final byte NEW = 0x1;
        public static final byte EDIT = 0x2;
        public static final byte DELETE = 0x4;
        public static final byte CONTACTS_DB_WIPE = 0x8;
        public static final byte UNKNOWN_ACTION = NEW | EDIT | DELETE;
        static final byte NO_ACTION = 0x0;

    }
}
