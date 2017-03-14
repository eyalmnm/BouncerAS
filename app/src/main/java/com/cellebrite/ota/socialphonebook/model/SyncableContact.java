package com.cellebrite.ota.socialphonebook.model;

/**
 * Represents a syncable contact.
 */
public class SyncableContact extends SyncableDataElement {
    /**
     * Syncable Contact constructor.
     *
     * @param deviceID   (String != null) syncable contact's device ID.
     * @param memoryType (String != null) syncable contact's memory type.
     */
    public SyncableContact(String deviceID, String memoryType) {
        super(deviceID, memoryType);
    }

    /**
     * Syncable Contact constructor.
     *
     * @param deviceID   (String != null) syncable contact's device ID.
     * @param memoryType (String != null) syncable contact's memory type.
     * @param serverID   (String) syncable contact's server ID.
     */
    public SyncableContact(String deviceID, String memoryType, String serverID) {
        super(deviceID, memoryType, serverID);
    }

    /**
     * Syncable Contact constructor.
     *
     * @param deviceID   (String != null) syncable contact's device ID.
     * @param memoryType (String != null) syncable contact's memory type.
     * @param serverID   (String) syncable contact's server ID.
     * @param data       (String) syncable contact's data, as vCard.
     */
    public SyncableContact(String deviceID, String memoryType, String serverID, String data) {
        super(deviceID, memoryType, serverID, data);
    }

    /**
     * Gets contact's data.
     *
     * @return (Object) contact's  data.
     */
    public Object getData() {
        return m_data;
    }

    /**
     * Sets contact's data.
     *
     * @param data (Object) the data to be set.
     */
    public void setData(Object data) {
        m_data = (String) data;
    }
}
