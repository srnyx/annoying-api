package xyz.srnyx.annoyingapi.utility;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A utility class for quickly generating {@link Map maps}
 */
@SuppressWarnings("DuplicatedCode")
public class MapUtility {
    /**
     * Generates a map from a list of keys and a list of values
     *
     * @param   keys    the list of keys
     * @param   values  the list of values
     *
     * @return          the generated map
     *
     * @param   <T>     the type of the keys
     * @param   <G>     the type of the values
     */
    @NotNull
    public static <T, G> Map<T, G> mapOf(@NotNull List<T> keys, @NotNull List<G> values) {
        final Map<T, G> map = new HashMap<>();
        for (int i = 0; i < keys.size(); i++) map.put(keys.get(i), values.get(i));
        return map;
    }

    /**
     * Generates a map from a key and a value
     *
     * @param   key     the key
     * @param   value   the value
     *
     * @return          the generated map
     *
     * @param   <T>     the type of the key
     * @param   <G>     the type of the value
     */
    @NotNull
    public static <T, G> Map<T, G> mapOf(@NotNull T key, @NotNull G value) {
        final Map<T, G> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    /**
     * Generates a map from two keys and two values
     *
     * @param   key     the first key
     * @param   value   the first value
     * @param   key2    the second key
     * @param   value2  the second value
     *
     * @return          the generated map
     *
     * @param   <T>     the type of the keys
     * @param   <G>     the type of the values
     */
    @NotNull
    public static <T, G> Map<T, G> mapOf(@NotNull T key, @NotNull G value, @NotNull T key2, @NotNull G value2) {
        final Map<T, G> map = new HashMap<>();
        map.put(key, value);
        map.put(key2, value2);
        return map;
    }

    /**
     * Generates a map from three keys and three values
     *
     * @param   key     the first key
     * @param   value   the first value
     * @param   key2    the second key
     * @param   value2  the second value
     * @param   key3    the third key
     * @param   value3  the third value
     *
     * @return          the generated map
     *
     * @param   <T>     the type of the keys
     * @param   <G>     the type of the values
     */
    @NotNull
    public static <T, G> Map<T, G> mapOf(@NotNull T key, @NotNull G value, @NotNull T key2, @NotNull G value2, @NotNull T key3, @NotNull G value3) {
        final Map<T, G> map = new HashMap<>();
        map.put(key, value);
        map.put(key2, value2);
        map.put(key3, value3);
        return map;
    }

    /**
     * Generates a map from four keys and four values
     *
     * @param   key     the first key
     * @param   value   the first value
     * @param   key2    the second key
     * @param   value2  the second value
     * @param   key3    the third key
     * @param   value3  the third value
     * @param   key4    the fourth key
     * @param   value4  the fourth value
     *
     * @return          the generated map
     *
     * @param   <T>     the type of the keys
     * @param   <G>     the type of the values
     */
    @NotNull
    public static <T, G> Map<T, G> mapOf(@NotNull T key, @NotNull G value, @NotNull T key2, @NotNull G value2, @NotNull T key3, @NotNull G value3, @NotNull T key4, @NotNull G value4) {
        final Map<T, G> map = new HashMap<>();
        map.put(key, value);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        return map;
    }

    /**
     * Generates a map from five keys and five values
     *
     * @param   key     the first key
     * @param   value   the first value
     * @param   key2    the second key
     * @param   value2  the second value
     * @param   key3    the third key
     * @param   value3  the third value
     * @param   key4    the fourth key
     * @param   value4  the fourth value
     * @param   key5    the fifth key
     * @param   value5  the fifth value
     *
     * @return          the generated map
     *
     * @param   <T>     the type of the keys
     * @param   <G>     the type of the values
     */
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

    /**
     * Generates a map from six keys and six values
     *
     * @param   key     the first key
     * @param   value   the first value
     * @param   key2    the second key
     * @param   value2  the second value
     * @param   key3    the third key
     * @param   value3  the third value
     * @param   key4    the fourth key
     * @param   value4  the fourth value
     * @param   key5    the fifth key
     * @param   value5  the fifth value
     * @param   key6    the sixth key
     * @param   value6  the sixth value
     *
     * @return          the generated map
     *
     * @param   <T>     the type of the keys
     * @param   <G>     the type of the values
     */
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

    /**
     * Generates a map from seven keys and seven values
     *
     * @param   key     the first key
     * @param   value   the first value
     * @param   key2    the second key
     * @param   value2  the second value
     * @param   key3    the third key
     * @param   value3  the third value
     * @param   key4    the fourth key
     * @param   value4  the fourth value
     * @param   key5    the fifth key
     * @param   value5  the fifth value
     * @param   key6    the sixth key
     * @param   value6  the sixth value
     * @param   key7    the seventh key
     * @param   value7  the seventh value
     *
     * @return          the generated map
     *
     * @param   <T>     the type of the keys
     * @param   <G>     the type of the values
     */
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

    /**
     * Generates a map from eight keys and eight values
     *
     * @param   key     the first key
     * @param   value   the first value
     * @param   key2    the second key
     * @param   value2  the second value
     * @param   key3    the third key
     * @param   value3  the third value
     * @param   key4    the fourth key
     * @param   value4  the fourth value
     * @param   key5    the fifth key
     * @param   value5  the fifth value
     * @param   key6    the sixth key
     * @param   value6  the sixth value
     * @param   key7    the seventh key
     * @param   value7  the seventh value
     * @param   key8    the eighth key
     * @param   value8  the eighth value
     *
     * @return          the generated map
     *
     * @param   <T>     the type of the keys
     * @param   <G>     the type of the values
     */
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

    /**
     * Generates a map from nine keys and nine values
     *
     * @param   key     the first key
     * @param   value   the first value
     * @param   key2    the second key
     * @param   value2  the second value
     * @param   key3    the third key
     * @param   value3  the third value
     * @param   key4    the fourth key
     * @param   value4  the fourth value
     * @param   key5    the fifth key
     * @param   value5  the fifth value
     * @param   key6    the sixth key
     * @param   value6  the sixth value
     * @param   key7    the seventh key
     * @param   value7  the seventh value
     * @param   key8    the eighth key
     * @param   value8  the eighth value
     * @param   key9    the ninth key
     * @param   value9  the ninth value
     *
     * @return          the generated map
     *
     * @param   <T>     the type of the keys
     * @param   <G>     the type of the values
     */
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

    /**
     * Generates a map from ten keys and ten values
     *
     * @param   key     the first key
     * @param   value   the first value
     * @param   key2    the second key
     * @param   value2  the second value
     * @param   key3    the third key
     * @param   value3  the third value
     * @param   key4    the fourth key
     * @param   value4  the fourth value
     * @param   key5    the fifth key
     * @param   value5  the fifth value
     * @param   key6    the sixth key
     * @param   value6  the sixth value
     * @param   key7    the seventh key
     * @param   value7  the seventh value
     * @param   key8    the eighth key
     * @param   value8  the eighth value
     * @param   key9    the ninth key
     * @param   value9  the ninth value
     * @param   key10   the tenth key
     * @param   value10 the tenth value
     *
     * @return          the generated map
     *
     * @param   <T>     the type of the keys
     * @param   <G>     the type of the values
     */
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

    /**
     * Constructs a new {@link MapUtility} instance (illegal)
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private MapUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
