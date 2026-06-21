package xyz.srnyx.annoyingapi.file.okaeri.serdes;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.message.json.message.JsonTitleMessage;


public class JsonTitleMessageSerializer implements ObjectSerializer<JsonTitleMessage> {
    @NotNull private final AnnoyingPlugin plugin;

    public JsonTitleMessageSerializer(@NotNull AnnoyingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean supports(@NotNull Class<?> type) {
        return JsonTitleMessage.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NotNull JsonTitleMessage object, @NotNull SerializationData data, @NotNull GenericsDeclaration generics) {
        data.set("title", object.title);
        data.set("subtitle", object.subtitle);
    }

    @Override @Nullable
    public JsonTitleMessage deserialize(@NotNull DeserializationData data, @NotNull GenericsDeclaration generics) {
        final String title = data.getOr("title", String.class, "");
        final String subtitle = data.getOr("subtitle", String.class, "");
        return new JsonTitleMessage(plugin, title, subtitle);
    }
}
