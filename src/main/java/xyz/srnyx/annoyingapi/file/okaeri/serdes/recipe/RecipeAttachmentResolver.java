package xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe;

import eu.okaeri.configs.serdes.SerdesAnnotationResolver;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Optional;


public class RecipeAttachmentResolver implements SerdesAnnotationResolver<RecipeSpec, RecipeSpecData> {
    @Override @NotNull
    public Class<RecipeSpec> getAnnotationType() {
        return RecipeSpec.class;
    }

    @Override @NotNull
    public Optional<RecipeSpecData> resolveAttachment(@NotNull Field field, @NotNull RecipeSpec annotation) {
        return Optional.of(new RecipeSpecData(annotation.name(), annotation.resultTransformer()));
    }

    @Override @NotNull
    public Optional<RecipeSpecData> resolveClassAttachment(@NotNull Class<?> clazz, @NotNull RecipeSpec annotation) {
        return Optional.of(new RecipeSpecData(annotation.name(), annotation.resultTransformer()));
    }
}
