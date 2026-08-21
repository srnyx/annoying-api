package xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe;

import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.file.okaeri.CommonAnnotationResolver;

import java.util.Optional;
import java.util.Set;


public class RecipeAttachmentResolver implements CommonAnnotationResolver<RecipeSpec, RecipeSpecData> {
    @Override @NotNull
    public Class<RecipeSpec> getAnnotationType() {
        return RecipeSpec.class;
    }

    @Override @NotNull
    public Optional<RecipeSpecData> resolveAttachment(@NotNull RecipeSpec annotation) {
        return Optional.of(new RecipeSpecData(annotation.name(), annotation.resultTransformer(), Set.of(annotation.disabledFeatures())));
    }
}
