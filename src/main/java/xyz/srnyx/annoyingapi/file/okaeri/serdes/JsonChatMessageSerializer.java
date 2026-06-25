package xyz.srnyx.annoyingapi.file.okaeri.serdes;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.message.json.message.JsonChatMessage;

import java.util.LinkedHashMap;
import java.util.Map;


public class JsonChatMessageSerializer implements ObjectSerializer<JsonChatMessage> {
    @NotNull private final AnnoyingPlugin plugin;

    public JsonChatMessageSerializer(@NotNull AnnoyingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean supports(@NotNull Class<?> type) {
        return JsonChatMessage.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NotNull JsonChatMessage object, @NotNull SerializationData data, @NotNull GenericsDeclaration generics) {
        // One component
        if (object.components.size() == 1) {
            data.setValue(object.components.values().iterator().next());
            return;
        }

        // Multiple components
        for (final Map.Entry<String, String> entry : object.components.entrySet()) data.set(entry.getKey(), entry.getValue());
    }

    @Override @Nullable
    public JsonChatMessage deserialize(@NotNull DeserializationData data, @NotNull GenericsDeclaration generics) {
        // One component
        if (data.isValue()) return new JsonChatMessage(plugin, data.getValue(String.class));

        // Multiple components
        final LinkedHashMap<String, String> rawComponents = new LinkedHashMap<>();
        for (final Map.Entry<String, Object> entry : data.asMap().entrySet()) rawComponents.put(entry.getKey(), entry.getValue().toString());
        return new JsonChatMessage(plugin, rawComponents);
    }
}
