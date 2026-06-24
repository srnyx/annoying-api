package xyz.srnyx.annoyingapi.file.okaeri.serdes.color;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerdesContext;
import eu.okaeri.configs.serdes.SerializationData;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class ColorSerializer implements ObjectSerializer<Color> {
    @NotNull private static final Map<Color, String> COLOR_TO_NAME = new HashMap<>();
    @NotNull private static final Map<String, Color> NAME_TO_COLOR = new HashMap<>();
    static {
        for (final Field field : Color.class.getFields()) {
            if (field.getType() == Color.class) try {
                final Color color = (Color) field.get(null);
                final String name = field.getName().toUpperCase();
                COLOR_TO_NAME.put(color, name);
                NAME_TO_COLOR.put(name, color);
            } catch (final IllegalAccessException ignored) {}
        }
    }

    @Override
    public boolean supports(@NotNull Class<?> type) {
        return Color.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NotNull Color object, @NotNull SerializationData data, @NotNull GenericsDeclaration generics) {
        // Get formats
        final Set<ColorFormat> format = getFormats(data.getContext());

        // CUSTOM
        if (format.contains(ColorFormat.CUSTOM)) {
            data.set("red", object.getRed());
            data.set("green", object.getGreen());
            data.set("blue", object.getBlue());
            return;
        }

        // NAME
        if (format.contains(ColorFormat.NAME)) {
            data.set("color", COLOR_TO_NAME.get(object));
            return;
        }

        // No supported formats
        throw new IllegalArgumentException("No supported color formats found");
    }

    @Override @Nullable
    public Color deserialize(@NotNull DeserializationData data, @NotNull GenericsDeclaration generics) {
        // Get formats
        final Set<ColorFormat> formats = getFormats(data.getContext());

        // CUSTOM
        if (formats.contains(ColorFormat.CUSTOM)) {
            if (data.isValue()) {
                // Get single value
                final String value = data.getValue(String.class);
                if (value == null) return null;

                // Single int
                try {
                    return Color.fromRGB(Integer.parseInt(value));
                } catch (final NumberFormatException ignored) {}

                // Single hex
                String hex = value.toUpperCase();
                if (hex.startsWith("#")) hex = hex.substring(1);
                try {
                    return Color.fromRGB(Integer.parseInt(hex, 16));
                } catch (final NumberFormatException ignored) {}
            } else {
                // Separate RGB
                final Integer red = data.get("red", Integer.class);
                final Integer green = data.get("green", Integer.class);
                final Integer blue = data.get("blue", Integer.class);
                if (red != null && green != null && blue != null) return Color.fromRGB(red, green, blue);
            }
        }

        // NAME
        if (formats.contains(ColorFormat.NAME)) {
            final String value = data.getValue(String.class);
            if (value == null) return null;
            return NAME_TO_COLOR.get(value.toUpperCase());
        }

        // No supported formats
        throw new IllegalArgumentException("No supported color formats found");
    }

    @NotNull
    private static Set<ColorFormat> getFormats(@NotNull SerdesContext context) {
        return context.getAttachment(ColorSpecData.class)
                .map(ColorSpecData::formats)
                .filter(formats -> !formats.isEmpty()) // Default to all
                .orElseGet(() -> Set.of(ColorFormat.values()));
    }
}
