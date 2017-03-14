package com.em_projects.bouncer;

public class BouncerProperties {

    public static final String SMS_DATABASE_NAME = "hidden_sms.db";
    public static final String CALLOGS_DATABASE_NAME = "hidden_callogs.db";
    public static final int DATABASE_VERSION = 1;
    public static String APP_VERSION;
    public static String APP_VERSION_NUMBER;
    //holds the time when application's foreground activity is paused.
    public static long LAST_ON_PAUSE_TIME;

    //holds tab types
    public static final class TAB_TYPE {
        public static final String CONTACTS = "contacts";
        public static final String CONVERSATIONS = "conversations";
        public static final String CALL_LOGS = "callogs";
    }
}
