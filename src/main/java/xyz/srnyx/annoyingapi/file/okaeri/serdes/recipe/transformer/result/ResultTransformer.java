package xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe.transformer.result;

import org.bukkit.inventory.ItemStack;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe.RecipeSerializer;

import java.util.function.BiFunction;


public interface ResultTransformer extends BiFunction<RecipeSerializer, ItemStack, ItemStack> {}
