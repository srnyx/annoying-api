package xyz.srnyx.annoyingapi.file.okaeri.serdes.itemstack.transformer;

import eu.okaeri.configs.serdes.SerdesContext;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.file.okaeri.AnnoyingConfig;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.itemstack.ItemStackSerializer;


/**
 * @param   <R> Must match the ROOT config type
 */
@FunctionalInterface
public interface ItemStackTransformer<R extends AnnoyingConfig> {
    @NotNull
    ItemStack apply(@NotNull ItemStack item, @NotNull Context<R> context);

    record Context<C extends AnnoyingConfig>(@NotNull ItemStackSerializer serializer, @NotNull SerdesContext serdesContext, @NotNull C rootConfig) {}
}
