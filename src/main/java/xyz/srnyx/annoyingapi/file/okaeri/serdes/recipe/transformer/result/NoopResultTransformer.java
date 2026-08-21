package xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe.transformer.result;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class NoopResultTransformer implements ResultTransformer {
    @Override @NotNull
    public ItemStack apply(@Nullable ItemStack itemStack, @NotNull Context context) {
        if (itemStack == null) throw new IllegalArgumentException("result cannot be disabled if using a NoopResultTransformer");
        return itemStack;
    }
}
