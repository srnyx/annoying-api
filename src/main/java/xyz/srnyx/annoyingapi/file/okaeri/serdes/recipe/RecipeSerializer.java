package xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe.transformer.result.ResultTransformer;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.NAMESPACED_KEY_CONSTRUCTOR;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefCookingRecipe.*;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefFurnaceRecipe.FURNACE_RECIPE_GET_EXPERIENCE_METHOD;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefFurnaceRecipe.newFurnaceRecipe;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefMerchantRecipe.*;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefRecipeChoice.RECIPE_CHOICE_CLASS;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefShapedRecipe.newShapedRecipe;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefShapelessRecipe.newShapelessRecipe;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefSmithingRecipe.SMITHING_RECIPE_CLASS;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefSmithingRecipe.SMITHING_RECIPE_CONSTRUCTOR;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefSmithingRecipe.newSmithingRecipe;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefStonecuttingRecipe.STONECUTTING_RECIPE_CLASS;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefStonecuttingRecipe.newStonecuttingRecipe;


public class RecipeSerializer implements ObjectSerializer<Recipe> {
    @NotNull private static final Map<Class<? extends ResultTransformer>, ResultTransformer> TRANSFORMERS = new HashMap<>();

    @NotNull public final AnnoyingPlugin plugin;
    @NotNull private final Map<Recipe, ItemStack> rawResults = new WeakHashMap<>();

    public RecipeSerializer(@NotNull AnnoyingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean supports(@NotNull Class<?> type) {
        return Recipe.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NotNull Recipe object, @NotNull SerializationData data, @NotNull GenericsDeclaration generics) {
        // result
        data.set("result", rawResults.getOrDefault(object, object.getResult()));

        // 1.14+ CookingRecipe
        if (COOKING_RECIPE_CLASS != null && COOKING_RECIPE_CLASS.isAssignableFrom(object.getClass())) {
            // ingredient
            if (COOKING_RECIPE_GET_INPUT != null) try {
                data.set("ingredient", ((ItemStack) COOKING_RECIPE_GET_INPUT.invoke(object)).getType());
            } catch (final IllegalAccessException | InvocationTargetException ignored) {}

            // experience
            if (COOKING_RECIPE_GET_EXPERIENCE_METHOD != null) try {
                data.set("experience", COOKING_RECIPE_GET_EXPERIENCE_METHOD.invoke(object));
            } catch (final IllegalAccessException | InvocationTargetException ignored) {}

            // cooking_time
            if (COOKING_RECIPE_GET_COOKING_TIME_METHOD != null) try {
                data.set("cooking_time", COOKING_RECIPE_GET_COOKING_TIME_METHOD.invoke(object));
            } catch (final IllegalAccessException | InvocationTargetException ignored) {}

            return;
        }

        // FurnaceRecipe
        if (object instanceof FurnaceRecipe furnace) {
            // ingredient
            data.set("ingredient", furnace.getInput().getType());

            // 1.9+ experience
            if (FURNACE_RECIPE_GET_EXPERIENCE_METHOD != null) try {
                data.set("experience", FURNACE_RECIPE_GET_EXPERIENCE_METHOD.invoke(object));
            } catch (final IllegalAccessException | InvocationTargetException ignored) {}

            return;
        }

        // ShapelessRecipe
        if (object instanceof ShapelessRecipe shapeless) {
            // shapeless
            data.set("shapeless", true);

            // Ingredients bimap (BiMap class doesn't exist during runtime)
            final Map<Character, Material> characterToMaterial = new HashMap<>();
            final Map<Material, Character> materialToCharacter = new HashMap<>();

            // shape
            final StringBuilder[] rows = {new StringBuilder(), new StringBuilder(), new StringBuilder()};
            final List<ItemStack> ingredientList = shapeless.getIngredientList();
            for (int i = 0; i < ingredientList.size(); i++) {
                final Material material = ingredientList.get(i).getType();

                // Get existing key
                Character key = materialToCharacter.get(material);
                if (key == null) {
                    // Get key from ingredient name
                    final String ingredient = material.name().toUpperCase();
                    for (int j = 0; j < ingredient.length(); j++) {
                        key = ingredient.charAt(j);
                        if (characterToMaterial.containsKey(key)) continue;
                        characterToMaterial.put(key, material);
                        materialToCharacter.put(material, key);
                        break;
                    }

                    // All characters in ingredient name are taken
                    if (key == null) {
                        // Find first unused letter
                        for (char c = 'A'; c <= 'Z'; c++) {
                            if (characterToMaterial.containsKey(c)) continue;
                            key = c;
                            characterToMaterial.put(key, material);
                            materialToCharacter.put(material, key);
                            break;
                        }

                        // All letters are taken (shouldn't happen: can't have more than 9 ingredients)
                        if (key == null) throw new IllegalStateException("Too many ingredients in shapeless recipe, cannot assign character key");
                    }
                }

                // Add to shape row
                rows[i / 3].append(key);
            }
            data.setCollection("shape", List.of(rows[0].toString(), rows[1].toString(), rows[2].toString()), String.class);

            // ingredients
            data.set("ingredients", characterToMaterial);

            return;
        }

        // ShapedRecipe
        if (object instanceof ShapedRecipe shaped) {
            // shapeless
            data.set("shapeless", false);

            // shape
            data.setArray("shape", shaped.getShape(), String.class);

            // ingredients
            final Map<Character, Material> ingredientMap = new HashMap<>();
            for (final Map.Entry<Character, ItemStack> entry : shaped.getIngredientMap().entrySet()) {
                ingredientMap.put(entry.getKey(), entry.getValue().getType());
            }
            data.set("ingredients", ingredientMap);

            return;
        }

        // Unknown
        throw new IllegalArgumentException("Unsupported recipe type: " + object.getClass().getName());
    }

    @Override @NotNull
    public Recipe deserialize(@NotNull DeserializationData data, @NotNull GenericsDeclaration generics) {
        // Get spec data
        final RecipeSpecData specData = data.getContext()
                .getAttachment(RecipeSpecData.class)
                .orElseThrow(() -> new IllegalStateException("DEVELOPER: Recipe name is required with @RecipeSpec"));

        // result
        final ItemStack rawResult = data.get("result", ItemStack.class);
        if (rawResult == null) throw new IllegalArgumentException("Missing required field: result");
        ItemStack result = rawResult.clone();

        // name
        final String name = specData.name();

        // Transform result
        final Class<? extends ResultTransformer> transformerClass = specData.resultTransformer();
        ResultTransformer transformer = TRANSFORMERS.get(transformerClass);
        if (transformer == null) try {
            transformer = transformerClass.getDeclaredConstructor().newInstance();
            TRANSFORMERS.put(transformerClass, transformer);
        } catch (final Exception e) {
            plugin.logErrorTrack(Level.SEVERE, "DEVELOPER: Failed to construct transformer " + transformerClass.getName() + " for recipe " + name, e);
        }
        if (transformer != null) result = transformer.apply(this, result);

        final Class<?> type = generics.getType();

        // CookingRecipe or FurnaceRecipe
        final boolean cooking = COOKING_RECIPE_CLASS != null && COOKING_RECIPE_CLASS.isAssignableFrom(type);
        final boolean furnace = FurnaceRecipe.class.isAssignableFrom(type);
        if (cooking || furnace) {
            // ingredient
            final Material ingredient = data.get("ingredient", Material.class);
            if (ingredient == null) throw new IllegalArgumentException("Missing required field: ingredient");

            // experience
            final Float experience = data.get("experience", Float.class);

            // cooking_time
            final Duration cookingTime = data.getOr("cooking_time", Duration.class, Duration.ofSeconds(10));
            final int cookingTimeTicks = (int) (cookingTime.toMillis() / 50);

            // 1.14+ CookingRecipe
            if (cooking) {
                final Recipe recipe = newCookingRecipe(plugin, name, result, ingredient, experience, cookingTimeTicks);
                if (recipe != null) return storeRawResult(recipe, result);
            }

            // FurnaceRecipe
            return storeRawResult(newFurnaceRecipe(result, ingredient, experience, plugin, name, cookingTimeTicks), rawResult);
        }

        // 1.9+ MerchantRecipe
        if (MERCHANT_RECIPE_CLASS != null && MERCHANT_RECIPE_CLASS.isAssignableFrom(type)) {
            // max_uses
            final Integer maxUses = data.get("max_uses", Integer.class);
            if (maxUses == null) throw new IllegalArgumentException("Missing required field: max_uses");

            // uses
            final int uses = data.getOr("uses", Integer.class, 0);

            // experience_reward
            final boolean experienceReward = data.getOr("experience_reward", Boolean.class, false);
            
            // villager_experience
            final int villagerExperience = data.getOr("villager_experience", Integer.class, 0);

            // price_multiplier
            final float priceMultiplier = data.getOr("price_multiplier", Float.class, 0.0f);
            
            // demand
            final int demand = data.getOr("demand", Integer.class, 0);

            // special_price
            final int specialPrice = data.getOr("special_price", Integer.class, 0);
            
            // ignore_discounts (PAPER)
            final boolean ignoreDiscounts = data.getOr("ignore_discounts", Boolean.class, false);

            // Create MerchantRecipe
            final Recipe recipe = newMerchantRecipe(result, maxUses, uses, experienceReward, villagerExperience, priceMultiplier, demand, specialPrice, ignoreDiscounts);
            if (recipe == null) throw new IllegalStateException("Could not find a valid MerchantRecipe constructor");
            return storeRawResult(recipe, rawResult);
        }

        // 1.14+ StonecuttingRecipe
        if (STONECUTTING_RECIPE_CLASS != null && STONECUTTING_RECIPE_CLASS.isAssignableFrom(type)) {
            // ingredient
            final Material ingredient = data.get("ingredient", Material.class);
            if (ingredient == null) throw new IllegalArgumentException("Missing required field: ingredient");

            final Recipe recipe = newStonecuttingRecipe(plugin, name, result, ingredient);
            if (recipe == null) throw new IllegalStateException("Could not find a valid StonecuttingRecipe constructor");
            return storeRawResult(recipe, rawResult);
        }

        // 1.16.1+ SmithingRecipe
        if (SMITHING_RECIPE_CLASS != null && SMITHING_RECIPE_CLASS.isAssignableFrom(type)) {
            if (SMITHING_RECIPE_CONSTRUCTOR == null || RECIPE_CHOICE_CLASS == null || NAMESPACED_KEY_CONSTRUCTOR == null) throw new IllegalStateException("SmithingRecipe constructor not found");

            // base
            final Object base = data.get("base", RECIPE_CHOICE_CLASS);
            if (base == null) throw new IllegalArgumentException("Missing required field: base");

            // addition
            final Object addition = data.get("addition", RECIPE_CHOICE_CLASS);
            if (addition == null) throw new IllegalArgumentException("Missing required field: addition");

            final Recipe recipe = newSmithingRecipe(plugin, name, result, base, addition);
            if (recipe == null) throw new IllegalStateException("Could not find a valid SmithingRecipe constructor");
            return storeRawResult(recipe, rawResult);
        }

        // shape
        final List<String> shape = data.getAsList("shape", String.class);
        if (shape == null || shape.isEmpty()) throw new IllegalArgumentException("Missing required field: shape");

        // ingredients
        final Map<Character, Material> ingredients = data.getAsMap("ingredients", Character.class, Material.class);
        if (ingredients == null || ingredients.isEmpty()) throw new IllegalArgumentException("Missing required field: ingredients");

        // shapeless
        if (data.getOr("shapeless", Boolean.class, false)) {
            // Create shapeless recipe
            final ShapelessRecipe shapeless = newShapelessRecipe(result, plugin, name);

            // Set ingredients
            for (final Map.Entry<Character, Material> entry : ingredients.entrySet()) {
                final String key = entry.getKey().toString();
                shapeless.addIngredient(
                        shape.stream()
                                .mapToInt(s -> s.length() - s.replace(key, "").length())
                                .sum(),
                        entry.getValue());
            }

            return storeRawResult(shapeless, rawResult);
        }

        // shaped
        final ShapedRecipe shaped = newShapedRecipe(result, plugin, name);

        // Set shape and ingredients
        shaped.shape(shape.stream()
                .map(string -> string.replace("-", " "))
                .toArray(String[]::new));
        ingredients.forEach(shaped::setIngredient);

        return storeRawResult(shaped, rawResult);
    }

    @NotNull
    private Recipe storeRawResult(@NotNull Recipe recipe, @NotNull ItemStack rawResult) {
        rawResults.put(recipe, rawResult);
        return recipe;
    }
}
