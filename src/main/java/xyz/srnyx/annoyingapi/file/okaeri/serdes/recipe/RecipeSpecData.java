package xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe;

import eu.okaeri.configs.serdes.SerdesContextAttachment;
import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe.transformer.result.ResultTransformer;


public record RecipeSpecData(@NotNull String name, @NotNull Class<? extends ResultTransformer> resultTransformer) implements SerdesContextAttachment {}
