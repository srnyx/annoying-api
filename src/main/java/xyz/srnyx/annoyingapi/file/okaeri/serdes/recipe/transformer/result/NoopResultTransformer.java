package xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe.transformer.result;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe.RecipeSerializer;


public class NoopResultTransformer implements ResultTransformer {
    @Override @NotNull
    public ItemStack apply(@NotNull RecipeSerializer serializer, @NotNull ItemStack itemStack) {
        return itemStack;
    }
}
