package com.cellebrite.ota.socialphonebook.repositories.handsetdata;


/**
 * Represents a device's call element.
 */
public class CallElement {
    String m_uid = null;

    //holds caller phone number
    String m_callerNumber;

    //holds caller name
    String m_callerName;

    //holds call time (the time the call occurred)
    long m_callTime;

    //holds call type (incoming, outgoing, missed)
    int m_callType;

    //holds call time duration
    long m_callDuration;

    /**
     * Call Element default constructor.
     */
    CallElement() {
    }

    /**
     * Gets caller phone number.
     *
     * @return (String) caller phone number.
     */
    public String getCallerNumber() {
        return m_callerNumber;
    }

    /**
     * Gets caller name.
     *
     * @return (String) caller name.
     */
    public String getCallerName() {
        return m_callerName;
    }

    /**
     * Gets the time the call occurred as formatted string (for example: 2 days ago, 1 hour ago, 5 minutes ago)
     *
     * @return (String) the call time.
     */
    public String getCallTime() {
        StringBuffer callTime = new StringBuffer(getCallTimeFormat())
                .append(" | ")
                .append(getCallDurationFormat());

        return callTime.toString();
    }

    /**
     * Gets the call duration as formatted string ( e.g : 1h 20m 30sec)
     *
     * @return call duration as formatted string
     */
    private String getCallDurationFormat() {
        StringBuffer formattedTimeDuration = new StringBuffer();

//    	//set the hours & minutes & seconds  of call duration 
//    	int seconds = (int)( m_callDuration % 60 );
//    	int minutes = (int)((m_callDuration / 60));
//    	int hours = minutes / 60 ;
//    	
//    	//handle case of less than one day passed since the time the call occurred
//		if (hours > 0)
//		{
//			//append hours and minutes
//			formattedTimeDuration.append(hours).append("h ");
//			formattedTimeDuration.append(minutes).append("m ");
//		}	
//		else if(minutes > 0)
//		{
//			//append only minutes
//			formattedTimeDuration.append(minutes).append("m ");
//
//		}
//		
//		//append seconds
//		formattedTimeDuration.append(seconds).append(" ").append(ContactListActivity.getInstance().getString(R.string.sec));
//		
//		return formattedTimeDuration.toString();

        return "Not implemented";

    }

    /**
     * Gets the call time as formatted string .</br>
     * There are 3 different kinds of format : </br>
     * 1) Call from today</br>
     * 2) call from yesterday</br>
     * 3) other calls .
     *
     * @return the call time as formatted string in the right format
     */
    private String getCallTimeFormat() {
//    	//convert call time to date
//    	Date currentCallDate = new Date(m_callTime);
//    	Calendar calenderCallTime = Calendar.getInstance();
//    	calenderCallTime.setTime(currentCallDate);
//    	
//    	//convert current time to date
//    	Date currentDate = new Date(System.currentTimeMillis());
//    	Calendar calenderCurrnetTime = Calendar.getInstance();
//    	calenderCurrnetTime.setTime(currentDate);
//    	
//    	//holds the contact list activity object
//    	ContactListActivity cla = ContactListActivity.getInstance();
//    	
//    	//check if the Months of current call time and current time are different
//		if(calenderCurrnetTime.get(Calendar.MONTH) == calenderCallTime.get(Calendar.MONTH))
//		{
//			//check if the days of current call time and current time are different
//			//the call is from today
//			if ((calenderCurrnetTime.get(Calendar.DATE) == calenderCallTime.get(Calendar.DATE)))
//	    	{
//				//get the time in today format
//	    		return getCallTimeTodayFormat();
//	    		
//	    	}
//	    	//check if it is call from yesterday
//	    	else if ((calenderCurrnetTime.get(Calendar.DATE) == (calenderCallTime.get(Calendar.DATE) + 1)))
//	    	{
//	    		try
//	    		{
//		    		// Format the current time.
//			       	SimpleDateFormat formatter = new SimpleDateFormat("hh:mm aaa");
//			       	return cla.getString(R.string.yesterday) + " "+ formatter.format(currentCallDate);
//	    		}
//	    		catch (Throwable t)
//    	        {
//    	            //#ifdef ERROR
//    	            Utils.error("SimpleDateFormat () throws exception: ", t);
//    	            //#endif
//    	            
//    	            //throws the error
//    	           return cla.getString(R.string.yesterday);
//    	        }
//	    	}
//		}
//		try
//		{
//    	// Format the current time.
//    	 SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM d, hh:mm a");
//    	 String callTimeForamt = formatter.format(currentCallDate);
//    	 
//    	 return callTimeForamt;
//		}
//		catch (Throwable t)
//	    {
//	        //#ifdef ERROR
//	        Utils.error("SimpleDateFormat () throws exception: ", t);
//	        //#endif
//	        
//	        throws the error
//	       return "format error";
//	    }

        return "Not implemented";
    }

    /**
     * Gets the call time as formatted string in case of this is call from <u>today</u>
     * ( e.g :4 hours ago , 20 minutes ago..)
     *
     * @return the call time as formatted string in case of this is c from today
     */
    private String getCallTimeTodayFormat() {
//      	//holds the time passed since the time the call occurred
//    	long timePassed = (long)(System.currentTimeMillis() - m_callTime);
//
//    	//set the hours & minutes that passed since the time the call occurred
//    	int hours = (int)(timePassed / (1000*60*60));
//    	int minutes = (int)(timePassed / (1000*60));
//    	
//    	//holds the formatted time 
//    	StringBuffer formattedTime = new StringBuffer();
//    	
//    	//holds the contact list activity object
//    	ContactListActivity cla = ContactListActivity.getInstance();
//    	
//		//handle case of less than one day passed since the time the call occurred
//		if (hours > 0)
//			formattedTime.append(hours).append(" ").append(cla.getString(R.string.hours));
//		else
//			formattedTime.append(minutes).append(" ").append(minutes == 1 ? cla.getString(R.string.minute) : cla.getString(R.string.minutes));
//    	
//
//    	//add the 'ago' postfix
//    	formattedTime.append(" ").append(cla.getString(R.string.ago)).append(" ");
//    	
//	    //return the formatted time
//    	return formattedTime.toString();

        return "Not implemented";
    }


    /**
     * Gets call type (incoming, outgoing, missed).
     *
     * @return (byte) call type.
     */
    public int getCallType() {
        return m_callType;
    }

    /**
     * @return (String != null) the UID of this call log.
     */
    public String getId() {
        return m_uid;
    }

    public long getCallAbsTime() {
        return m_callTime;
    }

    public long getCallAbsDuration() {
        return m_callDuration;
    }
}
