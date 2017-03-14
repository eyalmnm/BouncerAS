//package com.cellebrite.ota.socialphonebook.repositories.contacts;
//
//import android.content.ContentResolver;
//import android.content.ContentValues;
//import android.database.Cursor;
//
//import com.cellebrite.ota.socialphonebook.model.SocialNetworkActivity;
//import com.cellebrite.ota.socialphonebook.model.SocialNetworkStatus;
//import com.cellebrite.ota.socialphonebook.repositories.SQLQueryHelper;
//import com.vario.infra.application.BasicApplication;
//
///**
// * Provides services for the social network content (Statuses,activities)for repository  
// */
//public class RepositoryHelperForSNContent
//{
//	
//	private final static String CAN_COMMENT_VALUE = "1";
//	private final static String CANNNOT_COMMENT_VALUE = "0";
//
//	/**
//	 * Performs a query on the status table.
//	 * 
//	 * @param contactId (String) the contact id or null for all contacts.
//	 * 
//	 * @return (Cursor) cursor to a contact status or all contacts status or null in case there is no contacts / contact (with the given id) 
//	 */
//	static Cursor performQueryOnStatusTable(String contactId)
//	{
//		//holds the content resolver
//		ContentResolver cr = BasicApplication.getApplication().getContentResolver();
//		
//		//holds where and sort order parms for SQL
//		String where = null ;
//		String sortOrder = SocialNetworkStatusesTable.CONTACT_ID+" ," +SocialNetworkStatusesTable.TIME + " " + SQLQueryHelper.DESC_SORT_ORDER;;
//		
//		//if a contact id is given direct the query 
//		if (contactId != null)
//			where = SocialNetworkStatusesTable.CONTACT_ID+" = "+contactId ;
//		
//		//Query the given URI
//		Cursor c = cr.query(SocialNetworkStatusesTable.CONTENT_URI, null , where , null, sortOrder);
//		
//		return c;
//	}
//	
//	/**
//	 * Performs a query on the activities table.
//	 * 
//	 * @param contactId (String != null) the contact id .
//	 * 
//	 * @return (Cursor) cursor to a contact activities or all contacts activities or null in case there is no contacts / contact (with the given id) 
//	 */
//	static Cursor performQueryOnActivitiesTable(String contactId)
//	{
//		//holds the content resolver
//		ContentResolver cr = BasicApplication.getApplication().getContentResolver();
//		
//		//holds where and sort order parms for SQL
//		String where = null ;
//		String sortOrder = SocialNetworkActivitiesTable.CONTACT_ID+" ,"+SocialNetworkActivitiesTable.TIME + " " + SQLQueryHelper.DESC_SORT_ORDER;
//		
//		//if a contact id is given direct the query 
//		if (contactId != null)
//			where = SocialNetworkActivitiesTable.CONTACT_ID+" = "+contactId ;
//		
//		//Query the given URI
//		Cursor c = cr.query(SocialNetworkActivitiesTable.CONTENT_URI, null , where , null, sortOrder);
//	
//		return c;
//	}
//	
//	/**
//	 * Performs a query on the activities table.
//	 * 
//	 * @param contactId (String != null) the sort order .
//	 * 
//	 * @return (Cursor) cursor to a contact activities or all contacts activities or null in case there is no contacts / contact (with the given id) 
//	 */
//	static Cursor performQueryOnActivitiesTableSortedBy(String sortOrder, boolean isAscent)
//	{
//		//holds the content resolver
//		ContentResolver cr = BasicApplication.getApplication().getContentResolver();
//		
//		//holds the order way
//		String orderWay = (isAscent ? SQLQueryHelper.ASC_SORT_ORDER : SQLQueryHelper.DESC_SORT_ORDER);
//		
//		//Query the given URI
//		Cursor c = cr.query(SocialNetworkActivitiesTable.CONTENT_URI, null , null , null, sortOrder + " " + orderWay);
//	
//		return c;
//	}
//	
//	/**
//	 * Returns a contact status 
//	 * @param cursor (Cursor != null) a cursor directed to the relevant row.
//	 * 
//	 * @return (SocialNetworkStatus != null)
//	 */
//	static SocialNetworkStatus getContactStatusFromCursor(Cursor cursor)
//	{
//		 String id = cursor.getString(cursor.getColumnIndex(SocialNetworkStatusesTable.STATUS_ID));
//		 String contactId = cursor.getString(cursor.getColumnIndex(SocialNetworkStatusesTable.CONTACT_ID));
//		 String socialNetworkName = cursor.getString(cursor.getColumnIndex(SocialNetworkStatusesTable.SOCIAL_NETWORK_NAME));
//		 String text = cursor.getString(cursor.getColumnIndex(SocialNetworkStatusesTable.TEXT));
//		 long time = cursor.getLong(cursor.getColumnIndex(SocialNetworkStatusesTable.TIME));
//
//		return new SocialNetworkStatus(id, contactId, socialNetworkName,text,time);
//	
//	}
//	
//	/**
//	 * Returns a contact Activity 
//	 * @param cursor (Cursor != null) a cursor directed to the relevant row.
//	 * 
//	 * @return (SocialNetworkActivity != null)
//	 */
//	static SocialNetworkActivity getContactActivityFromCursor(Cursor cursor)
//	{
//		 String id = cursor.getString(cursor.getColumnIndex(SocialNetworkActivitiesTable.ACTIVITY_ID));
//		 String contactID = cursor.getString(cursor.getColumnIndex(SocialNetworkActivitiesTable.CONTACT_ID));
//		 String socialNetworkName = cursor.getString(cursor.getColumnIndex(SocialNetworkActivitiesTable.SOCIAL_NETWORK_NAME));
//		 String text = cursor.getString(cursor.getColumnIndex(SocialNetworkActivitiesTable.TEXT));
//		 long time = cursor.getLong(cursor.getColumnIndex(SocialNetworkActivitiesTable.TIME));
//		 String canCommentStr =cursor.getString(cursor.getColumnIndex(SocialNetworkActivitiesTable.CAN_COMMENT));
//		 boolean cancomment = canCommentStr.equalsIgnoreCase(CAN_COMMENT_VALUE);
//		
//
//		return new SocialNetworkActivity(id, contactID, socialNetworkName,text,time,cancomment);
//	
//	}
//	
//	/**
//	 * Deletes the record (if exists) that belongs to the contact with the given device ID
//	 * 
//	 * @param contactId (String != null) a valid device ID.
//	 * 
//	 * @return (int) The number of rows deleted. 
//	 */
//	static int deleteActivitiesByContactID(String contactId)
//	{
//		//holds the content resolver
//		ContentResolver cr = BasicApplication.getApplication().getContentResolver();
//		
//		//holds where and sort order parms for SQL
//		String where = SocialNetworkActivitiesTable.CONTACT_ID + " = "+contactId ;
//		
//		//delete records with the device id
//		return cr.delete(SocialNetworkActivitiesTable.CONTENT_URI, where, null) ;
//	}
//	
//	/**
//	 * Deletes the record (if exists) that belongs to the contact with the given contact ID
//	 * 
//	 * @param contactId (String != null) a valid device ID.
//	 * 
//	 * @return (int) The number of rows deleted. 
//	 */
//	public static int deleteStatusByDeviceID(String contactId)
//	{
//		//holds the content resolver
//		ContentResolver cr = BasicApplication.getApplication().getContentResolver();
//		
//		//holds where and sort order parms for SQL
//		String where = SocialNetworkStatusesTable.CONTACT_ID + " = "+contactId ;
//		
//		//delete records with the device id
//		return cr.delete(SocialNetworkStatusesTable.CONTENT_URI, where, null);
//	}
//	
//
//	
//	/**
//	 * save the contact activity in DB.
//	 * @param contactId(String != null) the contact id.
//	 * @param activities (SocialNetworkActivity != null) the contact's activity
//	 */
//	public static void saveActivityByContactID(String contactId, SocialNetworkActivity activity)
//	{
//		//gets the content resolver
//		ContentResolver cr = BasicApplication.getApplication().getContentResolver();
//		
//		//creates a content values object and sets activity values inside it
//		ContentValues contentValues = new ContentValues();
//		contentValues.put(SocialNetworkActivitiesTable.ACTIVITY_ID, activity.getID());
//		contentValues.put(SocialNetworkActivitiesTable.CONTACT_ID, contactId);
//		contentValues.put(SocialNetworkActivitiesTable.SOCIAL_NETWORK_NAME, activity.getSocialNetworkName());
//		contentValues.put(SocialNetworkActivitiesTable.TEXT, activity.getText());
//		contentValues.put(SocialNetworkActivitiesTable.TIME, activity.getTime());
//		contentValues.put(SocialNetworkActivitiesTable.CAN_COMMENT,activity.canComment() ? CAN_COMMENT_VALUE : CANNNOT_COMMENT_VALUE);
//		contentValues.put(SocialNetworkActivitiesTable.VALID, "1");
//		
//		//holds the where condition
//		String where = "("+SocialNetworkActivitiesTable.ACTIVITY_ID+ " = '"+activity.getID()+"') AND ("+SocialNetworkActivitiesTable.CONTACT_ID+" = "+contactId+")";
//
//		//inserts contact's activity to application's database. 
//		//try to update first than if failed try to insert (assuming it will not return 0 due to any other failure)
//		if (cr.update(SocialNetworkActivitiesTable.CONTENT_URI, contentValues, where, null) == 0)
//			cr.insert(SocialNetworkActivitiesTable.CONTENT_URI, contentValues);
//		
//	
//	}
//	
////	/**
////	 * Save contact status in application's contacts statuses table.
////	 * And turns its valid bit to valid (1) 
////	 *  
////	 * @param contactDeviceID (contactId != null) contact's device ID.
////	 * @param status (SocialNetworkStatus != null) contact's status.
////	 */
////	public static void saveStatusbyContactid(String contactId, SocialNetworkStatus status)
////	{
////		//gets the content resolver
////		ContentResolver cr = BasicApplication.getApplication().getContentResolver();
////		
////		//creates a content values object and sets status values inside it
////		ContentValues contentValues = new ContentValues();
////		contentValues.put(SocialNetworkStatusesTable.STATUS_ID, status.getID());
////		contentValues.put(SocialNetworkStatusesTable.CONTACT_ID, contactId);
////		contentValues.put(SocialNetworkStatusesTable.SOCIAL_NETWORK_NAME, status.getSocialNetworkName());
////		contentValues.put(SocialNetworkStatusesTable.TEXT, status.getText());
////		contentValues.put(SocialNetworkStatusesTable.TIME, status .getTime());
////		contentValues.put(SocialNetworkStatusesTable.VALID, "1");
////		
////		//holds the where condition
////		String where = "("+ SocialNetworkStatusesTable.STATUS_ID+ " = '"+status.getID()+"') AND ("+SocialNetworkStatusesTable.CONTACT_ID+" = "+contactId+ ")";
////		
////		//inserts contact's activity to application's database. 
////		//try to update first than if failed try to insert (assuming it will not return 0 due to any other failure)
////		if (cr.update(SocialNetworkStatusesTable.CONTENT_URI, contentValues, where, null) == 0)
////			cr.insert(SocialNetworkStatusesTable.CONTENT_URI, contentValues);
////		
////	}
//
//	static String getContactIDFromActivityCursor(Cursor activitiesCursor) 
//	{
//		return activitiesCursor.getString(activitiesCursor.getColumnIndex(SocialNetworkActivitiesTable.CONTACT_ID));
//	}
//}
