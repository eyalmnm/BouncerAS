package com.cellebrite.ota.socialphonebook.repositories.contacts;

import android.graphics.Bitmap;

import com.cellebrite.ota.socialphonebook.model.SocialNetworkStatus;
import com.cellebrite.ota.socialphonebook.utilities.ApplicationUtilities;
import com.em_projects.utils.StringUtil;
import com.em_projects.utils.Utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class represents a contact.
 */
public class DeviceContact {
    /**
     * Holds a constant for null UID.
     */
    static final String NULL_UID = "----";
    //holds a default contact photo
    public static Bitmap DEFAULT_SMALL_CONTACT_PHOTO;
    //holds a default contact photo
    public static Bitmap DEFAULT_LARGE_CONTACT_PHOTO;
    //holds a default contact photo data
    public static byte[] DEFAULT_CONTACT_PHOTO_DATA;
    ;

    static {
        try {
//			//creates the default contact photo (this code runs only ones, upon class creation)
//			DEFAULT_SMALL_CONTACT_PHOTO = BitmapFactory.decodeResource(ApplicationManager.getInstance().getResources(),
//					 R.drawable.cl_screen_contact_porfile_picture);
//
//			//holds photo's data
//	    	ByteArrayOutputStream photoByteArrayOutputStream = new ByteArrayOutputStream();
//
//	    	//turns default photo into byte stream data
//			DEFAULT_SMALL_CONTACT_PHOTO.compress(Bitmap.CompressFormat.PNG, 100, photoByteArrayOutputStream);

        } catch (Throwable throwable) {
            //#ifdef ERROR
            Utils.error("DeviceContact.staticInitialization() - cannot create small contact default photo.");
            //#endif
        }

        try {

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
//			DEFAULT_CONTACT_PHOTO_DATA = photoByteArrayOutputStream.toByteArray();
        } catch (Throwable throwable) {
            //#ifdef ERROR
            Utils.error("DeviceContact.staticInitialization() - cannot create large contact default photo.");
            //#endif
        }
    }

    //holds a bitwise for the contact's state
    int m_contactState = 0;
    //holds the contact UID
    String m_uid = DeviceContact.NULL_UID;
    //holds contact's display name
    String m_displayName;
    //holds contact's first name
    String m_firstName;
    //holds contact's last name
    String m_lastName;
    //holds contact's server ID
    private String m_serverID = null;
    //holds indication whether the contact is in the favorite list
    private boolean b_isInFavoriteList = false;
    //holds contact's online status in a social network
    private String m_onlineStatus = null;
    //holds contact's last social network status
    private SocialNetworkStatus m_lastSocialNetworkStatus = null;
    //holds contact's photo
    private Bitmap m_smallPhoto = null;
    //holds contact's photo
    private Bitmap m_largePhoto = null;
    //holds contact's photo data
    private byte[] m_photoData = null;

    /**
     * Device Contact default constructor.
     * This method is being used by the model manager to create device contacts which their data will be later set
     * from device's contacts data.
     */
    DeviceContact(String id, String displayName, boolean isFavorite) {
        //sets contact's ID
        m_uid = id;

        //gets and sets contact's display name
        m_displayName = displayName;
        if (m_displayName == null)
            m_displayName = "";

        //in case contact's display name was retrieved
        //TODO - handle first and last name in the contacts manager level!
        if (!m_displayName.equals("")) {
            //gets contact's first and last name from the display name
            String[] nameFields = StringUtil.split(m_displayName, ",");
            m_firstName = nameFields[0];
            if (nameFields.length > 1)
                m_lastName = nameFields[1];
        } else {
            //contact has not first and last name
            m_lastName = m_firstName = "";
            m_displayName = "";
        }

        //set whether the contact is in favorite list
        b_isInFavoriteList = isFavorite;

        //set the contact as lazy
        m_contactState = ContactState.LAZY;
    }

    public void setID(String id) {
        m_uid = id;
    }

    /**
     * Sets the default photos to this contact. (uses the DEFAULT_SMALL_CONTACT_PHOTO and DEFAULT_LARGE_CONTACT_PHOTO constants)
     */
    void setDefaultPhoto() {
//    	//set to the small photo
//    	m_smallPhoto = DEFAULT_SMALL_CONTACT_PHOTO;
//
//    	//set to the large photo
//    	m_largePhoto = DEFAULT_LARGE_CONTACT_PHOTO;

//    	//set default photo data
//    	m_photoData = DEFAULT_CONTACT_PHOTO_DATA;

        //set the contact state to photo loaded
        m_contactState = ContactState.PHOTO_LOADED;
    }

    /**
     * Gets contact's photo.
     *
     * @return (Bitmap) contact's photo, or null in case the contact has no photo.
     */
    public Bitmap getSmallPhoto() {
        //return the default photo in case the contact is lazy
        if (m_contactState == ContactState.LAZY)
            return DEFAULT_SMALL_CONTACT_PHOTO;

        return m_smallPhoto;
    }

    /**
     * Gets contact's photo.
     *
     * @return (Bitmap) contact's photo, or null in case the contact has no photo.
     */
    public Bitmap getLargePhoto() {
        //return the default photo in case the contact is lazy
        if (m_contactState == ContactState.LAZY)
            return DEFAULT_LARGE_CONTACT_PHOTO;

        return m_largePhoto;
    }

    /**
     * Returns photo's data.
     *
     * @return (byte[]) Photo's data.
     */
    public byte[] getPhotoData() {
        return m_photoData;
    }

    /**
     * Sets contac's photo data.
     *
     * @param photoData (InutStream) the photo data in a form of input stream.
     */
    public void setPhotoData(InputStream is) {
        //if the input stream is not null
        if (is != null) {
            BufferedInputStream bis = new BufferedInputStream(is, 8 * 1024);

            //creates contac's photo based on the given photo's data and sets it as contact's photo
            m_largePhoto = ApplicationUtilities.createDeviceContactScaledPhoto(bis, DEFAULT_LARGE_CONTACT_PHOTO.getWidth(), DEFAULT_LARGE_CONTACT_PHOTO.getHeight());

            try {
                //close
                bis.close();
            } catch (IOException e) {
                //#ifdef ERROR
                Utils.error("Contact.setPhotoData() - failed closing BufferInputStream:", e);
                //#endif
            }

            //if the large photo has been loaded
            if (m_largePhoto != null)
                //creates contac's photo based on the above photo and sets it as contact's smaller photo
                m_smallPhoto = ApplicationUtilities.createDeviceContactScaledPhoto(m_largePhoto, DEFAULT_SMALL_CONTACT_PHOTO.getWidth(), DEFAULT_SMALL_CONTACT_PHOTO.getHeight());
            else {
                //TODO - ?

                //#ifdef ERROR
                Utils.error("Contact.setPhotoData() - failed loading photo from stream, using the default photos instead");
                //#endif

//	    		m_largePhoto = DEFAULT_LARGE_CONTACT_PHOTO;
//	    		m_smallPhoto = DEFAULT_SMALL_CONTACT_PHOTO;
            }

            //hold output stream to contact's photo
            ByteArrayOutputStream photoByteArrayOutputStream = new ByteArrayOutputStream();

            //compress to byte stream
            m_largePhoto.compress(Bitmap.CompressFormat.PNG, 100, photoByteArrayOutputStream);

            //set the byte array
            m_photoData = photoByteArrayOutputStream.toByteArray();

            //set the contact state to photo loaded
            m_contactState = ContactState.PHOTO_LOADED;
        }
    }

    /**
     * @return (bitwise int) the state of a contact constructed from the inner ContactState class.
     */
    int getContactState() {
        return m_contactState;
    }

    /**
     * Returns the contact unique id.
     *
     * @return (String != null) the contact's UID if the contact has not been saved the UID will be equal to Contact.NULL_UID.
     */
    public String getId() {
        return m_uid;
    }

    /**
     * Gets contact's first name.
     *
     * @return (String) contact's first name.
     */
    public String getFirstName() {
        return m_firstName;
    }

    /**
     * Gets contact's last name.
     *
     * @return (String) contact's last name.
     */
    public String getLastName() {
        return m_lastName;
    }

    /**
     * Sets contact's last social network status.
     *
     * @param lastStatusUpdate (SocialNetworkStatus != null) contact's last social network status.
     */
    public void setLastStatusUpdate(SocialNetworkStatus socialNetworkStatus) {
        m_lastSocialNetworkStatus = socialNetworkStatus;
    }

    /**
     * Gets contact's favorite indication (whether or not this contact is in the favorites list).
     *
     * @return (boolean) contact's favorite indication.
     */
    public boolean isFavorite() {
        return b_isInFavoriteList;
    }

    /**
     * Sets contact's favorite indication (whether or not this contact is in the favorites list).
     *
     * @param isFavorite (boolean) contact's favorite indication.
     */
    void setFavorite(boolean isFavorite) {
        b_isInFavoriteList = isFavorite;
    }

    /**
     * Gets contact's display name.
     *
     * @return (String) contact's display name.
     */
    public String getDisplayName() {
        return m_displayName;
    }

    /**
     * Gets contact's last social network status.
     *
     * @return (SocialNetworkStatus) contact's last social network status.
     */
    public SocialNetworkStatus getLastSocialNetworkStatus() {
        return m_lastSocialNetworkStatus;
    }

    /**
     * Returns a clone of this contact.
     */
    DeviceContact getClone() {
        DeviceContact c = new DeviceContact(m_uid, m_displayName, b_isInFavoriteList);
        c.m_contactState = m_contactState;
        c.m_firstName = m_firstName;
        c.m_lastName = m_lastName;
        c.m_largePhoto = m_largePhoto;
        c.m_lastSocialNetworkStatus = m_lastSocialNetworkStatus;
        c.m_onlineStatus = m_onlineStatus;
        c.m_serverID = m_serverID;
        c.m_smallPhoto = m_smallPhoto;

        return c;
    }

    /**
     * copy the photos of given contact to to this contact.
     *
     * @param contact(Contact != null) the contact to copy photos from.
     */
    void copyPhotos(DeviceContact contact) {
        m_largePhoto = contact.m_largePhoto;
        m_smallPhoto = contact.m_smallPhoto;
        m_contactState = contact.m_contactState;


    }

    /**
     * Holds constants for the possible state of a contact. (bitwise int)
     */
    static class ContactState {
        static final int LAZY = 0x1;
        static final int PHOTO_LOADED = 0x2;
    }

}
