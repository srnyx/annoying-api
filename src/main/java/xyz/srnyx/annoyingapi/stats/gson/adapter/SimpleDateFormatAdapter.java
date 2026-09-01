package xyz.srnyx.annoyingapi.stats.gson.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.text.SimpleDateFormat;


public class SimpleDateFormatAdapter extends TypeAdapter<SimpleDateFormat> {
    @Override
    public void write(@NotNull JsonWriter out, @Nullable SimpleDateFormat value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        out.value(value.toPattern());
    }

    @Override @NotNull
    public SimpleDateFormat read(@NotNull JsonReader in) throws IOException {
        return new SimpleDateFormat(in.nextString());
    }
}
