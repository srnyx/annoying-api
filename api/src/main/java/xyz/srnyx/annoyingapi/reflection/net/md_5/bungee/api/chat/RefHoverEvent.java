package xyz.srnyx.annoyingapi.reflection.net.md_5.bungee.api.chat;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.reflection.net.md_5.bungee.api.chat.hover.content.RefContent;
import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static xyz.srnyx.annoyingapi.reflection.net.md_5.bungee.api.chat.hover.content.RefContent.CONTENT_ARRAY_CLASS;
import static xyz.srnyx.annoyingapi.reflection.net.md_5.bungee.api.chat.hover.content.RefText.TEXT_CONSTRUCTOR;


/**
 * net.md_5.bungee.api.chat.HoverEvent
 */
public class RefHoverEvent {
    /**
     * 1.16.1+ net.md_5.bungee.api.chat.HoverEvent({@link HoverEvent.Action}, {@link RefContent}[])
     */
    @Nullable public static final Constructor<HoverEvent> HOVER_EVENT_CONSTRUCTOR = ReflectionUtility.getConstructor(1, 16, 1, HoverEvent.class, HoverEvent.Action.class, CONTENT_ARRAY_CLASS);

    @NotNull
    public static HoverEvent createHoverEvent(@NotNull HoverEvent.Action action, @NotNull String content) {
        if (HOVER_EVENT_CONSTRUCTOR != null && CONTENT_ARRAY_CLASS != null && TEXT_CONSTRUCTOR != null) try {
            final Object contents = ReflectionUtility.createArray(CONTENT_ARRAY_CLASS, 1);
            if (contents == null) return getDefaultHoverEvent(action, content);
            Array.set(contents, 0, TEXT_CONSTRUCTOR.newInstance(content));
            return HOVER_EVENT_CONSTRUCTOR.newInstance(action, contents);
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException ignored) {}
        return getDefaultHoverEvent(action, content);
    }

    @NotNull
    private static HoverEvent getDefaultHoverEvent(@NotNull HoverEvent.Action action, @NotNull String content) {
        return new HoverEvent(action, new ComponentBuilder(content).create());
    }
}
