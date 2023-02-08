package xyz.srnyx.annoyingapi;

import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Class to manage and build JSON messages ({@link BaseComponent}[])
 */
public class AnnoyingJSON {
    @NotNull private final ComponentBuilder builder = new ComponentBuilder();

    /**
     * Constructs a new {@link AnnoyingJSON} instance
     */
    public AnnoyingJSON() {
        // Only exists to give the constructor a Javadoc
    }

    /**
     * Gets the {@link ComponentBuilder} of the {@link AnnoyingJSON} instance
     *
     * @return  the {@link ComponentBuilder} of the {@link AnnoyingJSON} instance
     */
    @NotNull
    public ComponentBuilder getBuilder() {
        return builder;
    }

    /**
     * Runs {@link ComponentBuilder#create()} and returns the result ({@link BaseComponent}[])
     *
     * @return  the result of {@link ComponentBuilder#create()} ({@link BaseComponent}[])
     */
    @NotNull
    public BaseComponent[] build() {
        return builder.create();
    }

    /**
     * Runs {@link ComponentBuilder#append(BaseComponent)} on the {@link ComponentBuilder} of the {@link AnnoyingJSON} instance
     *
     * @param   component   the component to append
     *
     * @return              the {@link AnnoyingJSON} instance
     */
    @NotNull
    public AnnoyingJSON append(@NotNull BaseComponent component) {
        builder.append(component);
        return this;
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
        return append(new TextComponent(AnnoyingUtility.color(display)));
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
        append(display);
        if (hover != null) builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(AnnoyingUtility.color(hover))));
        return this;
    }

    /**
     * Appends a {@link String} (ChatColors translated) to the {@link ComponentBuilder} of the {@link AnnoyingJSON} instance
     * <p>Also sets the {@link HoverEvent} and {@link ClickEvent} of the appended {@link TextComponent}
     *
     * @param  display  the {@link String} to append
     * @param  hover    the {@link HoverEvent} text to set
     * @param  action   the {@link ClickEvent} action to set
     * @param  value    the {@link ClickEvent} value to set
     *
     * @return  the {@link AnnoyingJSON} instance
     */
    @NotNull
    public AnnoyingJSON append(@NotNull String display, @Nullable String hover, @Nullable ClickEvent.Action action, @Nullable String value) {
        append(display, hover);
        if (action != null && value != null) builder.event(new ClickEvent(action, AnnoyingUtility.color(value)));
        return this;
    }
}
