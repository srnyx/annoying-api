package xyz.srnyx.annoyingapi.message;


/**
 * The different types of broadcasts for an {@link AnnoyingMessage}
 */
public enum AnnoyingBroadcast {
    /**
     * Message will be sent in chat
     */
    CHAT,
    /**
     * Message will be sent as a title
     */
    TITLE,
    /**
     * Message w ill be sent as a subtitle
     */
    SUBTITLE,
    /**
     * Message will be displayed in the action bar
     */
    ACTIONBAR
}
