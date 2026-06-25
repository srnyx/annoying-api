package xyz.srnyx.annoyingapi.message.json.message;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.util.LinkedHashMap;
import java.util.Map;


public final class JsonChatMessage extends JsonMessage {
    @NotNull public final Map<String, String> components;
    @Nullable public Boolean shouldCache = null;

    public JsonChatMessage(@NotNull AnnoyingPlugin plugin, @NotNull Map<String, String> components) {
        super(plugin);
        this.components = components;
    }

    public JsonChatMessage(@NotNull AnnoyingPlugin plugin, @NotNull String component) {
        super(plugin);
        this.components = new LinkedHashMap<>();
        this.components.put("suggest_default", component);
    }

    /**
     * Don't cache if a component contains %command% placeholder
     */
    public boolean shouldCache() {
        shouldCache = true;
        for (final String rawComponent : components.values()) {
            if (rawComponent.contains("%command%")) {
                shouldCache = false;
                break;
            }
        }
        return shouldCache;
    }
}
