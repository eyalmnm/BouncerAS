package com.cellebrite.ota.socialphonebook.utilities;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Calendar;

/**
 * Application's utility methods.
 */
public class ApplicationUtilities {
    /**
     * Converts a milliseconds date into string representation.
     *
     * @param millisecondsData (long >= 0) the date in milliseconds.
     * @return (String != null) the date in string representation.
     */
    public static String convertMillisecondsDateToString(long millisecondsDate) {
        //gets a calendar object and sets its time as the given date
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millisecondsDate);

        //returns the date in string representation (YYYY-MM-DD, HH:MM:SS)
        return String.format("%02d-%02d-%04d, %02d:%02d",
                calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));
    }

//	/**
//	 * Gets a resource ID of a social network icon, according to a given social network name.
//	 * 
//	 * @param socialNetworkName (String != null) social network's name.
//	 * 
//	 * @return (int) resource ID of a social network icon.
//	 */
//	public static int getSocialNetworkIconResourceID(String socialNetworkName, Context context)
//	{
//		//TODO - but why do we need this? we got icons from the server?! 
//
//		//in case of facebook social network
//		if(socialNetworkName.equalsIgnoreCase(context.getString(R.string.application_social_network_facebook)))
//		{
//			return R.drawable.cd_screen_facebook_icon;
//		}
//		//in case of twitter social network
//		else if(socialNetworkName.equalsIgnoreCase(context.getString(R.string.application_social_network_twitter)))
//		{
//			return R.drawable.cd_screen_twitter_icon;
//		}
//		//in case of linked in social network
//		else if(socialNetworkName.equalsIgnoreCase(context.getString(R.string.application_social_network_linkedin)))
//		{
//			return R.drawable.cd_screen_linkedin_icon;
//		}
//		else
//		{
//			return R.drawable.cd_screen_default_icon;					
//		}
//	}
//	
//	/**
//	 * Matchable Contacts Photo Loader.
//	 */
//	public static class MatchableContactsPhotoLoader extends Thread
//	{
//		//holds whether the thread is alive
//		private boolean b_isAlive = false;
//		
//		//holds the matchable contacts to get their photos
//		private final Vector<MatchableContact> m_matchableContacts;
//		
//		//holds the number of matchable contacts which were handled
//		private int m_numberOfHandledMatchableContacts;
//		
//		/**
//		 * Matchable Contacts Photo Loader default constructor.
//		 * 
//		 * @param numberOfMatchableContacts (int > 0) the number of matchable contacts to get their photos.
//		 */
//		public MatchableContactsPhotoLoader(int numberOfMatchableContacts)
//		{
//			//creates the matchable contacts collection, in the size of the number of matchable contacts to get their photos
//			m_matchableContacts = new Vector<MatchableContact>(numberOfMatchableContacts, 0);
//			
//			//sets that no matchable contact's photo was retrieved so fat
//			m_numberOfHandledMatchableContacts = 0;
//			
//			//starts the thread
//			this.start();
//		}
//		
//		/**
//		 * Runs the thread.
//		 */
//		public void run()
//		{
//			//holds the current handled matchable contact
//			MatchableContact matchableContact = null;
//			
//			//sets that the thread is alive
//			b_isAlive = true;
//			
//			//as long as the thread is alive
//			while(b_isAlive)
//			{
//				//holds the HTTP connection against contact's photo URL
//				HttpURLConnection httpURLConnection = null;
//				
//				//holds the input stream of the connection against contact's photo URL
//				InputStream inputStream = null;
//				
//				try
//				{
//					//synchronizes with the matchable contacts to get their photos collection
//					synchronized(m_matchableContacts)
//					{
//						//in case all matchable contacts photos were retrieved
//						if(m_numberOfHandledMatchableContacts == m_matchableContacts.capacity())
//						{
//							//sets that the thread is no longer alive
//							b_isAlive = false;
//							
//							return;
//						}
//						
//						//in case the collection is empty, waits till a new matchable contact will be inserted to the queue,
//						//or to when the thread will be terminated
//						if(m_matchableContacts.isEmpty())
//							m_matchableContacts.wait();
//						
//						//in case the thread is no longer alive
//						if(!b_isAlive)
//							return;
//						
//						//gets the matchable contact to get his photo from the social network
//						matchableContact = m_matchableContacts.elementAt(0);
//						m_matchableContacts.remove(0);
//					}
//						
//					//in case the matchable contact has a photo URL, retrieves his photo
//					URL matchableContactPhotoURL = null;
//					if((matchableContactPhotoURL = matchableContact.getPhotoURL()) != null)
//					{
//		                //#ifdef DEBUG
//		                Utils.debug("ApplicationUtilities.MatchableContactsPhotoLoader.run() - " +
//		                			"retrieving contact's photo - contact's ID " + matchableContact.getId() +
//		                			", " + matchableContactPhotoURL.toString());
//		                			
//		                //#endif
//						
//						//opens an HTTP connection against the photo URL
//						//TODO - how to set timeout on opening a connection ?
//						httpURLConnection = (HttpURLConnection)matchableContactPhotoURL.openConnection();
//						
//						//enables the HTTP connection to support input stream
//						httpURLConnection.setDoInput(true);
//						
//						
//						//sets the connection timeout and connects
//						httpURLConnection.setConnectTimeout(ContactListActivity.APPLICATION_PARAMETERS.NETWORK_CONNECTION_CONNECT_TIMEOUT);
//						httpURLConnection.setReadTimeout(ActivityManager.GLOBAL_PARAMETERS.NETWORK_CONNECTION_READ_TIMEOUT);
//						httpURLConnection.connect();
//						
//						//gets the input stream of the HTTP connection 
//						inputStream = httpURLConnection.getInputStream();
//
//						//holds a buffer reader
//						BufferedInputStream bis = new BufferedInputStream(inputStream,ActivityManager.GLOBAL_PARAMETERS.PHOTO_STREAM_INITIAL_BUFFER_SIZE);
//						
//						//gets the width of the original contact's photo
//						Bitmap originalPhoto = BitmapFactory.decodeStream(bis);
//
//						if(originalPhoto == null)
//						{
//			                //#ifdef DEBUG
//			                Utils.debug("ApplicationUtilities.MatchableContactsPhotoLoader.run() - " +
//			                			"cannot decode matchable contact's photo from the input stream. "+httpURLConnection.getContentType());
//			                			
//			                //#endif
//			                
//			                //continue to the next matchable contact
//			                continue;
//						}
//						
//						int originalImageWidth = originalPhoto.getWidth();
//						int originalImageHeight = originalPhoto.getHeight();
//						
//				        int scaledImageWidth = Contact.DEFAULT_SMALL_CONTACT_PHOTO.getWidth();
//				        int scaledImageHeight = Contact.DEFAULT_SMALL_CONTACT_PHOTO.getHeight();
//
//				        //calculates the scale
//				        float scaleWidth = ((float) scaledImageWidth) / originalImageWidth;
//				        float scaleHeight = ((float) scaledImageHeight) / originalImageHeight;
//
//				        //creates matrix for the manipulation
//				        Matrix matrix = new Matrix();
//				        
//				        //resizes the bit map
//				        matrix.postScale(scaleWidth, scaleHeight);
//				        //recreates the new bitmap
//				        Bitmap scaledPhoto = Bitmap.createBitmap(originalPhoto, 0, 0, originalImageWidth, originalImageHeight, matrix, true);
//				        
//				        //holds the scaled photo and the matchable contacts as final variables
//				        final Bitmap _scaledPhoto = scaledPhoto;
//				        final MatchableContact _matchableContact = matchableContact;
//				        
//				    	//check if contact list activity exists
//				    	if(ContactListActivity.getInstance() == null)
//				    		return;
//				        
//				        //sets matchable's contacts photo using the UI thread (MUST according to Android APIs)
//				        ContactListActivity.getInstance().runOnUiThread(new Runnable()
//				        {
//				        	/**
//				        	 * Runs the code.
//				        	 */
//							@Override public void run()
//							{
//								//sets matchable contact's photo, which will cause the contact match screen to be updated if necessary
//								_matchableContact.setPhoto(_scaledPhoto);
//							}
//						});
//					}
//				}
//				catch (Throwable throwable)
//				{
//	                //#ifdef ERROR
//	                Utils.error("ApplicationUtilities.MatchableContactsPhotoLoader.run() - " +
//	                			"an error has occurred - " + throwable + 
//	                			", getting contact's photo failed (for matchable contact with ID " + matchableContact.getId() + ")");
//	                //#endif
//					
//					continue;
//				}
//				finally
//				{
//					try
//					{
//						//releases resources
//						if(inputStream != null)
//							inputStream.close();
//						
//						if(httpURLConnection != null)
//							httpURLConnection.disconnect();
//					}
//					catch(Throwable throwable)
//					{
//		                //#ifdef ERROR
//		                Utils.error("ApplicationUtilities.MatchableContactsPhotoLoader.run() - " +
//		                			"an error has occurred - " + throwable + 
//		                			", releasing resources failed.");
//		                //#endif
//					}
//					
//		            //updates the number of matchable contacts which their photos were retrieved
//		            ++m_numberOfHandledMatchableContacts;
//				}
//			}
//		}
//		
//		/**
//		 * Adds a matchable contact to the matchable contact to get their photo collection.
//		 * 
//		 * @param matchableContact (MatchableContact != null) the matchable contact to add.
//		 */
//		public void addMatchableContact(MatchableContact matchableContact)
//		{
//			//synchronizes with the matchable contacts to get their photos collection
//			synchronized (m_matchableContacts)
//			{
//				//adds the matchable contact to the collection
//				m_matchableContacts.addElement(matchableContact);
//				
//				//notifies the thread that a new matchable contact has been added, in case the thread
//				//is currently waiting for notification
//				m_matchableContacts.notify();
//			}
//		}
//		
//		/**
//		 * Terminates the thread, stops retrieving matchable contacts photos. 
//		 */
//		public void terminate()
//		{
//			//synchronizes with the matchable contacts to get their photos collection
//			synchronized (m_matchableContacts)
//			{
//				//in case the thread was already terminated, do nothing
//				if(!b_isAlive)
//					return;
//				
//				//sets that the thread is no longer alive
//				b_isAlive = false;
//				
//				//notifies the thread in case it's waiting to be notified
//				m_matchableContacts.notify();
//			}
//		}
//		
//		/**
//		 * Returns whether the thread is alive.
//		 * 
//		 * @return (boolean) true in case the thread is alive, false otherwise.
//		 */
//		public boolean isThreadAlive()
//		{
//			return b_isAlive;
//		}
//		
//		/**
//		 * Starts this thread MIN priority
//		 */
//		public void start()
//		{
//			this.setPriority(MIN_PRIORITY);
//			super.start();
//		}
//	}

    /**
     * Creates device contact's scaled photo, based on his photo's input stream.
     *
     * @param photoInputStream (InputStream != null) device contact's photo input stream.
     * @return (Bitmap != null) device contact's scaled photo.
     */
    public static Bitmap createDeviceContactScaledPhoto(InputStream photoInputStream, int toWidth, int toHeight) {
        //gets the width of the original contact's photo
        //TODO - check if there is a way to scale without getting the whole photo
        Bitmap originalPhoto = BitmapFactory.decodeStream(photoInputStream);

        //in case the image could not be created, do nothing
        if (originalPhoto == null)
            return null;

        int originalImageWidth = originalPhoto.getWidth();
        int originalImageHeight = originalPhoto.getHeight();

        //calculates the scale
        float scaleWidth = ((float) toWidth) / originalImageWidth;
        float scaleHeight = ((float) toHeight) / originalImageHeight;

        //creates matrix for the manipulation
        Matrix matrix = new Matrix();

        //scale the bitmap
        matrix.postScale(scaleWidth, scaleHeight);

        //recreates the new bitmap
        return Bitmap.createBitmap(originalPhoto, 0, 0, originalImageWidth, originalImageHeight, matrix, true);
    }

    /**
     * Creates device contact's scaled photo, based on his photo's input stream.
     *
     * @param originalPhoto (Bitmap != null) device contact's photo input stream.
     * @return (Bitmap != null) device contact's scaled photo.
     */
    public static Bitmap createDeviceContactScaledPhoto(Bitmap originalPhoto, int toWidth, int toHeight) {
        //in case the image could not be created, do nothing
        if (originalPhoto == null)
            return null;

        int originalImageWidth = originalPhoto.getWidth();
        int originalImageHeight = originalPhoto.getHeight();

        //calculates the scale
        float scaleWidth = ((float) toWidth) / originalImageWidth;
        float scaleHeight = ((float) toHeight) / originalImageHeight;

        //creates matrix for the manipulation
        Matrix matrix = new Matrix();

        //scale the bitmap
        matrix.postScale(scaleWidth, scaleHeight);

        //recreates the new bitmap
        return Bitmap.createBitmap(originalPhoto, 0, 0, originalImageWidth, originalImageHeight, matrix, true);
    }

//	/**
//	 * Gets a photo from a given web URL.
//	 * @return a photo or null for no photo. 
//	 */
//	public static Bitmap getPhotoFromWebURL(String url)
//	{
//		HttpURLConnection httpURLConnection = null;
//		
//		try
//		{
//			URL matchableContactPhotoURL = new URL(url);
//			
//			//opens an HTTP connection against the photo URL
//			httpURLConnection = (HttpURLConnection)matchableContactPhotoURL.openConnection();
//			
//			//enables the HTTP connection to support input stream
//			httpURLConnection.setDoInput(true);
//			
//			//sets the connection timeout and connects
//			httpURLConnection.setConnectTimeout(ContactListActivity.APPLICATION_PARAMETERS.NETWORK_CONNECTION_CONNECT_TIMEOUT);
//			httpURLConnection.setReadTimeout(ActivityManager.GLOBAL_PARAMETERS.NETWORK_CONNECTION_READ_TIMEOUT);
//			httpURLConnection.connect();
//			
//			//gets the input stream of the HTTP connection 
//			InputStream inputStream = httpURLConnection.getInputStream();
//	
//			//holds a buffer reader
//			BufferedInputStream bis = new BufferedInputStream(inputStream,ActivityManager.GLOBAL_PARAMETERS.PHOTO_STREAM_INITIAL_BUFFER_SIZE);
//			
//			//gets the width of the original contact's photo
//			return BitmapFactory.decodeStream(bis);
//		}
//		catch (Throwable t) 
//		{
//			return null;
//		}
//		finally
//		{
//			if (httpURLConnection != null)
//			{
//				try
//				{
//					httpURLConnection.disconnect();						
//				}
//				catch (Throwable t)
//				{
//					//#ifdef ERROR
//					Utils.error("ApplicationUtilities.getPhotoFromWebURL() - cannot disconnect from HTTP server ", t);
//					//#endif
//										
//				}
//			}
//		}
//	}

    /**
     * Return the photo data from a given Bitmap.
     *
     * @param photo (Bitmap != null) a bitmap file.
     * @return (byte[] != null) the photo's data.
     */
    public static byte[] getDataFromBitmap(Bitmap photo) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        photo.compress(CompressFormat.PNG, 0, out);
        return out.toByteArray();
    }
}
