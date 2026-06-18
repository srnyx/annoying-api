package xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe;

import eu.okaeri.configs.serdes.SerdesContextAttachment;
import org.jetbrains.annotations.NotNull;


public record RecipeSpecData(@NotNull String name) implements SerdesContextAttachment {}
