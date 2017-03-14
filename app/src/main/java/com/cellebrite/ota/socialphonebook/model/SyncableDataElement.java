package com.cellebrite.ota.socialphonebook.model;

/**
 * Represents a syncable data element.
 */
public abstract class SyncableDataElement {
    //holds syncable data's unique identifier
    public String m_deviceID = null;
    //holds syncable data's memory type (0 - device (default), 1 - SIM card)
    public String m_memoryType = null;
    //holds syncable data's server identifier (used for identifying new data received from the server)
    public String m_serverID = null;
    //holds syncable data's cData
    protected Object m_data = null;
    //holds the sync action type
    protected SyncActionTypes m_syncActionType = SyncActionTypes.UNDEFINED;

    /**
     * Application Data Element constructor.
     *
     * @param deviceID   (String != null) syncable data element's device id.
     * @param memoryType (String != null) syncable data element's memory type.
     */
    public SyncableDataElement(String deviceID, String memoryType) {
        m_deviceID = deviceID;
        m_memoryType = memoryType;
        m_serverID = "0";
    }

    /**
     * Application Data Element constructor.
     *
     * @param deviceID   (String != null) syncable data element's device ID.
     * @param memoryType (String != null) syncable data element's memory type.
     * @param deviceID   (String) syncable data element's server ID.
     */
    public SyncableDataElement(String deviceID, String memoryType, String serverID) {
        m_deviceID = deviceID;
        m_memoryType = memoryType;
        m_serverID = serverID;
    }

    /**
     * Application Data Element constructor.
     *
     * @param deviceID   (String != null) syncable data element's device ID.
     * @param memoryType (String != null) syncable data element's memory type.
     * @param serverID   (String) syncable data element's server ID.
     * @param data       (Object) syncable data element's data.
     */
    public SyncableDataElement(String deviceID, String memoryType, String serverID, Object data) {
        m_deviceID = deviceID;
        m_memoryType = memoryType;
        m_serverID = serverID;
        m_data = data;
    }

    /**
     * Gets syncable data element's device ID.
     *
     * @return (String != null) syncable data element's device ID.
     */
    public String getDeviceID() {
        return m_deviceID;
    }

    /**
     * Sets syncable data element's device ID.
     *
     * @param deviceID (String != null) syncable data element's device ID.
     */
    public void setDeviceID(String deviceID) {
        m_deviceID = deviceID;
    }

    /**
     * Gets syncable data element's memory type.
     *
     * @return (String != null) syncable data element's memory type.
     */
    public String getMemoryType() {
        return m_memoryType;
    }

    /**
     * Sets syncable data element's memory type.
     *
     * @param isDevice (boolean) true in case syncable contact's memory type is Device, false otherwise (for SIM card).
     */
    public void setMemoryType(boolean isDevice) {
        m_memoryType = isDevice ? MemoryTypes.DEVICE : MemoryTypes.SIM_CARD;
    }

    /**
     * Gets syncable data element's device ID.
     *
     * @return (String != null) syncable data element's server ID.
     */
    public String getServerID() {
        return m_serverID;
    }

    /**
     * Sets syncable data element's server ID.
     *
     * @param serverID (String != null) syncable data element's server ID.
     */
    public void setServerID(String serverID) {
        m_serverID = serverID;
    }

    /**
     * Gets application data elemnt's data.
     *
     * @return (Object) application data elemnt's data.
     */
    public abstract Object getData();

    /**
     * Sets application data elemnt's data.
     *
     * @param data (Object) the data to be set.
     */
    public abstract void setData(Object data);

    /**
     * Returns this syncable object sync action.
     *
     * @return (SyncActionTypes)
     */
    public SyncActionTypes getSyncActionType() {
        return m_syncActionType;
    }

    /**
     * Sets the action type of this syncable object.
     *
     * @param type (SyncActionTypes)
     */
    public void setSyncActionType(SyncActionTypes type) {
        m_syncActionType = type;
    }

    /**
     * Holds the possible sync types
     */
    public enum SyncActionTypes {
        UNDEFINED,
        NEW,
        DELETE,
        MODIFIED
    }

    /**
     * Holds final values for syncalbe data elements memory types.
     */
    public static class MemoryTypes {
        public static final String DEVICE = "Phone";
        public static final String SIM_CARD = "Sim";
    }
}
