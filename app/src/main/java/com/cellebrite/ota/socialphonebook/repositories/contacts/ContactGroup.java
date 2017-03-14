package com.cellebrite.ota.socialphonebook.repositories.contacts;

import java.util.Hashtable;
import java.util.Vector;


/**
 * This class represents a contact group e.g Phone, SIM, Facebook.
 */
public class ContactGroup {
    //holds contact's group ID
    private Long m_id;
    //holds contact's group type
    private String m_type;
    //holds contact's group name
    private String m_groupName;
    //holds container from each data kind that belong to the contact group
    //map each data kind to it list of data
    private Hashtable<Object, Vector<DataArgs>> m_mappedDataKinds = new Hashtable<Object, Vector<DataArgs>>();

    /**
     * Ctor . initialize the contact group
     *
     * @param groupType (String) - the group type
     * @param groupName (String) - the group name
     */
    public ContactGroup(String groupType, String groupName) {
        m_type = groupType;
        m_groupName = groupName;
    }

    /**
     * this method create a default group ( Phone group) and return it.
     *
     * @return new default contact group.
     */
    public static ContactGroup getDefaultGroup() {
        return new ContactGroup(KnownTypes.PHONE, KnownTypes.PHONE);
    }

    /**
     * Set the group id.
     *
     * @param id (String != null) the goup's id.
     */
    public void setGroupId(Long id) {
        m_id = id;
    }

    /**
     * add a data which has a kind,type and value
     *
     * @param dataKind (int) the data kind
     * @param dataType (int) - the data type
     * @param value    (Object != null) - the data value - can be anything
     */
    public void addData(DataArgs args) {
        //try to find the given data kind in map container - if exist  - use it
        Vector<DataArgs> mappedDataKind = m_mappedDataKinds.get(args.getKind());

        //in case this is the first kind of this type
        if (mappedDataKind == null) {
            //create new container for this kind of data
            mappedDataKind = new Vector<DataArgs>();
            m_mappedDataKinds.put(args.getKind(), mappedDataKind);
        }

        //add new data to the specific kind
        mappedDataKind.add(args);
    }

    /**
     * Get data from specific data kind
     *
     * @param dataKind (int) - data kind to find
     * @return (Vector<DataArgs>) collection of data from the given kind , if not found any data return null.
     */
    public Vector<DataArgs> getData(int dataKind) {
        //try to find the given data kind in map container - if exist  - use it
        Vector<DataArgs> args = m_mappedDataKinds.get(dataKind);

        //if no such kind
        if (args == null)
            return new Vector<DataArgs>(0);

        //else return the args
        return args;
    }

    /**
     * @return the group type
     */
    public String getType() {
        return m_type;
    }

    /**
     * @return the group name
     */
    public String getName() {
        return m_groupName;
    }

    /**
     * @return the group ID
     */
    public Long getID() {
        return m_id;
    }

    /**
     * Holds constants for known group types.
     */
    public static class KnownTypes {
        public static final String PHONE = "Phone";
        public static final String SIM = "Sim";
        public static final String UNKNOWN = "Unknown";
    }
}
