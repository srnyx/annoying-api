package xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe.transformer.result;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.data.ItemData;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe.RecipeSerializer;


public abstract class DataResultTransformer implements ResultTransformer {
    public abstract void transform(@NotNull RecipeSerializer serializer, @NotNull ItemData data);

    @Override @NotNull
    public final ItemStack apply(@Nullable ItemStack itemStack, @NotNull Context context) {
        if (itemStack == null) throw new IllegalArgumentException("result cannot be disabled if using a DataResultTransformer");
        final ItemData data = new ItemData(context.serializer().plugin, itemStack);
        transform(context.serializer(), data);
        return data.target;
    }
}
