package xyz.srnyx.annoyingapi.file.okaeri.serdes.color;

import eu.okaeri.configs.exception.OkaeriConfigException;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerdesContext;
import eu.okaeri.configs.serdes.SerializationData;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;


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
        // Get format
        final ColorFormat format = getFormat(data.getContext());

        // NAME
        if (format == ColorFormat.NAME) {
            data.set("color", COLOR_TO_NAME.get(object));
            return;
        }

        // CUSTOM
        if (format == ColorFormat.CUSTOM) {
            data.set("red", object.getRed());
            data.set("green", object.getGreen());
            data.set("blue", object.getBlue());
            return;
        }

        // Unknown format
        throw new IllegalArgumentException("DEVELOPER: Unsupported color format: " + format);
    }

    @Override @NotNull
    public Color deserialize(@NotNull DeserializationData data, @NotNull GenericsDeclaration generics) {
        // Get format
        final ColorFormat format = getFormat(data.getContext());

        // NAME
        if (format == ColorFormat.NAME) {
            final String name = data.get("color", String.class);
            if (name == null) throw new IllegalStateException("Color name is required");
            return NAME_TO_COLOR.get(name.toUpperCase());
        }

        // CUSTOM
        if (format == ColorFormat.CUSTOM) {
            // Single values
            if (data.isValue()) {
                // Single int
                try {
                    final Integer rgb = data.getValue(Integer.class);
                    if (rgb != null) return Color.fromRGB(rgb);
                } catch (final OkaeriConfigException ignored) {}

                // Single hex
                String hex = data.getValue(String.class);
                if (hex != null) {
                    if (hex.startsWith("#")) hex = hex.substring(1);
                    return Color.fromRGB(Integer.parseInt(hex, 16));
                }

                // Unknown custom format (single)
                throw new IllegalArgumentException("Invalid custom color format");
            }

            // Separate RGB
            final Integer red = data.get("red", Integer.class);
            final Integer green = data.get("green", Integer.class);
            final Integer blue = data.get("blue", Integer.class);
            System.out.println("red: " + red + ", green: " + green + ", blue: " + blue);
            if (red != null && green != null && blue != null) return Color.fromRGB(red, green, blue);

            // Unknown custom format (object)
            throw new IllegalArgumentException("Invalid custom color format");
        }

        // Unknown format
        throw new IllegalArgumentException("DEVELOPER: Unsupported color format: " + format);
    }

    @NotNull
    private static ColorFormat getFormat(@NotNull SerdesContext context) {
        return context.getAttachment(ColorSpecData.class)
                .map(ColorSpecData::format)
                .orElse(ColorFormat.CUSTOM);
    }
}
