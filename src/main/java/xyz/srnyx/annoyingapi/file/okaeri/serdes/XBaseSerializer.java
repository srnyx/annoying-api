package xyz.srnyx.annoyingapi.file.okaeri.serdes;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.base.XBase;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class XBaseSerializer implements ObjectSerializer<XBase<?, ?>> {
    @NotNull private static final Map<Class<?>, Method> CACHED_METHODS = new HashMap<>();

    @Override
    public boolean supports(@NotNull Class<?> type) {
        final boolean supports = XBase.class.isAssignableFrom(type);
        System.out.println(type + " supports: " + supports);
        return supports;
    }

    @Override
    public void serialize(@NotNull XBase<?, ?> object, @NotNull SerializationData data, @NotNull GenericsDeclaration generics) {
        final String name = object.name();
        System.out.println("serialize name: " + name);
        data.setValue(name);
    }

    @Override @Nullable
    public XBase<?, ?> deserialize(@NotNull DeserializationData data, @NotNull GenericsDeclaration generics) {
        final String name = data.getValue(String.class);
        System.out.println("deserialize name: " + name);
        if (name == null) return null;
        final Class<?> type = generics.getType();
        System.out.println("type: " + type);

        // XMaterial (doesn't have of(String) method)
        if (XMaterial.class.isAssignableFrom(type)) return XMaterial.matchXMaterial(name).orElse(null);

        // Everything else
        Method method = CACHED_METHODS.get(type);
        try {
            // Method not cached, get and cache
            if (method == null) {
                method = type.getMethod("of", String.class);
                CACHED_METHODS.put(type, method);
            }

            // Invoke method
            final XBase<?, ?> base = ((Optional<XBase<?, ?>>) method.invoke(null, name)).orElse(null);
            System.out.println("base: " + base);
            return base;
        } catch (final IllegalArgumentException | ReflectiveOperationException e) {
            System.out.println("Failed to deserialize " + type + " from " + name);
            e.printStackTrace();
            return null;
        }
    }
}
