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
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.NAMESPACED_KEY_CONSTRUCTOR;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefCookingRecipe.COOKING_RECIPE_CLASS;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefCookingRecipe.COOKING_RECIPE_CONSTRUCTOR;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefCookingRecipe.COOKING_RECIPE_GET_COOKING_TIME_METHOD;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefCookingRecipe.COOKING_RECIPE_GET_EXPERIENCE_METHOD;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefCookingRecipe.COOKING_RECIPE_GET_INPUT;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefFurnaceRecipe.FURNACE_RECIPE_CONSTRUCTOR_1_13;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefFurnaceRecipe.FURNACE_RECIPE_CONSTRUCTOR_1_9;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefFurnaceRecipe.FURNACE_RECIPE_GET_EXPERIENCE_METHOD;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefMerchantRecipe.MERCHANT_RECIPE_CLASS;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefMerchantRecipe.MERCHANT_RECIPE_CONSTRUCTOR;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefMerchantRecipe.MERCHANT_RECIPE_CONSTRUCTOR_1_14;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefMerchantRecipe.MERCHANT_RECIPE_CONSTRUCTOR_1_18_1;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefRecipeChoice.RECIPE_CHOICE_CLASS;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefShapedRecipe.SHAPED_RECIPE_CONSTRUCTOR;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefShapelessRecipe.SHAPELESS_RECIPE_CONSTRUCTOR;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefSmithingRecipe.SMITHING_RECIPE_CLASS;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefSmithingRecipe.SMITHING_RECIPE_CONSTRUCTOR;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.StonecuttingRecipe.STONECUTTING_RECIPE_CLASS;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.StonecuttingRecipe.STONECUTTING_RECIPE_CONSTRUCTOR;


public class RecipeSerializer implements ObjectSerializer<Recipe> {
    @Nullable private final Plugin plugin;

    public RecipeSerializer(@Nullable Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean supports(@NotNull Class<?> type) {
        return Recipe.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NotNull Recipe object, @NotNull SerializationData data, @NotNull GenericsDeclaration generics) {
        // result
        data.set("result", object.getResult());

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

            final Map<Character, Material> ingredientMap = new HashMap<>();

            // shape TODO: this may cause characters to change every load
            final StringBuilder[] rows = {new StringBuilder(), new StringBuilder(), new StringBuilder()};
            final List<ItemStack> ingredientList = shapeless.getIngredientList();
            for (int i = 0; i < ingredientList.size(); i++) {
                Character key = null;
                final Material material = ingredientList.get(i).getType();

                // Get key from ingredient name
                final String ingredient = material.name().toUpperCase();
                for (int j = 0; j < ingredient.length(); j++) {
                    key = ingredient.charAt(j);
                    if (ingredientMap.containsKey(key)) continue;
                    ingredientMap.put(key, material);
                    break;
                }

                // All characters in ingredient name are taken
                if (key == null) {
                    // Find first unused letter
                    for (char c = 'A'; c <= 'Z'; c++) {
                        if (ingredientMap.containsKey(c)) continue;
                        key = c;
                        ingredientMap.put(key, material);
                        break;
                    }

                    // All letters are taken
                    if (key == null) throw new IllegalStateException("Too many ingredients in shapeless recipe, cannot assign character key");
                }

                // Add to shape row
                rows[i / 3].append(key);
            }
            data.set("shape", List.of(rows[0].toString(), rows[1].toString(), rows[2].toString()));

            // ingredients
            data.set("ingredients", ingredientMap);

            return;
        }

        // ShapedRecipe
        if (object instanceof ShapedRecipe shaped) {
            // shapeless
            data.set("shapeless", false);

            // shape
            data.set("shape", shaped.getShape());

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
        // name
        final String name = data.getContext().getAttachment(RecipeSpecData.class)
                .map(RecipeSpecData::name)
                .orElse(null);
        if (name == null) throw new IllegalStateException("DEVELOPER: Recipe name is required with @RecipeSpec");

        // result TODO: function
        final ItemStack result = data.get("result", ItemStack.class);
        if (result == null) throw new IllegalArgumentException("Missing required field: result");

        final Class<?> type = generics.getType();

        // CookingRecipe or FurnaceRecipe
        final boolean cooking = COOKING_RECIPE_CLASS != null && COOKING_RECIPE_CONSTRUCTOR != null && NAMESPACED_KEY_CONSTRUCTOR != null && COOKING_RECIPE_CLASS.isAssignableFrom(type);
        final boolean furnace = FurnaceRecipe.class.isAssignableFrom(type);
        if (cooking || furnace) {
            // ingredient
            final Material ingredient = data.get("ingredient", Material.class);
            if (ingredient == null) throw new IllegalArgumentException("Missing required field: ingredient");

            // experience
            final float experience = data.get("experience", float.class);

            // cooking_time
            final Duration cookingTime = data.getOr("cooking_time", Duration.class, Duration.ofSeconds(10));
            final int cookingTimeTicks = (int) (cookingTime.toMillis() / 50);

            // 1.14+ CookingRecipe
            if (cooking) try {
                return (Recipe) COOKING_RECIPE_CONSTRUCTOR.newInstance(NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, name), result, ingredient, experience, cookingTimeTicks);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException ignored) {}

            // 1.13+ FurnaceRecipe
            if (FURNACE_RECIPE_CONSTRUCTOR_1_13 != null && NAMESPACED_KEY_CONSTRUCTOR != null) try {
                return FURNACE_RECIPE_CONSTRUCTOR_1_13.newInstance(NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, name), result, ingredient, experience, cookingTimeTicks);
            } catch (final InstantiationException | IllegalAccessException | InvocationTargetException ignored) {}

            // 1.9+ FurnaceRecipe
            if (FURNACE_RECIPE_CONSTRUCTOR_1_9 != null) try {
                return FURNACE_RECIPE_CONSTRUCTOR_1_9.newInstance(result, new MaterialData(ingredient), experience);
            } catch (final InstantiationException | IllegalAccessException | InvocationTargetException ignored) {}

            // 1.8.8- FurnaceRecipe
            return new FurnaceRecipe(result, ingredient);
        }

        // 1.9+ MerchantRecipe TODO: Paper-specific ignoreDiscounts
        if (MERCHANT_RECIPE_CLASS != null && MERCHANT_RECIPE_CLASS.isAssignableFrom(type)) {
            // max_uses
            final Integer maxUses = data.get("max_uses", Integer.class);
            if (maxUses == null) throw new IllegalArgumentException("Missing required field: max_uses");

            // uses
            final int uses = data.getOr("uses", int.class, 0);

            // experience_reward
            final boolean experienceReward = data.getOr("experience_reward", boolean.class, false);

            // 1.14+
            if (MERCHANT_RECIPE_CONSTRUCTOR_1_14 != null) {
                // villager_experience
                final int villagerExperience = data.getOr("villager_experience", int.class, 0);

                // price_multiplier
                final float priceMultiplier = data.getOr("price_multiplier", float.class, 0.0f);

                // 1.18.1+
                if (MERCHANT_RECIPE_CONSTRUCTOR_1_18_1 != null) {
                    // demand
                    final int demand = data.getOr("demand", int.class, 0);

                    // special_price
                    final int specialPrice = data.getOr("special_price", int.class, 0);

                    try {
                        return (Recipe) MERCHANT_RECIPE_CONSTRUCTOR_1_18_1.newInstance(result, uses, maxUses, experienceReward, villagerExperience, priceMultiplier, demand, specialPrice);
                    } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    return (Recipe) MERCHANT_RECIPE_CONSTRUCTOR_1_14.newInstance(result, uses, maxUses, experienceReward, villagerExperience, priceMultiplier);
                } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

            // 1.9+
            if (MERCHANT_RECIPE_CONSTRUCTOR != null) try {
                return (Recipe) MERCHANT_RECIPE_CONSTRUCTOR.newInstance(result, uses, maxUses, experienceReward);
            } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

            // 1.8.8- (shouldn't happen)
            throw new IllegalStateException("Could not find a valid MerchantRecipe constructor");
        }

        // 1.14+ StonecuttingRecipe
        if (STONECUTTING_RECIPE_CLASS != null && STONECUTTING_RECIPE_CLASS.isAssignableFrom(type)) {
            if (STONECUTTING_RECIPE_CONSTRUCTOR == null || NAMESPACED_KEY_CONSTRUCTOR == null) throw new IllegalStateException("StonecuttingRecipe constructor not found");

            // ingredient
            final Material ingredient = data.get("ingredient", Material.class);
            if (ingredient == null) throw new IllegalArgumentException("Missing required field: ingredient");

            try {
                return (Recipe) STONECUTTING_RECIPE_CONSTRUCTOR.newInstance(NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, name), result, ingredient);
            } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Could not create StonecuttingRecipe", e);
            }
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

            try {
                return (Recipe) SMITHING_RECIPE_CONSTRUCTOR.newInstance(NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, name), result, base, addition);
            } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Could not create SmithingRecipe", e);
            }
        }

        // shape
        final List<String> shape = data.getAsList("shape", String.class);
        if (shape == null || shape.isEmpty()) throw new IllegalArgumentException("Missing required field: shape");

        // ingredients
        final Map<Character, Material> ingredients = data.getAsMap("ingredients", Character.class, Material.class);
        if (ingredients == null || ingredients.isEmpty()) throw new IllegalArgumentException("Missing required field: ingredients");

        // shapeless
        if (data.get("shapeless", boolean.class)) {
            // Create shapeless recipe
            ShapelessRecipe shapeless;
            if (plugin != null && SHAPELESS_RECIPE_CONSTRUCTOR != null && NAMESPACED_KEY_CONSTRUCTOR != null) {
                try {
                    // 1.12+
                    shapeless = SHAPELESS_RECIPE_CONSTRUCTOR.newInstance(NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, name), result);
                } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    // 1.11-
                    shapeless = new ShapelessRecipe(result);
                }
            } else {
                // 1.11-
                shapeless = new ShapelessRecipe(result);
            }

            // Set ingredients
            for (final Map.Entry<Character, Material> entry : ingredients.entrySet()) {
                final String key = entry.getKey().toString();
                shapeless.addIngredient(
                        shape.stream()
                                .mapToInt(s -> s.length() - s.replace(key, "").length())
                                .sum(),
                        entry.getValue());
            }

            return shapeless;
        }

        // shaped
        ShapedRecipe shaped;
        if (plugin != null && SHAPED_RECIPE_CONSTRUCTOR != null && NAMESPACED_KEY_CONSTRUCTOR != null) {
            try {
                // 1.12+
                shaped = SHAPED_RECIPE_CONSTRUCTOR.newInstance(NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, name), result);
            } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
                // 1.11-
                shaped = new ShapedRecipe(result);
            }
        } else {
            // 1.11-
            shaped = new ShapedRecipe(result);
        }

        // Set shape and ingredients
        shaped.shape(shape.stream()
                .map(string -> string.replace("-", " "))
                .toArray(String[]::new));
        ingredients.forEach(shaped::setIngredient);

        return shaped;
    }
}
