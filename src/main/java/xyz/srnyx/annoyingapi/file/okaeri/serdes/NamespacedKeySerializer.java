package xyz.srnyx.annoyingapi.file.okaeri.serdes;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.schema.GenericsPair;
import eu.okaeri.configs.serdes.BidirectionalTransformer;
import eu.okaeri.configs.serdes.SerdesContext;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.NAMESPACED_KEY_CLASS;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.NAMESPACED_KEY_CONSTRUCTOR;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.NAMESPACED_KEY_GET_KEY_METHOD;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.newNamespacedKeyThrow;


public class NamespacedKeySerializer extends BidirectionalTransformer<String, Object> implements Supplier<Boolean> {
    @NotNull private final Plugin plugin;

    public NamespacedKeySerializer(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    @Override @NotNull
    public Boolean get() {
        return NAMESPACED_KEY_CLASS != null && NAMESPACED_KEY_CONSTRUCTOR != null && NAMESPACED_KEY_GET_KEY_METHOD != null;
    }

    @Override @NotNull
    public GenericsPair<String, Object> getPair() {
        return new GenericsPair<>(GenericsDeclaration.of(String.class), GenericsDeclaration.of(NAMESPACED_KEY_CLASS));
    }

    @Override @NotNull
    public Object leftToRight(@NotNull String data, @NotNull SerdesContext serdesContext) {
        if (NAMESPACED_KEY_CONSTRUCTOR == null) {
            throw new IllegalStateException("NamespacedKey constructor is null, cannot transform String to NamespacedKey");
        }

        try {
            return newNamespacedKeyThrow(plugin, data);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to transform String to NamespacedKey", e);
        }
    }

    @Override @NotNull
    public String rightToLeft(@NotNull Object data, @NotNull SerdesContext serdesContext) {
        if (NAMESPACED_KEY_GET_KEY_METHOD == null) {
            throw new IllegalStateException("NamespacedKey getKey method is null, cannot transform NamespacedKey to String");
        }

        try {
            return (String) NAMESPACED_KEY_GET_KEY_METHOD.invoke(data);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to transform NamespacedKey to String", e);
        }
    }
}
