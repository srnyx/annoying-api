package xyz.srnyx.annoyingapi.stats.gson.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.Duration;


public class DurationAdapter extends TypeAdapter<Duration> {
    @Override
    public void write(@NotNull JsonWriter out, @Nullable Duration value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        out.value(value.toMillis());
    }

    @Override @NotNull
    public Duration read(@NotNull JsonReader in) throws IOException {
        return Duration.ofMillis(in.nextLong());
    }
}
