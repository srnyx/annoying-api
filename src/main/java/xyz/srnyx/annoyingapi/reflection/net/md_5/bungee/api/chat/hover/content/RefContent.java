package xyz.srnyx.annoyingapi.reflection.net.md_5.bungee.api.chat.hover.content;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.utility.ReflectionUtility;


/**
 * net.md_5.bungee.api.chat.hover.content.Content
 */
public class RefContent {
    /**
     * 1.16.1+ net.md_5.bungee.api.chat.hover.content.Content[]
     */
    @Nullable public static final Class<?> CONTENT_ARRAY_CLASS = ReflectionUtility.getClassArray(1, 16, 1, RefContent.class);

    /**
     * This class cannot be instantiated
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private RefContent() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
