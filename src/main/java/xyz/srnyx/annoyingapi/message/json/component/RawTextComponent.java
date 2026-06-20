package xyz.srnyx.annoyingapi.message.json.component;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class RawTextComponent {
    @Nullable public final String key;
    @NotNull public final String raw;
    @NotNull public String text;
    @Nullable public String hover;

    public RawTextComponent(@Nullable String key, @NotNull String raw, @NotNull String text, @Nullable String hover) {
        this.key = key;
        this.raw = raw;
        this.text = text;
        this.hover = hover;
    }

    public RawTextComponent(@NotNull String raw, @NotNull String text, @Nullable String hover) {
        this(null, raw, text, hover);
    }

    @NotNull
    public RawTextComponent copy() {
        return new RawTextComponent(key, raw, text, hover);
    }
}
