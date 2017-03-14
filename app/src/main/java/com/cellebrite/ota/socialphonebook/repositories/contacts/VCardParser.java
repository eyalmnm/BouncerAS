package com.cellebrite.ota.socialphonebook.repositories.contacts;

import android.provider.ContactsContract;

import com.cellebrite.ota.socialphonebook.utilities.StringUtilities;
import com.em_projects.utils.Base64;
import com.em_projects.utils.QuotedPrintable;
import com.em_projects.utils.StringUtil;
import com.em_projects.utils.Utils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;

import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 * Class provides API for parsing a contact.
 */
public final class VCardParser {
    //holds 'end of line' string
    private static final String NL = "\n";
    private final static String QUOTED_PRINTABLE_PATTERN = "ENCODING=QUOTED-PRINTABLE";
    private final static String CHARSET_UTF_8_PATTERN = "CHARSET=UTF-8";
    //holds the maximum number of chars in a single vCard line
    //TODO - move to application's params
    private static int VCardLineMaxChars = 70;

    /**
     * Returns a contact group (filled with details) from a given VCard.
     *
     * @param vCardString (String != null)
     * @return (ContactGroup != null)
     * @throws VCardParserException in case parser has failed.
     */
    public static ContactGroup getContactGroupFromVCard(String vCardString) throws VCardParserException {
        //get a default
        //TODO - should be set if not default by the parsing
        ContactGroup cg = ContactGroup.getDefaultGroup();

        try {
            //gets search start index (right after the vCard begin string)
            int searchStartIndex = vCardString.indexOf("BEGIN:VCARD");
            if (searchStartIndex == -1)
                throw new VCardParserException("parser cannot find BEGIN:VCARD");

            searchStartIndex += "BEGIN:VCARD".length();

            //holds vCard string's length
            int vCardStringLength = vCardString.length();

            //holds the next new line index in the vCard string
            int newLineIndex = vCardString.indexOf(NL, searchStartIndex);

            //holds new line string's length
            int newLineStringLength = NL.length();

            //as long as the vCard string contains another new line string
            //TODO - why the second condition is necessary
            while (newLineIndex != -1 && newLineIndex <= vCardStringLength) {
                //gets the index of the next colon in the vCard string
                int colonIndex = vCardString.indexOf(':', newLineIndex);

                //gets the name of the current vCard field (substring from the beginning of the line till the colon)
                String currentFieldName = vCardString.substring(newLineIndex + newLineStringLength, ++colonIndex);

                //holds the next new line index in the vCard string, to know where current field's value ends
                newLineIndex = vCardString.indexOf(NL, colonIndex);

                //in case current field's value continues to the next vCard string row
                while (newLineIndex != -1 && newLineIndex != (vCardStringLength - 1) && vCardString.charAt(newLineIndex + 1) == ' ') {
                    //gets the next new line index in the vCard string, to know where current field's value ends
                    newLineIndex = vCardString.indexOf(NL, newLineIndex + 1);
                }

                //gets the value of the current vCard field
                //(substring from the first character after the found colon, till the new line)
                String currentFieldValue = vCardString.substring(colonIndex, newLineIndex);

                //in case the current handled field is vCard's END field
                if (currentFieldName.equals("END:"))
                    break;
                    // phones
                else if (currentFieldName.startsWith("TEL")) {
                    addPhoneValueFromVcardToGroup(cg, currentFieldName, currentFieldValue);
                }
                // email
                else if (currentFieldName.startsWith("EMAIL")) {
                    addEmailValueFromVcardToGroup(cg, currentFieldName, currentFieldValue);
                }
                // addresses
                else if (currentFieldName.startsWith("ADR")) {

                    addAddressValueFromVcardToGroup(cg, currentFieldName, currentFieldValue);
                }
                // organization
                else if (currentFieldName.startsWith("ORG")) {
                    addOrganizationValueFromVcardToGroup(cg, currentFieldName, currentFieldValue);

                }
                // website
                else if (currentFieldName.startsWith("URL")) {
                    addWebsiteValueFromVcardToGroup(cg, currentFieldName, currentFieldValue);
                }
                // NickName
                else if (currentFieldName.startsWith("NICKNAME")) {
                    addNickNameValueFromVcardToGroup(cg, currentFieldName, currentFieldValue);
                }

            }


            //return the details
            return cg;
        } catch (Throwable t) {
            throw new VCardParserException("failed extracting/parsing details from VCard:" + vCardString);
        }
    }

    private static void addNickNameValueFromVcardToGroup(ContactGroup cg, String currentFieldName, String currentFieldValue) {
        //add NickName data to group
        DataArgs args = new DataArgs(ContactDataKinds.Nickname.KIND);
        args.addValue(ContactDataKinds.Nickname.NAME, currentFieldName);
        cg.addData(args);
    }

    private static void addWebsiteValueFromVcardToGroup(ContactGroup cg, String currentFieldName, String currentFieldValue) {
        //holds the WebSite type
        int webSiteType = 0;

        if (currentFieldName.indexOf(";HOME") != -1)
            webSiteType = ContactsContract.CommonDataKinds.Website.TYPE_HOME;
        else if (currentFieldName.indexOf(";WORK") != -1)
            webSiteType = ContactsContract.CommonDataKinds.Website.TYPE_WORK;
        else if (currentFieldName.indexOf(";BLOG") != -1)
            webSiteType = ContactsContract.CommonDataKinds.Website.TYPE_BLOG;
        else if (currentFieldName.indexOf(";FTP") != -1)
            webSiteType = ContactsContract.CommonDataKinds.Website.TYPE_FTP;
        else if (currentFieldName.indexOf(";HOMEPAGE") != -1)
            webSiteType = ContactsContract.CommonDataKinds.Website.TYPE_HOMEPAGE;
        else if (currentFieldName.indexOf(";PROFILE") != -1)
            webSiteType = ContactsContract.CommonDataKinds.Website.TYPE_PROFILE;
        else if (currentFieldName.indexOf(";CUSTOM") != -1)
            webSiteType = ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM;
        else
            webSiteType = ContactsContract.CommonDataKinds.Website.TYPE_OTHER;

        DataArgs args = new DataArgs(ContactDataKinds.Website.KIND);
        args.addValue(ContactDataKinds.Website.TYPE, webSiteType);
        args.addValue(ContactDataKinds.Website.URL, currentFieldValue);

        //add WebSite data to group
        cg.addData(args);
    }

    private static void addOrganizationValueFromVcardToGroup(ContactGroup cg, String currentFieldName, String currentFieldValue) {
        String[] organizationFields;

        organizationFields = StringUtilities.split(currentFieldValue, ";");

        DataArgs organization = new DataArgs(ContactDataKinds.Organization.KIND);

        if (organizationFields.length > 0) {
            organization.addValue(ContactDataKinds.Organization.COMPANY, organizationFields[0]);
        }
        if (organizationFields.length > 1) {
            organization.addValue(ContactDataKinds.Organization.TITLE, organizationFields[1]);
        }


        int organizationType = 0;

        // set default type if type not exist
        if (organizationFields.length < 3)
            organizationType = ContactsContract.CommonDataKinds.Organization.TYPE_WORK;

            // for client data

        else {
            //set the organization type
            if (currentFieldName.indexOf(";WORK") != -1)
                organizationType = ContactsContract.CommonDataKinds.Organization.TYPE_WORK;
            else if (currentFieldName.indexOf(";CUSTOM") != -1)
                organizationType = ContactsContract.CommonDataKinds.Organization.TYPE_CUSTOM;
            else
                organizationType = ContactsContract.CommonDataKinds.Organization.TYPE_OTHER;

        }

        //set the organization type
        organization.addValue(ContactDataKinds.Organization.TYPE, organizationType);

        //add organization data to group
        cg.addData(organization);

    }

    private static void addAddressValueFromVcardToGroup(ContactGroup cg, String currentFieldName, String currentFieldValue) {
        byte[] decodedByteArray = null;
        if (currentFieldName.indexOf(QUOTED_PRINTABLE_PATTERN) != -1) {
            try {
                decodedByteArray = QuotedPrintableCodec.decodeQuotedPrintable(currentFieldValue.getBytes());
            } catch (DecoderException e) {
                //#ifdef ERROR
                Utils.error("VCardParser.addAddressValueFromVcardToGroup() - decoding QuotedPrintable stream has failed, exception throwen ", e);
                //#endif
            }
        }

        String[] addressFields;
        if (decodedByteArray != null) {
            if (currentFieldName.indexOf(CHARSET_UTF_8_PATTERN) != -1) {
                try {
                    addressFields = StringUtilities.split(new String(decodedByteArray, "UTF-8"), ";");
                } catch (UnsupportedEncodingException e) {
                    addressFields = StringUtilities.split(new String(decodedByteArray), ";");
                }
            } else {
                addressFields = StringUtilities.split(new String(decodedByteArray), ";");
            }
        } else {
            addressFields = StringUtilities.split(currentFieldValue, ";");
        }

        //set postal address
        DataArgs address = new DataArgs(ContactDataKinds.Addresses.Postal.KIND);
        address.addValue(ContactDataKinds.Addresses.Postal.POBOX, addressFields[0]);
        address.addValue(ContactDataKinds.Addresses.Postal.STREET, addressFields[2]);
        address.addValue(ContactDataKinds.Addresses.Postal.CITY, addressFields[3]);
        address.addValue(ContactDataKinds.Addresses.Postal.REGION, addressFields[4]);
        address.addValue(ContactDataKinds.Addresses.Postal.POSTCODE, addressFields[5]);
        address.addValue(ContactDataKinds.Addresses.Postal.COUNTRY, addressFields[6]);

        //holds the address type
        int addressType = 0;

        if (currentFieldName.indexOf(";HOME") != -1) {
            addressType = ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME;
        } else if (currentFieldName.indexOf(";WORK") != -1) {
            addressType = ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK;
        } else if (currentFieldName.indexOf(";CUSTOM") != -1) {
            addressType = ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM;
        } else {
            addressType = ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER;
        }

        //set the address type
        address.addValue(ContactDataKinds.Addresses.Postal.TYPE, addressType);

        //add address data to group
        cg.addData(address);

    }

    private static void addEmailValueFromVcardToGroup(ContactGroup cg, String currentFieldName, String currentFieldValue) {
        int emailType = 0;

        if (currentFieldName.indexOf(";HOME") != -1)
            emailType = ContactsContract.CommonDataKinds.Email.TYPE_HOME;
        else if (currentFieldName.indexOf(";WORK") != -1)
            emailType = ContactsContract.CommonDataKinds.Email.TYPE_WORK;
        else if (currentFieldName.indexOf(";CELL") != -1)
            emailType = ContactsContract.CommonDataKinds.Email.TYPE_MOBILE;
        else
            emailType = ContactsContract.CommonDataKinds.Email.TYPE_OTHER;


        DataArgs email = new DataArgs(ContactDataKinds.Addresses.Email.KIND);
        email.addValue(ContactDataKinds.Addresses.Email.TYPE, emailType);
        email.addValue(ContactDataKinds.Addresses.Email.DATA, currentFieldValue);

        //add email data to group
        cg.addData(email);

    }

    private static void addPhoneValueFromVcardToGroup(ContactGroup cg, String currentFieldName, String currentFieldValue) {
        DataArgs phone = new DataArgs(ContactDataKinds.Phone.KIND);
        phone.addValue(ContactDataKinds.Phone.NUMBER, currentFieldValue);
        int phoneType = 0;

        if (currentFieldName.indexOf(";FAX") != -1) {
            if (currentFieldName.indexOf(";HOME") != -1)
                phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME;
            else if (currentFieldName.indexOf(";WORK") != -1)
                phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK;
            else
                phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX;


        } else if (currentFieldName.startsWith("TEL;CELL")) {
            if (currentFieldName.indexOf(";WORK") != -1)
                phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE;
            else
                phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;


        } else if (currentFieldName.startsWith("TEL;HOME"))
            phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_HOME;

        else if (currentFieldName.startsWith("TEL;WORK")) {
            if (currentFieldName.indexOf(";CELL") != -1)
                phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE;
            else if (currentFieldName.indexOf(";PAGER") != -1)
                phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER;
            else
                phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_WORK;

        } else if (currentFieldName.startsWith("TEL;CAR"))
            phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_CAR;

        else if (currentFieldName.startsWith("TEL;PAGER")) {
            if (currentFieldName.indexOf(";WORK") != -1)
                phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER;
            else
                phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_PAGER;


        } else if (currentFieldName.startsWith("TEL;ISDN"))
            phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_ISDN;

        else
            phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_OTHER;

        //set the phone type
        phone.addValue(ContactDataKinds.Phone.TYPE, phoneType);

        //add phone data to group
        cg.addData(phone);

    }

    /**
     * Returns a byte array containing the photo data if exists in the VCard.
     *
     * @param vCardString
     * @return (byte[]) containing the photo data or null if no photo was found in the VCards.
     * @throws VCardParserException in case parser has failed.
     */
    public static byte[] getPhotoDataFromVCard(String vCardString) throws VCardParserException {
        try {
            //gets search start index (right after the vCard begin string)
            int searchStartIndex = vCardString.indexOf("BEGIN:VCARD");
            if (searchStartIndex == -1)
                throw new VCardParserException("parser cannot find BEGIN:VCARD");

            searchStartIndex += "BEGIN:VCARD".length();

            //holds vCard string's length
            int vCardStringLength = vCardString.length();

            //holds the next new line index in the vCard string
            int newLineIndex = vCardString.indexOf(NL, searchStartIndex);

            //holds new line string's length
            int newLineStringLength = NL.length();

            //as long as the vCard string contains another new line string
            //TODO - why the second condition is necessary
            while (newLineIndex != -1 && newLineIndex <= vCardStringLength) {
                //gets the index of the next colon in the vCard string
                int colonIndex = vCardString.indexOf(':', newLineIndex);

                //gets the name of the current vCard field (substring from the beginning of the line till the colon)
                String currentFieldName = vCardString.substring(newLineIndex + newLineStringLength, ++colonIndex);

                //holds the next new line index in the vCard string, to know where current field's value ends
                newLineIndex = vCardString.indexOf(NL, colonIndex);

                //in case current field's value continues to the next vCard string row
                while (newLineIndex != -1 && newLineIndex != (vCardStringLength - 1) && vCardString.charAt(newLineIndex + 1) == ' ') {
                    //gets the next new line index in the vCard string, to know where current field's value ends
                    newLineIndex = vCardString.indexOf(NL, newLineIndex + 1);
                }

                //gets the value of the current vCard field
                //(substring from the first character after the found colon, till the new line)
                String currentFieldValue = vCardString.substring(colonIndex, newLineIndex);

                //in case the current handled field is vCard's END field
                if (currentFieldName.equals("END:"))
                    break;

                    //in case the current handled field is vCard's photo field
                else if (currentFieldName.startsWith("PHOTO")) {
                    //holds photo's data
                    StringBuffer photoData = new StringBuffer(currentFieldValue);

                    //in case the next character after the next new line string is an empty space character
                    while (vCardString.charAt(newLineIndex + 1) == ' ') {
                        //gets the next line's value (it is still the photo's data)
                        String morePhotoData = vCardString.substring(newLineIndex + 2, (newLineIndex = vCardString.indexOf(NL, newLineIndex + 2)));

                        //adds it to photo's data
                        photoData.append(morePhotoData);
                    }

                    //sets contact's photo data (after decoding it, since it's encoded in Base64 encoding)
                    return Base64.decode(photoData.toString().getBytes());
                }
            }
        } catch (Throwable t) {
            throw new VCardParserException("failed extracting/parsing photo from VCard:" + vCardString);
        }

        //else return null in case no photo was found
        return null;
    }

    /**
     * Returns a contact from a given VCard.
     *
     * @param vCardString (String != null)
     * @return (Contact != null)
     * @throws VCardParserException in case parser has failed.
     */
    public static DeviceContact getContactFromVCard(String vCardString) throws VCardParserException {
        //create a default contact
        DeviceContact c = new DeviceContact("", "", false);

        try {
            //gets search start index (right after the vCard begin string)
            int searchStartIndex = vCardString.indexOf("BEGIN:VCARD");
            if (searchStartIndex == -1)
                throw new VCardParserException("parser cannot find BEGIN:VCARD");

            searchStartIndex += "BEGIN:VCARD".length();

            //holds vCard string's length
            int vCardStringLength = vCardString.length();

            //holds the next new line index in the vCard string
            int newLineIndex = vCardString.indexOf(NL, searchStartIndex);

            //holds new line string's length
            int newLineStringLength = NL.length();

            //as long as the vCard string contains another new line string
            //TODO - why the second condition is necessary
            while (newLineIndex != -1 && newLineIndex <= vCardStringLength) {
                //gets the index of the next colon in the vCard string
                int colonIndex = vCardString.indexOf(':', newLineIndex);

                //gets the name of the current vCard field (substring from the beginning of the line till the colon)
                String currentFieldName = vCardString.substring(newLineIndex + newLineStringLength, ++colonIndex);

                //holds the next new line index in the vCard string, to know where current field's value ends
                newLineIndex = vCardString.indexOf(NL, colonIndex);

                //in case current field's value continues to the next vCard string row
                while (newLineIndex != -1 && newLineIndex != (vCardStringLength - 1) && vCardString.charAt(newLineIndex + 1) == ' ') {
                    //gets the next new line index in the vCard string, to know where current field's value ends
                    newLineIndex = vCardString.indexOf(NL, newLineIndex + 1);
                }

                //gets the value of the current vCard field
                //(substring from the first character after the found colon, till the new line)
                String currentFieldValue = vCardString.substring(colonIndex, newLineIndex);

                //in case the current handled field is vCard's END field
                if (currentFieldName.equals("END:"))
                    break;

                //in case the current handled field is vCard's full name field
                if (currentFieldName.equals("FN:")) {
                    //sets contact's display name
                    c.m_displayName = currentFieldValue;
                }
                //in case the current handled field is vCard's full name field, decoded in quoted printable
                else if (currentFieldName.equals("FN;CHARSET=UTF-8:")) {
                    try {
                        //decodes field's value and sets the result as contact's display name
                        String decodedFieldValue = QuotedPrintable.decode(currentFieldValue.getBytes("UTF-8"), "UTF-8");
                        c.m_displayName = decodedFieldValue;
                    } catch (UnsupportedEncodingException uee) {
                        //TODO - exception should be thrown ?
                    }
                }
//            	//in case the current handled field is vCard's full name field, decoded in quoted printable
//                else if (currentFieldName.equals("FN;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:"))
//                {
//                    try
//                    {
//                    	//decodes field's value and sets the result as contact's display name
//                        String decodedFieldValue = QuotedPrintable.decode(currentFieldValue.getBytes("UTF-8"), "UTF-8");
//                        c.m_displayName = decodedFieldValue;
//                    }
//                    catch(UnsupportedEncodingException uee)
//                    {
//                    	//TODO - exception should be thrown ?
//                    }
//                }

                //in case the current handled field is vCard's name field
                else if (currentFieldName.equals("N:")) {
                    //splits field's value to contact's first and last name
                    String[] firstAndLastNames = StringUtilities.split(currentFieldValue, ";");

                    //Note: we set only the first given name.
                    //the others are ignored in input and will not be overridden on the server in output
                    c.m_firstName = firstAndLastNames[1];
                    c.m_lastName = firstAndLastNames[0];

                    //sets contact's display name
                    StringBuffer dispNameSB = new StringBuffer(c.m_firstName);
                    if (!StringUtil.isNullOrEmpty(c.m_firstName) && !StringUtil.isNullOrEmpty(c.m_lastName))
                        dispNameSB.append(" ");
                    dispNameSB.append(c.m_lastName);
                    c.m_displayName = dispNameSB.toString();
                } else if (currentFieldName.equals("N;CHARSET=UTF-8:")) {
                    try {
                        //decodes field's value and sets the result as contact's first and last names after splitting it
                        String decodedFieldValue = QuotedPrintable.decode(currentFieldValue.getBytes("UTF-8"), "UTF-8");
                        String[] firstAndLastNames = StringUtilities.split(decodedFieldValue, ";");

                        //Note: we set only the first given name. t
                        //		he others are ignored in input and will not be overridden on the server in output
                        c.m_firstName = firstAndLastNames[1];
                        c.m_lastName = firstAndLastNames[0];
                    } catch (UnsupportedEncodingException uee) {
                        //TODO - exception should be thrown ?
                    }
                }
//            	//in case the current handled field is vCard's name field, decoded in quoted printable
//            	else if (currentFieldName.equals("N;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:"))
//                {
//                	try
//                    {
//                		//decodes field's value and sets the result as contact's first and last names after splitting it
//                        String decodedFieldValue = QuotedPrintable.decode(currentFieldValue.getBytes("UTF-8"), "UTF-8");
//                        String[] firstAndLastNames = StringUtilities.split(decodedFieldValue, ";");
//
//                        //Note: we set only the first given name. t
//                        //		he others are ignored in input and will not be overridden on the server in output
//                        c.m_firstName = firstAndLastNames[1];
//                        c.m_lastName = firstAndLastNames[0];
//                    }
//                	catch (UnsupportedEncodingException uee)
//                	{
//                		//TODO - exception should be thrown ?
//                    }
//                }
//            	else if(currentFieldName.contains("NOTE"))
//            	{
//            	    if(currentFieldName.contains("ENCODING=QUOTED-PRINTABLE"))
//            	    {
//            	      //decodes field's value and sets the result as contact's first and last names after splitting it
//            	        currentFieldValue = QuotedPrintable.decode(currentFieldValue.getBytes("UTF-8"), "UTF-8");
//            	    }
//            	    c.setNote(currentFieldValue);
//            	}
            }

            //return the contact
            return c;
        } catch (Throwable t) {
            throw new VCardParserException("failed extracting/parsing contact from VCard:" + vCardString);
        }
    }

    /**
     * Creates a VCard from the given data.
     *
     * @param contact   (Cotnact != null) a Contact object.
     * @param groups    (Vector<ContactGroup>) the contact groups.
     * @param photoData (byte[]) the photo data or null for no photo.
     * @return (String) vcard string.
     */
    static String createContactVCard(DeviceContact contact, Vector<ContactGroup> groups, byte[] photoData) {
        return createContactVCard(contact, groups, photoData, true);
    }

    /**
     * Creates a VCard from the given data.
     *
     * @param contact           (Cotnact != null) a Contact object.
     * @param groups            (Vector<ContactGroup>) the contact groups.
     * @param photoData         (byte[]) the photo data or null for no photo.
     * @param encodeDisplayName (boolean) whether to encode display name in UTF-8 format or not.
     * @return (String) vcard string.
     */
    static String createContactVCard(DeviceContact contact, Vector<ContactGroup> groups, byte[] photoData, boolean encodeDisplayName) {
        //holds the vCard string
        StringBuffer vCardString = new StringBuffer();

        //adds vCard's BEGIN and VERSION tags
        vCardString.append("BEGIN:VCARD").append(NL);
        vCardString.append("VERSION:2.1").append(NL);

        //holds the space splitted first name and last name
        //this was done during device bug when First and Last name was concated to m_lastName
        String FirstNameToSend = "", LastNameToSend = "";

        if (contact.m_displayName != null) {
            if (encodeDisplayName)
                appendField(vCardString, "FN;CHARSET=UTF-8:", contact.m_displayName, false);
            else
                appendField(vCardString, "FN:", contact.m_displayName, false);
        }

        //when space character exist in name
        //when first name contains more than one word and last name is empty
        if (contact.m_firstName != null && contact.m_firstName.trim().contains(" ") && (contact.m_lastName == null || contact.m_lastName.trim().length() == 0)) {
            FirstNameToSend = contact.m_firstName.split(" ")[0];
            LastNameToSend = contact.m_firstName.substring(FirstNameToSend.length() + 1);
        }
        //when first name contains more than one word and first name is empty
        else if (contact.m_lastName != null && contact.m_lastName.trim().contains(" ") && (contact.m_firstName == null || contact.m_firstName.trim().length() == 0)) {
            FirstNameToSend = contact.m_lastName.split(" ")[0];
            LastNameToSend = contact.m_lastName.substring(FirstNameToSend.length() + 1);
        } else {
            FirstNameToSend = contact.m_firstName;
            LastNameToSend = contact.m_lastName;
        }

        //adds last and first name
        String fullName = new StringBuffer().append((LastNameToSend != null) ? LastNameToSend.replace("\n", "\n ") : "")
                .append(";").append((FirstNameToSend != null) ? FirstNameToSend.replace("\n", "\n ") : "")
                .append(";").append(";").append(";").toString();

        //adds last and first name								    .append(";").append(";").toString();
        if (encodeDisplayName)
            appendField(vCardString, "N;CHARSET=UTF-8:", fullName, false);
        else
            appendField(vCardString, "N:", fullName, false);

        //TODO - extract to methods!
        //if groups are given
        if (groups != null) {
            //handle phones
            for (ContactGroup cg : groups) {
                try {
                    //get the phones in this group
                    Vector<DataArgs> args = cg.getData(ContactDataKinds.Phone.KIND);
                    appendPhoneData(vCardString, args);

                    //get the postals in this group
                    args = cg.getData(ContactDataKinds.Addresses.Postal.KIND);
                    appendStructuredPostalData(vCardString, args);

                    //get the emails in this group
                    args = cg.getData(ContactDataKinds.Addresses.Email.KIND);
                    appendEmailData(vCardString, args);

                    //get the organization details in this group
                    args = cg.getData(ContactDataKinds.Organization.KIND);
                    appendOrganizationData(vCardString, args);

                    //get the Website details in this group
                    args = cg.getData(ContactDataKinds.Website.KIND);
                    appendWebsiteData(vCardString, args);

//		        	//get the nickname details in this group
//		        	args = cg.getData(ContactDataKinds.Nickname.KIND);
//		        	appendNicknameData(vCardString, args);

                    //get the Note details in this group
//		        	args = cg.getData(ContactDataKinds.Note.KIND);
//		        	appendNoteData(vCardString, args);

                    //get the Event details in this group
//		        	args = cg.getData(ContactDataKinds.Event.KIND);
//		        	appendEventData(vCardString, args);
                } catch (Exception e) {

                    //#ifdef ERROR
                    Utils.error("VCarParse.createContactVCard() - failed :" + e);
                    //#endif

                }
            }

        }
        //adds contact's photo (encoded in Base64 encoding) in case it exists
        if (photoData != null)
            appendField(vCardString, "PHOTO;ENCODING=BASE64;TYPE=JPG:", new String(Base64.encode(photoData)), false);

        //adds the END tag
        vCardString.append("END:VCARD").append(NL);

        //returns the vCard string
        return new String(vCardString);

    }

    // add phone data
    private static void appendPhoneData(StringBuffer vCardString, Vector<DataArgs> dataArgs) {
        //check every phone
        for (DataArgs phoneArg : dataArgs) {
            //get the phone value
            int phoneType = phoneArg.getValue(ContactDataKinds.Phone.TYPE);
            String phoneNumber = phoneArg.getValue(ContactDataKinds.Phone.NUMBER);
            StringBuffer phoneFormat = new StringBuffer().append("TEL;");

            //check the type of phone and write according to the type
            switch (phoneType) {
                case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                    phoneFormat.append("CELL;");
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                    phoneFormat.append("VOICE;HOME");
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME:
                    phoneFormat.append("HOME;FAX;");
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK:
                    phoneFormat.append("WORK;FAX;");
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                    phoneFormat.append("VOICE;WORK;");
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_PAGER:
                    phoneFormat.append("PAGER;");
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
                    phoneFormat.append("VOICE;");
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_CAR:
                    phoneFormat.append("CAR;");
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_ISDN:
                    phoneFormat.append("ISDN;");
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX:
                    phoneFormat.append("FAX;");
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:
                    phoneFormat.append("WORK;CELL");
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER:
                    phoneFormat.append("WORK;PAGER");
                    break;
            }
            //add UTF-8 charset
            phoneFormat.append("CHARSET=UTF-8:");

            //append field to VCard
            appendField(vCardString, phoneFormat.toString(), phoneNumber, false);
        }
    }


//    private static void appendEventData(StringBuffer vCardString,Vector<DataArgs> dataArgs) 
//    {
//      	//check every event detail
//    	for (DataArgs eventArg : dataArgs)
//    	{
//    		
//    		//get the event value 
//    		Event event = eventArg.getValue();
//    		String eventdate = event.getStartDate();
//        	
//        	//event format
//        	String eventFormat = "BDAY:";
//
//        	//append field to VCard
//            appendField(vCardString, eventFormat, eventdate, false);
//    	}
//
//	}

//	private static void appendNoteData(StringBuffer vCardString,Vector<DataArgs> dataArgs) 
//	{
//      	//check every note detail
//    	for (DataArgs noteArg : dataArgs)
//    	{
//    		//get the event value 
//    		String note = noteArg.getValue();
//
//        	//event format
//        	String noteFormat = "NOTE;CHARSET=UTF-8:";
//
//        	//append field to VCard
//            appendField(vCardString, noteFormat, note, true);
//    	}
//		
//	}

//	private static void appendNicknameData(StringBuffer vCardString,Vector<DataArgs> dataArgs) 
//	{
//	  //check every phone
//        for (DataArgs nickArg : dataArgs)
//        {
//            //get the Nickname value 
//            NickName nickname = nickArg.getValue();
//            
//            String nickNameFormat = "NICKNAME;CHARSET=UTF-8:";
//            String name = nickname.getName();
//            
//            //append field to VCard
//            appendField(vCardString, nickNameFormat, name, false);
//
//        }
//		
//	}

    // add structured postal
    private static void appendStructuredPostalData(StringBuffer vCardString, Vector<DataArgs> dataArgs) {
        //check every postal
        for (DataArgs postal : dataArgs) {
            //get the postal value
            StringBuffer postalFormat = new StringBuffer().append("ADR;");
            StringBuffer postalValue = new StringBuffer()
                    .append(replaceNullWithEmptyString(postal.getValue(ContactDataKinds.Addresses.Postal.POBOX))).append(";;")
                    .append(replaceNullWithEmptyString(postal.getValue(ContactDataKinds.Addresses.Postal.STREET))).append(";")
                    .append(replaceNullWithEmptyString(postal.getValue(ContactDataKinds.Addresses.Postal.CITY))).append(";")
                    .append(replaceNullWithEmptyString(postal.getValue(ContactDataKinds.Addresses.Postal.REGION))).append(";")
                    .append(replaceNullWithEmptyString(postal.getValue(ContactDataKinds.Addresses.Postal.POSTCODE))).append(";")
                    .append(replaceNullWithEmptyString(postal.getValue(ContactDataKinds.Addresses.Postal.COUNTRY))).append(";");

            int postalType = postal.getValue(ContactDataKinds.Addresses.Postal.TYPE);

            switch (postalType) {
                case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME:
                    postalFormat.append("HOME");
                    break;

                case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK:
                    postalFormat.append("WORK");
                    break;

                case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM:
                    postalFormat.append("CUSTOM");
                    break;
            }

            //add UTF-8 charset
            postalFormat.append(":");

            //append field to VCard
            appendField(vCardString, postalFormat.toString(), postalValue.toString(), false);
        }
    }

    // adds email data
    private static void appendEmailData(StringBuffer vCardString, Vector<DataArgs> dataArgs) {
        //check every email
        for (DataArgs emailArg : dataArgs) {
            //get the email value
            int emailType = emailArg.getValue(ContactDataKinds.Addresses.Email.TYPE);

            //email address
            String emailAddr = emailArg.getValue(ContactDataKinds.Addresses.Email.DATA);

            //the email format for VCard
            StringBuffer emailFormat = new StringBuffer().append("EMAIL;");

            //check the type of phone and write according to the type
            switch (emailType) {
                case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
                    emailFormat.append("HOME;");
                    break;
                case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
                    emailFormat.append("WORK;");
                    break;
                case ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM:
                    emailFormat.append("CUSTOM;");
                    break;
                case ContactsContract.CommonDataKinds.Email.TYPE_MOBILE:
                    emailFormat.append("CELL;");
                    break;
            }

            //add UTF-8 charset
            emailFormat.append("CHARSET=UTF-8:");

            //append field to VCard
            appendField(vCardString, emailFormat.toString(), emailAddr, false);
        }
    }

    // adds organization data
    private static void appendOrganizationData(StringBuffer vCardString, Vector<DataArgs> dataArgs) {
        //check every organization detail
        for (DataArgs orgArg : dataArgs) {

            //get the org value
            int orgType = orgArg.getValue(ContactDataKinds.Organization.TYPE);
            StringBuffer orgValue = new StringBuffer().
                    append(replaceNullWithEmptyString(orgArg.getValue(ContactDataKinds.Organization.COMPANY))).
                    append(";").
                    append(replaceNullWithEmptyString(orgArg.getValue(ContactDataKinds.Organization.TITLE)));

            String orgTypeValue = "";

            //check the type of phone and write according to the type
            switch (orgType) {
                case ContactsContract.CommonDataKinds.Organization.TYPE_OTHER: {
                    orgTypeValue = "OTHER;";
                }
                break;
                case ContactsContract.CommonDataKinds.Organization.TYPE_WORK: {
                    orgTypeValue = "WORK;";
                }
                break;
                case ContactsContract.CommonDataKinds.Organization.TYPE_CUSTOM: {
                    orgTypeValue = "CUSTOM;";
                }
                break;


            }

            //organization format
            StringBuffer orgFormat = new StringBuffer().append("ORG;").
                    append(orgTypeValue).
                    append("CHARSET=UTF-8:");

            //append field to VCard
            appendField(vCardString, orgFormat.toString(), orgValue.toString(), false);

        }
    }

    // adds website data
    private static void appendWebsiteData(StringBuffer vCardString, Vector<DataArgs> dataArgs) {
        //check every organization detail
        for (DataArgs webArg : dataArgs) {
            //get the WebSite value
            int webSiteType = webArg.getValue(ContactDataKinds.Website.TYPE);
            String url = webArg.getValue(ContactDataKinds.Website.URL);

            //the email format for VCard
            StringBuffer webSiteFormat = new StringBuffer().append("EMAIL;");

            String webTypeValue = "";

            //check the type of website and write according to the type
            switch (webSiteType) {
                case ContactsContract.CommonDataKinds.Website.TYPE_BLOG: {
                    webTypeValue = "BLOG;";
                }
                break;
                case ContactsContract.CommonDataKinds.Website.TYPE_FTP: {
                    webTypeValue = "FTP;";
                }
                break;
                case ContactsContract.CommonDataKinds.Website.TYPE_HOME: {
                    webTypeValue = "HOME;";
                }
                break;
                case ContactsContract.CommonDataKinds.Website.TYPE_HOMEPAGE: {
                    webTypeValue = "HOMEPAGE;";
                }
                break;
                case ContactsContract.CommonDataKinds.Website.TYPE_OTHER: {
                    webTypeValue = "OTHER;";
                }
                break;
                case ContactsContract.CommonDataKinds.Website.TYPE_PROFILE: {
                    webTypeValue = "PROFILE;";
                }
                break;
                case ContactsContract.CommonDataKinds.Website.TYPE_WORK: {
                    webTypeValue = "WORK;";
                }
                break;

            }

            if (url != null && url.length() > 0) {
                StringBuffer websiteFormat = new StringBuffer().append("URL;").
                        append(webTypeValue).
                        append("CHARSET=UTF-8:");
                //append field to VCard
                appendField(vCardString, websiteFormat.toString(), url, false);
            }

        }
    }

    /**
     * Appends a field to vCard's StringBuffer.
     *
     * @param vCardStringBuffer (StringBuffer != null) vCard's string buffer.
     * @param fieldName         (String != null) vCard field's name.
     * @param fieldValue        (String != null) vCard field's value.
     * @param isQuotedPrintable (boolean) true in case the field should be encoded in quoted printable, false otherwise.
     */
    private static void appendField(StringBuffer vCardStringBuffer, String fieldName, String fieldValue, boolean isQuotedPrintable) {
        //in case there is no value for the current field
        if (fieldValue == null || fieldValue.trim().equals("")) {
            //appends field's name with no value
            //vCardStringBuffer.append(fieldName).append("").append(NL);
            return;
        }

        //in case the field should be encoded in quoted printable
//    	if(isQuotedPrintable)
//    		fieldValue = fieldValue.replace("\n", "\n ");

        //holds the name with the value
        StringBuffer nameAndValue = new StringBuffer(fieldName).append(fieldValue);

        if (nameAndValue.indexOf(NL) != -1) {
            String filteredNameAndValue = nameAndValue.toString();
            filteredNameAndValue = filteredNameAndValue.replace(NL, NL + " ");
            nameAndValue = new StringBuffer(filteredNameAndValue);
        }

        //in case field's length is longer than the maximum number of chars allowed in a single line
        else if (nameAndValue.length() > VCardLineMaxChars) {
            //appends a single line from the field's chars to the output buffer
            appendLongFieldSingleLine(nameAndValue, vCardStringBuffer, isQuotedPrintable);

            //as long as the length of the remain field's chars is longer than the maximum number of chars allowed in a single line
            while (nameAndValue.length() > VCardLineMaxChars) {
                //appends a single line from the field's chars to the output buffer
                appendLongFieldSingleLine(nameAndValue, vCardStringBuffer, isQuotedPrintable);
            }
        }

        //in the end of the vCard field, add a new line char so that the next field will start in a new lines
        vCardStringBuffer.append(nameAndValue).append(NL);
    }

    /**
     * Appends a field to vCard's StringBuffer.
     *
     * @param vCardStringBuffer (StringBuffer != null) vCard's string buffer.
     * @param fieldName         (String != null) vCard field's name.
     * @param fieldValue        (String != null) vCard field's value.
     * @param toReplace         (String) the string to replace in the field value.
     * @param replacement       (String) the string to put instead of the string to replace in the field value.
     * @param isQuotedPrintable (boolean) true in case the field should be encoded in quoted printable, false otherwise.
     */
    private static void appendField(StringBuffer vCardStringBuffer, String fieldName, String fieldValue, String toReplace, String replacement, boolean isQuotedPrintable) {
        //in case there is no value for the current field
        if (fieldValue == null || fieldValue.trim().equals("")) {
            //appends field's name with no value
            //vCardStringBuffer.append(fieldName).append("").append(NL);
            return;
        }

        //replaces the string to replace with the replacement string, if necessary
        fieldValue = fieldValue.replace(toReplace, replacement);

        //appends the field with its value to the vCard string
        appendField(vCardStringBuffer, fieldName, fieldValue, isQuotedPrintable);
    }

    /**
     * Appends a vCard field's single line to an output buffer.
     * <p>
     * In case the vCard field's remain chars length is longer than the maximum number of allowed chars in vCard single line,
     * the method appends only the maximum number of allowed chars to the output buffer, and deletes the appended chars
     * from the field's remain chars.
     * <p>
     * In case the vCard field's remain chars length is not longer than the maximum number of allowed char in vCard single line,
     * the method appends all of field's remain chars to the output buffer, and deletes the appended chars from the field's remain
     * chars, which will cause the field's remain chars to be empty.
     *
     * @param fieldRemainChars  (StringBuffer != null) the remain chars of the vCard field to append to the output buffer.
     * @param vCardStringBuffer (StringBuffer != null) vCard's string buffer.
     * @param isQuotedPrintable (boolean) true in case the field is encoded in quoted printable, false otherwise.
     */
    private static void appendLongFieldSingleLine(StringBuffer fieldRemainChars, StringBuffer vCardStringBuffer, boolean isQuotedPrintable) {
        //substrings the maximum number of chars from the beginning of the field, as the current line
        String currentLine = fieldRemainChars.substring(0, VCardLineMaxChars);

        //appends the current line text to the output
        vCardStringBuffer.append(currentLine);

//		//in case the field is encoded in quoted printable, adds the '=' char at the end of the current line
//		if(isQuotedPrintable)
//			vCardStringBuffer.append("=");

        //appends a new line to the output
        vCardStringBuffer.append(NL);

        //in case the field is not encoded in quoted printable, adds a space char in the beginning of the new line in the output
//		if(!isQuotedPrintable)
        vCardStringBuffer.append(" ");

        //deletes the current line's chars from the original field value
        fieldRemainChars.delete(0, VCardLineMaxChars);
    }

    private static String replaceNullWithEmptyString(Object object) {
        if (object == null)
            return "";

        return String.valueOf(object);
    }

    /**
     * Exception for parsing of VCard.
     */
    public static class VCardParserException extends Exception {
        public VCardParserException(String message) {
            super(message);
        }
    }
}
