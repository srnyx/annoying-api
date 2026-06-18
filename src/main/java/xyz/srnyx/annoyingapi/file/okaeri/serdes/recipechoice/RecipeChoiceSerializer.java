package xyz.srnyx.annoyingapi.file.okaeri.serdes.recipechoice;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefRecipeChoice.RECIPE_CHOICE_CLASS;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefRecipeChoice.RefExactChoice.EXACT_CHOICE_CONSTRUCTOR;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefRecipeChoice.RefExactChoice.EXACT_CHOICE_GET_CHOICES_METHOD;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefRecipeChoice.RefMaterialChoice.MATERIAL_CHOICE_CONSTRUCTOR;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefRecipeChoice.RefMaterialChoice.MATERIAL_CHOICE_GET_CHOICES_METHOD;


public class RecipeChoiceSerializer implements ObjectSerializer<Object> {
    @Override
    public boolean supports(@NotNull Class<?> type) {
        return RECIPE_CHOICE_CLASS != null && RECIPE_CHOICE_CLASS.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NotNull Object object, @NotNull SerializationData data, @NotNull GenericsDeclaration generics) {
        // type
        final RecipeChoiceType type = RecipeChoiceType.getByClass(object.getClass());
        if (type == null) return;
        data.set("type", type);

        if (type == RecipeChoiceType.EXACT) {
            // choices
            if (EXACT_CHOICE_GET_CHOICES_METHOD != null) try {
                data.set("choices", EXACT_CHOICE_GET_CHOICES_METHOD.invoke(object));
            } catch (final Exception e) {
                e.printStackTrace();
            }
        } else if (type == RecipeChoiceType.MATERIAL) {
            // choices
            if (MATERIAL_CHOICE_GET_CHOICES_METHOD != null) try {
                data.set("choices", MATERIAL_CHOICE_GET_CHOICES_METHOD.invoke(object));
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override @NotNull
    public Object deserialize(@NotNull DeserializationData data, @NotNull GenericsDeclaration generics) {
        // Get type
        final RecipeChoiceType type = data.get("type", RecipeChoiceType.class);
        if (type == null) throw new IllegalArgumentException("Missing required field: type");

        if (type == RecipeChoiceType.EXACT) {
            if (EXACT_CHOICE_CONSTRUCTOR == null) throw new IllegalStateException("ExactChoice constructor not found");

            // choices
            final List<ItemStack> choices = data.getAsList("choices", ItemStack.class);

            try {
                return EXACT_CHOICE_CONSTRUCTOR.newInstance(choices);
            } catch (final Exception e) {
                throw new RuntimeException("Failed to deserialize ExactChoice", e);
            }
        } else if (type == RecipeChoiceType.MATERIAL) {
            if (MATERIAL_CHOICE_CONSTRUCTOR == null) throw new IllegalStateException("MaterialChoice constructor not found");

            // choices
            final List<Material> choices = data.getAsList("choices", Material.class);

            try {
                return MATERIAL_CHOICE_CONSTRUCTOR.newInstance(choices);
            } catch (final Exception e) {
                throw new RuntimeException("Failed to deserialize MaterialChoice", e);
            }
        }

        // Unknown type
        throw new IllegalArgumentException("Unknown RecipeChoice type: " + type + " (valid types: " + Arrays.toString(RecipeChoiceType.values()) + ")");
    }
}
