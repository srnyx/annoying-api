package xyz.srnyx.annoyingapi.stats;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.Nullable;


public interface Statable {
    @Nullable
    default JsonElement toStat() {
        return new JsonPrimitive(toString());
    }
}
