package com.cellebrite.ota.socialphonebook.repositories.handsetdata;


/**
 * Represents a device's sms element.
 */
public class SmsElement {
    //holds sms number
    String m_messageNumber;

    //holds sms time (the time the sms occurred)
    long m_messageDate;

    //holds sms type (incoming, outgoing)
    byte m_messageType;

    //holds message person ID
    String m_messagePersonID;

    //holds message body
    String m_messageBody;

    //holds device data element's id
    String m_uid;

    /**
     * Sms Element default constructor.
     */
    SmsElement() {
    }

    /**
     * Gets device data element's identifier
     *
     * @return (int) device data element's identifier.
     */
    public String getId() {
        //return Integer.parseInt(_id);
        return m_uid;
    }

    /**
     * Gets sms phone number.
     *
     * @return (String) sms phone number.
     */
    public String getMessageNumber() {
        return m_messageNumber;
    }

    /**
     * Gets the time the sms received as formatted string (for example: 2 days ago, 1 hour ago, 5 minutes ago)
     *
     * @return (String) the call time.
     */
    public String getMessageDate() {
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

        return "Not implemented";
    }

    /**
     * returns 1  if incoming sms otherwise is outgoing sms
     *
     * @return (byte) sms type.
     */
    public byte getMessageType() {
        return m_messageType;
    }

    /**
     * Gets message contact id.
     *
     * @return (int) contact ID.
     */
    public String getMessagePersonID() {
        return m_messagePersonID;
    }

    /**
     * Gets message body
     *
     * @return (String) message body text.
     */
    public String getMessageBody() {
        return m_messageBody;
    }

    /**
     * @return (long) the message raw time.
     */
    public long getRawMessageDate() {
        return m_messageDate;
    }
}
