package com.em_projects.utils;

//#ifdef Blackberry
//# import com.vario.infra.gui.AppGuiParams;
//#endif

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.em_projects.bouncer.BouncerApplication;
import com.em_projects.bouncer.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This class holds utilities for:
 * <p>
 * 1. translations
 * ===============
 * handles i18n issues of the application.
 * It supplies a translation by a key and parameters.
 * This is the way to support multiple languages for the application, and
 * get over grammar changes issues between different languages, without changing
 * the actual code.
 * <p>
 * Flow & Usage:
 * 1. upon application start, the properties file is parsed into a map of key
 * to value.
 * 2. when translation is needed, the caller passes a key and parameters,
 * and the translated string is returned.
 * <p>
 * 2. logging
 * ==========
 * handles logging in several level: debug, info and error.
 * all but error are ifdeffed-out in case DEBUG flag is not specified in build.xml.
 * <p>
 * 3. file handling
 * ================
 * handles file reading.
 * It supplies various file handling methods, s.a. reading textual file,
 * properties files management etc.
 * To be used by i18n implementation.
 * <p>
 * <p>
 * All held in one class file to save JAR size.
 */
public class Utils {
    /****************************************************/

    public static final String EOL = "\r\n";

    /****************************************************/
    /*                  File Handling                   */
    private static final String TAG = "Utils";
    //holds properties file delimiter
    private static final char s_delimeter = '=';

    //comment line is ignored, identified by '#' on line beginnig
    private static final String s_commentChar = "#";

    //#if INFO || NET || DEBUG || ERROR
    //holds debug log collection (for on device debugging)
//    public static Vector<String> s_debugLog = new Vector<String>();

    //holds debug log record store name
//    public static String s_logRecordStoreName = "LogRS";
    //#endif

    //holds a player
    //private static Player s_player = null;

    //holds a static Http connection element
//    private static HttpConnection s_con = null;

    //holds a static file connection element, 1 for folders and 1 for files
    private static File s_fileCon = null;
    private static File s_folderCon = null;

    //holds static input stream and output stream elements
    private static InputStream s_fileInputStream = null;
    private static OutputStream s_fileOutputStream = null;

//    public static final Boolean TRUE = Boolean.TRUE;
//    public static final Boolean FALSE = Boolean.FALSE;

    //holds true & false string values as static String objects (prevent multiple casting)
//    public static final String TRUE_STR = TRUE.toString();
//    public static final String FALSE_STR = FALSE.toString();

//    /**
//     * Compares two strings regardless their character case.
//     *
//     * @param str1 (String != null) first string to compare.
//     * @param str2 (String != null) second string to compare.
//     *
//     * @return (boolean) true if strings are equals regardless their character case (upper / lower case), false otherwise.
//     */
//    public static boolean equalsIgnoreCase(String str1, String str2)
//    {
//        return str1.equalsIgnoreCase(str2);
//    }
    /****************************************************/

    //holds the translations of the application.
    //initialized from the properties file upon startup.
    //structure: Hashtable<String:key, String:value>
    private static Hashtable s_stringsMap = null;

    /**
     * Handles reading from data file, into a representing string.
     *
     * @param filePath (String != null) the full path of the resource file.
     * @return (String != null) the data of the file, as a string.
     * @throws java.io.IOException is thrown if an error occures while handling the file.
     */
    public static String readFile(String filePath) throws IOException {
        //open an input stream to the file
        InputStream is = filePath.getClass().getResourceAsStream(filePath);

        //get its content into a string buffer
        StringBuffer sb = new StringBuffer();
        byte[] byteArr = new byte[256];

        while (is.read(byteArr, 0, byteArr.length) > 0) {
            sb.append(new String(byteArr));
        }

        //return the resulting string
        return sb.toString();
    }

    /****************************************************/
    /*                  Logging                         */
    /****************************************************/

    //#ifdef NET
    //# /**
    //# * Prints networking information.
    //# *
    //# * @param text (String != null) text to print as networking log.
    //# */
    //# public static void netLog(String text)
    //# {
    //# System.out.println(text);
//# 
    //#if INFO || NET || DEBUG || ERROR
    //# s_debugLog.addElement(text);
    //# //addRecordLog(s_logRecordStoreName, text);
    //#endif
    //# }
    //#endif

    //#ifdef DEBUG

    /**
     * Handles reading from data file, into a representing string.
     *
     * @param filePath (String != null) the full path of the file.
     * @param encoding (String != null) the file encoding.
     * @return (String != null) the data of the file, as a string.
     * @throws java.io.IOException is thrown if an error occures while handling the file.
     */
    public static String readFile(String filePath, String encoding) throws IOException {
        //open an input stream to the file
        InputStream is = filePath.getClass().getResourceAsStream(filePath);

        //creates the reader from the stream
        InputStreamReader isr = new InputStreamReader(is, encoding);

        //get its content into a string buffer
        StringBuffer sb = new StringBuffer();

        //#ifdef READ_FILE_CRLF_BUG
        //# //read the file bytes one after the other and replace the '\n' and '\r' chars to strings,
        //# //since some devices have bug when trying to read the data with inputStreamReader
        //#
        //# char[] buffer = new char[1];
        //#
        //# //as long as there are characters to read from the stream
        //# while (isr.read(buffer, 0, 1) > -1)
        //# {
        //#     //in case of a special char - '\n' or '\r', add it as s string
        //#     if (buffer[0] == '\n')
        //#         sb.append("\n");
        //#     else if (buffer[0] == '\r')
        //#         sb.append("\r");
        //#
        //#     //in case of a regular char, add it as usual
        //#     else
        //#         sb.append(buffer);
        //# }
        //#else

        char[] charArr = new char[256];

        while (isr.ready()) {
            int numOfBytes = isr.read(charArr, 0, charArr.length);
            String curStr = new String(charArr, 0, numOfBytes);
            sb.append(curStr);
        }

        //#endif

        //return the resulting string
        return sb.toString();
    }

    /**
     * Prints debug information.
     *
     * @param text (String != null) debug text to print.
     */
    public static void debug(String text) {
        Log.d("DEBUG", text);

        //#if INFO || NET || DEBUG || ERROR
//        s_debugLog.addElement(text);
        //addRecordLog(s_logRecordStoreName, text);
        //#endif
    }
    //#endif

    //#ifdef INFO

    /**
     * Prints debug information.
     *
     * @param classObj (Class != null) the class from which this method is called.
     * @param text     (String != null) debug text to print.
     */
    public static void debug(Class<?> classObj, String text) {
        Log.d(classObj.getSimpleName(), text);

        //#if INFO || NET || DEBUG || ERROR
//        s_debugLog.addElement(text);
        //addRecordLog(s_logRecordStoreName, text);
        //#endif
    }

    /**
     * Prints debug information.
     *
     * @param text (String != null) debug text to print.
     */
    public static void info(String text) {
        Log.i("INFO", text);

        //#if INFO || NET || DEBUG || ERROR
//        s_debugLog.addElement(text);
        //addRecordLog(s_logRecordStoreName, text);
        //#endif
    }
    //#endif


    //#ifdef ERROR

    /**
     * Prints debug information.
     *
     * @param text (String != null) debug text to print.
     */
    public static void info(Class<?> tag, String text) {
        Log.i(tag.getSimpleName(), text);

        //#if INFO || NET || DEBUG || ERROR
//        s_debugLog.addElement(text);
        //addRecordLog(s_logRecordStoreName, text);
        //#endif
    }

    /**
     * Prints error information.
     *
     * @param text (String != null) error text to print.
     */
    public static void error(String text) {
        error(text, null);
    }
    //#endif

    /****************************************************/
    /*                  Memory Management               */
    /****************************************************/

//    /**
//     * This method checks the run-time available memory.
//     * In case available memory is less than minimum available memory allowed,
//     * call the System.gc() method.
//     *
//     * @param context (String != null context of the call for this methos (relevant only for debug).
//     *
//     * @return (boolean) indication wheter or not the available memory is higher than the allowed limitaion.
//     */
//    public static boolean releaseMemoryIfNeeded(
//        //#if DEBUG
//        //# String context
//        //#endif
//        )
//	{
//
//        //allow context switch
//        Thread.yield();
//
//        try
//        {
//            //holds current runtime available memory
//            long availMem = Runtime.getRuntime().freeMemory();
//
//            if (availMem < ActivityManager.APP_PARAMS.MIN_AVAILABLE_MEM)
//            {
//                //#ifdef DEBUG
//                //# debug(context+": Runtime memory before gc: ("+availMem+"). calling System.gc()");
//                //#endif
//
//                System.gc();
//
//                //allow context switch
//                Thread.yield();
//
//                //re-load current runtime available memory
//                availMem = Runtime.getRuntime().freeMemory();
//
//                //#ifdef DEBUG
//                //# debug(context+": Runtime memory after gc: ("+availMem+").");
//                //#endif
//            }
//
//            return (availMem > ActivityManager.APP_PARAMS.MIN_AVAILABLE_MEM);
//        }
//
//        catch (Throwable t)
//        {
//            return true;
//        }
//    }

    /****************************************************/
	/*                  Translations                    */

    /**
     * Prints error information.
     *
     * @param text (String != null) error text to print.
     * @param t    (Throwable) the throwable exception / error.
     */
    public static void error(String text, Throwable t) {
//        //try to recover from the error
//		releaseMemoryIfNeeded(
//			//#if DEBUG
//			"error message - " + text
//			//#endif
//		);

        try {

            if (t != null) {
                String detalis = t.getMessage();
                if (detalis == null) {
                    detalis = t.getLocalizedMessage();
                }
                if (detalis == null) {
                    detalis = t.toString();
                }

                Log.e("ERROR", text + " " + (detalis == null ? "No details were provided!" : detalis));
            } else
                Log.e("ERROR", text);
        } catch (Throwable t1) {
            t1.printStackTrace();
        }
    }

//	/**
//	 * This method is responsible for getting all data from the properties file.
//	 * Then it is stored as a map of key to value, waiting to translation calls.
//	 *
//	 * @param fileName (String != null) the path to the propeties file holding
//	 * the translations & their representing keys.
//     * @param mapInitialCapacity (int) the initial capacity of the map (hashtable).
//     * @param isDefaultEncoding (boolean) true for using default encoding, false for using utf-8
//	 */
//	public static void initTranslations(String fileName, int mapInitialCapacity, boolean isDefaultEncoding)
//	{
//		try
//		{
//            //get the propeties file content
//            //note: change initial capacity in case config file content changes
//            s_stringsMap = readPropertiesFile(fileName, mapInitialCapacity, isDefaultEncoding);
//        }
//
//        catch (Exception e)
//		{
//            //#ifdef ERROR
//            error("Failed to read translations file !!!", e);
//            //#endif
//        }
//	}

    /**
     * Translates a string, without parameters.
     * Simply fetch the string from the translated strings table, and return it.
     * In case the string is not found in the map - return the key itself.
     *
     * @param key (String != null) the string's application representation
     * @return (String != null) the translated string, or the key (if key no
     * found on the translated strings table).
     */
    public static String translate(String key) {
        //try to fetch the translated string from the table
        Object value = s_stringsMap.get(key);

        //if not found - return the key itself
        if (value == null) {
            //#ifdef ERROR
            error("translate() Fail to find string for key (" + key + ")");
            //#endif

            return key;
        }

        //found - return it
        return (String) value;
    }

    /**
     * Translates a string, with a collection of parameters.
     * 1. fetch the string from the translated strings table
     * 2. place each parameter into it
     * In case the string is not found in the map - return the key itself.
     *
     * @param key    (String != null) the string's application representation
     * @param params (String[], size>0) the parameters to set into the string.
     * @return (String != null) the translated string, or the key (if key no
     * found on the translated strings table).
     */
    public static String translate(String key, String[] params) {
        //translate the string (according tothe key)
        String value = translate(key);

        //format the string
        return formatString(value, params);
    }

    /**
     * Handles string's formation (replace parameters with their values).
     *
     * @param str    (String != null) the original string
     * @param params (String[], size>0) the parameters to set into the string.
     * @return (String != null) the formatted string.
     */
    public static String formatString(String str, String[] params) {
        //use StringBuffer to handle string formation efficiently
        StringBuffer sb = new StringBuffer(str);

        //traverse the paraeters, replace their occurences in the string
        int numOfElements = params.length;
        for (int i = 0; i < numOfElements; ++i) {
            //init this 'round'
            String temp = sb.toString();
            sb.setLength(0);

            //locate parameter's index
            int index = temp.indexOf("{" + i + "}");
            if (index == -1) {
                //#ifdef DEBUG
                debug("formatString() failed to place param (" + i + ") for string (" + str + ").");
                //#endif

                return str;
            }

            //'replace' occurence
            sb.append(temp.substring(0, index));
            sb.append(params[i]);
            sb.append(temp.substring(index + 3));
        }

        //return the formatted string
        return sb.toString();
    }

    //#ifdef REV_HEBREW
//# 
//#    /****************************************************/
//#    /*                  Reverse Hebrew                   */
//#    /****************************************************/
//# 
//# 
    //# //set an object for synchronization
    //# private static final Object s_syncObject = new Object();
//# 
    //# //string buffer to hold the reversed text
    //# private static StringBuffer s_revText = new StringBuffer();
//# 
    //# //string buffer to hold a temporary substring
    //# private static StringBuffer s_tempString = new StringBuffer();
//# 
    //# /**
    //# * This method is for reversing all Hebrew words.
    //# * It is relevant for devices with a "reverse Hebrew" bug.
    //# *
    //# * @param text (String) the original text.
    //# * @param isLtr (boolean) text input direction.
    //# *
    //# * @return (String) the manipulated text.
    //# */
    //# public static String reverseHebrewWords(String text, boolean isLtr)
    //# {
    //# synchronized(s_syncObject)
    //# {
    //#ifdef INFO
    //# info("Original text: "+text);
    //#endif
//# 
    //# //init the string buffers for the algorithm
    //# s_revText.setLength(0);
    //# s_tempString.setLength(0);
//# 
    //# //holds the index of the last default language char
    //# int lastDefaultLangCharIndex = -1;
//# 
    //# //holds whether the original string contains hebrew chars
    //# boolean containHebrewChar = false;
//# 
    //# //holds whether last original text char was referred as the text LTR
    //# boolean isLastCharAsTextLTR = true;
//# 
    //# //loop over all original text chars
    //# int textLength = text.length();
    //# for (int pos=0 ; pos<textLength ; ++pos)
    //# {
    //# //gets original text current char
    //# char c = text.charAt(pos);
//# 
    //# //while original text current char is a latin char ('a-z')
    //# while(isLtrLang(c))
    //# {
    //# //insert the char into the end of the temporary substring
    //# s_tempString.append(c);
//# 
    //# //increases original text current char index
    //# ++pos;
//# 
    //# //stop condition - in case we reached the end of the original text
    //# if (pos == textLength)
    //# break;
//# 
    //# //gets original text next char
    //# c = text.charAt(pos);
    //# }
//# 
    //# //in case we had a text of latin chars
    //# if (s_tempString.length() != 0)
    //# {
    //# //if text writing direction is LTR
    //# if (isLtr)
    //# {
    //# //insert latin chars temporary substring at the end of the reversed text
    //# s_revText.append(s_tempString);
//# 
    //# //last original text char was referred as a default language char
    //# isLastCharAsTextLTR = true;
//# 
    //# //update last default language char index
    //# lastDefaultLangCharIndex = s_revText.length()-1;
    //# }
    //# //if text writing direction is RTL
    //# else
    //# {
    //# //insert latin chars temporary substring into the reversed text
    //# s_revText.insert(lastDefaultLangCharIndex < 0 ?
//# //                        s_revText.length() : (lastDefaultLangCharIndex == 0 ? 0 : lastDefaultLangCharIndex-1),
    //# s_revText.length() : (lastDefaultLangCharIndex == 0 ? 0 : lastDefaultLangCharIndex),
    //# s_tempString.toString());
//# 
    //# //last original text char wasn't referred as a default language char
    //# isLastCharAsTextLTR = false;
//# 
    //# //update last default language char index
    //# lastDefaultLangCharIndex = lastDefaultLangCharIndex == -1 ?
    //# -1 : lastDefaultLangCharIndex + s_tempString.length();
    //# }
//# 
    //# //clears the temporary substring
    //# //s_tempString.setLength(0);
    //# s_tempString.setLength(0);
    //# }
//# 
    //# //while original text current char is an hebrew char ('�-�')
    //# while (isRtlLang(c))
    //# {
    //# //insert the char into the end of the temporary substring
    //# s_tempString.append(c);
//# 
    //# //increases original text current char index
    //# ++pos;
//# 
    //# //stop condition - in case we reached the end of the original text
    //# if (pos == textLength)
    //# break;
//# 
    //# //gets original text current char
    //# c = text.charAt(pos);
    //# }
//# 
    //# //in case we had a raw of hebrew chars
    //# if (s_tempString.length() != 0)
    //# {
    //# //update that the original text contain hebrew chars
    //# containHebrewChar = true;
//# 
    //# //if text writing direction is LTR
    //# if (isLtr)
    //# {
    //# //reverses hebrew temporary sunstring, and inserts it at the next hebrew chars index
    //# s_revText.insert(lastDefaultLangCharIndex+1, s_tempString.reverse().toString());
//# 
    //# //last original text char wasn't referred as a default language char
    //# isLastCharAsTextLTR = false;
//# 
    //# //last default language char index remains unchanged!
    //# }
    //# //if text writing direction is RTL
    //# else
    //# {
    //# //reverses hebrew temporary substring, and inserts it at the begining of the revresed text
    //# s_revText.insert(0, s_tempString.reverse().toString());
//# 
    //# //last original text char was referred as a default language char
    //# isLastCharAsTextLTR = true;
//# 
    //# //update last default language char index
    //# lastDefaultLangCharIndex = 0;
    //# }
//# 
    //# //clears the temporary substring
    //# s_tempString.setLength(0);
    //# }
//# 
    //# //while original text current char is a digit ('0-9')
    //# while (Character.isDigit(c))
    //# {
    //# //insert the char into the end of the temporary substring
    //# s_tempString.append(c);
//# 
    //# //increases original text current char index
    //# ++pos;
//# 
    //# //stop condition - in case we reached the end of the original text
    //# if (pos == textLength)
    //# break;
//# 
    //# //gets original text current char
    //# c = text.charAt(pos);
    //# }
//# 
    //# //in case we had a raw of digit chars
    //# if (s_tempString.length() != 0)
    //# {
    //# //if text writing direction is LTR
    //# if (isLtr)
    //# {
    //# //if last char was referred as a latin char
    //# if (isLastCharAsTextLTR)
    //# {
    //# //insert digit chars temporary substring at the end of the reversed text
    //# s_revText.append(s_tempString);
//# 
    //# //last original text char was referred as a latin char
    //# isLastCharAsTextLTR = true;
//# 
    //# //update last default language char index
    //# lastDefaultLangCharIndex = s_revText.length()-1;
    //# }
    //# //if last char was referred as an hebrew char
    //# else
    //# {
    //# //insert digit chars temporary substring at the next hebrew chars index
    //# s_revText.insert(lastDefaultLangCharIndex+1, s_tempString.toString());
//# 
    //# //last original text char wasn't referred as a default language char
    //# isLastCharAsTextLTR = false;
//# 
    //# //last default language cahr index remains unchanged!
    //# }
    //# }
    //# //if text writing direction is RTL
    //# else
    //# {
    //# //if last char was referred as an hebrew char
    //# if (isLastCharAsTextLTR)
    //# {
    //# //insert digit chars temporary substring at the beginig of the reversed text
    //# s_revText.insert(0, s_tempString.toString());
//# 
    //# //last original text char was referred as an hebrew char
    //# isLastCharAsTextLTR = true;
//# 
    //# //update last default language char index
    //# lastDefaultLangCharIndex = 0;
    //# }
    //# //if last char was referred as a latin char
    //# else
    //# {
//# //                        //insert digit chars temporary substring into the reversed text
//# //                        s_revText.insert(lastDefaultLangCharIndex < 0 ?
//# //                            s_revText.length() : lastDefaultLangCharIndex-1,
//# //                            s_tempString.toString());
//# 
    //# //insert latin chars temporary substring into the reversed text
    //# s_revText.insert(lastDefaultLangCharIndex < 0 ?
    //# s_revText.length() : (lastDefaultLangCharIndex == 0 ? 0 : lastDefaultLangCharIndex),
    //# s_tempString.toString());
//# 
    //# //last original text char was referred as a hebrew char
    //# isLastCharAsTextLTR = false;
//# 
    //# //update last default language char index
    //# lastDefaultLangCharIndex = lastDefaultLangCharIndex == -1 ?
    //# -1 : lastDefaultLangCharIndex + s_tempString.length();
    //# }
    //# }
//# 
    //# //clears the temporary substring
    //# s_tempString.setLength(0);
    //# }
//# 
    //# //while original text current char is a special char
    //# while (!isLtrLang(c) && !isRtlLang(c) && !Character.isDigit(c))
    //# {
    //# //special treatment for the '(' & ')' special chars,
    //# //when text writing direction in RTL and last char was referred as hebrew
    //# if (!isLtr && isLastCharAsTextLTR)
    //# {
    //# if (c == ')') c = '(';
    //# else if (c == '(') c = ')';
    //# }
//# 
    //# //insert the char into the end of the temporary substring
    //# s_tempString.append(c);
//# 
    //# //increases original text current char index
    //# ++pos;
//# 
    //# //stop condition - in case we reached the end of the original text
    //# if (pos == textLength)
    //# break;
//# 
    //# //gets original text current char
    //# c = text.charAt(pos);
    //# }
//# 
    //# //in case we had a raw of special chars
    //# if (s_tempString.length() != 0)
    //# {
    //# //if text writing direction is LTR
    //# if (isLtr)
    //# {
    //# //if last char was referred as a latin char
    //# if (isLastCharAsTextLTR)
    //# {
    //# //insert special chars temporary substring at the end of the reversed text
    //# s_revText.append(s_tempString);
//# 
    //# //last original text char was referred as a latin char
    //# isLastCharAsTextLTR = true;
//# 
    //# //update last default language char index
    //# lastDefaultLangCharIndex = s_revText.length()-1;
    //# }
    //# //if last char was referred as an hebrew char
    //# else
    //# {
    //# //reverses special chars temporary substring, and insert it at the next hebrew chars index
    //# s_revText.insert(lastDefaultLangCharIndex+1, s_tempString.reverse().toString());
//# 
    //# //last original text char wasn't referred as a default language char
    //# isLastCharAsTextLTR = false;
//# 
    //# //last default language cahr index ramains unchanged!
    //# }
    //# }
    //# //if text writing direction is RTL
    //# else
    //# {
    //# //if last char was referred as an hebrew char
    //# if (isLastCharAsTextLTR)
    //# {
    //# //reverses special chars temporary substring, and insert it at the begining of the reversed text
    //# s_revText.insert(0, s_tempString.reverse().toString());
//# 
    //# //last original text char was referred as an hebrew char
    //# isLastCharAsTextLTR = true;
//# 
    //# //update last default language char index
    //# lastDefaultLangCharIndex = 0;
    //# }
    //# //if last char was referred as a latin char
    //# else
    //# {
//# //                        //insert special chars temporary substring into the reversed text
//# //                        s_revText.insert(lastDefaultLangCharIndex < 0 ?
//# //                            s_revText.length() : lastDefaultLangCharIndex-1,
//# //                            s_tempString.toString());
//# 
    //# //insert latin chars temporary substring into the reversed text
    //# s_revText.insert(lastDefaultLangCharIndex < 0 ?
    //# s_revText.length() : (lastDefaultLangCharIndex == 0 ? 0 : lastDefaultLangCharIndex),
    //# s_tempString.toString());
//# 
    //# //last original text char was referred as a hebrew char
    //# isLastCharAsTextLTR = false;
//# 
    //# //update last default language char index
    //# lastDefaultLangCharIndex = lastDefaultLangCharIndex == -1 ?
    //# -1 : lastDefaultLangCharIndex + s_tempString.length();
    //# }
    //# }
//# 
    //# //clears the temporary substring
    //# s_tempString.setLength(0);
    //# }
//# 
    //# //decreases original text current char index
    //# --pos;
    //# }
//# 
    //#ifdef INFO
    //# info("Reversed text: "+ s_revText);
    //#endif
//# 
    //# //in case we are in LTR text, and the original text does not contain ��� hebrew chars -
    //# //text is not being reversed
    //# if (!containHebrewChar && isLtr)
    //# return text;
//# 
    //# //returns the text after reversing it
    //# return s_revText.toString();
    //# }
    //# }
    //#endif

    /****************************************************/
    /*                  Validations                     */
    /****************************************************/

    /**
     * Checks if a char belongs to LTR language.
     *
     * @param c (char) char to check
     * @return (boolean) true if the char is latin, false otherwise
     */
    public static boolean isLtrLang(char c) {
        //todo add Russian and other LTR languages
        return (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z');
    }

    /**
     * Checks if a char belongs to RTL language.
     *
     * @param c (char) char to check
     * @return (boolean) true if the char is in hebrew, false otherwise
     */
    public static boolean isRtlLang(char c) {
        //TODO - add Arabic
        return (c >= 1488 && c <= 1544);
    }

    /**
     * Checks whether an e-mail address is valid.
     *
     * @param emailAddress (String != null) the e-mail address.
     * @return (boolean) true if the e-mail address is valid, flase otherwise.
     */
    public static boolean isEmailValid(String emailAddress) {
        //valid email address must contain one '@' sign, which is not the first char
        int atIndex = emailAddress.indexOf("@");

        //#ifdef DEBUG
        debug("isEmailValid() email is (" + emailAddress + "), index is (" + atIndex + ").");
        //#endif

        //verify it exist and it is not the first char
        if (atIndex == -1 || atIndex == 0)
            return false;

        //#ifdef DEBUG
        debug("isEmailValid() passed 1st test.");
        //#endif

        //verify there is no other '@'
        if (emailAddress.indexOf('@', atIndex + 1) != -1)
            return false;

        //#ifdef DEBUG
        debug("isEmailValid() passed 2nd test.");
        //#endif

        //verify there is at least one '.' after '@', with characters in between
        int dotIndex = emailAddress.indexOf('.', atIndex + 1);
        if (dotIndex == -1 ||
                dotIndex == emailAddress.length() - 1 ||
                dotIndex - atIndex == 1)
            return false;

        //#ifdef DEBUG
        debug("isEmailValid() passed 3rd test. email is valid.");
        //#endif

        //e-mail address is valid
        return true;
    }

//    /**
//     * This method converts a byte array to a hexadecimal string.
//     *
//     * @param data (byte[] != null)
//     *
//     * @return (String) the resulting hexadecimal string.
//     */
//    private static String toHexString(byte[] data)
//    {
//        StringBuffer buf = new StringBuffer();
//        char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
//        int high, low;
//        int len = data.length;
//        for (int i=0; i<len; ++i)
//        {
//            high = ((data[i] & 0xf0) >> 4);
//            low = (data[i] & 0x0f);
//            buf.append(hexChars[high]);
//            buf.append(hexChars[low]);
//        }
//
//        return buf.toString();
//    }

    /****************************************************/
    /*                  RMS Handling                    */
    /****************************************************/

//    /**
//     * This method stores the given data in a record store (also create the
//     * record store if necessary)
//     *
//     * @param recordStoreName (String, length>0) the name of the record store to store data in.
//     * @param data (byte[][] != null) the data to store.
//     */
//    public static void addRecordsData(String recordStoreName, byte[][] data)
//    {
//	    RecordStore rs = null;
//	    try
//	    {
//		    //get the record store (create it, if necessary)
//		    rs = RecordStore.openRecordStore(recordStoreName, true);
//
//		    //add each of the records to the record store
//		    int numOfRecords = data.length;
//		    for (int i=0 ; i<numOfRecords ; ++i)
//		        rs.addRecord(data[i], 0, data[i].length);
//
//		    //#ifdef DEBUG
//		    debug("Utils.addRecordsData() - Done");
//		    //#endif
//	    }
//        
//        catch (RecordStoreException rse)
//	    {
//		    //#ifdef ERROR
//		    error("addRecordsData() failed " + recordStoreName, rse);
//		    //#endif
//	    }
//
//        finally
//        {
//	        //close record store
//	        if (rs != null)
//            {
//                try
//		        {
//			        rs.closeRecordStore();
//		        }
//
//                catch (Throwable t)
//		        {
//			        //#ifdef ERROR
//			        error("Closing record store failed !!!", t);
//			        //#endif
//		        }
//            }
//        }
//    }
//
//    /**
//     * Sets a new data at a specific record at the record store.
//     *
//     * @param recordStoreName (String, length>0) the name of the record store to store data in.
//     * @param recordId (int > 0) the record id of the record to be set.
//     * @param data (byte[][] != null) the data to store.
//     */
//    public static void setRecordStore(String recordStoreName, int recordId, byte[] data)
//    {
//	    RecordStore rs = null;
//        try
//        {
//            //get the record store
//            rs = RecordStore.openRecordStore(recordStoreName, false);
//
//            //set the new record
//            rs.setRecord(recordId, data, 0, data.length);
//        }
//
//        catch (InvalidRecordIDException ex)
//        {
//            //#ifdef ERROR
//            error("Record id: "+recordId+" doesn't exist");
//            //#endif
//        }
//
//        catch (RecordStoreException e)
//        {
//	        //#ifdef ERROR
//            error("No record stores data found for " + recordStoreName, e);
//	        //#endif
//        }
//
//        finally
//        {
//            //close record store
//            if (rs != null)
//            {
//                try
//	            {
//		            rs.closeRecordStore();
//	            }
//	            catch (RecordStoreException e1)
//	            {
//		            //#ifdef ERROR
//		            error("Closing record store failed !!!");
//		            //#endif
//	            }
//            }
//        }
//    }
//
//    /**
//     * This method checks whether or not a given record store exists (check it by it's name).
//     *
//     * @param recordStoreName (String, length>0) the name of the record store to check.
//     * 
//     * @return (boolean) inidcation for existence of the record store.
//     */
//    public static boolean isRecordStoreExist(String recordStoreName)
//    {
//        //holds all existing record store names
//        String[] recordStoreNames = RecordStore.listRecordStores();
//
//        //no record stores at all - return false
//        if (recordStoreNames == null)
//            return false;
//
//        //try to find the given record store
//        for (int i=0; i<recordStoreNames.length; ++i)
//            if (recordStoreNames[i].equals(recordStoreName))
//                return true;
//
//        //record store wasn't found - return false
//        return false;
//    }
//
//    /**
//     * This method returns all the data which is stored in the record store.
//     *
//     * @param recordStoreName (String, length>0) the name of the record store to read.
//     *
//     * @return (byte[][] != null) the stored data
//     */
//    public static byte[][] getRecordStoreData(String recordStoreName)
//    {
//	    RecordStore rs = null;
//        try
//        {
//            //get the record store
//            rs = RecordStore.openRecordStore(recordStoreName, false);
//
//            //get the record store data
//            int numOfRecords = rs.getNumRecords();
//            byte[][] rsData = new byte[numOfRecords][];
//            for (int i=0; i<numOfRecords; ++i)
//                rsData[i] = rs.getRecord(i+1);
//
//            //#ifdef DEBUG
//            debug("Utils.getRecordStoreData() - Done");
//            //#endif
//
//            //return the gathered data
//            return rsData;
//        }
//
//        catch (RecordStoreException e)
//        {
//	        //#ifdef ERROR
//            error("No record data found for " + recordStoreName, e);
//	        //#endif
//
//            return null;
//        }
//
//        finally
//        {
//            //close record store
//            if (rs != null)
//            {
//                try
//	            {
//		            rs.closeRecordStore();
//	            }
//	            catch (RecordStoreException e1)
//	            {
//		            //#ifdef ERROR
//		            error("Closing record store failed !!!");
//		            //#endif
//	            }
//            }
//        }
//    }
//
//    /**
//     * This method returns the data of a record in the given id, stored on a specific record store.
//     *
//     * @param recordStoreName (String, length>0) the name of the record store to read.
//     * @param recordId (int >= 1) the index of the retrieved record.
//     *
//     * @return (byte[] != null) the data of the record at the given record id.
//     */
//    public static byte[] getRecordDataAtIndex(String recordStoreName, int recordId)
//    {
//	    RecordStore rs = null;
//        try
//        {
//            //get the record store
//            rs = RecordStore.openRecordStore(recordStoreName, false);
//
//            //get the data
//            byte[] data = rs.getRecord(recordId);
//
//            //return the gathered data
//            return data;
//        }
//
//        catch (RecordStoreException e)
//        {
//	        //#ifdef ERROR
//            error("No record data found for " + recordStoreName + " at index: "+recordId, e);
//	        //#endif
//
//	        return null;
//        }
//
//        finally
//        {
//	        //close record store
//	        if (rs != null)
//            {
//                try
//		        {
//			        rs.closeRecordStore();
//		        }
//		        catch (RecordStoreException e1)
//		        {
//			        //#ifdef ERROR
//			        error("Closing record store failed !!!");
//			        //#endif
//		        }
//            }
//        }
//    }
//
//    /**
//     * Remove all elements from the specified record store.
//     *
//     * @param recordStoreName (String, length>0) the name of the record store to delete.
//     */
//    public static void deleteRecordStore(String recordStoreName)
//    {
//        try
//        {
//            RecordStore.deleteRecordStore(recordStoreName);
//
//            //#ifdef DEBUG
//            debug("Utils.deleteRecordStore() "+recordStoreName+" - Done.");
//            //#endif
//
//        }
//
//        catch (RecordStoreException e)
//        {
//	        //#ifdef ERROR
//            error("Record store (" + recordStoreName + ") not found.", e);
//	        //#endif
//        }
//    }

    /****************************************************/
    /*                  RMS Log Handling                */
    /****************************************************/

//    //#if INFO || NET || DEBUG || ERROR
//    /**
//     * This is a utility method for on device debugging. it works as follows:
//     * 1. Debug info text is stored in a record store (record store is created if necessary).
//     * 2. Other Midlet in the same midlet suite will rerieve the data later.
//     * The method is called per each debug call. each log is stored in a different record.
//     *
//     * @param recordStoreName (String, length>0) the name of the record store to store data in.
//     * @param logText (String, length>0) the log text to store.
//     */
//    public static void addRecordLog(String recordStoreName, String logText)
//    {
//	    RecordStore rs = null;
//	    try
//	    {
//		    //get the record store (create it as public, if necessary)
//		    rs = RecordStore.openRecordStore(recordStoreName, true, RecordStore.AUTHMODE_ANY, false);
//
//            //create the output stream for storing the data
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            DataOutputStream dos = new DataOutputStream(baos);
//
//            //set the data into the stream
//            dos.writeUTF(logText);
//
//            //add the data to the record store
//            rs.addRecord(baos.toByteArray(), 0, baos.size());
//	    }
//	    catch (Exception e)
//	    {
//            e.printStackTrace();
//	    }
//	    finally
//        {
//	        //close record store
//	        if (rs != null)
//            {
//                try
//		        {
//			        rs.closeRecordStore();
//		        }
//		        catch (RecordStoreException rse)
//		        {
//                    //calling error() method here will cause endless recursive call
//                    rse.printStackTrace();
//		        }
//            }
//        }
//    }
//    //#endif

    /****************************************************/
    /*                  Media Handling                  */
    /****************************************************/

//    /**
//     * Plays an audio file from byte array.
//     *
//     * @param data (byte[]) the media data to play
//     * @param type (String) the content type of the media
//     */
//    public static void playAudio(byte[] data, String type)
//    {
//        try
//        {
//            //release resources (if necessary)
//            if (s_player != null)
//            {
//                s_player.deallocate();
//                s_player.close();
//            }
//
//            //create the input stream for storing the audio file
//            //and read the audio file from the byte array into it
//            ByteArrayInputStream bais = new ByteArrayInputStream(data);
//
//            //create the player from an input stream
//            s_player = Manager.createPlayer(bais, type);
//
//            //construct player portions
//            s_player.realize();
//
//            //get volume control for player and set volume to maximum
//            VolumeControl vc = (VolumeControl) s_player.getControl("VolumeControl");
//            if (vc != null)
//                vc.setLevel(100); //high volume
//
//            //the player can start with the smallest latency
//            s_player.prefetch();
//            s_player.start();
//        }
//        catch (Exception e)
//        {
//            //#ifdef ERROR
//            error("Can not play audio file, exception: "+e.getMessage());
//            //#endif
//        }
//    }
//
//    /**
//     * Plays an audio file from URL address.
//     *
//     * @param url (String != null) the media file URL address.
//     */
//    public static void playAudio(String url)
//    {
//        try
//        {
//            //release resources (if necessary)
//            if (s_player != null)
//            {
//                s_player.deallocate();
//                s_player.close();
//            }
//
//            //create the player with the audio file URL
//            s_player = Manager.createPlayer(url);
//
//            //construct player portions
//            s_player.realize();
//
//            //get volume control for player and set volume to maximum
//            VolumeControl vc = (VolumeControl) s_player.getControl("VolumeControl");
//            if (vc != null)
//                vc.setLevel(100); //high volume
//
//            //the player can start with the smallest latency
//            s_player.prefetch();
//            s_player.start();
//        }
//        catch (Exception e)
//        {
//            //#ifdef ERROR
//            error("Can not play audio file, exception: "+e.getMessage());
//            //#endif
//        }
//    }

    /****************************************************/
    /*                  File System Handling            */
    /****************************************************/

    /**
     * Opens & returns connection for folder or file.
     * the connection is also held in single spot in order to close it upn cancellation.
     *
     * @param path     (String != null) the path to open connection to.
     * @param isFolder (boolean) truew for folder, false for regular file.
     * @return (File != null) the connection opened (upon success).
     * @throws IOException in case an error occures.
     */
    public static File openFileConnection(String path, boolean isFolder) throws IOException {
        //open folder connection
        if (isFolder) {
            //closeFileConnection(true);
            s_folderCon = new File(path);
            return s_folderCon;
        }

        //open file connection
        //closeFileConnection(false);
        s_fileCon = new File(path);
        return s_fileCon;
    }

    /**
     * Opens & returns an input stream for a file connection.
     * the input stream is also held in single spot in order to close it upon cancellation.
     *
     * @param file (File != null) an opened file connection.
     * @return (InputStream) the input stream of the file connection.
     * @throws IOException - in case the input stream couldn't be opened.
     */
    public static InputStream openFileInputStream(File file) throws IOException {
        //in case an input stream is opened, close it
        closeFileInputStream();

        //opens the input stream for the file connection
        s_fileInputStream = new FileInputStream(file);

        //returns the input stream
        return s_fileInputStream;
    }

    /**
     * Closes the opened input stream
     */
    public static void closeFileInputStream() {
        if (s_fileInputStream != null) {
            try {
                //close the opened input stream
                s_fileInputStream.close();
            } catch (Throwable t) {
                //#ifdef ERROR
                error("Utils.closeInputStream() throws exception: ", t);
                //#endif
            } finally {
                s_fileInputStream = null;
            }
        }
    }

    /**
     * Opens & returns an output stream for a file connection.
     * the output stream is also held in single spot in order to close it upon cancellation.
     *
     * @param file           (File != null) an opened file connection.
     * @param locationInFile (long >= 0) the location in file to seek to.
     * @return (OutputStream) the output stream of the file connection.
     * @throws IOException - in case the output stream couldn't be opened.
     */
    public static OutputStream openFileOutputStream(File file, long locationInFile) throws IOException {
        //#ifdef DEBUG
        Utils.debug("Closing any previous opened output stream.");
        //#endif

        //in case an output stream is opened, close it
        closeFileOutputStream();

        //#ifdef DEBUG
        Utils.debug("Before performing random access in the file.");
        //#endif

        //seeks to the requierd location in the file
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.seek(locationInFile);

        //#ifdef DEBUG
        Utils.debug("Before opening the output stream for the file.");
        //#endif

        //opens the output stream for the file connection
        s_fileOutputStream = new FileOutputStream(file, true);

        //returns the output stream
        return s_fileOutputStream;
    }

    /**
     * Closes the opened output stream
     */
    public static void closeFileOutputStream() {
        if (s_fileOutputStream != null) {
            try {
                //close the opened output stream
                s_fileOutputStream.close();
            } catch (Throwable t) {
                //#ifdef ERROR
                error("Utils.closeOutputStream() throws exception: ", t);
                //#endif
            } finally {
                s_fileOutputStream = null;
            }
        }
    }

//    /**
//     * Closes file/folder connection.
//     *
//     * @param isFolder (boolean) true for folder, false for file
//     */
//    public static void closeFileConnection(boolean isFolder)
//    {
//        try
//        {
//            //open folder connection
//            if (isFolder)
//            {
//                if (s_folderCon != null)
//                {
//                    s_folderCon.close();
//                    s_folderCon = null;
//                }
//            }
//            else
//            {
//                //open file connection
//                if (s_fileCon != null)
//                {
//                    s_fileCon.close();
//                    s_fileCon = null;
//                }
//            }
//        }
//
//        catch (IOException io)
//        {
//            //#ifdef ERROR
//            error("Utils.closeFileConnection() exception occured while closing ("+isFolder+").", io);
//            //#endif
//        }
//    }


//    /**
//     * This methods create an image from the local file system according to a given file-connection.
//     *
//     * @param fileConnection (FileConnection != null) an open file connection to the image file.
//     *
//     * @return (Image) the retreved image, null if not found.
//     */
//    public static Image getImageFromFileSystem(FileConnection fileConnection)
//    {
//        //#ifdef DEBUG
//        debug("Utils.getImageFromFileSystem() - trying to get image for path ("+fileConnection.getURL()+").");
//        //#endif
//
//        //to help OS - release unused memory
//        releaseMemoryIfNeeded(
//                //#ifdef DEBUG
//                "getImageFromFileSystem()"
//                //#endif
//        );
//
//        //holds the input stream
//        InputStream is = null;
//
//        try
//        {
//            //open input stream
//            is = openFileInputStream(fileConnection);
//
//            //creates & return the image from the file system
//            return Image.createImage(is);
//        }
//
//        catch (Throwable t)
//        {
//            //#ifdef ERROR
//            error("Utils.getImageFromFileSystem() - Failed to create image ("+fileConnection.getName()+"), throws: ", t);
//            //#endif
//
//            //in case image retrieval failed - return null
//            return null;
//        }
//
//        finally
//        {
//            try
//            {
//                //to help OS - release unused memory
//                releaseMemoryIfNeeded(
//                        //#ifdef DEBUG
//                        "getImageFromFileSystem(2)"
//                        //#endif
//                );
//
//                //close the input stream
//                if (is != null)
//                    is.close();
//            }
//            
//            catch (Throwable t)
//            {
//                //#ifdef ERROR
//                error("Utils.getImageFromFileSystem() - Failed to release resources. throws: ", t);
//                //#endif
//            }
//        }
//    }

//    //#ifndef DONT_GET_THUMBNAIL_FROM_HEADER
//    /**
//     * Traverse an input-stream and return a thumbnail image if any.
//     * Thumbnail is built directly from the input-stream, thus avoiding to run out of memory on very large picture files.
//     *
//     * @param fileConnection (FileConnection != null) an open file connection to the image file.
//     * @param thumbToSaveExt (String, may be null) in case not null - extension of the thumbnail file to save in the same path.
//     *                                             if null - thumbnail won't be saved into filesystem.
//     *
//     * @return Image - created from thumbnail iside jpeg file.
//     */
//    public static Image getThumbFromExifHeader(FileConnection fileConnection, String thumbToSaveExt)
//    {
//        //#ifdef DEBUG
//        debug("Utils.getThumbFromExifHeader() - trying to get thumb of JPEG header for file ("+fileConnection.getURL()+").");
//        //#endif
//
//        //to help OS - release unused memory
//        releaseMemoryIfNeeded(
//                //#ifdef DEBUG
//                "getThumbFromExifHeader()"
//                //#endif
//        );
//
//        //holds the input stream
//        InputStream is = null;
//
//        try
//        {
//            //open input stream
//            is = openFileInputStream(fileConnection);
//
//            byte[] bytefileReader = {0}; // lazy byte reader
//            byte firstByte, secondByte;
//
//            is.read(bytefileReader);
//            firstByte = bytefileReader[0];
//            is.read(bytefileReader);
//            secondByte = bytefileReader[0];
//
//            //check if its not a JPEG image
//            if (!isJPEG(firstByte, secondByte))
//                return null;
//
//            //read as many bytes as possible from the file
//            int total = 0, read = 0, length = 30000;
//            byte[] fileData = new byte[length];
//            while ((length - total) > 0)
//            {
//                read = is.read(fileData, total, length - total);
//                if (read < 0)
//                    break;
//                total += read;
//            }
//
//            //close the input stream
//            is.close();
//            is = null;
//
//            int curIndex = 0;
//            int thumbStartIndex = -1;
//            int thumbEndIndex = -1;
//            byte rByte = fileData[curIndex++];
//            do
//            {
//                while (curIndex < total && rByte != -1)
//                {
//                    rByte = fileData[curIndex++];
//                }
//
//                if (curIndex < total)
//                    rByte = fileData[curIndex++];
//            }
//            while (curIndex < total && (rByte & 0xFF) != 0xD8); // thumb starts
//
////            baos.write(-1);
////            baos.write(rByte);
//            thumbStartIndex = curIndex-1;
//
//            rByte = 0;
//            do
//            {
//                while (curIndex < total && rByte != -1)
//                {
//                    rByte = fileData[curIndex++];
//                }
//
//                if (curIndex < total)
//                    rByte = fileData[curIndex++];
//            }
//            while (curIndex < total && (rByte & 0xFF) != 0xD9); // thumb ends
//
//            thumbEndIndex = curIndex+1;
//
//            if (curIndex < total)
//            {
//                //the +2 is for the first & last (-1)
//                byte[] thumbData = new byte[thumbEndIndex - thumbStartIndex + 1];
//                thumbData[0] = -1;
//                thumbData[thumbData.length-1] = -1;
//                System.arraycopy(fileData, thumbStartIndex, thumbData, 1, thumbEndIndex - thumbStartIndex - 1);
//
//                //#ifdef DEBUG
////                StringBuffer sb = new StringBuffer("the first 20 bytes of thumb are:\n");
////                for (int i=0; i<20; ++i)
////                    sb.append("index: (").append(i).append("), value: (").append(thumbData[i]).append(").\n");
////                sb.append("the last 20 bytes of thumb are:\n");
////                for (int i=19; i>=0; --i)
////                    sb.append("index: (").append(thumbData.length-i-1).append("), value: (").append(thumbData[thumbData.length-i-1]).append(").\n");
////                sb.append("total number of bytes to process image are: (").append(thumbData.length).append(").\n");
////                sb.append("curIndex: (").append(curIndex).append("), thumbStart: (").append(thumbStartIndex).append(") thumbEnd: (").append(thumbEndIndex).append(")\n");
////                debug(sb.toString());
//                //#endif
//
//                //creates thumb image
//                Image thumbImage = Image.createImage(thumbData, 0, thumbData.length);
//
//                //in case the thumbnail should be saved into filesystem
//                if (thumbImage != null && thumbToSaveExt != null)
//                {
//                    //holds thumb's file full path
//                    String thumbFilePath = new StringBuffer(fileConnection.getURL()).append(thumbToSaveExt).toString();
//
//                    //saves the thumb file into the filesystem
//                    if (!saveDataToFile(thumbData, thumbFilePath, true))
//                    {
//                        //#ifdef ERROR
//                        error("Utils.getThumbFromExifHeader() - Failed to save CTF File to filesystem");
//                        //#endif
//                    }
//                }
//
//                //returns the created image
//                return thumbImage;
//            }
//            else
//            {
//                //#ifdef DEBUG
//                debug("Utils.getThumbFromExifHeader() - Failed to get thumb from JPEG header for file ("+fileConnection.getName()+")");
//                //#endif
//
//                return null;
//            }
//        }
//
//        catch (Throwable t)
//        {
//            //#ifdef ERROR
//            error("Utils.getThumbFromExifHeader() - Failed to get thumb from JPEG header for file ("+fileConnection.getName()+"), throws: ", t);
//            //#endif
//
//            //in case thumbnail retrieval failed - return null
//            return null;
//
//        }
//
//        finally
//        {
//            try
//            {
//                //to help OS - release unused memory
//                releaseMemoryIfNeeded(
//                        //#ifdef DEBUG
//                        "getThumbFromExifHeader()"
//                        //#endif
//                );
//
//                //close the input stream
//                if (is != null)
//                    is.close();
//            }
//
//            catch (Throwable t)
//            {
//                //#ifdef ERROR
//                error("Utils.getThumbFromExifHeader() - Failed to release resources. throws: ", t);
//                //#endif
//            }
//        }
//    }

//    /**
//     * Saves data to file in device's file system.
//     *
//     * @param dataToSave (byte[] != null) the data to save into the file.
//     * @param fileFullPath (String != null) the file to be saved full path in the filesystem.
//     * @param isHidden (boolean) true in case the file should be saved as an hidden file, false otherwise.
//     *
//     * @return (boolean) true in case the file was successfully saved into the filesystem, false otherwise.
//     */
//    public static boolean saveDataToFile(byte[] dataToSave, String fileFullPath, boolean isHidden)
//    {
//        //#ifdef DEBUG
//        debug("Utils.saveDataToFile() for file ("+fileFullPath+"), ("+dataToSave.length+") bytes.");
//        //#endif
//
//        //holds file's output stream
//        OutputStream os = null;
//
//        try
//        {
//            //opens a file connection to the file
//            File file = openFileConnection(fileFullPath, Connector.READ_WRITE, false);
//
//            //in case the file doesn't exist
//            if (!file.exists())
//            {
//                //creates the file
//                file.create();
//            }
//
//            //sets the file as hidden
//            file.setHidden(isHidden);
//
//            //gets file's output stream
//            os = openFileOutputStream(file, 0);
//
//            //write data into the file
//            os.write(dataToSave);
//            os.flush();
//
//            //the file was successfully saved into the file system
//            return true;
//        }
//
//        catch (Throwable t)
//        {
//            //#ifdef ERROR
//            error("Utils.saveDataToFile() throws exception: ", t);
//            //#endif
//        }
//
//        finally
//        {
//            try
//            {
//                //closes the output stream of the file
//                if (os != null)
//                    os.close();
//
//                //closes the file connection
//                closeFileConnection(false);
//
//                //#ifdef DEBUG
//                debug("Utils.saveDataToFile(): " + fileFullPath + " created!");
//                //#endif
//            }
//
//            catch (Throwable t)
//            {
//                //#ifdef ERROR
//                error("Utils.saveDataToFile() throws exception: ", t);
//                //#endif
//            }
//        }
//
//        //the file was not saved into the file system
//        return false;
//    }
//
//    /**
//     * Checks if two consectutive bytes can be interpreted as a jpeg.
//     *
//     * @param b1 first byte.
//     * @param b2 second byte.
//     *
//     * @return true if b1 and b2 are jpeg markers.
//     */
//    private static boolean isJPEG (byte b1,byte b2)
//    {
//      return ((b1 & 0xFF) == 0xFF && (b2 & 0xFF) == 0xD8);
//    }
//    //#endif
//
//    /**
//     * Returns the byte array of a file.
//     *
//     * @param filePath (String != null) file's path.
//     *
//     * @return (byte[]) file's bytes.
//     */
//    public static byte[] getDataFromFile(String filePath)
//    {
//        //holds the file connection to the file
//        FileConnection fc;
//        InputStream is = null;
//
//        //holds files' bytes
//        byte[] fileBytes = null;
//
//        try
//        {
//            //opens the connection to the file
//            fc = openFileConnection(filePath, Connector.READ, false);
//
//            //initialzies the byte array
//            int fileSize = (int)fc.fileSize();
//            fileBytes = new byte[fileSize];
//
//            //holds the total number of bytes which have been read so far
//            int totalNumOfReadBytes = 0;
//
//            //gets file's input stream
//            is = openFileInputStream(fc);
//
//            //reads files' bytes to the byte array
//            do
//            {
//                int NumOfReadBytes = is.read(fileBytes, totalNumOfReadBytes, fileSize - totalNumOfReadBytes);
//
//                //stop condition - nothing was read
//                if (NumOfReadBytes < 0)
//                    break;
//
//                totalNumOfReadBytes += NumOfReadBytes;
//            }
//            while (totalNumOfReadBytes < fileSize);
//        }
//
//        catch (Exception e)
//        {
//            //#ifdef ERROR
//            error("getDataFromFile() throws exception: ", e);
//            //#endif
//        }
//
//        finally
//        {
//            try
//            {
//                //close the file connection
//                if (is != null)
//                    is.close();
//                closeFileConnection(false);
//            }
//
//            catch (IOException e)
//            {
//                //#ifdef ERROR
//                error("getDataFromFile() Failed to close resources, e: ", e);
//                //#endif
//            }
//        }
//
//        //returns files' bytes
//        return fileBytes;
//    }

    /**
     * Kills the static file connection. todo - is method necessary? we can call closeFileConnection() instead
     */
    public static void killFileConnection() {
        //#ifdef DEBUG
        debug("Utils.killFileConnection() start");
        //#endif

        //close the inputstream, output stream and file connections
        closeFileInputStream();
        closeFileOutputStream();
        //closeFileConnection(true);
        //closeFileConnection(false);

        //#ifdef DEBUG
        debug("Utils.killFileConnection() end");
        //#endif
    }

    /****************************************************/
    /*                  Miscellaneous                   */
    /****************************************************/

    /**
     * This method creates a scaled image from a given image.
     *
     * @param srcImage (Bitmap != null) the source image.
     * @param width    (int) the destination image width.
     * @param height   (int) the destination image height.
     * @return (Bitmap != null) the created scaled image.
     */
    public static Bitmap scaleImage(Bitmap srcImage, int width, int height) {
        //holds source image's height and width
        int sourceWidth = srcImage.getWidth();
        int sourceHeight = srcImage.getHeight();

        //calculates the scale parameters
        float scaleWidth = (float) width / (float) sourceWidth;
        float scaleHeight = (float) height / (float) sourceHeight;

        //creates a matrix for the manipulation
        Matrix matrix = new Matrix();

        //sets the scale parameters for resizing the image
        matrix.postScale(scaleWidth, scaleHeight);

        //creates the scaled image from the original image
        Bitmap scaledImg = Bitmap.createBitmap(srcImage, 0, 0, sourceWidth, sourceHeight, matrix, true);

        //return it
        return scaledImg;
    }

//    /**
//     * Rounds a floating point number.
//     *
//     * @param num (float) number to round.
//     *
//     * @return (int) the rounded number.
//     */
//    private static int getRoundNum(float num)
//    {
//        if ((num - (int)num) > 0.5)
//            return (int)Math.ceil(num);
//        else
//            return (int)Math.floor(num);
//    }
//    //#endif

    /**
     * Returns 2 strings which indicates date and time, according to a number of miliseconds which have passed since 1.1.1970.
     *
     * @param time (long) the number of miliseconds which have passed since 1.1.1970 and indicates the date and time to be returned as string.
     * @return (String[]) the date and time, as strings, may be null.
     */
    public static String[] getDateAndTimeAsString(long time) {
        //nothing to do in case of negetive time
        if (time < 0)
            return null;

        //get date parameters
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(time));
        String year = String.valueOf(cal.get(Calendar.YEAR));
        String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
        String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        //#ifdef EMULATOR
        //# String hour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY) + 3); //j2me emulator bug
        //#else
        String hour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
        //#endif
        String minute = String.valueOf(cal.get(Calendar.MINUTE));

        //holds the date string
        StringBuffer sbDate = new StringBuffer();

        //appends date's day
        if (day.length() > 1)
            sbDate.append(day);
        else
            sbDate.append("0").append(day);
        sbDate.append("/");

        //appends date's month
        if (month.length() > 1)
            sbDate.append(month);
        else
            sbDate.append("0").append(month);
        sbDate.append("/");

        //appends date's year
        sbDate.append(year.substring(2, 4));

        //holds the time string
        StringBuffer sbTime = new StringBuffer();

        //appends time's hour
        if (hour.length() > 1)
            sbTime.append(hour);
        else
            sbTime.append("0").append(hour);

        sbTime.append(":");

        //appends time's minute
        if (minute.length() > 1)
            sbTime.append(minute);
        else
            sbTime.append("0").append(minute);

        //return date and time indications 
        return new String[]{sbDate.toString(), sbTime.toString()};
    }

    /**
     * generates a unique id for general usage.
     *
     * @return (String != null) an id that is unique.
     */
    public static String generateUniqueId() {
        return String.valueOf(System.currentTimeMillis() % 1000000);
    }

    /****************************************************/
    /*              QuickSort Implementation            */
    /****************************************************/

    /**
     * This is the recursive method implementation of the sort.
     * It holds pivot (middle of the handled range), and swaps elements according to comparisons.
     *
     * @param objectsVector (Vector != null) Comparable objects container
     * @param low           (int >= 0) the starting index of the range to handle
     * @param high          (int < Vector.size()) the ending index of the range to handle
     */
    public static void quicksort(Vector objectsVector, int low, int high) {
        int CUTOFF = 10;
        if (low + CUTOFF > high) {
            insertionSort(objectsVector, low, high);
        } else {
            int middle = (low + high) / 2;
            if (((Comparable) objectsVector.elementAt(middle)).compareTo((Comparable) objectsVector.elementAt(low)) < 0) {
                swapReferences(objectsVector, low, middle);
            }
            if (((Comparable) objectsVector.elementAt(high)).compareTo((Comparable) objectsVector.elementAt(low)) < 0) {
                swapReferences(objectsVector, low, high);
            }
            if (((Comparable) objectsVector.elementAt(high)).compareTo((Comparable) objectsVector.elementAt(middle)) < 0) {
                swapReferences(objectsVector, middle, high);
            }

            swapReferences(objectsVector, middle, high - 1);
            Comparable pivot = (Comparable) objectsVector.elementAt(high - 1);

            int i, j;
            for (i = low, j = high - 1; ; ) {
                while (i < (high - 1) && ((Comparable) objectsVector.elementAt(++i)).compareTo(pivot) < 0) {
                    ;
                }
                while (j > low && pivot.compareTo((Comparable) objectsVector.elementAt(--j)) < 0) {
                    ;
                }
                if (i >= j) {
                    break;
                }
                swapReferences(objectsVector, i, j);
            }
            swapReferences(objectsVector, i, high - 1);

            quicksort(objectsVector, low, i - 1);
            quicksort(objectsVector, i + 1, high);
        }
//base code is:
//        int CUTOFF = 10;
//        if (low + CUTOFF > high)
//        {
//            insertionSort(a, low, high);
//        }
//        else
//        {
//            int middle = (low + high) / 2;
//            if (a[middle].compareTo(a[low]) < 0)
//            {
//                swapReferences(a, low, middle);
//            }
//            if (a[high].compareTo(a[low]) < 0)
//            {
//                swapReferences(a, low, high);
//            }
//            if (a[high].compareTo(a[middle]) < 0)
//            {
//                swapReferences(a, middle, high);
//            }
//
//            swapReferences(a, middle, high - 1);
//            Comparable pivot = a[high - 1];
//
//            int i, j;
//            for (i = low, j = high - 1; ;)
//            {
//                while (a[++i].compareTo(pivot) < 0)
//                {
//                    ;
//                }
//                while (pivot.compareTo(a[--j]) < 0)
//                {
//                    ;
//                }
//                if (i >= j)
//                {
//                    break;
//                }
//                swapReferences(a, i, j);
//            }
//            swapReferences(a, i, high - 1);
//
//            quicksort(a, low, i - 1);
//            quicksort(a, i + 1, high);
//        }
    }

    /**
     * Replaces (on place) objects on objects container.
     * items on indices 1 & 2 are replaced.
     *
     * @param objectsVector (Vector != null) Comparable objects container
     * @param index1        (int) first index for replacement
     * @param index2        (int) second index for replacement
     */
    public static void swapReferences(Vector objectsVector, int index1, int index2) {
        Object tmp = objectsVector.elementAt(index1);
        objectsVector.setElementAt(objectsVector.elementAt(index2), index1);
        objectsVector.setElementAt(tmp, index2);
//base code is:
//        Object tmp = a[index1];
//        a[index1] = a[index2];
//        a[index2] = tmp;
    }


    /**
     * This method implements insertion sort (bubble sort).
     *
     * @param objectsVector (Vector != null) Comparable objects container
     * @param low           (int >= 0) the starting index of the range to handle
     * @param high          (int < Vector.size()) the ending index of the range to handle
     */
    private static void insertionSort(Vector objectsVector, int low, int high) {
        for (int p = low + 1; p <= high; p++) {
            Comparable tmp = (Comparable) objectsVector.elementAt(p);
            int j;

            for (j = p; j > low && tmp.compareTo((Comparable) objectsVector.elementAt(j - 1)) < 0; j--) {
                objectsVector.setElementAt(objectsVector.elementAt(j - 1), j);
            }
            objectsVector.setElementAt(tmp, j);
        }
//base code is:
//        for (int p = low + 1; p <= high; p++)
//        {
//            Comparable tmp = a[p];
//            int j;
//
//            for (j = p; j > low && tmp.compareTo(a[j - 1]) < 0; j--)
//            {
//                a[j] = a[j - 1];
//            }
//            a[j] = tmp;
//        }
    }

    //#ifndef DONT_GET_THUMBNAIL_FROM_HEADER

    /**
     * Traverse an input-stream and return a thumbnail image if any.
     * Thumbnail is built directly from the input-stream, thus avoiding to run out of memory on very large picture files.
     *
     * @param fileConnection (FileConnection != null) an open file connection to the image file.
     * @param thumbToSaveExt (String, may be null) in case not null - extension of the thumbnail file to save in the same path.
     *                       if null - thumbnail won't be saved into filesystem.
     * @return Image - created from thumbnail iside jpeg file.
     */
    public static Bitmap getThumbFromExifHeader(File fileConnection, String thumbToSaveExt) {
        //#ifdef DEBUG
        debug("Utils.getThumbFromExifHeader() - trying to get thumb of JPEG header for file (" + fileConnection.getAbsoluteFile() + ").");
        //#endif

//        //to help OS - release unused memory
//        releaseMemoryIfNeeded(
//                //#ifdef DEBUG
//                //# "getThumbFromExifHeader()"
//                //#endif
//        );

        //holds the input stream
        InputStream is = null;

        try {
            //open input stream
            is = openFileInputStream(fileConnection);

            byte[] bytefileReader = {0}; // lazy byte reader
            byte firstByte, secondByte;

            is.read(bytefileReader);
            firstByte = bytefileReader[0];
            is.read(bytefileReader);
            secondByte = bytefileReader[0];

            //check if its not a JPEG image
            if (!isJPEG(firstByte, secondByte))
                return null;

            //read as many bytes as possible from the file
            int total = 0, read = 0, length = 30000;
            byte[] fileData = new byte[length];
            while ((length - total) > 0) {
                read = is.read(fileData, total, length - total);
                if (read < 0)
                    break;
                total += read;
            }

            //close the input stream
            is.close();
            is = null;

            int curIndex = 0;
            int thumbStartIndex = -1;
            int thumbEndIndex = -1;
            byte rByte = fileData[curIndex++];
            do {
                while (curIndex < total && rByte != -1) {
                    rByte = fileData[curIndex++];
                }

                if (curIndex < total)
                    rByte = fileData[curIndex++];
            }
            while (curIndex < total && (rByte & 0xFF) != 0xD8); // thumb starts

//            baos.write(-1);
//            baos.write(rByte);
            thumbStartIndex = curIndex - 1;

            rByte = 0;
            do {
                while (curIndex < total && rByte != -1) {
                    rByte = fileData[curIndex++];
                }

                if (curIndex < total)
                    rByte = fileData[curIndex++];
            }
            while (curIndex < total && (rByte & 0xFF) != 0xD9); // thumb ends

            thumbEndIndex = curIndex + 1;

            if (curIndex < total) {
                //the +2 is for the first & last (-1)
                byte[] thumbData = new byte[thumbEndIndex - thumbStartIndex + 1];
                thumbData[0] = -1;
                thumbData[thumbData.length - 1] = -1;
                System.arraycopy(fileData, thumbStartIndex, thumbData, 1, thumbEndIndex - thumbStartIndex - 1);

                //#ifdef DEBUG
//                StringBuffer sb = new StringBuffer("the first 20 bytes of thumb are:\n");
//                for (int i=0; i<20; ++i)
//                    sb.append("index: (").append(i).append("), value: (").append(thumbData[i]).append(").\n");
//                sb.append("the last 20 bytes of thumb are:\n");
//                for (int i=19; i>=0; --i)
//                    sb.append("index: (").append(thumbData.length-i-1).append("), value: (").append(thumbData[thumbData.length-i-1]).append(").\n");
//                sb.append("total number of bytes to process image are: (").append(thumbData.length).append(").\n");
//                sb.append("curIndex: (").append(curIndex).append("), thumbStart: (").append(thumbStartIndex).append(") thumbEnd: (").append(thumbEndIndex).append(")\n");
//                debug(sb.toString());
                //#endif

                //creates thumb image
                Bitmap thumbImage = BitmapFactory.decodeByteArray(thumbData, 0, thumbData.length);

                //TODO - take care of this code
//                //in case the thumbnail should be saved into filesystem
//                if (thumbImage != null && thumbToSaveExt != null)
//                {
//                    //holds thumb's file full path
//                    String thumbFilePath = new StringBuffer(fileConnection.getURL()).append(thumbToSaveExt).toString();
//
//                    //saves the thumb file into the filesystem
//                    if (!saveDataToFile(thumbData, thumbFilePath, true))
//                    {
//                        //#ifdef ERROR
//                        error("Utils.getThumbFromExifHeader() - Failed to save CTF File to filesystem");
//                        //#endif
//                    }
//                }

                //returns the created image
                return thumbImage;
            } else {
                //#ifdef DEBUG
                debug("Utils.getThumbFromExifHeader() - Failed to get thumb from JPEG header for file (" + fileConnection.getName() + ")");
                //#endif

                return null;
            }
        } catch (Throwable t) {
            //#ifdef ERROR
            error("Utils.getThumbFromExifHeader() - Failed to get thumb from JPEG header for file (" + fileConnection.getName() + "), throws: ", t);
            //#endif

            //in case thumbnail retrieval failed - return null
            return null;

        } finally {
            try {
//                //to help OS - release unused memory
//                releaseMemoryIfNeeded(
//                        //#ifdef DEBUG
//                        //# "getThumbFromExifHeader()"
//                        //#endif
//                );

                //close the input stream
                if (is != null)
                    is.close();
            } catch (Throwable t) {
                //#ifdef ERROR
                error("Utils.getThumbFromExifHeader() - Failed to release resources. throws: ", t);
                //#endif
            }
        }
    }

    /**
     * Checks if two consectutive bytes can be interpreted as a jpeg.
     *
     * @param b1 first byte.
     * @param b2 second byte.
     * @return true if b1 and b2 are jpeg markers.
     */
    private static boolean isJPEG(byte b1, byte b2) {
        return ((b1 & 0xFF) == 0xFF && (b2 & 0xFF) == 0xD8);
    }
    //#endif

    /**
     * Gets data in milliseconds from a string date.
     *
     * @param value (String != null) a string date.
     * @return (long) the date in milliseconds.
     */
    public static long dateToEpochMills(String value) {
        //first, replace all ':' and '-' with empty chars
        value = value.replace(" ", "T");
        value = value.replace(":", "").replace("/", "");

        int indexT = value.indexOf('T');
        int indexZ = value.indexOf('Z');

        if (indexT == -1)
            return 0;

        if (indexZ == -1) {
            indexZ = value.length();

            if (indexZ - indexT != 7)
                return 0;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyyHHmmss");
        String sdate = value.substring(0, indexT) + value.substring(indexT + 1, indexZ);

        try {
            Date date = sdf.parse(sdate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            return calendar.getTimeInMillis();
        } catch (ParseException e) {
            return 0;
        }
    }

    /**
     * Gets the data of a file in a given path in device's file system.
     *
     * @param filePath (String != null) the path of the file to get its data.
     * @return (byte[], may be null) the file's data, or null in case of an error.
     */
    public static byte[] getFileData(String filePath) {
        try {
            //#ifdef DEBUG
            Utils.error("Utils.getFileData() - getting file's data from device's file system...");
            //#endif

            //opens an input stream to the file in device's file system
            File file = openFileConnection(filePath, false);
            InputStream fileIS = openFileInputStream(file);

            //holds the size of the file
            int fileSize = (int) file.length();

            //creates a byte array to hold the file data
            byte[] fileData = new byte[fileSize];

            //reads the file data from device's file system into the byte array
            int totalNumberOfReadBytes = 0;
            while (totalNumberOfReadBytes < fileSize) {
                //reads bytes from the file to the byte array
                int numberOfReadBytes = fileIS.read(fileData, totalNumberOfReadBytes, fileSize - totalNumberOfReadBytes);

                //in case of an error
                if (numberOfReadBytes < 1)
                    return null;

                //updates the total number of bytes which was read from the file
                totalNumberOfReadBytes += numberOfReadBytes;
            }

            //closes the input stream to the file
            closeFileInputStream();

            //#ifdef DEBUG
            Utils.error("Utils.getFileData() - getting file's data from device's file system completed successfully.");
            //#endif

            //returns the file data
            return fileData;
        } catch (Throwable throwable) {
            //#ifdef ERROR
            Utils.error("Utils.getFileData() - an error has occurred - " + throwable +
                    ", getting file's data from device's file system failed.");
            //#endif

            //an error has occurred
            return null;
        }
    }

    public static void showMessage(int titleResouceId, int strResourceId, Context context) {
        Log.d(TAG, "showMessage");
        // create the dialog
        AlertDialog.Builder messageDialog = new AlertDialog.Builder(context);

        // add title
        messageDialog.setTitle(titleResouceId);

        // add message
        messageDialog.setMessage(strResourceId);

        // add ok button that close the dialog
        messageDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        // show the dialog
        messageDialog.show();
    }

    public static void showExitMessage(Context context) {
        Log.d(TAG, "showExitMessage");
        // create the dialog
        AlertDialog.Builder messageDialog = new AlertDialog.Builder(context);

        // add title
        messageDialog.setTitle(R.string.warning);

        // add message
        messageDialog.setMessage(R.string.exit_message);

        // add ok button to exit from application
        messageDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BouncerApplication.getApplication().getActivityInForeground().finish();
            }
        });

        // add cancel button to close the dialog
        messageDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        // show the dialog
        messageDialog.show();
    }

    /**
     * Shows gauge popup.
     */
    public static void showGauge(final Context context) {
        Log.d(TAG, "showGauge");
        // hold the progress dialog
        final ProgressDialog gaugeDialog = new ProgressDialog(context);

        // handler for dismiss progress dialog
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                gaugeDialog.dismiss();
            }
        };

        // runnable for show progress dialog
        Runnable showGaugeRunnable = new Runnable() {
            @Override
            public void run() {
                // prepare the thread to loop
                Looper.prepare();

                // set message for dialog
                gaugeDialog.setMessage(context.getString(R.string.gauge_message));

                // show dialog
                gaugeDialog.show();

                // dismiss dialog
                handler.sendEmptyMessage(0);

                // start looping
                Looper.loop();

                // finish looping
                Looper.myLooper().quit();
            }
        };

        // start new thread for progress dialog
        Thread gaugeThread = new Thread(showGaugeRunnable);
        gaugeThread.start();
    }

}
