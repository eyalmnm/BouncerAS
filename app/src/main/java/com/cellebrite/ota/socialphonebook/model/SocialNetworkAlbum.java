package com.cellebrite.ota.socialphonebook.model;

/**
 * Represents a social network album.
 */
public class SocialNetworkAlbum {
    //holds album's id
    private String m_id = null;

    //holds album's name
    private String m_name = null;

    /**
     * Social Network Album constructor.
     *
     * @param albumName (String != null) album's name.
     * @param albumId   (String != null) album's id.
     */
    public SocialNetworkAlbum(String albumId, String albumName) {
        m_id = albumId;
        m_name = albumName;
    }

    /**
     * Gets social network album's id.
     *
     * @return (String != null) social network album's id.
     */
    public String getId() {
        return m_id;
    }

    /**
     * Gets social network album's name.
     *
     * @return (String != null) social network album's name.
     */
    public String getName() {
        return m_name;
    }

    /**
     * Gets album's name.
     *
     * @return (String != null) album's name.
     */
    public String toString() {
        return m_name;
    }
}
