package com.cellebrite.ota.socialphonebook.repositories.contacts;

import android.provider.ContactsContract;

/**
 * Holds the kinds of data that may exist for a contact.
 */
public final class ContactDataKinds {
    /**
     * A data kind representing the contact's proper name
     */
    public static final class StructuredName {
        //holds a unique kind number which represent the StructuredName kind
        public static final int KIND = 100;

        public static final String DISPLAY_NAME = ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME;
        public static final String GIVEN_NAME = ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME;
        public static final String FAMILY_NAME = ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME;
        public static final String PREFIX = ContactsContract.CommonDataKinds.StructuredName.PREFIX;
        public static final String MIDDLE_NAME = ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME;
        public static final String SUFFIX = ContactsContract.CommonDataKinds.StructuredName.SUFFIX;
        public static final String PHONETIC_GIVEN_NAME = ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME;
        public static final String PHONETIC_MIDDLE_NAME = ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME;
        public static final String PHONETIC_FAMILY_NAME = ContactsContract.CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME;
    }

    /**
     * A data kind representing a telephone number.
     */
    public static final class Phone {
        //holds a unique kind number which represent the Phone kind
        public static final int KIND = 101;

        public static final int TYPE_CUSTOM = ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM;
        public static final int TYPE_WORK = ContactsContract.CommonDataKinds.Phone.TYPE_WORK;
        public static final int TYPE_OTHER = ContactsContract.CommonDataKinds.Phone.TYPE_OTHER;
        public static final int TYPE_HOME = ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
        public static final int TYPE_FAX_HOME = ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME;
        public static final int TYPE_FAX_WORK = ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK;
        public static final int TYPE_MOBILE = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;

        public static String TYPE = ContactsContract.CommonDataKinds.Phone.TYPE;
        public static String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
    }

    /**
     * A data kind representing a general address.
     */
    public static final class Addresses {
        /**
         * Unknown address kind
         */
        public static final int UNKNOWN = 0;

        /**
         * A data kind representing an email address.
         */
        public static final class Email {
            //holds a unique kind number which represent the Email kind
            public static final int KIND = 102;

            public static final int TYPE_HOME = ContactsContract.CommonDataKinds.Email.TYPE_HOME;
            public static final int TYPE_WORK = ContactsContract.CommonDataKinds.Email.TYPE_WORK;
            public static final int TYPE_OTHER = ContactsContract.CommonDataKinds.Email.TYPE_OTHER;
            public static final String DISPLAY_NAME = ContactsContract.CommonDataKinds.Email.DISPLAY_NAME;
            public static final String DATA = ContactsContract.CommonDataKinds.Email.DATA;
            public static String TYPE = ContactsContract.CommonDataKinds.Email.TYPE;


        }

        /**
         * A data kind representing a postal address.
         */
        public static final class Postal {
            //holds a unique kind number which represent the Email kind
            public static final int KIND = 112;

            public static final int TYPE_HOME = ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME;
            public static final int TYPE_WORK = ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK;
            public static final int TYPE_OTHER = ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER;

            public static final String CITY = ContactsContract.CommonDataKinds.StructuredPostal.CITY;
            public static final String COUNTRY = ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY;
            public static final String FORMATTED_ADDRESS = ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS;
            public static final String NEIGHBORHOOD = ContactsContract.CommonDataKinds.StructuredPostal.NEIGHBORHOOD;
            public static final String POBOX = ContactsContract.CommonDataKinds.StructuredPostal.POBOX;
            public static final String POSTCODE = ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE;
            public static final String REGION = ContactsContract.CommonDataKinds.StructuredPostal.REGION;
            public static final String STREET = ContactsContract.CommonDataKinds.StructuredPostal.STREET;

            public static final String TYPE = ContactsContract.CommonDataKinds.StructuredPostal.TYPE;
        }
    }

    /**
     * A data kind representing an organization.
     */
    public static final class Organization {
        //holds a unique kind number which represent the organization kind
        public static final int KIND = 103;

        public static final int TYPE_CUSTOM = ContactsContract.CommonDataKinds.Organization.TYPE_CUSTOM;
        public static final int TYPE_WORK = ContactsContract.CommonDataKinds.Organization.TYPE_WORK;
        public static final int TYPE_OTHER = ContactsContract.CommonDataKinds.Organization.TYPE_OTHER;

        public static String COMPANY = ContactsContract.CommonDataKinds.Organization.COMPANY;
        public static String TYPE = ContactsContract.CommonDataKinds.Organization.TYPE;
        public static String LABEL = ContactsContract.CommonDataKinds.Organization.LABEL;
        public static String TITLE = ContactsContract.CommonDataKinds.Organization.TITLE;
        public static String DEPARTMENT = ContactsContract.CommonDataKinds.Organization.DEPARTMENT;
        public static String JOB_DESCRIPTION = ContactsContract.CommonDataKinds.Organization.JOB_DESCRIPTION;
        public static String SYMBOL = ContactsContract.CommonDataKinds.Organization.SYMBOL;
        public static String PHONETIC_NAME = ContactsContract.CommonDataKinds.Organization.PHONETIC_NAME;
        public static String OFFICE_LOCATION = ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION;
        public static String PHONETIC_NAME_STYLE = ContactsContract.CommonDataKinds.Organization.PHONETIC_NAME;
    }

    /**
     * A data kind representing an IM address
     */
    public static final class Im {
        //holds a unique kind number which represent the IM address kind
        public static final int KIND = 104;

        public static final int TYPE_CUSTOM = ContactsContract.CommonDataKinds.Im.TYPE_CUSTOM;
        public static final int TYPE_WORK = ContactsContract.CommonDataKinds.Im.TYPE_WORK;
        public static final int TYPE_OTHER = ContactsContract.CommonDataKinds.Im.TYPE_OTHER;
        public static final int TYPE_HOME = ContactsContract.CommonDataKinds.Im.TYPE_HOME;

        public static String DATA = ContactsContract.CommonDataKinds.Im.DATA;
        public static String TYPE = ContactsContract.CommonDataKinds.Im.TYPE;
        public static String LABEL = ContactsContract.CommonDataKinds.Im.LABEL;
        public static String PROTOCOL = ContactsContract.CommonDataKinds.Im.PROTOCOL;
        public static String CUSTOM_PROTOCOL = ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL;
    }

    /**
     * A data kind representing the contact's nickname. For example, for Bob Parr ("Mr. Incredible"):
     */
    public static final class Nickname {
        //holds a unique kind number which represent the Nickname kind
        public static final int KIND = 105;

        public static int TYPE_DEFAULT = ContactsContract.CommonDataKinds.Nickname.TYPE_DEFAULT;
        public static int TYPE_INITIALS = ContactsContract.CommonDataKinds.Nickname.TYPE_INITIALS;
        public static int TYPE_MAINDEN_NAME = ContactsContract.CommonDataKinds.Nickname.TYPE_MAINDEN_NAME;
        public static int TYPE_OTHER_NAME = ContactsContract.CommonDataKinds.Nickname.TYPE_OTHER_NAME;
        public static int TYPE_SHORT_NAME = ContactsContract.CommonDataKinds.Nickname.TYPE_SHORT_NAME;
        public static int TYPE_CUSTOM = ContactsContract.CommonDataKinds.Nickname.TYPE_CUSTOM;

        public static String TYPE = ContactsContract.CommonDataKinds.Nickname.TYPE;
        public static String LABEL = ContactsContract.CommonDataKinds.Nickname.LABEL;
        public static String NAME = ContactsContract.CommonDataKinds.Nickname.NAME;
    }

    /**
     * Notes about the contact.
     */
    public static final class Note {
        //holds a unique kind number which represent the Notes kind
        public static final int KIND = 106;

        public static String NOTE_TEXT = ContactsContract.CommonDataKinds.Note.NOTE;
    }

//	/**
//	 * A data kind representing a postal addresses.
//	 */
//	public static final class StructuredPostal
//	{
//		//holds a unique kind number which represent the StructuredPostal kind
//		public static final int KIND = 107 ;
//		
//		public static int FORMATTED_ADDRESS = 0;	
//		public static int TYPE = 1;	
//		public static int LABEL = 2;	
//		public static int STREET = 3;	
//		public static int POBOX = 4;	
//		public static int NEIGHBORHOOD = 5;	
//		public static int CITY = 6;	
//		public static int REGION = 7;	
//		public static int POSTCODE = 8;	
//		public static int COUNTRY = 9;	
//	}

    /**
     * Group Membership.
     */
    public static final class GroupMembership {
        //holds a unique kind number which represent the Group Membership kind
        public static final int KIND = 108;

        public static int GROUP_ROW_ID = 0;
        public static int GROUP_SOURCE_ID = 1;
    }

    /**
     * A data kind representing a web site related to the contact.
     */
    public static final class Website {
        //holds a unique kind number which represent the web site kind
        public static final int KIND = 109;

        public static int TYPE_BLOG = ContactsContract.CommonDataKinds.Website.TYPE_BLOG;
        public static int TYPE_FTP = ContactsContract.CommonDataKinds.Website.TYPE_FTP;
        public static int TYPE_HOME = ContactsContract.CommonDataKinds.Website.TYPE_HOME;
        public static int TYPE_HOMEPAGE = ContactsContract.CommonDataKinds.Website.TYPE_HOMEPAGE;
        public static int TYPE_OTHER = ContactsContract.CommonDataKinds.Website.TYPE_OTHER;
        public static int TYPE_PROFILE = ContactsContract.CommonDataKinds.Website.TYPE_PROFILE;
        public static int TYPE_WORK = ContactsContract.CommonDataKinds.Website.TYPE_WORK;
        public static int TYPE_CUSTOM = ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM;

        public static String URL = ContactsContract.CommonDataKinds.Website.URL;
        public static String TYPE = ContactsContract.CommonDataKinds.Website.TYPE;
        public static String LABEL = ContactsContract.CommonDataKinds.Website.LABEL;
    }

}
