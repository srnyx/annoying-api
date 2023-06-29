package xyz.srnyx.annoyingapi.message;


/**
 * The different types of broadcasts for an {@link AnnoyingMessage}
 */
public enum BroadcastType {
    /**
     * Message will be sent in chat
     */
    CHAT,
    /**
     * Message will be displayed in the action bar (1.11+ only, {@link #CHAT} will be used for older versions)
     */
    ACTIONBAR,
    /**
     * Message will be sent as a title
     */
    TITLE,
    /**
     * Message will be sent as a subtitle
     */
    SUBTITLE,
    /**
     * Only use this if the key has 2 children, "title" and "subtitle"
     * <p>The "title" child will be sent as the title and the "subtitle" child will be sent as the subtitle
     */
    FULL_TITLE;

    /**
     * Whether the broadcast type is a title ({@link #TITLE}, {@link #SUBTITLE}, or {@link #FULL_TITLE}), aka anything that has a {@code fadeIn}, {@code stay}, and {@code fadeOut}
     *
     * @return true if the broadcast type is a title
     */
    public boolean isTitle() {
        return this == TITLE || this == SUBTITLE || this == FULL_TITLE;
    }
}
