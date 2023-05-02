package xyz.srnyx.annoyingapi.utility;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MapUtility {
    @NotNull
    public static <T, G> Map<T, G> mapOf(@NotNull List<T> keys, @NotNull List<G> values) {
        final Map<T, G> map = new HashMap<>();
        for (int i = 0; i < keys.size(); i++) map.put(keys.get(i), values.get(i));
        return map;
    }

    @NotNull
    public static <T, G> Map<T, G> mapOf(@NotNull T key, @NotNull G value) {
        final Map<T, G> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    @NotNull
    public static <T, G> Map<T, G> mapOf(@NotNull T key, @NotNull G value, @NotNull T key2, @NotNull G value2) {
        final Map<T, G> map = new HashMap<>();
        map.put(key, value);
        map.put(key2, value2);
        return map;
    }

    @NotNull
    public static <T, G> Map<T, G> mapOf(@NotNull T key, @NotNull G value, @NotNull T key2, @NotNull G value2, @NotNull T key3, @NotNull G value3) {
        final Map<T, G> map = new HashMap<>();
        map.put(key, value);
        map.put(key2, value2);
        map.put(key3, value3);
        return map;
    }

    @NotNull
    public static <T, G> Map<T, G> mapOf(@NotNull T key, @NotNull G value, @NotNull T key2, @NotNull G value2, @NotNull T key3, @NotNull G value3, @NotNull T key4, @NotNull G value4) {
        final Map<T, G> map = new HashMap<>();
        map.put(key, value);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        return map;
    }

    @NotNull
    public static <T, G> Map<T, G> mapOf(@NotNull T key, @NotNull G value, @NotNull T key2, @NotNull G value2, @NotNull T key3, @NotNull G value3, @NotNull T key4, @NotNull G value4, @NotNull T key5, @NotNull G value5) {
        final Map<T, G> map = new HashMap<>();
        map.put(key, value);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        map.put(key5, value5);
        return map;
    }

    @NotNull
    public static <T, G> Map<T, G> mapOf(@NotNull T key, @NotNull G value, @NotNull T key2, @NotNull G value2, @NotNull T key3, @NotNull G value3, @NotNull T key4, @NotNull G value4, @NotNull T key5, @NotNull G value5, @NotNull T key6, @NotNull G value6) {
        final Map<T, G> map = new HashMap<>();
        map.put(key, value);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        map.put(key5, value5);
        map.put(key6, value6);
        return map;
    }

    @NotNull
    public static <T, G> Map<T, G> mapOf(@NotNull T key, @NotNull G value, @NotNull T key2, @NotNull G value2, @NotNull T key3, @NotNull G value3, @NotNull T key4, @NotNull G value4, @NotNull T key5, @NotNull G value5, @NotNull T key6, @NotNull G value6, @NotNull T key7, @NotNull G value7) {
        final Map<T, G> map = new HashMap<>();
        map.put(key, value);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        map.put(key5, value5);
        map.put(key6, value6);
        map.put(key7, value7);
        return map;
    }

    @NotNull
    public static <T, G> Map<T, G> mapOf(@NotNull T key, @NotNull G value, @NotNull T key2, @NotNull G value2, @NotNull T key3, @NotNull G value3, @NotNull T key4, @NotNull G value4, @NotNull T key5, @NotNull G value5, @NotNull T key6, @NotNull G value6, @NotNull T key7, @NotNull G value7, @NotNull T key8, @NotNull G value8) {
        final Map<T, G> map = new HashMap<>();
        map.put(key, value);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        map.put(key5, value5);
        map.put(key6, value6);
        map.put(key7, value7);
        map.put(key8, value8);
        return map;
    }

    @NotNull
    public static <T, G> Map<T, G> mapOf(@NotNull T key, @NotNull G value, @NotNull T key2, @NotNull G value2, @NotNull T key3, @NotNull G value3, @NotNull T key4, @NotNull G value4, @NotNull T key5, @NotNull G value5, @NotNull T key6, @NotNull G value6, @NotNull T key7, @NotNull G value7, @NotNull T key8, @NotNull G value8, @NotNull T key9, @NotNull G value9) {
        final Map<T, G> map = new HashMap<>();
        map.put(key, value);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        map.put(key5, value5);
        map.put(key6, value6);
        map.put(key7, value7);
        map.put(key8, value8);
        map.put(key9, value9);
        return map;
    }

    @NotNull
    public static <T, G> Map<T, G> mapOf(@NotNull T key, @NotNull G value, @NotNull T key2, @NotNull G value2, @NotNull T key3, @NotNull G value3, @NotNull T key4, @NotNull G value4, @NotNull T key5, @NotNull G value5, @NotNull T key6, @NotNull G value6, @NotNull T key7, @NotNull G value7, @NotNull T key8, @NotNull G value8, @NotNull T key9, @NotNull G value9, @NotNull T key10, @NotNull G value10) {
        final Map<T, G> map = new HashMap<>();
        map.put(key, value);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        map.put(key5, value5);
        map.put(key6, value6);
        map.put(key7, value7);
        map.put(key8, value8);
        map.put(key9, value9);
        map.put(key10, value10);
        return map;
    }
}
