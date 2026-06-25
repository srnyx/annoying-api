package xyz.srnyx.annoyingapi.file.okaeri.serdes.color;

import eu.okaeri.configs.serdes.SerdesContextAttachment;
import org.jetbrains.annotations.NotNull;

import java.util.Set;


public record ColorSpecData(@NotNull Set<ColorFormat> formats) implements SerdesContextAttachment {}
