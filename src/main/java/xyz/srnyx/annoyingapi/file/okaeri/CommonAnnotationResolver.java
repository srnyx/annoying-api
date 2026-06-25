package xyz.srnyx.annoyingapi.file.okaeri;

import eu.okaeri.configs.serdes.SerdesAnnotationResolver;
import eu.okaeri.configs.serdes.SerdesContextAttachment;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Optional;


public interface CommonAnnotationResolver<A extends Annotation, D extends SerdesContextAttachment> extends SerdesAnnotationResolver<A, D> {
    Optional<D> resolveAttachment(@NotNull A annotation);

    @Override @NotNull
    default Optional<D> resolveAttachment(@NotNull Field field, @NotNull A annotation) {
        return resolveAttachment(annotation);
    }

    @Override @NotNull
    default Optional<D> resolveClassAttachment(@NotNull Class<?> clazz, @NotNull A annotation) {
        return resolveAttachment(annotation);
    }
}
