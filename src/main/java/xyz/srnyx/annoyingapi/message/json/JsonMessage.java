package xyz.srnyx.annoyingapi.message.json;

import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.message.AnnoyingMessage;
import xyz.srnyx.annoyingapi.message.json.component.RawTextComponent;

import java.util.LinkedHashMap;
import java.util.Map;


public record JsonMessage(@NotNull AnnoyingPlugin plugin, @NotNull Map<String, RawTextComponent> components) {
    public JsonMessage(@NotNull AnnoyingPlugin plugin, @NotNull RawTextComponent component) {
        this(plugin, new LinkedHashMap<>());
        this.components.put("text_default", component);
    }

    public JsonMessage(@NotNull AnnoyingPlugin plugin, @NotNull String raw) {
        this(plugin, new LinkedHashMap<>());
        this.components.put("text_default", plugin.deserializeTextComponent(raw));
    }

    @NotNull
    public AnnoyingMessage newAnnoyingMessage() {
        return new AnnoyingMessage(this);
    }
}
