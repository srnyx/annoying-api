package xyz.srnyx.annoyingapi;

import net.md_5.bungee.api.chat.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.utility.AnnoyingUtility;

import java.util.ArrayList;
import java.util.List;


/**
 * Class to manage and build JSON messages ({@link BaseComponent}[])
 */
public class AnnoyingJSON {
    /**
     * The {@link ComponentBuilder} to build the JSON message with
     */
    @NotNull private final List<BaseComponent> components = new ArrayList<>();

    /**
     * Constructs a new {@link AnnoyingJSON} instance
     */
    public AnnoyingJSON() {
        // Only exists to give the constructor a Javadoc
    }

    /**
     * Converts the {@link #components} to a {@link BaseComponent} array and returns it
     *
     * @return  the {@link #components} as an array
     */
    @NotNull
    public BaseComponent[] build() {
        return components.toArray(new BaseComponent[0]);
    }

    /**
     * Appends a {@link BaseComponent} to the message
     *
     * @param   component   the {@link BaseComponent} to append
     *
     * @return              the {@link AnnoyingJSON} instance
     */
    @NotNull
    public AnnoyingJSON append(@NotNull BaseComponent component) {
        components.add(component);
        return this;
    }

    /**
     * Appends a {@link String} (ChatColors translated) to the {@link ComponentBuilder} of the {@link AnnoyingJSON} instance
     * <p>Also sets the {@link HoverEvent} and {@link ClickEvent} of the appended {@link TextComponent}
     *
     * @param  display      the {@link String} to append
     * @param  hover        the {@link HoverEvent} value to set
     * @param  clickAction  the {@link ClickEvent.Action} to set
     * @param  clickValue   the {@link ClickEvent} value to set
     *
     * @return              the {@link AnnoyingJSON} instance
     */
    @NotNull
    public AnnoyingJSON append(@NotNull String display, @Nullable String hover, @Nullable ClickEvent.Action clickAction, @Nullable String clickValue) {
        final TextComponent component = new TextComponent(AnnoyingUtility.color(display));
        if (hover != null) component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(AnnoyingUtility.color(hover)).create()));
        if (clickAction != null && clickValue != null) component.setClickEvent(new ClickEvent(clickAction, AnnoyingUtility.color(clickValue)));
        return append(component);
    }

    /**
     * Appends a {@link String} (ChatColors translated) to the {@link ComponentBuilder} of the {@link AnnoyingJSON} instance
     * <p>Also sets the {@link HoverEvent} of the appended {@link TextComponent}
     *
     * @param  display  the {@link String} to append
     * @param  hover    the {@link HoverEvent} text to set
     *
     * @return  the {@link AnnoyingJSON} instance
     */
    @NotNull
    public AnnoyingJSON append(@NotNull String display, @Nullable String hover) {
        return append(display, hover, null, null);
    }

    /**
     * Appends a {@link String} (ChatColors translated) to the {@link ComponentBuilder} of the {@link AnnoyingJSON} instance
     *
     * @param   display the {@link String} to append
     *
     * @return          the {@link AnnoyingJSON} instance
     */
    @NotNull
    public AnnoyingJSON append(@NotNull String display) {
        return append(display, null);
    }
}
