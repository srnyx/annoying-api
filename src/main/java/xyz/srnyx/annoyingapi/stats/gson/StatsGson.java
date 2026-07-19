package xyz.srnyx.annoyingapi.stats.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.okaeri.configs.OkaeriConfig;
import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.stats.Statable;
import xyz.srnyx.annoyingapi.stats.gson.adapter.ConfigAdapter;
import xyz.srnyx.annoyingapi.stats.gson.adapter.DurationAdapter;
import xyz.srnyx.annoyingapi.stats.gson.adapter.StatableAdapter;

import java.time.Duration;


public class StatsGson {
    @NotNull public static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(OkaeriConfig.class, new ConfigAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeHierarchyAdapter(Statable.class, new StatableAdapter())
            .create();
}
