package xyz.srnyx.annoyingapi.file.okaeri.serdes;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.message.json.JsonMessage;
import xyz.srnyx.annoyingapi.message.json.component.RawTextComponent;

import java.util.LinkedHashMap;
import java.util.Map;


public class JsonMessageSerializer implements ObjectSerializer<JsonMessage> {
    @NotNull private final AnnoyingPlugin plugin;

    public JsonMessageSerializer(@NotNull AnnoyingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean supports(@NotNull Class<?> type) {
        return JsonMessage.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NotNull JsonMessage object, @NotNull SerializationData data, @NotNull GenericsDeclaration generics) {
        // One component
        if (object.components().size() == 1) {
            data.setValue(object.components().values().iterator().next().raw);
            return;
        }

        // Multiple components
        for (final Map.Entry<String, RawTextComponent> entry : object.components().entrySet()) {
            data.set(entry.getKey(), entry.getValue().raw);
        }
    }

    @Override @Nullable
    public JsonMessage deserialize(@NotNull DeserializationData data, @NotNull GenericsDeclaration generics) {
        final LinkedHashMap<String, RawTextComponent> components = new LinkedHashMap<>();

        // One component
        if (data.isValue()) return new JsonMessage(plugin, plugin.deserializeTextComponent(null, data.getValue(String.class)));

        // Multiple components
        for (final Map.Entry<String, Object> entry : data.asMap().entrySet()) {
            components.put(entry.getKey(), plugin.deserializeTextComponent(entry.getKey(), entry.getValue().toString()));
        }
        return new JsonMessage(plugin, components);
    }
}
