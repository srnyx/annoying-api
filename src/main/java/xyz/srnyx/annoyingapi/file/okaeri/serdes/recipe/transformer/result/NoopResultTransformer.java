package xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe.transformer.result;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.file.okaeri.AnnoyingConfig;


/**
 * @param   <R> Must match the ROOT config type
 */
public class NoopResultTransformer<R extends AnnoyingConfig> implements ResultTransformer<R> {
    @Override @NotNull
    public ItemStack apply(@Nullable ItemStack itemStack, @NotNull Context<R> context) {
        if (itemStack == null) throw new IllegalArgumentException("result cannot be disabled if using a NoopResultTransformer");
        return itemStack;
    }
}
