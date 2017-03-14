package com.cellebrite.ota.socialphonebook.repositories.contacts;

import java.util.Hashtable;

public class DataArgs {
    //holds the data type
    private int m_kind;

    //holds the inner values as lazy collection
    private Hashtable<Object, Object> m_values = new Hashtable<Object, Object>();

    public DataArgs(int kind) {
        m_kind = kind;
    }

    /**
     * Get the type of the data
     *
     * @return the type of the data
     */
    int getKind() {
        return m_kind;
    }

    /**
     * Adds an inner data argument to this data argument to support structured data args.
     *
     * @param type  (int) the type of the inner data argument.
     * @param value (Object) the data value.
     */
    public void addValue(Object type, Object value) {
        if (type != null && value != null)
            m_values.put(type, value);
    }

    /**
     * Get inner value of a inner argument type.
     *
     * @return the value of the data
     */
    public <T> T getValue(Object key) {
        return (T) m_values.get(key);
    }
}
