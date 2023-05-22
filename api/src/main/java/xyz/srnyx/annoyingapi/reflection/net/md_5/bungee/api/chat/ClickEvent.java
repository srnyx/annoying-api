package xyz.srnyx.annoyingapi.reflection.net.md_5.bungee.api.chat;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.utility.ReflectionUtility;


/**
 * net.md_5.bungee.api.chat.ClickEvent
 */
public class ClickEvent {
    /**
     * This class cannot be instantiated
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private ClickEvent() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }

    /**
     * net.md_5.bungee.api.chat.ClickEvent.Action
     */
    public enum Action {
        ;

        /**
         * 1.15+ net.md_5.bungee.api.chat.ClickEvent.Action#COPY_TO_CLIPBOARD
         */
        @Nullable public static final net.md_5.bungee.api.chat.ClickEvent.Action COPY_TO_CLIPBOARD = ReflectionUtility.getEnumValue(10150, net.md_5.bungee.api.chat.ClickEvent.Action.class, "COPY_TO_CLIPBOARD");
    }
}
