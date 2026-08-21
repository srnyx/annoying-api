package xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe.transformer.result;

import eu.okaeri.configs.serdes.SerdesContext;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.file.okaeri.AnnoyingConfig;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe.RecipeFeature;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe.RecipeSerializer;


/**
 * @param   <R> Must match the ROOT config type
 */
@FunctionalInterface
public interface ResultTransformer<R extends AnnoyingConfig> {
    /**
     * @param   item    Only null if {@link RecipeFeature#RESULT} is disabled
     */
    @NotNull
    ItemStack apply(@Nullable ItemStack item, @NotNull Context<R> context);

    record Context<C extends AnnoyingConfig>(@NotNull RecipeSerializer serializer, @NotNull SerdesContext serdesContext, @NotNull C config) {}
}
