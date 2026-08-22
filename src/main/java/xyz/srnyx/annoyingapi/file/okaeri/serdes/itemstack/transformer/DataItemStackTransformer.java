package xyz.srnyx.annoyingapi.file.okaeri.serdes.itemstack.transformer;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.data.ItemData;
import xyz.srnyx.annoyingapi.file.okaeri.AnnoyingConfig;


/**
 * @param   <R> Must match the ROOT config type
 */
public abstract class DataItemStackTransformer<R extends AnnoyingConfig> implements ItemStackTransformer<R> {
    public abstract void transform(@NotNull ItemData data, @NotNull Context<R> context);

    @Override @NotNull
    public final ItemStack apply(@NotNull ItemStack itemStack, @NotNull Context<R> context) {
        final ItemData data = new ItemData(context.serializer().plugin, itemStack);
        transform(data, context);
        return data.target;
    }
}
