package com.cellebrite.ota.socialphonebook.model;

import java.io.Serializable;

/**
 * Represents a social network status.
 */
public class SocialNetworkStatus implements Serializable {
    //holds status' ID in the social network it belongs to
    private String m_id = null;

    //holds status' social network name
    private String m_socialNetworkName = null;

    //holds status' text
    private String m_text = null;

    //holds status' time in milliseconds
    private long m_time;

    //holds the contact ID
    private String m_contactId = null;

    /**
     * Social Network Status constructor.
     *
     * @param id                (String != null) status ID in the social network it belongs to.
     * @param socialNetworkName (String != null) status' social network name.
     * @param text              (String != null) status' text.
     * @param time              (long) status' time in milliseconds.
     */
    public SocialNetworkStatus(String id, String contactID, String socialNetworkName, String text, long time) {
        m_id = id;
        m_contactId = contactID;
        m_socialNetworkName = socialNetworkName;
        m_text = text;
        m_time = time;
    }

    /**
     * Gets status' ID.
     *
     * @return (String != null) status' ID.
     */
    public String getID() {
        return m_id;
    }

    /**
     * Gets the contact's ID.
     *
     * @return (String != null) contact ID.
     */
    public String getContactID() {
        return m_contactId;
    }

    /**
     * Gets status' social network name.
     *
     * @return (String != null) status' social network name.
     */
    public String getSocialNetworkName() {
        return m_socialNetworkName;
    }

    /**
     * Gets status' text.
     *
     * @return (String != null) status' text.
     */
    public String getText() {
        return m_text;
    }

    /**
     * Gets status' time.
     *
     * @return (long >= 0) status' time in milliseconds.
     */
    public long getTime() {
        return m_time;
    }
}
