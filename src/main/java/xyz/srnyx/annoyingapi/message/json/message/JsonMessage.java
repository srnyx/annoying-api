package xyz.srnyx.annoyingapi.message.json.message;

import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.message.AnnoyingMessage;


public class JsonMessage {
    @NotNull public final AnnoyingPlugin plugin;

    public JsonMessage(@NotNull AnnoyingPlugin plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public AnnoyingMessage newAnnoyingMessage() {
        return new AnnoyingMessage(this);
    }
}
