package com.cellebrite.ota.socialphonebook.repositories.contacts;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContacts.Entity;

import com.em_projects.infra.application.BasicApplication;
import com.em_projects.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Provides services for the contacts repository in OS version under 1.6
 */
public class RepositoryHelperForOS2_0 {

    /**
     * Returns an input stream for a photo.
     *
     * @return (InputStream) the input stream or null if not exists.
     */
    static InputStream getISToContactPhoto(String contactId) {
        Cursor c = null;

        try {
//			//holds the content resolver
//			ContentResolver cr = BasicApplication.getApplication().getContentResolver();
//			
//			//holds where and sort order parms for SQL
//			String where = null ;

//			//if a contact id is given direct the query 
//			if (contactId != null)
//				where = ContactsSNPhotosTable.CONTACT_ID+" = "+contactId ;
//			
//			//Query the given URI
//			c = cr.query(ContactsSNPhotosTable.CONTENT_URI, null , where , null, null);
//		
//			if (c.moveToFirst())
//			{
//				byte[] photoData = c.getBlob(c.getColumnIndex(ContactsSNPhotosTable.PHOTO_DATA));
//				
//				if (photoData != null)
//					return new ByteArrayInputStream(photoData);
//			}

            //#ifdef DEBUG
            Utils.debug("RepositoryHelper.getISToContactPhoto() - contact with ID:" + contactId + " has no photo in the application data base, getting photo from device");
            //#endif

            Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contactId));
            return ContactsContract.Contacts.openContactPhotoInputStream(BasicApplication.getApplication().getContentResolver(), contactUri);
        } catch (Throwable th) {
            //#ifdef ERROR
            Utils.error("RepositoryHelperForOS2_0.getISToContactPhoto() failed getting contact's photo ", th);
            //#endif
        } finally {
            if (c != null)
                c.close();
        }

        return null;
    }

    /**
     * This method deletes a specific contact from device DB according to a URI that represents a specific contact row.
     *
     * @param contactId (String != null) the device ID of the contact to delete.
     * @param (boolean) true in case the contact was deleted from the device, false otherwise.
     */
    static boolean deleteContact(String contactId) {
        String where = ContactsContract.RawContacts.CONTACT_ID + "=?";
        ContentProviderOperation deleteOp = ContentProviderOperation.newDelete
                (ContactsContract.RawContacts.CONTENT_URI).withSelection
                (where, new String[]{contactId}).build();

        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        operations.add(deleteOp);

        //deletes the contact and returns whether it was deleted successfully
        try {
            BasicApplication.getApplication().getContentResolver().applyBatch(ContactsContract.AUTHORITY, operations);
        } catch (Throwable t) {
            return false;
        }

        return true;
    }

    /**
     * This method deletes a specific contact photo from the application DB according to the given contact id.
     *
     * @param contactId (String != null) the device ID of the contact to delete.
     * @param (boolean) true in case the contact's photo was deleted from the application DB, false otherwise (occurs also if the contact has no photo).
     */
    static boolean deleteContactPhoto(String contactId) {
        //creates the URI for the contact to delete
//		String wherePhoto = ContactsSNPhotosTable.CONTACT_ID+" = "+contactId;
//		return BasicApplication.getApplication().getContentResolver().delete(ContactsSNPhotosTable.CONTENT_URI, wherePhoto, null) > 0;

        //TODO - add support for contact's photo

        return false;
    }

    /**
     * Returns a lazy contact (currently only without photo)
     *
     * @param cursor (Cursor != null) a cursor directed to the relevent row.
     * @return (Contact != null)
     */
    static DeviceContact getLazyContactFromCursor(Cursor cursor) {
        String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
        String contactDisplayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        boolean isfavorite = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.STARRED)) > 0 ? true : false;

        return new DeviceContact(contactId, contactDisplayName, isfavorite);

    }

    /**
     * Returns the projection on a contacts table.
     */
    static String[] getContactsTableQueryProjection() {
        return new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.STARRED};
    }

    /**
     * Performs a query on the contacts table.
     *
     * @param contactId (String) the contact id or null for all contacts.
     * @return (Cursor) cursor positioned before the first entry to a contact or all contacts or null in case there is no contacts / contact (with the given id)
     */
    public static Cursor performQueryOnContactsTable(String contactId) {
        //creates a URI for the given contact's device ID
        Uri contactURI = ContactsContract.Contacts.CONTENT_URI;

        //holds the content resolver
        ContentResolver cr = BasicApplication.getApplication().getContentResolver();

        //if a contact id is given direct the query
        if (contactId != null) {
            contactURI = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactId);
        }

        //return the cursor sorted by display name
        return cr.query(contactURI, getContactsTableQueryProjection(), ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1", null, "UPPER(" + ContactsContract.Contacts.DISPLAY_NAME + ")");
    }

    /**
     * Saves a contact to the data base.
     *
     * @param cg        (ContactGroup != null) the contact group.
     * @param photoData (byte[]) input stream to the photo or null if the contact has no photo.
     * @param contact   (Contact != null)
     * @return (String) the contact's unique id if saved successfully, or the constant Contact.NULL_UID otherwise.
     */
    static String saveContact(DeviceContact contact, ContactGroup cg, byte[] photoData, String accountType, String accountName) {
        String newContactDeviceID;
        ArrayList<ContentProviderOperation> contentProviderOperations = new ArrayList<ContentProviderOperation>();

        try {
            if (accountName != null && accountType != null) {
                contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.AGGREGATION_MODE, ContactsContract.RawContacts.AGGREGATION_MODE_DISABLED)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, accountType)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, accountName)
                        .build());
            } else {
                contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.AGGREGATION_MODE, ContactsContract.RawContacts.AGGREGATION_MODE_DISABLED)
                        .build());
            }

            putNamesValuesToContentProviderOperation(contentProviderOperations, contact);
            putNoteValuesToContentProviderOperation(contentProviderOperations, contact);
            putPhonesValuesToContentProviderOperation(contentProviderOperations, cg);
            putEmailsValuesToContentProviderOperation(contentProviderOperations, cg);
            putAddressesValuesToContentProviderOperation(contentProviderOperations, cg);
//           putOrganizationsValuesToContentProviderOperation(contentProviderOperations, cg);
            putWebsitesValuesToContentProviderOperation(contentProviderOperations, cg);
            putNicknameValuesToContentProviderOperation(contentProviderOperations, cg);
            putPhotoDataValuesToContentProviderOperation(contentProviderOperations, contact);

            ContentProviderResult[] providerResults = BasicApplication.getApplication().getContentResolver().applyBatch(ContactsContract.AUTHORITY, contentProviderOperations);
            //Uri rawContactUri = providerResults[0].uri;
            newContactDeviceID = Long.toString(ContentUris.parseId(providerResults[0].uri));

        } catch (Throwable th) {
            //#ifdef ERROR
            Utils.error("ContactDataElement.putContactData() Put contact name exception: " + th);
            //#endif

            return DeviceContact.NULL_UID;
        }

        //returns new contact's device ID
        return newContactDeviceID;
    }

    private static void putNicknameValuesToContentProviderOperation(ArrayList<ContentProviderOperation> contentProviderOperations, ContactGroup cg) {
        //get the Nickname in this group
        Vector<DataArgs> args = cg.getData(ContactDataKinds.Nickname.KIND);
        for (DataArgs nickNameArg : args) {

            try {
                contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Nickname.TYPE, nickNameArg.getValue(ContactDataKinds.Nickname.TYPE))
                        .withValue(ContactsContract.CommonDataKinds.Nickname.NAME, nickNameArg.getValue(ContactDataKinds.Nickname.NAME))
                        .build());
            } catch (Throwable th) {
                //#ifdef ERROR
                Utils.error("ContactBuilder.putNicknameValues() catch exception: " + th);
                //#endif

                continue;
            }
        }

    }

    private static void putNoteValuesToContentProviderOperation(ArrayList<ContentProviderOperation> contentProviderOperations, DeviceContact contact) {
        //TODO - if commented in note should NOT be a part of Contact!!!
//		if(contact.getNote() != null)
//		{
//			 contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//		               .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//		               .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
//		               .withValue(ContactsContract.CommonDataKinds.Note.NOTE, contact.getNote())
//		               .build());
//		}

    }

    private static void putNamesValuesToContentProviderOperation(ArrayList<ContentProviderOperation> contentProviderOperations, DeviceContact contact) {
        if (contact.m_displayName != null && !contact.m_displayName.equals("")) {
            contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.m_displayName)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, contact.m_firstName)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, contact.m_lastName)
                    .build());
        }

    }

    private static void putPhotoDataValuesToContentProviderOperation(ArrayList<ContentProviderOperation> contentProviderOperations, DeviceContact contact) {
        if (contact.getLargePhoto() != null) {
            //holds the photo daat
            byte[] photoData = null;

            //get the large photo
            Bitmap photo = contact.getLargePhoto();

            //if its not the large photo
            if (photo != null) {
                //extract PNG in form of byte array
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                photo.compress(CompressFormat.PNG, 0, bos);
                photoData = bos.toByteArray();

                contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, photoData)
                        .build());
            }


        }

    }

    private static void putWebsitesValuesToContentProviderOperation(ArrayList<ContentProviderOperation> contentProviderOperations, ContactGroup cg) {
        //get the WebSite in this group
        Vector<DataArgs> args = cg.getData(ContactDataKinds.Website.KIND);
        for (DataArgs webSiteArg : args) {

            try {
                contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Website.TYPE, webSiteArg.getValue(ContactDataKinds.Website.TYPE))
                        .withValue(ContactsContract.CommonDataKinds.Website.URL, webSiteArg.getValue(ContactDataKinds.Website.URL))
                        .build());
            } catch (Throwable th) {
                //#ifdef ERROR
                Utils.error("ContactBuilder.putEmailByType() catch exception: ", th);
                //#endif

                continue;
            }
        }

    }

//	private static void putOrganizationsValuesToContentProviderOperation(ArrayList<ContentProviderOperation> contentProviderOperations,ContactGroup cg) 
//	{
//		 //get the address in this group
//	      Vector<DataArgs> args = cg.getData(ContactDataKinds.Organization.KIND);
//	      for(DataArgs orgArg : args)
//	      {
//	      
//		      Organization organization = orgArg.getValue();
//		      
//		      try 
//		      {
//		          contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//		              .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//		              .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
//		              .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, orgArg.getType())
//		              .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, organization.getCompany())
//		              .withValue(ContactsContract.CommonDataKinds.Organization.TITLE, organization.getTitle())
//		              .build());
//		      }
//		      catch ( Throwable th ) 
//		      {
//		    	  //#ifdef ERROR
//		          Utils.error("ContactBuilder.putOrganizationsValuesToContentProviderOperation() catch exception: "+ th);
//		          //#endif
//		          
//		          continue;
//		      }
//		  }
//		
//	}

    private static void putAddressesValuesToContentProviderOperation(ArrayList<ContentProviderOperation> contentProviderOperations, ContactGroup cg) {
        //get the address in this group
        Vector<DataArgs> args = cg.getData(ContactDataKinds.Addresses.Postal.KIND);
        for (DataArgs addressArg : args) {
            try {
                contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.STREET, addressArg.getValue(ContactDataKinds.Addresses.Postal.STREET))
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.POBOX, addressArg.getValue(ContactDataKinds.Addresses.Postal.POBOX))
                        //.withValue(ContactsContract.CommonDataKinds.StructuredPostal.NEIGHBORHOOD, address.getNeighborhood())
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.CITY, addressArg.getValue(ContactDataKinds.Addresses.Postal.CITY))
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.REGION, addressArg.getValue(ContactDataKinds.Addresses.Postal.REGION))
                        //.withValue(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY, address.getCountry())
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, addressArg.getValue(ContactDataKinds.Addresses.Postal.TYPE))
                        .build());

            } catch (Throwable th) {
                //#ifdef ERROR
                Utils.error("ContactBuilder.putAddressesValuesToContentProviderOperation() catch exception: " + th);
                //#endif

                continue;
            }
        }

    }

    private static void putEmailsValuesToContentProviderOperation(ArrayList<ContentProviderOperation> contentProviderOperations, ContactGroup cg) {
        //get the Email in this group
        Vector<DataArgs> args = cg.getData(ContactDataKinds.Addresses.Email.KIND);
        for (DataArgs emailArg : args) {
            try {
                contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, emailArg.getValue(ContactDataKinds.Addresses.Email.TYPE))
                        .withValue(ContactsContract.CommonDataKinds.Email.DATA, emailArg.getValue(ContactDataKinds.Addresses.Email.DATA))
                        .build());

            } catch (Throwable th) {
                //#ifdef ERROR
                Utils.error("ContactBuilder.putEmailsValuesToContentProviderOperation() catch exception: " + th);
                //#endif

                continue;
            }
        }

    }

    private static void putPhonesValuesToContentProviderOperation(ArrayList<ContentProviderOperation> contentProviderOperations, ContactGroup cg) {
        //get the phones in this group
        Vector<DataArgs> args = cg.getData(ContactDataKinds.Phone.KIND);
        for (DataArgs phoneArg : args) {
            try {
                contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, phoneArg.getValue(ContactDataKinds.Phone.TYPE))
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneArg.getValue(ContactDataKinds.Phone.NUMBER))
                        .build());

            } catch (Throwable th) {
                //#ifdef ERROR
                Utils.error("ContactBuilder.putPhonesValuesToContentProviderOperation() catch exception: " + th);
                //#endif

                continue;
            }
        }

    }


    /**
     * Save a photo for a contact with the given ID.
     *
     * @param deviceID  (String != null) a valid contact ID.
     * @param photoData (byte[]) the photo data.
     * @return (boolean) true if succeeded false otherwise.
     */
    static boolean saveContactPhoto(String deviceID, byte[] photoData) {
        if (photoData == null)
            return false;

        try {
            //gets the content resolver
//			ContentResolver cr = BasicApplication.getApplication().getContentResolver();

            //TODO - add support for photo

//			//creates a content values object and sets activity values inside it
//			ContentValues contentValues = new ContentValues();
//			contentValues.put(ContactsSNPhotosTable.CONTACT_ID, deviceID);
//			contentValues.put(ContactsSNPhotosTable.PHOTO_DATA, photoData);
//			
//			//holds the where condition
//			String where = ContactsSNPhotosTable.CONTACT_ID+ " = "+deviceID;
//
//			//inserts contact's activity to application's database. 
//			//try to update first than if failed try to insert (assuming it will not return 0 due to any other failure)
//			if (cr.update(ContactsSNPhotosTable.CONTENT_URI, contentValues, where, null) == 0)
//				if (cr.insert(ContactsSNPhotosTable.CONTENT_URI, contentValues) == null)
//				{
//					//#ifdef ERROR
//					Utils.error("RepositoryHelper.saveContactPhoto() - failed to insert contact photo");
//					//#endif
//				}

        } catch (Throwable throwable) {
            //#ifdef ERROR
            Utils.error("RepositoryHelperForOS2_0.saveContact() - " +
                    "an error has occurred while trying to save contact - " + throwable);
            //#endif

            return false;
        }

        return true;
    }


    /**
     * @return return if a contact with the given ID exists.
     */
    static boolean isContactExists(String contactId) {
        //creates a URI for the given contact's device ID
        Uri contactURI = ContactsContract.Contacts.CONTENT_URI;

        //holds the content resolver
        ContentResolver cr = BasicApplication.getApplication().getContentResolver();

        //if a contact id is given direct the query
        if (contactId != null) {
            contactURI = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactId);
        }

        //return the cursor sorted by display name
        Cursor c = cr.query(contactURI, new String[]{ContactsContract.Contacts._ID}, null, null, null);

        try {
            //return false in case there is no data
            if (c == null || !c.moveToFirst())
                return false;

            //holds if exists
            boolean isExists = c.getCount() == 1;

            //close the cursor
            c.close();

            return isExists;
        } finally {
            //close the cursor if exists
            if (c != null)
                c.close();
        }
    }

    /**
     * Modifies a contact in the DB.
     *
     * @param c         (Contact != null) a contact with the given contact id.
     * @param cg        (ContactGroup) the contact's details.
     * @param photoData (byte[] != null) the contact's data.
     * @return (booelan) true if succeeded false otherwise.
     */
    public static boolean modifyContact(String contactId, DeviceContact contact, ContactGroup cg, byte[] photoData, String accountType, String accountName) {
        //remove contact
        deleteContact(contactId);

        //save as new contact
        saveContact(contact, cg, photoData, accountType, accountName);

        //returns success
        return false;
    }


    private static void saveContactDataRaw(ContactGroup group, String mimeType, Cursor cursor) {
        int dataKind = getContactDataKind(mimeType);

        //for any valid data kind
        if (dataKind > 0) {
            switch (dataKind) {
                case ContactDataKinds.Phone.KIND:
                    readPhoneFromDataRaw(cursor, group);
                    break;
                case ContactDataKinds.Addresses.Email.KIND:
                    readEmailFromDataRaw(cursor, group);
                    break;
                case ContactDataKinds.StructuredName.KIND:
                    readStructuredNameFromDataRaw(cursor, group);
                    break;
                case ContactDataKinds.Addresses.Postal.KIND:
                    readStructuredPostalFromDataRaw(cursor, group);
                    break;
                case ContactDataKinds.Nickname.KIND:
                    readNicknameFromDataRaw(cursor, group);
                    break;
//        	       case ContactDataKinds.Event.KIND:
//        		   readEventFromDataRaw(cursor,group);
//        		   break;
                case ContactDataKinds.Note.KIND:
                    readNoteFromDataRaw(cursor, group);
                    break;
                case ContactDataKinds.Organization.KIND:
//        		   readOrganizationFromDataRaw(cursor,group);
                    break;

                case ContactDataKinds.Website.KIND:
                    readWebsiteFromDataRaw(cursor, group);
                    break;
//             	     case ContactDataKinds.Im.KIND:
//         		  readImFromDataRaw(cursor,group);
//         		  break;
//           	       case ContactDataKinds.GroupMembership.KIND:
//        		   readGroupMembershipFromDataRaw(cursor,group);
//        		   break;
//        	       case ContactDataKinds.Relation.KIND:
//        		   readRelationFromDataRaw(cursor,group);
//        		   break;
            }
        }

    }

    private static void readStructuredPostalFromDataRaw(Cursor cursor, ContactGroup group) {
        DataArgs postal = new DataArgs(ContactDataKinds.Addresses.Postal.KIND);

        //get the formated address
        int type = cursor.getInt(cursor.getColumnIndex(CommonDataKinds.StructuredPostal.TYPE));
        postal.addValue(ContactDataKinds.Addresses.Postal.TYPE, type);

        //get the formated address
        String streetAddress = cursor.getString(cursor.getColumnIndex(Entity.DATA4));
        postal.addValue(ContactDataKinds.Addresses.Postal.STREET, streetAddress);

        //get the PoBox
        String pobox = cursor.getString(cursor.getColumnIndex(Entity.DATA5));
        postal.addValue(ContactDataKinds.Addresses.Postal.POBOX, pobox);

        //get the neighborhood
        String neighborhood = cursor.getString(cursor.getColumnIndex(Entity.DATA6));
        postal.addValue(ContactDataKinds.Addresses.Postal.NEIGHBORHOOD, neighborhood);

        //get the city
        String city = cursor.getString(cursor.getColumnIndex(Entity.DATA7));
        postal.addValue(ContactDataKinds.Addresses.Postal.CITY, city);

        //get the region
        String region = cursor.getString(cursor.getColumnIndex(Entity.DATA8));
        postal.addValue(ContactDataKinds.Addresses.Postal.REGION, region);

        //get the post code
        String postcode = cursor.getString(cursor.getColumnIndex(Entity.DATA9));
        postal.addValue(ContactDataKinds.Addresses.Postal.POSTCODE, postcode);

        //get the country
        String country = cursor.getString(cursor.getColumnIndex(Entity.DATA10));
        postal.addValue(ContactDataKinds.Addresses.Postal.COUNTRY, country);

        //get the formmated address
        String formattedAddress = cursor.getString(cursor.getColumnIndex(StructuredPostal.FORMATTED_ADDRESS));
        postal.addValue(ContactDataKinds.Addresses.Postal.FORMATTED_ADDRESS, formattedAddress);

        group.addData(postal);
    }

//	/**
//	 * Read and fill group with event from given cursor
//	 * @param cursor (Cursor != null ) the
//	 * @param group
//	 */
//	private static void readEventFromDataRaw(Cursor cursor,ContactGroup group)
//	{
//	    //get the start date
//	    String eventStartDate = cursor.getString(cursor.getColumnIndex(Entity.DATA1));
//
//	    //get the event type
//	    int eventType = cursor.getInt(cursor.getColumnIndex(Entity.DATA2));
//
//	    //get the event description
//	    String eventLabel = cursor.getString(cursor.getColumnIndex(Entity.DATA3));
//
//	    group.addData(ContactDataKinds.Event.KIND,ContactDataKinds.Event.TYPE , new Event(eventType, eventStartDate, eventLabel));
//	    
//	}

    private static void readNoteFromDataRaw(Cursor cursor, ContactGroup group) {
        //get the note
        String noteText = cursor.getString(cursor.getColumnIndex(Entity.DATA1));
        if (noteText != null && noteText.length() > 0) {
            DataArgs args = new DataArgs(ContactDataKinds.Note.KIND);
            args.addValue(ContactDataKinds.Note.NOTE_TEXT, noteText);
            group.addData(args);
        }

    }

//	private static void readImFromDataRaw(Cursor cursor, ContactGroup group)
//	{
//	    // TODO Auto-generated method stub
//	    
//	}

//	private static void readOrganizationFromDataRaw(Cursor cursor,ContactGroup group)
//	{
//	    
//	    //get the company name
//	    String companyName = cursor.getString(cursor.getColumnIndex(Entity.DATA1));
//	   
//	    //get the organiztion type
//	    int orgType = cursor.getInt(cursor.getColumnIndex(Entity.DATA2));
//	    
//	    //get the org title
//	    String titleOrg = cursor.getString(cursor.getColumnIndex(Entity.DATA4));	
//		
//	   //add company to data
//	    group.addData(ContactDataKinds.Organization.KIND, ContactDataKinds.Organization.TYPE, new Organization(orgType, companyName, titleOrg));
//	    
//    
//	}

//	private static void readGroupMembershipFromDataRaw(Cursor cursor,ContactGroup group)
//	{
//	    // TODO Auto-generated method stub
//	    
//	}

//	private static void readRelationFromDataRaw(Cursor cursor,ContactGroup group)
//	{
//	    // TODO Auto-generated method stub
//	    
//	}

    private static void readWebsiteFromDataRaw(Cursor cursor, ContactGroup group) {
        //get the URL
        String websiteUrl = cursor.getString(cursor.getColumnIndex(Entity.DATA1));
        if (websiteUrl != null) {
            int type = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.TYPE));
            DataArgs args = new DataArgs(ContactDataKinds.Website.KIND);
            args.addValue(ContactDataKinds.Website.TYPE, type);
            args.addValue(ContactDataKinds.Website.URL, websiteUrl);
            group.addData(args);
        }
    }

    private static void readNicknameFromDataRaw(Cursor cursor, ContactGroup group) {
        //get the nick name
        String nickName = cursor.getString(cursor.getColumnIndex(Entity.DATA1));
        if (nickName != null && nickName.length() > 0) {
            //get the nickName type
            int nicknameType = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.TYPE));
            DataArgs args = new DataArgs(ContactDataKinds.Nickname.KIND);
            args.addValue(ContactDataKinds.Nickname.TYPE, nicknameType);
            args.addValue(ContactDataKinds.Nickname.NAME, nickName);

            //add nick name to group
            group.addData(args);
        }

    }

    private static void readStructuredNameFromDataRaw(Cursor cursor, ContactGroup group) {
//	    //get the display Name
//	    String displayName = cursor.getString(cursor.getColumnIndex(Entity.DATA1));
//	    if(displayName != null)
//		group.addData(ContactDataKinds.StructuredName.KIND, ContactDataKinds.StructuredName.DISPLAY_NAME, displayName);
//	    
//	    //get the given name
//	    String givenName = cursor.getString(cursor.getColumnIndex(Entity.DATA2));
//	    if(givenName != null)
//		group.addData(ContactDataKinds.StructuredName.KIND, ContactDataKinds.StructuredName.GIVEN_NAME, givenName);
//	    
//	    //get the family name
//	    String familyName = cursor.getString(cursor.getColumnIndex(Entity.DATA3));
//	    if(familyName != null)
//		group.addData(ContactDataKinds.StructuredName.KIND, ContactDataKinds.StructuredName.FAMILY_NAME, familyName);
//	    
//	    //get the prefix name
//	    String prefixName = cursor.getString(cursor.getColumnIndex(Entity.DATA4));
//	    if(prefixName != null)
//		group.addData(ContactDataKinds.StructuredName.KIND, ContactDataKinds.StructuredName.PREFIX, prefixName);
//	    
//	    //get the middle name
//	    String middleName = cursor.getString(cursor.getColumnIndex(Entity.DATA5));
//	    if(middleName != null)
//		group.addData(ContactDataKinds.StructuredName.KIND, ContactDataKinds.StructuredName.MIDDLE_NAME, middleName);
//	    
//	    //get the suffix name
//	    String suffixName = cursor.getString(cursor.getColumnIndex(Entity.DATA6));
//	    if(suffixName != null)
//		group.addData(ContactDataKinds.StructuredName.KIND, ContactDataKinds.StructuredName.SUFFIX, suffixName);

    }

    private static void readEmailFromDataRaw(Cursor cursor, ContactGroup group) {
        //get the email address
        String emailAddress = cursor.getString(cursor.getColumnIndex(Entity.DATA1));
        if (emailAddress != null) {
            //get the email type
            int emailType = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
            DataArgs args = new DataArgs(ContactDataKinds.Addresses.Email.KIND);
            args.addValue(ContactDataKinds.Addresses.Email.TYPE, emailType);
            args.addValue(ContactDataKinds.Addresses.Email.DATA, emailAddress);

            group.addData(args);
        }

    }

    private static void readPhoneFromDataRaw(Cursor cursor, ContactGroup group) {
        //get the phone number
        String phoneNumber = cursor.getString(cursor.getColumnIndex(Entity.DATA1));
        if (phoneNumber != null) {
            //get the phone type
            int phoneType = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
            DataArgs args = new DataArgs(ContactDataKinds.Phone.KIND);
            args.addValue(ContactDataKinds.Phone.TYPE, phoneType);
            args.addValue(ContactDataKinds.Phone.NUMBER, phoneNumber);

            group.addData(args);
        }

    }

    /**
     * @param mimeType (String) - the mime type of data
     * @return the data kind id of the given mime type , return 0 for not valid mime type
     */
    private static int getContactDataKind(String mimeType) {
        if (ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(mimeType))
            return ContactDataKinds.Phone.KIND;
        if (ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE.equals(mimeType))
            return ContactDataKinds.Addresses.Email.KIND;
        if (ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE.equals(mimeType))
            return ContactDataKinds.StructuredName.KIND;
        if (ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE.equals(mimeType))
            return ContactDataKinds.Addresses.Postal.KIND;
        if (ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE.equals(mimeType))
            return ContactDataKinds.Nickname.KIND;
//	    if(ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE.equals(mimeType))
//		return ContactDataKinds.Event.KIND;
        if (ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE.equals(mimeType))
            return ContactDataKinds.Note.KIND;
        if (ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE.equals(mimeType))
            return ContactDataKinds.Im.KIND;
        if (ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE.equals(mimeType))
            return ContactDataKinds.Organization.KIND;
        if (ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE.equals(mimeType))
            return ContactDataKinds.GroupMembership.KIND;
//	    if(ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE.equals(mimeType))
//		return ContactDataKinds.Relation.KIND;
        if (ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE.equals(mimeType))
            return ContactDataKinds.Website.KIND;


        return 0;
    }


    /**
     * Get groups from cursor
     *
     * @param cursorContactGroups (Cursor) - the cursor with the contact's groups
     * @return list of groups
     */
    private static Vector<ContactGroup> getGroups(Cursor cursorContactGroups) {
        //create empty list of groups
        Vector<ContactGroup> groups = new Vector<ContactGroup>();

        if (cursorContactGroups == null)
            //return empty list of groups
            return groups;


        //if there is some data
        if (cursorContactGroups.moveToFirst()) {
            //traverse over all data
            do {
                //get group id
                Long groupID = cursorContactGroups.getLong(cursorContactGroups.getColumnIndex(RawContacts._ID));

                //get contact's group name
                String groupName = cursorContactGroups.getString(cursorContactGroups.getColumnIndex(RawContacts.ACCOUNT_NAME));

                //get contact's group type
                String groupType = cursorContactGroups.getString(cursorContactGroups.getColumnIndex(RawContacts.ACCOUNT_TYPE));

                //create new contact group
                ContactGroup cg = new ContactGroup(groupType, groupName);
                cg.setGroupId(groupID);

                //#ifdef DEBUG
                Utils.debug("RepositoryHelperForOS2_0.getGroups() - groupName=" + groupName + "groupType=" + groupType);
                //#endif

                //add group to list
                groups.add(cg);
            }
            while (cursorContactGroups.moveToNext());

            //close the cursor
            cursorContactGroups.close();
        }


        return groups;

    }


    public static Vector<ContactGroup> getContactDetails(String contactId) {
        ContentResolver cr = BasicApplication.getApplication().getContentResolver();

        //get cursor with all raw contacts in a Contact
        Cursor cursorContactGroups = cr.query(
                RawContacts.CONTENT_URI,
                new String[]{RawContacts._ID, RawContacts.ACCOUNT_NAME, RawContacts.ACCOUNT_TYPE},
                RawContacts.CONTACT_ID + "=?",
                new String[]{String.valueOf(contactId)},
                null);

        //get the groups from the cursor
        Vector<ContactGroup> contactgroups = getGroups(cursorContactGroups);

        //for each Contact's group
        for (ContactGroup group : contactgroups) {
            Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, group.getID());
            Uri entityUri = Uri.withAppendedPath(rawContactUri, Entity.CONTENT_DIRECTORY);
            Cursor cursorContact = cr.query(
                    entityUri,
                    new String[]{RawContacts.SOURCE_ID,
                            Entity.DATA_ID,
                            Entity.MIMETYPE,
                            Entity.DATA1,
                            Entity.DATA2,
                            Entity.DATA3,
                            Entity.DATA4,
                            Entity.DATA5,
                            Entity.DATA6,
                            Entity.DATA7,
                            Entity.DATA8,
                            Entity.DATA9,
                            Entity.DATA10
                    },
                    null, null, null);

            try {
                //if there is some data
                if (cursorContact.moveToFirst()) {
                    //traverse over all data
                    do {
//						// get a uniquely identifies this row to its source account.
//						String sourceId = cursorContact.getString(0);

                        // if there is available data
                        if (!cursorContact.isNull(1)) {
                            // get the data mime type
                            String mimeType = cursorContact.getString(2);

                            // save the raw data in contact group
                            saveContactDataRaw(group, mimeType, cursorContact);
                        }
                    }
                    while (cursorContact.moveToNext());
                }
            } catch (Exception e) {
                //#ifdef ERROR
                Utils.error("RepositoryHelperForOS2_0.getContactDetails() - Error:" + e);
                //#endif
            } finally {
                cursorContact.close();
            }
        }

        return contactgroups;
    }

    /**
     * sets contact to favorites
     *
     * @param String contactID, boolean setAsFavorite
     * @return true if success
     */
    public static boolean setContactFavoriteIndication(String contactID, boolean setAsFavorite) {
        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contactID));

        //set content value to update contact's 'favorite' indication
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactsContract.Contacts.STARRED, setAsFavorite ? 1 : 0);

        //update the relevant row in the contacts table
        return (BasicApplication.getApplication().getContentResolver().update(uri, contentValues, null, null) == 1);
    }
}
