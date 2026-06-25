package xyz.srnyx.annoyingapi.file.okaeri.serdes.color;

import eu.okaeri.configs.serdes.SerdesAnnotationResolver;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;


public class ColorAttachmentResolver implements SerdesAnnotationResolver<ColorSpec, ColorSpecData> {
    @Override @NotNull
    public Class<ColorSpec> getAnnotationType() {
        return ColorSpec.class;
    }

    @Override @NotNull
    public Optional<ColorSpecData> resolveAttachment(@NotNull Field field, @NotNull ColorSpec annotation) {
        return Optional.of(new ColorSpecData(Set.of(annotation.formats())));
    }

    @Override @NotNull
    public Optional<ColorSpecData> resolveClassAttachment(@NotNull Class<?> clazz, @NotNull ColorSpec annotation) {
        return Optional.of(new ColorSpecData(Set.of(annotation.formats())));
    }
}
