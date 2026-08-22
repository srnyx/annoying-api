package xyz.srnyx.annoyingapi.file.okaeri.serdes.itemstack.spec;

import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.file.okaeri.CommonAnnotationResolver;

import java.util.Optional;


public class ItemStackAttachmentResolver implements CommonAnnotationResolver<ItemStackSpec, ItemStackSpecData> {
    @Override @NotNull
    public Class<ItemStackSpec> getAnnotationType() {
        return ItemStackSpec.class;
    }

    @Override @NotNull
    public Optional<ItemStackSpecData> resolveAttachment(@NotNull ItemStackSpec annotation) {
        return Optional.of(new ItemStackSpecData(annotation.transformer()));
    }
}
