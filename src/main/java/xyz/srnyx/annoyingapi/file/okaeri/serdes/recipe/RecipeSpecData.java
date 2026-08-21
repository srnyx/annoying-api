package xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe;

import eu.okaeri.configs.serdes.SerdesContextAttachment;
import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe.transformer.result.ResultTransformer;

import java.util.Set;


public record RecipeSpecData(
        @NotNull String name,
        @NotNull Class<? extends ResultTransformer> resultTransformer,
        @NotNull Set<RecipeFeature> disabledFeatures
) implements SerdesContextAttachment {}
