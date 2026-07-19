package xyz.srnyx.annoyingapi.stats.gson.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.schema.FieldDeclaration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.stats.Stat;
import xyz.srnyx.annoyingapi.stats.gson.StatsGson;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Set;


public class ConfigAdapter extends TypeAdapter<OkaeriConfig> {
    @Override
    public void write(@NotNull JsonWriter out, @Nullable OkaeriConfig config) throws IOException {
        if (config == null) {
            out.nullValue();
            return;
        }

        // Build object (not using JsonWriter so that we can check if empty before writing)
        final JsonObject json = new JsonObject();
        for (final FieldDeclaration field : config.getDeclaration().getFields()) {
            // Get value
            final Object value = field.getValue();
            if (value == null) continue;
            final Class<?> type = field.getType().getType();

            // Get @Stat
            final Stat stat = field.getAnnotation(Stat.class).orElse(null);
            final String key = stat != null && !stat.key().isEmpty() ? stat.key() : field.getName();

            if (!OkaeriConfig.class.isAssignableFrom(type)) {
                // OkaeriConfig instances don't require @Stat
                if (stat == null) continue;

                // Array/Collection/Map specials
                final boolean isArray = type.isArray();
                final boolean isCollection = Collection.class.isAssignableFrom(type);
                final boolean isMap = Map.class.isAssignableFrom(type);
                if (isArray || isCollection || isMap) {
                    // Size-only
                    if (stat.sizeOnly()) {
                        // Get size
                        final int size;
                        if (isArray) {
                            // Array
                            size = Array.getLength(value);
                        } else if (isCollection) {
                            // Collection
                            size = ((Collection<?>) value).size();
                        } else {
                            // Map
                            size = ((Map<?, ?>) value).size();
                        }

                        // Put size
                        json.addProperty(key + "_size", size);
                        continue;
                    }

                    // Map keys only
                    if (stat.mapKeysOnly() && isMap) {
                        json.add(key + "_keys", StatsGson.GSON.toJsonTree(((Map<?, ?>) value).keySet(), Set.class));
                        continue;
                    }
                }
            }

            // Else (skip null elements and empty objects)
            final JsonElement element = StatsGson.GSON.toJsonTree(value, type);
            if (!element.isJsonNull() && (!element.isJsonObject() || !element.getAsJsonObject().isEmpty())) {
                json.add(key, element);
            }
        }

        // We don't want empty objects
        if (!json.isEmpty()) StatsGson.GSON.toJson(json, out);
    }

    @Override @Nullable
    public OkaeriConfig read(@NotNull JsonReader in) {
        return null;
    }
}
