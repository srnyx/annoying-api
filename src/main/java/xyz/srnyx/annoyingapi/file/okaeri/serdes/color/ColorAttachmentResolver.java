package xyz.srnyx.annoyingapi.file.okaeri.serdes.color;

import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.file.okaeri.CommonAnnotationResolver;

import java.util.Optional;
import java.util.Set;


public class ColorAttachmentResolver implements CommonAnnotationResolver<ColorSpec, ColorSpecData> {
    @Override @NotNull
    public Class<ColorSpec> getAnnotationType() {
        return ColorSpec.class;
    }

    @Override @NotNull
    public Optional<ColorSpecData> resolveAttachment(@NotNull ColorSpec annotation) {
        return Optional.of(new ColorSpecData(Set.of(annotation.formats())));
    }
}
