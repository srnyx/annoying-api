package xyz.srnyx.annoyingapi.file.okaeri.serdes.color;

import eu.okaeri.configs.serdes.SerdesContextAttachment;
import org.jetbrains.annotations.NotNull;


public record ColorSpecData(@NotNull ColorFormat format) implements SerdesContextAttachment {}
