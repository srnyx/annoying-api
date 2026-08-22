package xyz.srnyx.annoyingapi.file.okaeri.serdes.itemstack.spec;

import eu.okaeri.configs.serdes.SerdesContextAttachment;
import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.itemstack.transformer.ItemStackTransformer;


public record ItemStackSpecData(@NotNull Class<? extends ItemStackTransformer> transformer) implements SerdesContextAttachment {}
