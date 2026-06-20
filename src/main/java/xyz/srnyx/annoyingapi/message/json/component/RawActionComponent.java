package xyz.srnyx.annoyingapi.message.json.component;

import net.md_5.bungee.api.chat.ClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class RawActionComponent extends RawTextComponent {
    @NotNull public ClickEvent.Action action;
    @NotNull public String actionValue;

    public RawActionComponent(@Nullable String key, @NotNull String raw, @NotNull String text, @Nullable String hover, @NotNull ClickEvent.Action action, @NotNull String actionValue) {
        super(key, raw, text, hover);
        this.action = action;
        this.actionValue = actionValue;
    }

    public RawActionComponent(@NotNull String raw, @NotNull String text, @Nullable String hover, @NotNull ClickEvent.Action action, @NotNull String actionValue) {
        this(null, raw, text, hover, action, actionValue);
    }
}
