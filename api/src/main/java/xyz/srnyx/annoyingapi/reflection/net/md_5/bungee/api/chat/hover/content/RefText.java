package xyz.srnyx.annoyingapi.reflection.net.md_5.bungee.api.chat.hover.content;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Constructor;


/**
 * net.md_5.bungee.api.chat.hover.content.Text
 */
public class RefText {
    /**
     * 1.16.1+ net.md_5.bungee.api.chat.hover.content.Text
     */
    @Nullable public static final Class<?> TEXT_CLASS = ReflectionUtility.getClass(1, 16, 1, RefText.class);
    /**
     * 1.16.1+ net.md_5.bungee.api.chat.hover.content.Text(String)
     */
    @Nullable public static final Constructor<?> TEXT_CONSTRUCTOR = ReflectionUtility.getConstructor(1, 16, 1, TEXT_CLASS, String.class);

    /**
     * This class cannot be instantiated
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private RefText() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
