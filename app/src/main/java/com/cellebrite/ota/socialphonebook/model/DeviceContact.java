package com.cellebrite.ota.socialphonebook.model;
//package com.vario.model;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.UnsupportedEncodingException;
//import java.util.ArrayList;
//import java.util.Vector;
//
//import com.vario.application.ContactListActivity;
//import com.vario.deviceData.DeviceContactAddressesManager;
//import com.vario.deviceData.DeviceContactPhonesManager;
//import com.vario.deviceData.DeviceContactsManager;
//import com.vario.deviceData.DeviceContactAddressesManager.Address;
//import com.vario.deviceData.DeviceContactPhonesManager.PhoneNumber;
//import com.vario.infra.application.ApplicationManager;
//import com.vario.infra.model.DeviceDataElement;
//import com.vario.infra.utils.Base64;
//import com.vario.infra.utils.QuotedPrintable;
//import com.vario.infra.utils.StringUtil;
//import com.vario.infra.utils.Utils;
//import com.vario.services.SyncAndStatusUpdaterService;
//import com.vario.utilities.ApplicationUtilities;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//
///**
// * Represents a device contact.
// * 
// * TODO - logs
// * TODO - phone -number-
// */
//public class DeviceContact extends DeviceDataElement
//{
//    //holds contact's display name
//    private String m_displayName;
//    
//    //holds contact's photo
//    private Bitmap m_smallPhoto = null;
//    
//  //holds contact's photo
//    private Bitmap m_largePhoto = null;
//    
//    //holds contact's photo data (as byte stream, not encoded)
//    private byte[] m_actualPhotoData = null;
//    
//    //holds contact's server ID
//    private String m_serverID = null;
//    
//    //holds whether the contact is a lazy contact (only partial data exist for him)
//    private boolean b_isLazyContact = false;
//	
//    //holds contact's first name
//    private String m_firstName;
//    
//    //holds contact's last name
//    private String m_lastName;
//    
//    //holds contact's home phone
//    //TODO - in an android device there can be 2 home phones, should this member be a collection ?
//    private Vector<String> m_homePhones = null;
//    
//    //holds contact's mobile phone
//    private Vector<String> m_mobilePhones;
//    
//    //holds contact's work phone
//    private Vector<String> m_workPhones;
//
//    //holds contact's home faxes
//    private Vector<String> m_homeFaxes;
//
//    //holds contact's home faxes
//    private Vector<String> m_workFaxes;
//    
//    //holds contact's other phone
//    private String m_otherPhone;
//    
//    //holds contact's home email
//    private Vector<String> m_homeEmails;
//    
//    //holds contact's work email
//    private Vector<String> m_workEmails;
//    
//    //holds contact's other email
//    private Vector<String> m_otherEmails;
//    
//    //hold's contact's home address
//    private String m_homeAddress;
//    
//    //holds contact's work address
//    private String m_workAddress;
//    
//    //holds contact's other address
//    private String m_otherAddress;
//    
//    //holds contact's company name
//    private String m_company;
//    
//    //holds contact's title
//    private String m_title;
//    
//    //holds contact's notes
//    private String m_note;
//
//    //holds indication whether the contact is in the favorite list
//    private boolean b_isInFavoriteList = false;
//    
//    //holds if the contact was deleted 
//    private boolean b_wasDeleted = false;
//
//	//holds 'end of line' string
//    private static final String NL = "\n";
//    
//    //holds the maximum number of chars in a single vCard line
//    //TODO - move to application's params
//    private int m_vCardLineMaxChars = 70;
//    
//    //holds contact's online status in a social network
//    private String m_onlineStatus = null;
//    
//    //holds contact's last social network status
//    private SocialNetworkStatus m_lastSocialNetworkStatus = null;
//    
//    //holds contact's social network activities
//    private Vector<SocialNetworkActivity> m_socialNetworkActivities = null;
//    
//    //holds a default contact photo
//	public static Bitmap DEFAULT_SMALL_CONTACT_PHOTO;
//	
//    //holds a default contact photo data
//	public static byte[] DEFAULT_SMALL_CONTACT_PHOTO_DATA;
//	
//	//holds a default contact photo
//	public static Bitmap DEFAULT_LARGE_CONTACT_PHOTO;
//	
//    //holds a default contact photo data
//	public static byte[] DEFAULT_LARGE_CONTACT_PHOTO_DATA;
//	
//	static
//	{
//		try
//		{
//			//creates the default contact photo (this code runs only ones, upon class creation)
//			DEFAULT_SMALL_CONTACT_PHOTO = BitmapFactory.decodeResource(ApplicationManager.getInstance().getResources(), 
//					 R.drawable.cl_screen_contact_porfile_picture);
//			
//			//holds photo's data
//	    	ByteArrayOutputStream photoByteArrayOutputStream = new ByteArrayOutputStream();
//			
//	    	//turns default photo into byte stream data
//			DEFAULT_SMALL_CONTACT_PHOTO.compress(Bitmap.CompressFormat.PNG, 100, photoByteArrayOutputStream);
//    	
//	    	//returns contact's photo data
//			DEFAULT_SMALL_CONTACT_PHOTO_DATA = photoByteArrayOutputStream.toByteArray();
//			
//		}
//		catch(Throwable throwable)
//		{
//			//#ifdef ERROR
//			Utils.error("DeviceContact.staticInitialization() - cannot create small contact default photo.");
//			//#endif
//		}
//		
//		try
//		{
//			
//			//creates the default contact photo (this code runs only ones, upon class creation)
//			DEFAULT_LARGE_CONTACT_PHOTO = BitmapFactory.decodeResource(ApplicationManager.getInstance().getResources(), 
//					 R.drawable.cd_screen_contact_porfile_picture);
//			
//	    	//and once for the larger photo
//			ByteArrayOutputStream photoByteArrayOutputStream = new ByteArrayOutputStream();
//			
//			//compress to byte stream
//			DEFAULT_LARGE_CONTACT_PHOTO.compress(Bitmap.CompressFormat.PNG, 100, photoByteArrayOutputStream);
//			
//			//set the byte array
//			DEFAULT_LARGE_CONTACT_PHOTO_DATA = photoByteArrayOutputStream.toByteArray();
//		}
//		catch(Throwable throwable)
//		{
//			//#ifdef ERROR
//			Utils.error("DeviceContact.staticInitialization() - cannot create large contact default photo.");
//			//#endif
//		}
//	}
//
//    /**
//     * Device Contact default constructor.
//     * This method is being used by the model manager to create device contacts which their data will be later set
//     * from device's contacts data.
//     */
//    private DeviceContact()
//    {
//    	//creates contact's mobile phone numbers collection
//    	m_mobilePhones = new Vector<String>();
//    	
//    	//creates contact's home phone numbers collection
//    	m_homePhones = new Vector<String>();
//    	
//    	//creates contact's work phone numbers collection
//    	m_workPhones = new Vector<String>();
//
//    	//creates contact's home email addresses collection
//    	m_homeEmails = new Vector<String>();
//    	
//    	//creates contact's work email addresses collection
//    	m_workEmails = new Vector<String>();
//    	
//    	//creates contact's other email addresses collection
//    	m_otherEmails = new Vector<String>();
//
//    	//creates contact's home fax numbers collection
//    	m_homeFaxes = new Vector<String>();
//
//    	//creates contact's work fax numbers collection
//    	m_workFaxes = new Vector<String>();
//    	
//    	//creates contact's social network activities collection
//    	//TODO - set the number of being held activities in application's configurable parameters file
//    	m_socialNetworkActivities = new Vector<SocialNetworkActivity>(3);
//    }
//    
//    /**
//     * Gets a lazy device contact (a contact with only specific details - usually name and photo).
//     * 
//     * @param deviceContactsMananger (DeviceContactsManager != null) a device contacts manager pointing on the contact.
//     */
//    public static DeviceContact getLazyDeviceContact(DeviceContactsManager deviceContactsMananger)
//    {
//    	//creates the lazy device contact
//    	DeviceContact deviceContact = new DeviceContact();
//    	deviceContact.setContactData(deviceContactsMananger);
//    	deviceContact.b_isLazyContact = true;
//    	
//    	//returns the created lazy device contact
//    	return deviceContact;
//    }
//    
//    /**
//     * Sets contact's data - this method set only contact's data for the contact to be a lazy contact.
//     * 
//     * @param deviceContactsMananger (DeviceContactsManager != null) a device contacts manager pointing on the contact.
//     */
//    private void setContactData(DeviceContactsManager deviceContactsMananger)
//    {
//        //gets and sets contact's ID
//        _id = String.valueOf(deviceContactsMananger.getDeviceID());
//
//        //gets and sets contact's display name
//        m_displayName = deviceContactsMananger.getName();
//        if(m_displayName == null)
//        	m_displayName = "";
//        
//        //in case contact's display name was retrieved
//        //TODO - handle first and last name in the contacts manager level!
//        if (!m_displayName.equals(""))
//        {
//        	//gets contact's first and last name from the display name
//        	String[] nameFields = StringUtil.split(m_displayName, ",");
//        	m_lastName = nameFields[0];
//        	if (nameFields.length > 1)
//        		m_firstName = nameFields[1];
//        }
//        else
//        {
//        	//contact has not first and last name
//        	m_lastName = m_firstName = "";
//        	m_displayName = ContactListActivity.getInstance().getResources().getString(R.string.unknown_contact_display_name);
//        }
//
//        //in case favorite indication is positive (1) - set it as true,
//        //otherwise (not in the favorites list or all device's contacts have already been traversed by the manager) - set it to false.
//        b_isInFavoriteList = (deviceContactsMananger.isInFavorites() > 0);
//
//        //set contact's default photo
//        setDefaultPhotoData();
//    }
//        
//    /**
//     * Turns the contact into a full contact (in case he is currently a lazy contact).
//     * In case the contact is already a full contact, nothing will happen.
//     * 
//     * @param deviceContactsManager (DeviceContactsManager != null) a device contact's manager pointing on the contact.
//     * @param deviceContactPhonesManager (DeviceContactPhonesManager != null) a device contact's phones manager pointing on contact's phone numbers.
//     * @param deviceContactAddressesManager (DeviceContactAddressesManager != null) a device contact's addresses manager pointing on contact's addresses.
//     * @param loadPhotoFromDevice (boolean) true in case contact's photo needs to be loaded from the device, false otherwise.
//     */
//    public void turnIntoFullContact(DeviceContactsManager deviceContactsManager,
//    								DeviceContactPhonesManager deviceContactPhonesManager, 
//    								DeviceContactAddressesManager deviceContactAddressesManager,
//    								boolean loadPhotoFromDevice)
//    {
//    	//in case the contact is already a full contact, do nothing
//    	if (!b_isLazyContact)
//    		return;
//    	
//		//sets contact's phone numbers
//    	setContactPhoneNumbers(deviceContactPhonesManager);
//		
//		//sets contact's addresses
//    	setContactAddresses(deviceContactAddressesManager);
//    	
//    	//in case there is a need to load contact's photo from the device
//    	Bitmap contactPhoto;
//    	if(loadPhotoFromDevice && 
//		   ((contactPhoto = ApplicationUtilities.loadContactPhotoFromDevice(deviceContactsManager, this)) != null))
//    	{
//    		setPhoto(contactPhoto);
//    	}
//    	
//		//sets that the contact is no longer a lazy contact
//		b_isLazyContact = false;
//    }
//    
//    /**
//     * Sets contact's phone numbers.
//     * 
//     * @param deviceContactPhonesManager (DeviceContactPhonesManager != null) a device contact's phones manager pointing contact's phone numbers.
//     */
//    private void setContactPhoneNumbers(DeviceContactPhonesManager deviceContactPhonesManager)
//    {
//    	//get all phone by contact id
//    	Vector<PhoneNumber> phoneNumbers = deviceContactPhonesManager.getPhonesByContactId(_id);
//    	
//    	//for each number
//    	for (PhoneNumber number : phoneNumbers)
//    	{
//            //holds the index of the phone number column
//            String phoneNumber = number.getContent();
//            
//    		//in case this is an empty phone number
//    		if(phoneNumber == null)
//    			continue;
//            
//            //holds the phone type
//            int phoneType = number.getType();
//
//            //gets the current phone number according to the phone type,
//            //and stores it in the relevant member 
//            switch(phoneType)
//            {
//            	case(DeviceContactPhonesManager.PhoneTypes.TYPE_MOBILE):
//            	{
//            		m_mobilePhones.add(phoneNumber);
//            		break;
//            	}
//            	case(DeviceContactPhonesManager.PhoneTypes.TYPE_WORK):
//            	{
//            		m_workPhones.add(phoneNumber);
//            		break;
//            	}
//            	case(DeviceContactPhonesManager.PhoneTypes.TYPE_HOME):
//            	{
//            		m_homePhones.add(phoneNumber);
//            		break;
//            	}
//            	case(DeviceContactPhonesManager.PhoneTypes.TYPE_FAX_HOME):
//            	{
//            		m_homeFaxes.add(phoneNumber);
//            		break;
//            	}
//            	case(DeviceContactPhonesManager.PhoneTypes.TYPE_FAX_WORK):
//            	{
//            		m_workFaxes.add(phoneNumber);
//            		break;
//            	}	            	
//            	case(DeviceContactPhonesManager.PhoneTypes.TYPE_OTHER):
//            	{
//            		m_otherPhone = phoneNumber;
//            		break;
//            	}
//            }
//        }
//    }
//    
//    /**
//     * Sets contact's addresses.
//     * 
//     * @param deviceContactAddressesManager (DeviceContactAddressesManager != null) a device contact's addresses manager pointing contact's addresses.
//     */
//    private void setContactAddresses(DeviceContactAddressesManager deviceContactAddressesManager)
//    {
//    	//gets the address
//		Vector<Address> addresses = deviceContactAddressesManager.getAddressesByContactId(_id);
//		
//		//loop over all addresses
//		for (Address address : addresses)
//		{
//			//gets current address kind
//	        int addressKind = address.getKind();
//			
//			//gets the address type
//			int addressType = address.getType();
//	        
//	    	//in case current address is an email address
//	    	if (addressKind == DeviceContactAddressesManager.AddressKinds.KIND_EMAIL)
//	    	{
//	            //sets the current email address in the relevant member 
//	            switch (addressType)
//	            {
//	            	case DeviceContactAddressesManager.AddressTypes.TYPE_HOME:
//	            	{
//	            		m_homeEmails.add(address.getContent());
//	            		break;
//	            	}
//	            	case DeviceContactAddressesManager.AddressTypes.TYPE_WORK:
//	            	{
//	            		m_workEmails.add(address.getContent());
//	            		break;
//	            	}
//	            	case DeviceContactAddressesManager.AddressTypes.TYPE_OTHER:
//	            	{
//	            		m_otherEmails.add(address.getContent());
//	            		break;
//	            	}
//	            }
//	            
//	            //TODO - in case of an email address which its type doesn't match any type, what to do with the address ?
//	            //break;
//	    	}
//	    	
//	    	//in case current address is postal address
//	    	if(addressKind == DeviceContactAddressesManager.AddressKinds.KIND_POSTAL)
//	    	{
//	            //sets the current postal address in the relevant member 
//	            switch(addressType)
//	            {
//	            	case(DeviceContactAddressesManager.AddressTypes.TYPE_HOME):
//	            	{
//	            		m_homeAddress = address.getContent();
//	            		break;
//	            	}
//	            	case(DeviceContactAddressesManager.AddressTypes.TYPE_WORK):
//	            	{
//	            		m_workAddress = address.getContent();
//	            		break;
//	            	}
//	            	case(DeviceContactAddressesManager.AddressTypes.TYPE_OTHER):
//	            	{
//	            		m_otherAddress = address.getContent();
//	            		break;
//	            	}
//	            }
//	    	}
//		}
//    }    
//    
//    /**
//     * Sets contact's photo.
//     * This method will be used by application's photo loader, to set contact's photo from the device.
//     * The method will set contact's photo data based on the given photo's data.
//     * 
//     * @param photo (Bitmap != null) a bitmap photo to set for this contact.
//     */
//    public void setPhoto(Bitmap photo)
//    {
//    	m_largePhoto = photo;
//    	
//    	//holds photo's data
//    	ByteArrayOutputStream photoByteArrayOutputStream = new ByteArrayOutputStream();
//		
//    	//turns default photo into byte stream data
//    	photo.compress(Bitmap.CompressFormat.PNG, 100, photoByteArrayOutputStream);
//	
//    	//returns contact's photo data
//		m_actualPhotoData = photoByteArrayOutputStream.toByteArray();
//		
//		//create the small photo from the data also
//		m_smallPhoto = ApplicationUtilities.createDeviceContactScaledPhoto(new ByteArrayInputStream(m_actualPhotoData), 
//				DEFAULT_SMALL_CONTACT_PHOTO.getWidth(), DEFAULT_SMALL_CONTACT_PHOTO.getHeight());
//    }
//    
//    /**
//     * Sets contac's photo data.
//     * This method will be used by the vCard parser for the contact, to set contact's photo from the received vCard.
//     * The method will set contact's photo based on the given photo's data.
//     *  
//     * @param photoData (byte[] ) -the photo data if exist , if not exist the default photo would be set instead
//     */
//    public void setPhotoData(byte[] photoData)
//    {
//    	//need to set default photo instead of current photo
//    	if (photoData == null)
//    	{
//    		//set the default photo
//    		setDefaultPhotoData();
//    	}
//    	else{
//    	
//	    	//holds the photo data
//	    	m_actualPhotoData = photoData;
//	    	
//	    	//creates contac's photo based on the given photo's data and sets it as contact's photo
//	    	ByteArrayInputStream photoInputStream = new ByteArrayInputStream(photoData);
//	    	m_smallPhoto = ApplicationUtilities.createDeviceContactScaledPhoto(photoInputStream, DEFAULT_SMALL_CONTACT_PHOTO.getWidth(), DEFAULT_SMALL_CONTACT_PHOTO.getHeight());
//	    	
//	    	//creates contac's photo based on the given photo's data and sets it as contact's photo
//	    	photoInputStream = new ByteArrayInputStream(photoData);
//	    	m_largePhoto = ApplicationUtilities.createDeviceContactScaledPhoto(photoInputStream, DEFAULT_LARGE_CONTACT_PHOTO.getWidth(), DEFAULT_LARGE_CONTACT_PHOTO.getHeight());
//    	}
//    }    
//    
//    /**
//     * set all the device contact photo with default photos
//     */
//    private void setDefaultPhotoData() 
//    {
//        m_smallPhoto = DEFAULT_SMALL_CONTACT_PHOTO;
//        m_largePhoto = DEFAULT_LARGE_CONTACT_PHOTO;
//        m_actualPhotoData = DEFAULT_LARGE_CONTACT_PHOTO_DATA;
//		
//	}
//
//	/**
//     * Creates a device contact based on a given vCard string.
//     * 
//     * @param vCardString (String != null) the vCard string to create the contact from.
//     * 
//     * @return (DeviceContact) the created device contact based on the given vCard string,
//     * 						   or null in case of an error.
//     */
//    public static DeviceContact getFullDeviceContactFromVCard(String vCardString)
//    {
//    	try
//    	{
//	    	//creates a new device contact
//	    	DeviceContact deviceContact = new DeviceContact();
//	    	
//	    	//parses the given vCard for the new contact
//	    	deviceContact.parseVCardString(vCardString);
//	    	
//	    	//returns the created device contact
//	    	return deviceContact;
//    	}
//    	catch(Throwable throwable)
//    	{
//    		//#ifdef ERROR
//    		Utils.error("DeviceContact.createDeviceContactFromVCard() - " + 
//    					"an error has occurred - " + throwable +
//    					", creating a device contact from a given vCard failed.");	
//    		//#endif
//    		
//    		//creating the new contact for the given vCard failed
//    		return null;
//		}
//    }
//    
//    /**
//     * Parses a vCard string to contact's data.
//     * TODO - all vCard string should be declared in a private class ?
//     * TODO - this method should throw an exception in case it fails in parsing the given vCard.
//     * 
//     * @param vCardString (String != null) contact's vCard string.
//     */
//    private void parseVCardString(String vCardString)
//    {
//    	//gets search start index (right after the vCard begin string)
//        int searchStartIndex = vCardString.indexOf("BEGIN:VCARD");
//        if (searchStartIndex == -1)
//            return;
//        
//        searchStartIndex += "BEGIN:VCARD".length();
//
//        //holds vCard string's length
//        int vCardStringLength = vCardString.length();
//        
//        //holds the next new line index in the vCard string
//        int newLineIndex = vCardString.indexOf(NL, searchStartIndex);
//        
//        //holds new line string's length
//        int newLineStringLength = NL.length();
//
//        //as long as the vCard string contains another new line string
//        //TODO - why the second condition is necessary
//        while (newLineIndex != -1 && newLineIndex <= vCardStringLength)
//        {
//            //gets the index of the next colon in the vCard string
//            int colonIndex = vCardString.indexOf(':', newLineIndex);
//            
//            //gets the name of the current vCard field (substring from the beginning of the line till the colon)
//            String currentFieldName = vCardString.substring(newLineIndex + newLineStringLength, ++colonIndex);
//
//            //holds the next new line index in the vCard string, to know where current field's value ends
//            newLineIndex = vCardString.indexOf(NL, colonIndex);
//            
//            //in case current field's value continues to the next vCard string row
//            while(newLineIndex != - 1 && newLineIndex != (vCardStringLength - 1) && vCardString.charAt(newLineIndex + 1) == ' ')
//            {
//                //gets the next new line index in the vCard string, to know where current field's value ends
//                newLineIndex = vCardString.indexOf(NL, newLineIndex + 1);
//            }
//
//            //gets the value of the current vCard field
//            //(substring from the first character after the found colon, till the new line)
//            String currentFieldValue = vCardString.substring(colonIndex, newLineIndex);
//
//            //in case the current handled field is vCard's END field
//            if (currentFieldName.equals("END:"))
//                break;
//
//            //in case the current handled field is vCard's full name field
//        	if (currentFieldName.equals("FN:"))
//            {
//        		//sets contact's display name
//                m_displayName = currentFieldValue;
//            }
//        	//in case the current handled field is vCard's full name field, decoded in quoted printable
//            else if (currentFieldName.equals("FN;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:"))
//            {
//                try
//                {
//                	//decodes field's value and sets the result as contact's display name
//                    String decodedFieldValue = QuotedPrintable.decode(currentFieldValue.getBytes("UTF-8"), "UTF-8");
//                    m_displayName = decodedFieldValue;
//                }
//                catch(UnsupportedEncodingException uee)
//                {
//                	//TODO - exception should be thrown ?
//                }
//            }
//        	//in case the current handled field is vCard's name field
//            else if (currentFieldName.equals("N:"))
//            {
//            	//splits field's value to contact's first and last name
//                String[] firstAndLastNames = StringUtil.split(currentFieldValue, ";");
//                
//                //Note: we set only the first given name. t
//                //		he others are ignored in input and will not be overridden on the server in output
//                m_firstName = firstAndLastNames[1];
//                m_lastName = firstAndLastNames[0];
//
//            }
//        	//in case the current handled field is vCard's name field, decoded in quoted printable        	
//        	else if (currentFieldName.equals("N;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:"))
//            {
//            	try
//                {
//            		//decodes field's value and sets the result as contact's first and last names after splitting it
//                    String decodedFieldValue = QuotedPrintable.decode(currentFieldValue.getBytes("UTF-8"), "UTF-8");
//                    String[] firstAndLastNames = StringUtil.split(decodedFieldValue, ";");
//                    
//                    //Note: we set only the first given name. t
//                    //		he others are ignored in input and will not be overridden on the server in output
//                    m_firstName = firstAndLastNames[1];
//                    m_lastName = firstAndLastNames[0];
//                }
//            	catch (UnsupportedEncodingException uee)
//            	{
//            		//TODO - exception should be thrown ?
//                }
//            }
//
//            //in case the current handled field is vCard's mobile telephone number field
//        	else if (currentFieldName.equals("TEL;CELL:") || currentFieldName.equals("TEL;VOICE;CELL:") || currentFieldName.equals("TEL;CELL;VOICE:"))
//            { 
//        		//sets contact's mobile phone number
//                m_mobilePhones.add(currentFieldValue);
//            }
//        	//in case the current handled field is vCard's home telephone number field
//            else if (currentFieldName.equals("TEL;HOME:") || currentFieldName.equals("TEL;VOICE;HOME:") || currentFieldName.equals("TEL;HOME;VOICE:"))
//            { 
//            	//adds the number to contact's home phone numbers collection
//                m_homePhones.add(currentFieldValue);
//            }
//        	//in case the current handled field is vCard's work telephone number field
//        	else if (currentFieldName.equals("TEL;WORK:") || currentFieldName.equals("TEL;VOICE;WORK:") || currentFieldName.equals("TEL;WORK;VOICE:"))
//            { 
//        		//sets contact's work phone number
//                m_workPhones.add(currentFieldValue);
//            }
//        	//in case the current handled field is vCard's home fax telephone number field
//            else if (currentFieldName.equals("TEL;HOME;FAX:"))
//            { 
//            	//sets contact's home fax phone number
//                m_homeFaxes.add(currentFieldValue);
//            }
//        	//in case the current handled field is vCard's work fax telephone number field
//            else if (currentFieldName.equals("TEL;WORK;FAX:"))
//            { 
//            	//sets contact's work fax phone number
//                m_workFaxes.add(currentFieldValue);
//            }        	
//            //in case the current handled field is vCard's other telephone number field
//        	else if (currentFieldName.equals("TEL:") || currentFieldName.equals("TEL;VOICE:"))            	
//            {
//        		//sets contact's other phone number
//                m_otherPhone = currentFieldValue;
//            }            
//            //in case the current handled field is vCard's home email address field
//            else if (currentFieldName.equals("EMAIL;HOME:") || currentFieldName.equals("EMAIL;INTERNET;HOME:"))
//            {
//            	//sets contact's home email address
//            	//TODO - email validation was done here in Celleshare, should it be done ?
//            	m_homeEmails.add(currentFieldValue);
//            }            
//            //in case the current handled field is vCard's work email address field
//        	else if (currentFieldName.equals("EMAIL;WORK:") || currentFieldName.equals("EMAIL;INTERNET;WORK:"))
//            {
//            	//sets contact's work email address
//            	//TODO - email validation was done here in Celleshare, should it be done ?
//        		m_workEmails.add(currentFieldValue);
//            }
//            //in case the current handled field is vCard's other email address field
//        	else if (currentFieldName.equals("EMAIL:") || currentFieldName.equals("EMAIL;INTERNET:"))
//            { 
//            	//sets contact's work email address
//            	//TODO - email validation was done here in Celleshare, should it be done ?
//                m_otherEmails.add(currentFieldValue);
//            }
//            
//            //in case the current handled field is vCard's home address field
//        	else if (currentFieldName.equals("ADR;HOME:"))
//            {
//        		//splits field's value to contact's home address fields
//                String[] homeAddressFields = StringUtil.split(currentFieldValue, ";");
//                m_homeAddress = homeAddressFields[2] + ", "
//                        + homeAddressFields[3] + ", " 
//                        + homeAddressFields[4] + ", " 
//                        + homeAddressFields[5] + ", "
//                        + homeAddressFields[6];
//            }
//        	//in case the current handled field is vCard's home address field, decoded in quoted printable
//        	else if (currentFieldName.equals("ADR;HOME;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:"))
//        	{
//                try
//                {
//                	//decodes field's value and sets the result as contact's home address after splitting it
//                    String decodedFieldValue = QuotedPrintable.decode(currentFieldValue.getBytes("UTF-8"), "UTF-8");
//                    String[] homeAddressFields = StringUtil.split(decodedFieldValue, ";");
//                    m_homeAddress = homeAddressFields[2] + ", "
//                            + homeAddressFields[3] + ", "
//                            + homeAddressFields[4] + ", "
//                            + homeAddressFields[5] + ", "
//                            + homeAddressFields[6];
//                }
//                catch (UnsupportedEncodingException uee)
//                {
//                	//TODO - exception should be thrown ?
//                }
//            }
//            //in case the current handled field is vCard's work address field
//        	else if (currentFieldName.equals("ADR;WORK:"))
//            {
//        		//splits field's value to contact's home address fields
//                String[] workAddressFields = StringUtil.split(currentFieldValue, ";");
//                m_workAddress = workAddressFields[2] + ", "
//                        + workAddressFields[3] + ", " 
//                        + workAddressFields[4] + ", " 
//                        + workAddressFields[5] + ", "
//                        + workAddressFields[6];
//            }
//        	//in case the current handled field is vCard's work address field, decoded in quoted printable
//            else if (currentFieldName.equals("ADR;WORK;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:"))
//            {
//                try
//                {
//                	//decodes field's value and sets the result as contact's work address after splitting it
//                    String decodedFieldValue = QuotedPrintable.decode(currentFieldValue.getBytes("UTF-8"), "UTF-8");
//                    String[] workAddressFields = StringUtil.split(decodedFieldValue, ";");
//                    m_homeAddress = workAddressFields[2] + ", "
//                            + workAddressFields[3] + ", "
//                            + workAddressFields[4] + ", "
//                            + workAddressFields[5] + ", "
//                            + workAddressFields[6];
//                }
//                catch (UnsupportedEncodingException uee)
//                {
//                	//TODO - exception should be thrown ?
//                }
//            }
//            //in case the current handled field is vCard's other address field
//            else if (currentFieldName.equals("ADR:"))
//            {
//        		//splits field's value to contact's other address fields
//                String[] workAddressFields = StringUtil.split(currentFieldValue, ";");
//                m_otherAddress = workAddressFields[2] + ", "
//                        + workAddressFields[3] + ", "
//                        + workAddressFields[4] + ", " 
//                        + workAddressFields[5] + ", "
//                        + workAddressFields[6];
//            }
//        	//in case the current handled field is vCard's other address field, decoded in quoted printable
//            else if (currentFieldName.equals("ADR;WORK;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:"))
//            {
//                try
//                {
//                	//decodes field's value and sets the result as contact's other address after splitting it
//                    String decodedFieldValue = QuotedPrintable.decode(currentFieldValue.getBytes("UTF-8"), "UTF-8");
//                    String[] workAddressFields = StringUtil.split(decodedFieldValue, ";");
//                    m_otherAddress = workAddressFields[2] + ", "
//                            + workAddressFields[3] + ", "
//                            + workAddressFields[4] + ", "
//                            + workAddressFields[5] + ", "
//                            + workAddressFields[6];
//                }
//                catch (UnsupportedEncodingException uee)
//                {
//                	//TODO - exception should be thrown ?
//                }
//            }
//        	//in case the current handled field is vCard's photo field
//            else if (currentFieldName.startsWith("PHOTO"))
//            {
//            	//holds photo's data
//            	StringBuffer photoData = new StringBuffer(currentFieldValue);
//            	
//            	//in case the next character after the next new line string is an empty space character
//            	while(vCardString.charAt(newLineIndex + 1) == ' ')
//            	{
//            		//gets the next line's value (it is still the photo's data)
//            		String morePhotoData = vCardString.substring(newLineIndex + 2, (newLineIndex = vCardString.indexOf(NL, newLineIndex + 2)));
//            		
//            		//adds it to photo's data
//            		photoData.append(morePhotoData);
//            	}
//            	
//            	//sets contact's photo data (after decoding it, since it's encoded in Base64 encoding)
//            	setPhotoData(Base64.decode(photoData.toString().getBytes()));
//            }
//        }
//    }      
//    
//    /**
//     * Sets contact's last social network status.
//     * 
//     * @param lastStatusUpdate (SocialNetworkStatus != null) contact's last social network status.
//     */
//    public void setLastStatusUpdate(SocialNetworkStatus socialNetworkStatus)
//    {
//    	m_lastSocialNetworkStatus = socialNetworkStatus;
//    }
//    
//    /**
//     * Adds contact's social network activity.
//     * 
//     * @param socialNetworkActivity (SocialNetworkActivity != null) contact's social network activitiy.
//     */
//    public void addSocialNetworkActivity(SocialNetworkActivity socialNetworkActivity)
//    {
//    	//adds the social network activity to the collection
//    	m_socialNetworkActivities.add(socialNetworkActivity);
//    }
//    
//    /**
//     * Sets contact's favorite indication (whether or not this contact is in the favorites list).
//     * 
//     * @param isFavorite (boolean) contact's favorite indication.
//     */
//    public void setFavorite(boolean isFavorite)
//    {
//    	b_isInFavoriteList = isFavorite;
//    }    
//    
//    /**
//     * Gets contact's favorite indication (whether or not this contact is in the favorites list).
//     * 
//     * @return (boolean) contact's favorite indication.
//     */
//    public boolean isFavorite()
//    {
//    	return b_isInFavoriteList;
//    }    
//    
//    /**
//     * Gets contact's display name.
//     * 
//     * @return (String) contact's display name.
//     */
//	public String getDisplayName()
//    {
//    	return m_displayName;
//    }
//    
//    /**
//     * Gets contact's first name.
//     * 
//     * @return (String) contact's first name.
//     */
//    public String getFirstName()
//    {
//    	return m_firstName;
//    }
//    
//    /**
//     * Gets contact's last name.
//     * 
//     * @return (String) contact's last name.
//     */
//    public String getLastName()
//    {
//    	return m_lastName;
//    }    
//
//    /**
//     * Gets contact's photo.
//     * 
//     * @return (Bitmap) contact's photo, or null in case the contact has no photo.
//     */
//    public Bitmap getSmallPhoto()
//    {
//    	return m_smallPhoto;
//    }
//    
//    /**
//     * Gets contact's photo.
//     * 
//     * @return (Bitmap) contact's photo, or null in case the contact has no photo.
//     */
//    public Bitmap getLargePhoto()
//    {
//    	return m_largePhoto;
//    }
//    
//    /**
//     * Gets contact's photo data, as byte stream.
//     * 
//     * @return (byte[]) contact's photo data as byte stream, or null in case the contact has no photo.
//     */
//    public byte[] getPhotoData()
//    {
//    	return m_actualPhotoData;
//    }    
//    
//    /**
//     * Gets contact's server ID.
//     * 
//     * @return (String) contact's server ID.
//     */
//    public String getServerID()
//    {
//    	return m_serverID;
//    }
//    
//    /**
//     * Gets contact's last social network status.
//     * 
//     * @return (SocialNetworkStatus) contact's last social network status.
//     */
//    public SocialNetworkStatus getLastSocialNetworkStatus()
//    {
//    	return m_lastSocialNetworkStatus;
//    }
//    
//    /**
//     * Gets contact's social network activities.
//     * 
//     * @return (Vector<SocialNetworkActivities> != null) contact's social network activities.
//     */
//    public Vector<SocialNetworkActivity> getSocialNetworkActivities()
//    {
//    	//TODO - there is a need to return a cloned vector or there is no way the collection
//    	//		 will be updated after the collection will be returned ?
//    	return m_socialNetworkActivities;
//    }
//    
//    /**
//     * Retruns the number of mobile phones for the contact.
//     * 
//     * @return (int >= 0) the number of mobile phones for the contact.
//     */
//    public int getNumberOfMobilePhones()
//    {
//    	return m_mobilePhones.size();
//    }
//    
//    /**
//     * Gets contact's mobile phone.
//     * 
//     * @param mobilePhoneIndex (int) the index of the mobile phone in contact's mobile phones collection.
//     * 
//     * @return (String) contact's mobile phone, or null in case the given index is greater than the number of mobile phones for the contact.
//     */
//    public String getMobilePhone(int mobilePhoneIndex)
//    {
//    	if(mobilePhoneIndex >= m_mobilePhones.size())
//    		return null;
//    	
//    	return m_mobilePhones.elementAt(mobilePhoneIndex);
//    }
//    
//    /**
//     * Retruns the number of home phones for the contact.
//     * 
//     * @return (int >= 0) the number of home phones for the contact.
//     */
//    public int getNumberOfHomePhones()
//    {
//    	return m_homePhones.size();
//    }
//    
//    /**
//     * Gets contact's home phone.
//     * 
//     * @param homePhoneIndex (int) the index of the home phone in contact's home phones collection.
//     * 
//     * @return (String) contact's home phone, or null in case the given index is greater than the number of home phones for the contact.
//     */
//    public String getHomePhone(int homePhoneIndex)
//    {
//    	if(homePhoneIndex >= m_homePhones.size())
//    		return null;
//    	
//    	return m_homePhones.elementAt(homePhoneIndex);
//    }
//    
//    /**
//     * Retruns the number of work phones for the contact.
//     * 
//     * @return (int >= 0) the number of work phones for the contact.
//     */
//    public int getNumberOfWorkPhones()
//    {
//    	return m_workPhones.size();
//    }
//    
//    /**
//     * Gets contact's work phone.
//     * 
//     * @param workPhoneIndex (int) the index of the work phone in contact's work phones collection.
//     * 
//     * @return (String) contact's work phone, or null in case the given index is greater than the number of work phones for the contact.
//     */
//    public String getWorkPhone(int workPhoneIndex)
//    {
//    	if(workPhoneIndex >= m_workPhones.size())
//    		return null;
//    	
//    	return m_workPhones.elementAt(workPhoneIndex);
//    }
//    
//    /**
//     * Gets contact's phone numbers.
//     * 
//     * @return (String[]) contact's phone numbers, or null in case the contact has no phone numbers.
//     */
//    public String[] getPhoneNumbers()
//    {
//    	//gets the amount of phone number for the contact
//    	int amountOfPhoneNumbers = m_mobilePhones.size() + m_homePhones.size() + m_workPhones.size() + m_homeFaxes.size() + m_workFaxes.size();
//
//    	if(m_otherPhone != null && m_otherPhone.length() > 0)
//    		amountOfPhoneNumbers++;
//    	
//    	//in case the contact has no phone numbers, do nothing
//    	if(amountOfPhoneNumbers == 0)
//    		return null;
//    	    	
//    	//stores all contact's phone numbers
//    	String[] phoneNumbers = new String[amountOfPhoneNumbers];
//    	int currentIndex = 0;
//    	for(String phoneNumber : m_mobilePhones)
//    		phoneNumbers[currentIndex++] = phoneNumber;
//
//    	for(String phoneNumber : m_homePhones)
//    		phoneNumbers[currentIndex++] = phoneNumber;
//
//    	for(String phoneNumber : m_workPhones)
//    		phoneNumbers[currentIndex++] = phoneNumber;
//    	
//    	for(String phoneNumber : m_homeFaxes)
//    		phoneNumbers[currentIndex++] = phoneNumber;
//    	
//    	for(String phoneNumber : m_workFaxes)
//    		phoneNumbers[currentIndex++] = phoneNumber;
//    	
//    	if(m_otherPhone != null && m_otherPhone.length() > 0)
//    		phoneNumbers[currentIndex++] = m_otherPhone;
//    	
//    	//returns contact's phone numbers
//    	return phoneNumbers;
//    }
//    
//    /**
//     * Returns the number of home faxes for the contact.
//     * 
//     * @return (int >= 0) the number of home faxes for the contact.
//     */
//    public int getNumberOfHomeFaxes()
//    {
//    	return m_homeFaxes.size();
//    }
//    
//    /**
//     * Gets contact's home fax.
//     * 
//     * @param homeFaxIndex (int) the index of the home fax in contact's home faxes collection.
//     * 
//     * @return (String) contact's home fax, or null in case the given index is greater than the number of home faxes for the contact.
//     */
//    public String getHomeFax(int homeFaxIndex)
//    {
//    	if(homeFaxIndex >= m_homeFaxes.size())
//    		return null;
//    	
//    	return m_homeFaxes.elementAt(homeFaxIndex);
//    }
//    
//    /**
//     * Returns the number of work faxes for the contact.
//     * 
//     * @return (int >= 0) the number of work faxes for the contact.
//     */
//    public int getNumberOfWorkFaxes()
//    {
//    	return m_workFaxes.size();
//    }
//    
//    /**
//     * Gets contact's work fax.
//     * 
//     * @param workFaxIndex (int) the index of the work fax in contact's work faxes collection.
//     * 
//     * @return (String) contact's work fax, or null in case the given index is greater than the number of work faxes for the contact.
//     */
//    public String getWorkFax(int workFaxIndex)
//    {
//    	if(workFaxIndex >= m_workFaxes.size())
//    		return null;
//    	
//    	return m_workFaxes.elementAt(workFaxIndex);
//    }     
//    
//    /**
//     * Gets contact's other phone.
//     * 
//     * @return (String) contact's other phone.
//     */
//    public String getOtherPhone()
//    {
//    	return m_otherPhone;
//    }
//    
//    /**
//     * Returns the number of home email addresses for the contact.
//     * 
//     * @return (int >= 0) the number of home email addresses for the contact.
//     */
//    public int getNumberOfHomeEmails()
//    {
//    	return m_homeEmails.size();
//    }
//    
//    /**
//     * Gets contact's home email.
//     * 
//     * @param homeEmailIndex (int) the index of the home email in contact's home emails collection.
//     * 
//     * @return (String) contact's home email, or null in case the given index is greater than the number of home emails for the contact.
//     */
//    public String getHomeEmail(int homeEmailIndex)
//    {
//    	if(homeEmailIndex >= m_homeEmails.size())
//    		return null;
//    	
//    	return m_homeEmails.elementAt(homeEmailIndex);
//    }
//
//    /**
//     * Returns the number of work email addresses for the contact.
//     * 
//     * @return (int >= 0) the number of work email addresses for the contact.
//     */
//    public int getNumberOfWorkEmails()
//    {
//    	return m_workEmails.size();
//    }
//    
//    /**
//     * Gets contact's work email.
//     * 
//     * @param workEmailIndex (int) the index of the work email in contact's work emails collection.
//     * 
//     * @return (String) contact's work email, or null in case the given index is greater than the number of work emails for the contact.
//     */
//    public String getWorkEmail(int workEmailIndex)
//    {
//    	if(workEmailIndex >= m_workEmails.size())
//    		return null;
//    	
//    	return m_workEmails.elementAt(workEmailIndex);
//    }
//    
//    /**
//     * Returns the number of other email addresses for the contact.
//     * 
//     * @return (int >= 0) the number of other email addresses for the contact.
//     */
//    public int getNumberOfOtherEmails()
//    {
//    	return m_otherEmails.size();
//    }
//
//    /**
//     * Gets contact's other email.
//     * 
//     * @param otherEmailIndex (int) the index of the other email in contact's other emails collection.
//     * 
//     * @return (String) contact's other email, or null in case the given index is greater than the number of other emails for the contact.
//     */
//    public String getOtherEmail(int otherEmailIndex)
//    {
//    	if (otherEmailIndex >= m_otherEmails.size()) 
//    	{
//    		return null;
//    	}
//    	
//    	return m_otherEmails.elementAt(otherEmailIndex);
//    } 
//    
//
//    /**
//     * Gets contact's emails.
//     * 
//     * @return (String[]) contact's emails, or null in case the contact has no emails.
//     */
//    public String[] getEmails()
//    {
//    	//gets the amount of emails for the contact
//    	int amountOfEmails = m_homeEmails.size() + m_workEmails.size() + m_otherEmails.size();
//    	
//    	//in case the contact has no emails, do nothing
//    	if (amountOfEmails == 0) 
//    	{
//    		return null;
//    	}
//    	
//    	//stores all contact's emails
//    	String[] emails = new String[amountOfEmails];
//    	int currentIndex = 0;
//    	for (String email : m_homeEmails) 
//    	{
//    		emails[currentIndex++] = email;
//    	}
//
//    	for (String email : m_workEmails) 
//    	{
//    		emails[currentIndex++] = email;
//    	}
//    	
//    	for (String email : m_otherEmails) 
//    	{
//    		emails[currentIndex++] = email;
//    	}
//
//    	//returns contact's emails
//    	return emails;
//    }    
//    
//    /**
//     * Gets contact's home address.
//     * 
//     * @return (String) contact's home address.
//     */
//    public String getHomeAddress()
//    {
//    	return m_homeAddress;
//    }
//
//    /**
//     * Gets contact's work address.
//     * 
//     * @return (String) contact's work address.
//     */
//    public String getWorkAddress()
//    {
//    	return m_workAddress;
//    }
//    
//    /**
//     * Gets contact's other address.
//     * 
//     * @return (String) contact's other address.
//     */
//    public String getOtherAddress()
//    {
//    	return m_otherAddress;
//    }
//    
//    /**
//     * Returns whether the contact is a lazy contact.
//     * 
//     * @return (boolean) true in case the contact is a lazy contact, false in case the contact is a full contact.
//     */
//    public boolean isLazy()
//    {
//    	return b_isLazyContact;
//    }
//    
//    /**
//     * Gets device contact's vCard string.
//     * TODO - hold tag values in inner class ?
//     * 
//     * @return (String != null) device cotact's vCard.
//     */
//    public String formatVCard()
//    {
//    	//holds the vCard string
//        StringBuffer vCardString = new StringBuffer();
//
//        //adds vCard's BEGIN and VERSION tags
//        vCardString.append("BEGIN:VCARD").append(NL);
//        vCardString.append("VERSION:2.1").append(NL);
//
//        //adds last and first name
//        String fullName = new StringBuffer().append((m_lastName != null) ? m_lastName.replace("\n", "\n ") : "").append(";")
//        								    .append((m_firstName != null) ? m_firstName.replace("\n", "\n ") : "").append(";")
//        								    .append(";").append(";").toString();
//        appendField(vCardString, "N;CHARSET=UTF-8:", fullName, false);
//
//        //adds mobile phones
//        int numberOfMobilePhones = getNumberOfMobilePhones();
//        for(int position = 0; position < numberOfMobilePhones; ++position)
//        	appendField(vCardString, "TEL;CELL;CHARSET=UTF-8:", getMobilePhone(position), false);
//        
//        //adds home phones
//        int numberOfHomePhones = getNumberOfHomePhones();
//        for(int position = 0; position < numberOfHomePhones; ++position)
//        	appendField(vCardString, "TEL;VOICE;HOME;CHARSET=UTF-8:", getHomePhone(position), false);
//        
//        //adds work phones
//        int numberOfWorkPhones = getNumberOfWorkPhones();
//        for(int position = 0; position < numberOfWorkPhones; ++position)
//        	appendField(vCardString, "TEL;VOICE;WORK;CHARSET=UTF-8:", getWorkPhone(position), false);
//
//        //adds home faxes
//        int numberOfHomeFaxes = getNumberOfHomeFaxes();
//        for(int position = 0; position < numberOfHomeFaxes; ++position)
//        	appendField(vCardString, "TEL;HOME;FAX;CHARSET=UTF-8:", getHomeFax(position), false);
//        
//        //adds work faxes
//        int numberOfWorkFaxes = getNumberOfWorkFaxes();
//        for(int position = 0; position < numberOfWorkFaxes; ++position)
//        	appendField(vCardString, "TEL;WORK;FAX;CHARSET=UTF-8:", getWorkFax(position), false);
//        
//        //adds other phone
//    	appendField(vCardString, "TEL;CHARSET=UTF-8:", m_otherPhone, false);
//    	
//        //adds home address
//    	appendField(vCardString, "ADR;HOME;CHARSET=UTF-8:", m_homeAddress, ", ", ";", false);
//        
//        //adds work address
//    	appendField(vCardString, "ADR;WORK;CHARSET=UTF-8:", m_workAddress, ", ", ";", false);
//        
//        //adds other address
//    	appendField(vCardString, "ADR;CHARSET=UTF-8:", m_otherAddress, ", ", ";", false);
//
//        //adds home email addresses
//        int numberOfHomeEmails = getNumberOfHomeEmails();
//        for (int position = 0; position < numberOfHomeEmails; ++position) 
//        {
//        	appendField(vCardString, "EMAIL;HOME;CHARSET=UTF-8:", getHomeEmail(position), false);
//        }
//
//        //adds work email addresses
//        int numberOfWorkEmails = getNumberOfWorkEmails();
//        for (int position = 0; position < numberOfWorkEmails; ++position)
//        {
//        	appendField(vCardString, "EMAIL;WORK;CHARSET=UTF-8:", getWorkEmail(position), false);
//        }
//
//        //adds other email addresses
//        int numberOfOtherEmails = getNumberOfOtherEmails();
//        for (int position = 0; position < numberOfOtherEmails; ++position)
//        {
//        	appendField(vCardString, "EMAIL;CHARSET=UTF-8:", getOtherEmail(position), false);
//        }
//
//        //adds contact's photo (encoded in Base64 encoding) in case it exists
//        if(m_actualPhotoData != null && m_actualPhotoData != DeviceContact.DEFAULT_LARGE_CONTACT_PHOTO_DATA)
//        	appendField(vCardString, "PHOTO;ENCODING=BASE64;TYPE=JPG:", new String(Base64.encode(m_actualPhotoData)), false);
//    	
//        //adds the END tag
//        vCardString.append("END:VCARD").append(NL);
//
//        //returns the vCard string
//        return new String(vCardString);
//    }
//    
//    /**
//	 * Appends a field to vCard's StringBuffer.
//	 * 
//	 * @param vCardStringBuffer (StringBuffer != null) vCard's string buffer.
//	 * @param fieldName (String != null) vCard field's name.
//	 * @param fieldValue (String != null) vCard field's value.
//	 * @param isQuotedPrintable (boolean) true in case the field should be encoded in quoted printable, false otherwise.
//	 */
//    private void appendField(StringBuffer vCardStringBuffer, String fieldName, String fieldValue, boolean isQuotedPrintable)
//    {
//    	//in case there is no value for the current field
//        if(fieldValue == null || fieldValue.trim().equals(""))
//        {
//        	//appends field's name with no value
//        	//vCardStringBuffer.append(fieldName).append("").append(NL);
//        	return;
//        }
//        
//    	//in case the field should be encoded in quoted printable
//    	if(isQuotedPrintable)
//    		fieldValue = QuotedPrintable.encode(fieldValue.replace("\n", "\n "));
//    	
//    	//holds the name with the value
//    	StringBuffer nameAndValue = new StringBuffer(fieldName).append(fieldValue);
//    	
//    	//in case field's length is longer than the maximum number of chars allowed in a single line
//    	if(nameAndValue.length() > m_vCardLineMaxChars)
//    	{
//    		//appends a single line from the field's chars to the output buffer
//    		appendLongFieldSingleLine(nameAndValue, vCardStringBuffer, isQuotedPrintable);
//    		
//    		//as long as the length of the remain field's chars is longer than the maximum number of chars allowed in a single line
//        	while(nameAndValue.length() > m_vCardLineMaxChars)
//        	{
//	    		//appends a single line from the field's chars to the output buffer
//        		appendLongFieldSingleLine(nameAndValue, vCardStringBuffer, isQuotedPrintable);
//        	}
//    	}
//
//    	//in the end of the vCard field, add a new line char so that the next field will start in a new lines
//    	vCardStringBuffer.append(nameAndValue).append(NL);
//    }
//    
//    /**
//	 * Appends a field to vCard's StringBuffer.
//	 * 
//	 * @param vCardStringBuffer (StringBuffer != null) vCard's string buffer.
//	 * @param fieldName (String != null) vCard field's name.
//	 * @param fieldValue (String != null) vCard field's value.
//	 * @param toReplace (String) the string to replace in the field value.
//	 * @param replacement (String) the string to put instead of the string to replace in the field value.
//	 * @param isQuotedPrintable (boolean) true in case the field should be encoded in quoted printable, false otherwise.
//	 */
//    private void appendField(StringBuffer vCardStringBuffer, String fieldName, String fieldValue, String toReplace, String replacement, boolean isQuotedPrintable)
//    {
//    	//in case there is no value for the current field
//        if(fieldValue == null || fieldValue.trim().equals(""))
//        {
//        	//appends field's name with no value
//        	//vCardStringBuffer.append(fieldName).append("").append(NL);
//        	return;
//        }
//        
//    	//replaces the string to replace with the replacement string, if necessary
//    	fieldValue = fieldValue.replace(toReplace, replacement);
//    	
//    	//appends the field with its value to the vCard string
//    	appendField(vCardStringBuffer, fieldName, fieldValue, isQuotedPrintable);
//    }
//    
//    /**
//     * Appends a vCard field's single line to an output buffer.
//     * 
//     * In case the vCard field's remain chars length is longer than the maximum number of allowed chars in vCard single line,
//     * the method appends only the maximum number of allowed chars to the output buffer, and deletes the appended chars
//     * from the field's remain chars.
//     * 
//     * In case the vCard field's remain chars length is not longer than the maximum number of allowed char in vCard single line,
//     * the method appends all of field's remain chars to the output buffer, and deletes the appended chars from the field's remain
//     * chars, which will cause the field's remain chars to be empty.
//     * 
//     * @param fieldRemainChars (StringBuffer != null) the remain chars of the vCard field to append to the output buffer.
//     * @param vCardStringBuffer (StringBuffer != null) vCard's string buffer.
//     * @param isQuotedPrintable (boolean) true in case the field is encoded in quoted printable, false otherwise.
//     */
//    private void appendLongFieldSingleLine(StringBuffer fieldRemainChars, StringBuffer vCardStringBuffer, boolean isQuotedPrintable)
//    {
//		//substrings the maximum number of chars from the beginning of the field, as the current line
//		String currentLine = fieldRemainChars.substring(0, m_vCardLineMaxChars);
//		
//		//appends the current line text to the output
//		vCardStringBuffer.append(currentLine);
//		
//		//in case the field is encoded in quoted printable, adds the '=' char at the end of the current line
//		if(isQuotedPrintable)
//			vCardStringBuffer.append("=");
//
//		//appends a new line to the output
//		vCardStringBuffer.append(NL);
//		
//		//in case the field is not encoded in quoted printable, adds a space char in the beginning of the new line in the output
//		if(!isQuotedPrintable)
//			vCardStringBuffer.append(" ");
//		
//		//deletes the current line's chars from the original field value
//		fieldRemainChars.delete(0, m_vCardLineMaxChars);
//    }
//
//    
//    /**
//     * Sets if the contact was removed from DB.
//     */
//	void markAsDeleted() 
//	{
//		b_wasDeleted = true;		
//	}
//	
//	/**
//	 * Returns true if the contact no longer exists in the DB. 
//	 */
//	boolean wasDeleted()
//	{
//		return b_wasDeleted;
//	}
//}
