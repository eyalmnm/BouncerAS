package com.em_projects.bouncer.model;

import android.util.Log;

import com.cellebrite.ota.socialphonebook.utilities.StringUtilities;
import com.em_projects.infra.model.Entity;

import java.util.UUID;


/**
 * Represents a device's sms element.
 */
@SuppressWarnings("serial")
public class SmsElement extends Entity<String> {
    private static final String TAG = "SmsElement";

    //holds message id
    public int MessageId;

    //holds message thread id
    public int ThreadId;

    //holds message number (address)
    public String PhoneNumber = "";

    //holds message person ID
    //	public int PersonID;

    //holds message date (the time it has occured)
    public long Date;

    //holds message protocol
    //	public int Protocol;

    //holds message read state (read/unread)
    public int Read = 0;

    //holds message status
    public int Status = -1;

    //holds sms type (incoming, outgoing)
    public int Type;
    //holds message subject text
    public String Subject = "";
    //holds message body text
    public String Body = "";
    //holds other message members
    //	public int ReplyPathPresent;
    public String ServiceCenter = "";
    public int Locked = 0;

    /**
     * Ctor.
     */
    public SmsElement(int id, int threadId, String address, long date, int read, int status,
                      int type, String subject, String body, String serviceCenter, int locked) {
        super(UUID.randomUUID().toString());
        Log.d(TAG, "SmsElement");
        MessageId = id;
        ThreadId = threadId;
        PhoneNumber = address == null ? "" : address;
        Date = date;
        Read = read;
        Status = status;
        Type = type;
        Subject = subject == null ? "" : subject;
        Body = body == null ? "" : body;
        ServiceCenter = serviceCenter == null ? "" : serviceCenter;
        Locked = locked;
    }

    /**
     * Returns comma separated string that includes all element's members.
     */
    @Override
    public String toString() {
        Log.d(TAG, "toString");
        //add all element members into a string array
        String[] list = new String[]
                {
                        String.valueOf(MessageId),
                        String.valueOf(ThreadId),
                        PhoneNumber,
                        //    		String.valueOf(PersonID),
                        String.valueOf(Date),
                        //    		String.valueOf(Protocol),
                        String.valueOf(Read),
                        String.valueOf(Status),
                        String.valueOf(Type),
                        Subject,
                        Body,
                        //    		String.valueOf(ReplyPathPresent),
                        ServiceCenter,
                        String.valueOf(Locked)
                };

        //return a string with all members joined and separated with comma
        return StringUtilities.join(list, ",");
    }

    public static final class SMS_TYPE {
        public static final int INCOMING = 1;
        public static final int OUTGOING = 2;
    }

    //    /**
    //     * Gets device data element's identifier
    //     *
    //     * @return (int) device data element's identifier.
    //     */
    //    public int getMessageId()
    //    {
    //        //return Integer.parseInt(_id);
    //    	return MessageId;
    //    }
    //
    //    /**
    //     * Gets the time the sms received as formatted string (for example: 2 days ago, 1 hour ago, 5 minutes ago)
    //     *
    //     * @return (String) the call time.
    //     */
    //    public String getMessageDate()
    //    {
    //    	//holds the time passed since the time the call occurred
    //    	long timePassed = (long)(System.currentTimeMillis() - m_messageDate);
    //
    //    	//set the days, hours & minutes that passed since the time the call occurred
    //    	int days = (int)(timePassed / (1000*60*60*24));
    //    	int hours = (int)(timePassed / (1000*60*60));
    //    	int minutes = (int)(timePassed / (1000*60));
    //
    //    	//holds the formatted time
    //    	StringBuffer formattedTime = new StringBuffer();
    //
    //    	//holds the contact list activity object
    //    	ContactListActivity cla = ContactListActivity.getInstance();
    //
    //    	//handle case of more than one day passed since the time the call occurred
    //    	if (days > 0)
    //    		formattedTime.append(days).append(" ").append(days == 1 ? cla.getString(R.string.day) : cla.getString(R.string.days));
    //    	else
    //    	{
    //    		//handle case of less than one day passed since the time the call occurred
    //    		if (hours > 0)
    //    			formattedTime.append(hours).append(" ").append(cla.getString(R.string.hours));
    //    		else
    //    		{
    //    			formattedTime.append(minutes).append(" ").append(minutes == 1 ? cla.getString(R.string.minute) : cla.getString(R.string.minutes));
    //    		}
    //    	}
    //
    //    	//add the 'ago' postfix
    //    	formattedTime.append(" ").append(cla.getString(R.string.ago));
    //
    //	    //return the formatted time
    //    	return formattedTime.toString();
    //
    //    	return "Not implemented";
    //    }

    public static final class READ_STATE {
        public static final int UNREAD = 0;
        public static final int READ = 1;
    }
}
