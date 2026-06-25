package xyz.srnyx.annoyingapi.file.okaeri.serdes.recipechoice;

import org.jetbrains.annotations.Nullable;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefRecipeChoice.RefExactChoice.EXACT_CHOICE_CLASS;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefRecipeChoice.RefMaterialChoice.MATERIAL_CHOICE_CLASS;


public enum RecipeChoiceType {
    EXACT(EXACT_CHOICE_CLASS),
    MATERIAL(MATERIAL_CHOICE_CLASS);

    @Nullable public final Class<?> clazz;

    RecipeChoiceType(@Nullable Class<?> clazz) {
        this.clazz = clazz;
    }

    @Nullable
    public static RecipeChoiceType getByClass(@Nullable Class<?> clazz) {
        if (clazz == null) return null;
        for (final RecipeChoiceType type : values()) if (type.clazz != null && type.clazz.equals(clazz)) return type;
        return null;
    }
}
