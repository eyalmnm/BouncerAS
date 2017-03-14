package com.em_projects.bouncer.model;

import android.util.Log;

import com.em_projects.bouncer.repositories.ContactsRepository;
import com.em_projects.infra.model.Entity;
import com.em_projects.utils.StringUtil;

import java.util.Comparator;
import java.util.Vector;

@SuppressWarnings("serial")
public class Conversation extends Entity<String> {
    private static final String TAG = "Conversation";
    //Comparator for conversations list sorting
    public static Comparator<Conversation> COMPARE_BY_DATE = new Comparator<Conversation>() {
        public int compare(Conversation one, Conversation other) {
            if (one != null && other != null)
                return String.valueOf(other.getDate()).compareTo(String.valueOf(one.getDate()));
            else
                return 0;
        }
    };
    //hold members
    private long m_date;
    private int m_messageCount;
    private String m_phoneNumber = null;
    private String[] m_recipientIds = null;
    private String m_recipientNames = null;
    private String m_snippet = null;
    private int m_readState;
    private byte[] m_thumbData = null;

    /**
     * Lazy Conversation Ctor.
     */
    public Conversation(String threadId, long date, String phoneNumber, int readState) {
        super(threadId);
        Log.d(TAG, "Conversation");

        m_date = date;
        m_phoneNumber = phoneNumber;
        m_readState = readState;
    }

    /**
     * Ctor.
     */
    public Conversation(String threadId, long date, int messageCount, String phoneNumber, Vector<String> recipientIds,
                        Vector<String> displayNames, String snippet, int readState) {
        super(threadId);
        Log.d(TAG, "Conversation");

        this.m_date = date;
        this.m_messageCount = messageCount;
        this.m_phoneNumber = phoneNumber;
        this.m_recipientIds = recipientIds.toArray(new String[recipientIds.size()]);
        this.m_recipientNames = StringUtil.join(displayNames.toArray(new String[displayNames.size()]), ",");
        this.m_thumbData = retrieveThumbData(m_recipientIds);
        this.m_snippet = snippet;
        this.m_readState = readState;
    }

    /**
     * Returns contact's thumb image data.
     * In case the conversation has more then one recipient, default thumb image data is returned.
     *
     * @param recipentIds (String[] != null) Recipients ids array.
     * @return (byte[] != null) Contact's thumb image data.
     */
    private byte[] retrieveThumbData(String[] recipentIds) {
        Log.d(TAG, "retrieveThumbData");
        byte[] thumbData = null;

        //return recipient's photo only if there is one recipient
        if (recipentIds.length == 1)
            thumbData = ContactsRepository.getInstance().getByUID(recipentIds[0]).getPhotoData();

        return thumbData;
    }

    public byte[] getThumbData() {
        Log.d(TAG, "getThumbData");
        return m_thumbData;
    }

    public long getDate() {
        Log.d(TAG, "getDate");
        return m_date;
    }

    public int getMessageCount() {
        Log.d(TAG, "getMessageCount");
        return m_messageCount;
    }

    public String getPhoneNumber() {
        Log.d(TAG, "getPhoneNumber");
        return m_phoneNumber;
    }

    public String[] getRecipientIds() {
        Log.d(TAG, "getRecipientIds");
        return m_recipientIds;
    }

    public String getRecipientNames() {
        Log.d(TAG, "getRecipientNames");
        return m_recipientNames;
    }

    public String getSnippet() {
        Log.d(TAG, "getSnippet");
        return m_snippet;
    }

    public int getReadState() {
        Log.d(TAG, "getReadState");
        return m_readState;
    }

    public String getDisplayName() {
        Log.d(TAG, "getDisplayName");
        //holds the display name
        StringBuffer dispName = new StringBuffer(m_recipientNames);

        //in case we have more then 1 message, add message count string
        if (m_messageCount > 1)
            dispName.append(" (").append(m_messageCount).append(")");

        return dispName.toString();
    }

    //statics for conversation's read state
    public final static class READ_STATE {
        public static final int UNREAD = 0;
        public static final int READ = 1;
    }
}
