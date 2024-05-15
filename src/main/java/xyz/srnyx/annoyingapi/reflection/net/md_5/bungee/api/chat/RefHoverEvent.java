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
import static xyz.srnyx.annoyingapi.reflection.net.md_5.bungee.api.chat.hover.content.RefContent.CONTENT_CLASS;
import static xyz.srnyx.annoyingapi.reflection.net.md_5.bungee.api.chat.hover.content.RefText.TEXT_CONSTRUCTOR;


/**
 * net.md_5.bungee.api.chat.HoverEvent
 */
public class RefHoverEvent {
    /**
     * 1.16.1+ net.md_5.bungee.api.chat.HoverEvent({@link HoverEvent.Action}, {@link RefContent}[])
     */
    @Nullable public static final Constructor<HoverEvent> HOVER_EVENT_CONSTRUCTOR = ReflectionUtility.getConstructor(1, 16, 1, HoverEvent.class, HoverEvent.Action.class, CONTENT_ARRAY_CLASS);

    /**
     * Create a {@link HoverEvent} with the given {@link HoverEvent.Action} and content, using reflection if needed
     *
     * @param   action  the {@link HoverEvent.Action}
     * @param   content the content of the hover event
     *
     * @return          the created {@link HoverEvent}
     */
    @NotNull
    public static HoverEvent createHoverEvent(@NotNull HoverEvent.Action action, @NotNull String content) {
        if (HOVER_EVENT_CONSTRUCTOR != null && CONTENT_CLASS != null && TEXT_CONSTRUCTOR != null) try {
            final Object contents = ReflectionUtility.createArray(CONTENT_CLASS, 1);
            if (contents == null) return getDefaultHoverEvent(action, content);
            Array.set(contents, 0, TEXT_CONSTRUCTOR.newInstance(content));
            return HOVER_EVENT_CONSTRUCTOR.newInstance(action, contents);
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException ignored) {}
        return getDefaultHoverEvent(action, content);
    }

    /**
     * Get the default {@link HoverEvent}
     *
     * @param   action  the {@link HoverEvent.Action}
     * @param   content the content of the hover event
     *
     * @return          the default {@link HoverEvent}
     */
    @NotNull
    private static HoverEvent getDefaultHoverEvent(@NotNull HoverEvent.Action action, @NotNull String content) {
        return new HoverEvent(action, new ComponentBuilder(content).create());
    }

    /**
     * This class cannot be instantiated
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private RefHoverEvent() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
