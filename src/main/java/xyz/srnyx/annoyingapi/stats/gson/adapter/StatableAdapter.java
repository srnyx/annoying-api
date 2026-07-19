package xyz.srnyx.annoyingapi.stats.gson.adapter;

import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.stats.Statable;
import xyz.srnyx.annoyingapi.stats.gson.StatsGson;

import java.io.IOException;


public class StatableAdapter extends TypeAdapter<Statable> {
    @Override
    public void write(@NotNull JsonWriter out, @Nullable Statable value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        final JsonElement json = value.toStat();
        if (json == null) {
            out.nullValue();
            return;
        }

        StatsGson.GSON.toJson(json, out);
    }

    @Override @Nullable
    public Statable read(@NotNull JsonReader in) {
        return null;
    }
}
