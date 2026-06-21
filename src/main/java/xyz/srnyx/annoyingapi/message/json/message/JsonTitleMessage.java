package xyz.srnyx.annoyingapi.message.json.message;

import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;


public class JsonTitleMessage extends JsonMessage {
    @NotNull public final String title;
    @NotNull public final String subtitle;

    public JsonTitleMessage(@NotNull AnnoyingPlugin plugin, @NotNull String title, @NotNull String subtitle) {
        super(plugin);
        this.title = title;
        this.subtitle = subtitle;
    }
}
