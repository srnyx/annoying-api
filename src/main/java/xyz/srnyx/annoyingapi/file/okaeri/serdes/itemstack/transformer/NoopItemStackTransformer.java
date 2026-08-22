package xyz.srnyx.annoyingapi.file.okaeri.serdes.itemstack.transformer;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.file.okaeri.AnnoyingConfig;


/**
 * @param   <R> Must match the ROOT config type
 */
public class NoopItemStackTransformer<R extends AnnoyingConfig> implements ItemStackTransformer<R> {
    @Override @NotNull
    public ItemStack apply(@NotNull ItemStack itemStack, @NotNull Context<R> context) {
        return itemStack;
    }
}
