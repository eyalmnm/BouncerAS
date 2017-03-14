package com.cellebrite.ota.socialphonebook.model;

/**
 * Represents a social network activity.
 */
public class SocialNetworkActivity extends SocialNetworkStatus {
    //holds whether the user can add comment to the current activity
    private boolean b_canComment;

    /**
     * Social Network Activity constructor.
     *
     * @param id                (String != null) activity ID in the social network it belongs to.
     * @param socialNetworkName (String != null) activity's social network name.
     * @param text              (String != null) activity's text.
     * @param time              (long) activity's time in milliseconds.
     */
    public SocialNetworkActivity(String id, String contactID, String socialNetworkName, String text, long time, boolean canComment) {
        //must be called
        super(id, contactID, socialNetworkName, text, time);

        b_canComment = canComment;
    }

    /**
     * Get whether the user can comment for this current activity
     *
     * @return true in case the activity can be commented.
     */
    public boolean canComment() {
        return b_canComment;
    }
}
