package xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe.transformer.result;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.data.ItemData;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe.RecipeSerializer;


public abstract class DataResultTransformer implements ResultTransformer {
    public abstract void transform(@NotNull RecipeSerializer serializer, @NotNull ItemData data);

    @Override @NotNull
    public final ItemStack apply(@NotNull RecipeSerializer serializer, @NotNull ItemStack itemStack) {
        final ItemData data = new ItemData(serializer.plugin, itemStack);
        transform(serializer, data);
        return data.target;
    }
}
