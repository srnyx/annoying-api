package xyz.srnyx.annoyingapi.file;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.parents.Stringable;
import xyz.srnyx.annoyingapi.reflection.org.bukkit.RefSoundCategory;
import xyz.srnyx.annoyingapi.utility.BukkitUtility;
import xyz.srnyx.annoyingapi.data.ItemData;
import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.NAMESPACED_KEY_CONSTRUCTOR;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.attribute.RefAttribute.ATTRIBUTE_ENUM;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.attribute.RefAttributeModifier.*;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.attribute.RefAttributeModifier.RefOperation.ATTRIBUTE_MODIFIER_OPERATION_ENUM;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefShapedRecipe.SHAPED_RECIPE_CONSTRUCTOR;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefShapelessRecipe.SHAPELESS_RECIPE_CONSTRUCTOR;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.meta.RefDamageable.*;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.meta.RefItemMeta.*;


/**
 * Represents a file in the plugin's folder
 */
public abstract class AnnoyingFile extends YamlConfiguration {
    /**
     * The {@link AnnoyingPlugin} instance
     */
    @NotNull public final AnnoyingPlugin plugin;
    /**
     * The path to the file
     */
    @NotNull public final String path;
    /**
     * The file constructed using the {@link #path}
     */
    @NotNull public final File file;
    /**
     * The {@link Options} for the file
     */
    @NotNull protected final AnnoyingFile.Options<?> fileOptions;

    /**
     * Constructs a new {@link AnnoyingFile}
     *
     * @param   plugin          {@link #plugin}
     * @param   path            {@link #path}
     * @param   fileOptions     {@link #fileOptions}
     */
    protected AnnoyingFile(@NotNull AnnoyingPlugin plugin, @NotNull String path, @Nullable AnnoyingFile.Options<?> fileOptions) {
        this.plugin = plugin;
        this.path = path;
        this.file = new File(plugin.getDataFolder(), path);
        this.fileOptions = fileOptions == null ? new Options<>() : fileOptions;
        load();
    }

    @Override @NotNull
    public String toString() {
        return Stringable.toString(this);
    }

    /**
     * Constructs a new {@link AnnoyingFile}
     *
     * @param   plugin          {@link #plugin}
     * @param   path            {@link #path}
     */
    protected AnnoyingFile(@NotNull AnnoyingPlugin plugin, @NotNull String path) {
        this(plugin, path, null);
    }

    /**
     * Creates the {@link #file}
     */
    public abstract void create();

    /**
     * Loads the YAML from the path
     */
    public void load() {
        // Create the file if it doesn't exist and can be empty
        final boolean doesntExist = !file.exists();
        if (fileOptions.canBeEmpty && doesntExist) {
            create();
        } else if (doesntExist) {
            return;
        }

        // Load
        try {
            this.load(file);
        } catch (final IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes the {@link #file}
     */
    public void delete() {
        try {
            Files.delete(file.toPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets a value in the YAML and then {@link #save() saves} the file
     *
     * @param   path    the path to the node
     * @param   value   the value to set the node to
     */
    public void setSave(@Nullable String path, @Nullable Object value) {
        set(path, value);
        save();
    }

    /**
     * Saves the YAML to the {@link #file}
     */
    public void save() {
        // Stop process if it's empty when it can't be
        if (!fileOptions.canBeEmpty && getKeys(true).isEmpty()) {
            // Delete file if it exists
            if (file.exists()) delete();
            return;
        }

        // Create file if it can be empty and doesn't exist
        if (fileOptions.canBeEmpty && !file.exists()) create();

        // Save file
        try {
            save(file);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a formatted log message to the console
     * Sends a formatted warning message to the console
     *
     * @param   level   the {@link Level} of the message
     * @param   key     the key of the node that the message is about ({@code null} if it's not about a node)
     * @param   message the message to send
     */
    public void log(@NotNull Level level, @Nullable String key, @NotNull String message) {
        AnnoyingPlugin.log(level, ChatColor.getLastColors(message) + path + (key == null ? "" : ", " + key) + " | " + message);
    }

    /**
     * Gets a {@link Sound} from the path
     *
     * @param   path    the path to the node
     *
     * @return          the {@link Sound} or {@code null} if it doesn't exist
     */
    @Nullable
    public Sound getSound(@NotNull String path) {
        final Object def = getDefault(path);
        return getSound(path, def instanceof Sound ? (Sound) def : null);
    }

    /**
     * Gets a {@link Sound} from the path
     *
     * @param   path    the path to the node
     * @param   def     the default value
     *
     * @return          the {@link Sound} or {@code def} if it doesn't exist
     */
    @Nullable
    public Sound getSound(@NotNull String path, @Nullable Sound def) {
        final String sound = getString(path);
        if (sound == null) return def;
        try {
            return Sound.valueOf(sound.toUpperCase());
        } catch (final IllegalArgumentException e) {
            return def;
        }
    }

    /**
     * Gets a {@link PlayableSound} from the path. See <a href="https://api.srnyx.com/wiki/file-objects">the wiki</a> for more information
     *
     * @param   path    the path to the node
     *
     * @return          the {@link PlayableSound} or {@code null} if it's invalid
     */
    @Nullable
    public PlayableSound getPlayableSound(@NotNull String path) {
        final Object def = getDefault(path);
        return getPlayableSound(path, def instanceof PlayableSound ? (PlayableSound) def : null);
    }

    /**
     * Gets a {@link PlayableSound} from the path. See <a href="https://api.srnyx.com/wiki/file-objects">the wiki</a> for more information
     *
     * @param   path    the path to the node
     * @param   def     the default value
     *
     * @return          the {@link PlayableSound} or {@code def} if it's invalid
     */
    @Nullable
    public PlayableSound getPlayableSound(@NotNull String path, @Nullable PlayableSound def) {
        final Sound sound = getSound(path + ".sound");
        if (sound == null) return def;
        final ConfigurationSection section = getConfigurationSection(path);
        if (section == null) return def;

        // Get category
        Object category = null;
        final String categoryString = section.getString("category");
        if (categoryString != null) try {
            category = ReflectionUtility.getEnumValue(1, 11, 0, RefSoundCategory.SOUND_CATEGORY_ENUM, categoryString.toUpperCase());
        } catch (final IllegalArgumentException e) {
            log(Level.WARNING, path, "&cInvalid sound category: &4" + categoryString);
        }

        // Return SoundData
        return new PlayableSound(sound, category, (float) section.getDouble("volume", 1), (float) section.getDouble("pitch", 1));
    }

    /**
     * {@code 1.9+} Gets an {@code AttributeModifier} from the path. See <a href="https://api.srnyx.com/wiki/file-objects">the wiki</a> for more information
     *
     * @param   path    the path to the node
     *
     * @return          the {@code AttributeModifier} or {@code null} if it's invalid
     *
     * @param   <T>     the {@code AttributeModifier} class
     */
    @Nullable
    public <T> T getAttributeModifier(@NotNull String path) {
        final Object def = getDefault(path);
        return (T) getAttributeModifier(path, ATTRIBUTE_MODIFIER_CLASS != null && ATTRIBUTE_MODIFIER_CLASS.isInstance(def) ? ATTRIBUTE_MODIFIER_CLASS.cast(def) : null);
    }

    /**
     * {@code 1.9+} Gets an {@code AttributeModifier} from the path. See <a href="https://api.srnyx.com/wiki/file-objects">the wiki</a> for more information
     *
     * @param   path    the path to the node
     * @param   def     the default value
     *
     * @param   <T>     the {@code AttributeModifier} class
     *
     * @return          the {@code AttributeModifier} or {@code def} if it's invalid
     */
    @Nullable @SuppressWarnings("unchecked")
    public <T> T getAttributeModifier(@NotNull String path, @Nullable T def) {
        if (ATTRIBUTE_MODIFIER_OPERATION_ENUM == null) return def;

        final ConfigurationSection section = getConfigurationSection(path);
        if (section == null) {
            log(Level.WARNING, path, "&cInvalid attribute modifier");
            return def;
        }

        final String name = section.getString("name");
        final String operationString = section.getString("operation");
        if (name == null || operationString == null) {
            log(Level.WARNING, path, "&cInvalid attribute modifier");
            return def;
        }

        // operation
        final Object operation;
        try {
            operation = Enum.valueOf(ATTRIBUTE_MODIFIER_OPERATION_ENUM, operationString);
        } catch (final IllegalArgumentException e) {
            log(Level.WARNING, path, "&cInvalid attribute modifier operation: &4" + operationString);
            return def;
        }

        // amount
        final double amount = section.getDouble("amount");

        // 1.13.2+
        if (ATTRIBUTE_MODIFIER_CONSTRUCTOR_5 != null) {
            // slot
            EquipmentSlot slot = null;
            final String equipmentSlotString = section.getString("slot");
            if (equipmentSlotString != null) try {
                slot = EquipmentSlot.valueOf(equipmentSlotString);
            } catch (final IllegalArgumentException e) {
                log(Level.WARNING, path, "&cInvalid equipment slot: &4" + equipmentSlotString);
            }

            // Return
            try {
                return (T) ATTRIBUTE_MODIFIER_CONSTRUCTOR_5.newInstance(UUID.randomUUID(), name, amount, operation, slot);
            } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return def;
            }
        }

        // Return
        if (ATTRIBUTE_MODIFIER_CONSTRUCTOR_3 != null) try {
            return (T) ATTRIBUTE_MODIFIER_CONSTRUCTOR_3.newInstance(name, amount, operation);
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return def;
    }

    /**
     * Gets an {@link ItemStack} from the path. See <a href="https://api.srnyx.com/wiki/file-objects">the wiki</a> for more information
     *
     * @param   path    the path to the node
     *
     * @return          the {@link ItemStack} or {@code null} if it's invalid
     */
    @Override @Nullable
    public ItemStack getItemStack(@NotNull String path) {
        final Object def = getDefault(path);
        return getItemStack(path, def instanceof ItemStack ? (ItemStack) def : null);
    }

    /**
     * Gets an {@link ItemStack} from the path. See <a href="https://api.srnyx.com/wiki/file-objects">the wiki</a> for more information
     *
     * @param   path    the path to the node
     * @param   def     the default value
     *
     * @return the {@link ItemStack} or {@code def} if it's invalid
     */
    @Override @Nullable
    public ItemStack getItemStack(@NotNull String path, @Nullable ItemStack def) {
        final ConfigurationSection section = getConfigurationSection(path);
        if (section == null) return def;
        final String materialString = section.getString("material");
        if (materialString == null) return def;
        final Material material = Material.matchMaterial(materialString);
        if (material == null) return def;
        final int amount = section.getInt("amount", 1);
        final int damage = section.getInt("damage", 0);

        // Material, amount, and durability (1.12.2-)
        final ItemStack item = DAMAGEABLE_CLASS != null && DAMAGEABLE_SET_DAMAGE_METHOD != null ? new ItemStack(material, amount) : new ItemStack(material, amount, (short) damage);

        // Durability (1.13+), name, lore, unbreakable, enchantments, flags, attribute modifiers, and custom model data
        final ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Durability (1.13+)
            if (DAMAGEABLE_CLASS != null && DAMAGEABLE_SET_DAMAGE_METHOD != null && DAMAGEABLE_CLASS.isInstance(meta)) try {
                DAMAGEABLE_SET_DAMAGE_METHOD.invoke(meta, damage);
            } catch (final IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

            // Name
            final String name = section.getString("name");
            if (name != null) meta.setDisplayName(BukkitUtility.color(name));

            // Lore
            meta.setLore(BukkitUtility.colorCollection(section.getStringList("lore")));

            // Enchantments
            final ConfigurationSection enchantmentsSection = section.getConfigurationSection("enchantments");
            if (enchantmentsSection != null) for (final String enchantmentKey : enchantmentsSection.getKeys(false)) {
                final Enchantment enchantment = Enchantment.getByName(enchantmentKey);
                if (enchantment == null) {
                    log(Level.WARNING, path, "&cInvalid enchantment: &4" + enchantmentKey);
                    continue;
                }
                meta.addEnchant(enchantment, enchantmentsSection.getInt(enchantmentKey), true);
            }

            // Flags
            section.getStringList("flags").stream()
                    .map(string -> {
                        try {
                            return ItemFlag.valueOf(string.toUpperCase());
                        } catch (final IllegalArgumentException e) {
                            log(Level.WARNING, section.getCurrentPath() + "." + "flags", "&cInvalid item flag: &4" + string);
                            return null;
                        }
                    })
                    .forEach(meta::addItemFlags);

            // 1.11+ (unbreakable)
            if (ITEM_META_SET_UNBREAKABLE != null) try {
                ITEM_META_SET_UNBREAKABLE.invoke(meta, section.getBoolean("unbreakable"));
            } catch (final IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

            // 1.13.2+ (attribute modifiers)
            if (ATTRIBUTE_ENUM != null && ITEM_META_ADD_ATTRIBUTE_MODIFIER != null) {
                final ConfigurationSection attributeModifiersSection = section.getConfigurationSection("attribute-modifiers");
                if (attributeModifiersSection != null) for (final String attributeKey : attributeModifiersSection.getKeys(false)) {
                    final String pathString = attributeModifiersSection.getCurrentPath() + "." + attributeKey;

                    // Get attribute
                    final Object attribute;
                    try {
                        //noinspection unchecked
                        attribute = Enum.valueOf(ATTRIBUTE_ENUM, attributeKey.toUpperCase());
                    } catch (final IllegalArgumentException e) {
                        log(Level.WARNING, pathString, "&cInvalid attribute: &4" + attributeKey);
                        continue;
                    }

                    // Get attribute modifier
                    final Object attributeModifier = getAttributeModifier(pathString);
                    if (attributeModifier == null) continue;

                    // Add attribute modifier
                    try {
                        ITEM_META_ADD_ATTRIBUTE_MODIFIER.invoke(meta, attribute, attributeModifier);
                    } catch (final IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }

                // 1.14+ (custom model data)
                if (ITEM_META_SET_CUSTOM_MODEL_DATA != null) {
                    final int customModelData = section.getInt("custom-model-data");
                    if (customModelData != 0) try {
                        ITEM_META_SET_CUSTOM_MODEL_DATA.invoke(meta, customModelData);
                    } catch (final IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Set meta
            item.setItemMeta(meta);
        }

        // Data
        final ItemData dataUtility = new ItemData(plugin, item);
        final ConfigurationSection dataSection = section.getConfigurationSection("data");
        if (dataSection != null) for (final String key : dataSection.getKeys(false)) dataUtility.set(key, dataSection.getString(key));

        // Return
        return dataUtility.target;
    }

    /**
     * Gets a {@link Recipe} from the YAML. See <a href="https://api.srnyx.com/wiki/file-objects">the wiki</a> for more information
     *
     * @param   path    the path to get the recipe from
     *
     * @return          the {@link Recipe} or {@code null} if it doesn't exist / is invalid / something went wrong
     */
    @Nullable
    public Recipe getRecipe(@NotNull String path) {
        return getRecipe(path, null, null);
    }

    /**
     * Gets a {@link Recipe} from the YAML. See <a href="https://api.srnyx.com/wiki/file-objects">the wiki</a> for more information
     *
     * @param   path            the path to get the recipe from
     * @param   itemFunction    the function to apply to the {@link ItemStack} before returning it
     *
     * @return          the {@link Recipe} or {@code null} if it doesn't exist / is invalid / something went wrong
     */
    @Nullable
    public Recipe getRecipe(@NotNull String path, @Nullable UnaryOperator<ItemStack> itemFunction) {
        final Object def = getDefault(path);
        return getRecipe(path, itemFunction, def instanceof Recipe ? (Recipe) def : null, null);
    }

    /**
     * Gets a {@link Recipe} from the YAML. See <a href="https://api.srnyx.com/wiki/file-objects">the wiki</a> for more information
     *
     * @param   path            the path to get the recipe from
     * @param   itemFunction    the function to apply to the {@link ItemStack} before returning it
     * @param   def             the default {@link Recipe} to return if the recipe doesn't exist / is invalid / something went wrong
     *
     * @return          the {@link Recipe} or {@code null} if it doesn't exist / is invalid / something went wrong
     */
    @Nullable
    public Recipe getRecipe(@NotNull String path, @Nullable UnaryOperator<ItemStack> itemFunction, @Nullable Recipe def) {
        return getRecipe(path, itemFunction, def, null);
    }

    /**
     * Gets a {@link Recipe} from the YAML. See <a href="https://api.srnyx.com/wiki/file-objects">the wiki</a> for more information
     *
     * @param   path            the path to get the recipe from
     * @param   itemFunction    the function to apply to the {@link ItemStack} before returning it
     * @param   def             the default {@link Recipe} to return if the recipe doesn't exist / is invalid / something went wrong
     * @param   name            the name of the recipe (only used in 1.12+ for the {@code NamespacedKey}), or {@code null} to use the node name
     *
     * @return          the {@link Recipe} or the {@code def} if it doesn't exist / is invalid / something went wrong
     */
    @Nullable
    public Recipe getRecipe(@NotNull String path, @Nullable UnaryOperator<ItemStack> itemFunction, @Nullable Recipe def, @Nullable String name) {
        // section, shape, result, & ingredientMaterials
        final ConfigurationSection section = getConfigurationSection(path);
        if (section == null) return def;
        final ConfigurationSection ingredients = section.getConfigurationSection("ingredients");
        final List<String> shape = section.getStringList("shape").stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
        ItemStack result = getItemStack(path + ".result");
        if (ingredients == null || shape.isEmpty() || result == null) return def;
        final Map<Character, Material> ingredientMaterials = new HashMap<>();
        for (final Map.Entry<String, Object> entry : ingredients.getValues(false).entrySet()) {
            final String key = entry.getKey();
            final Material material = Material.matchMaterial(String.valueOf(entry.getValue()));
            if (material == null) {
                log(Level.WARNING, ingredients.getCurrentPath() + "." + key, "&cInvalid material: &4" + entry.getValue());
                continue;
            }
            ingredientMaterials.put(key.toUpperCase().charAt(0), material);
        }
        if (ingredientMaterials.isEmpty()) return def;

        // Apply itemFunction
        if (itemFunction != null) {
            result = itemFunction.apply(result);
            if (result == null) return def;
        }

        // Set name if null
        if (name == null) {
            final String[] split = path.split("\\.");
            name = split[split.length - 1];
        }

        // Shapeless
        if (section.getBoolean("shapeless")) {
            ShapelessRecipe shapeless;
            if (SHAPELESS_RECIPE_CONSTRUCTOR != null && NAMESPACED_KEY_CONSTRUCTOR != null) {
                try {
                    // 1.12+
                    shapeless = SHAPELESS_RECIPE_CONSTRUCTOR.newInstance(NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, name), result);
                } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    // 1.11-
                    shapeless = new ShapelessRecipe(result);
                }
            } else {
                // 1.11-
                shapeless = new ShapelessRecipe(result);
            }
            for (Map.Entry<Character, Material> entry : ingredientMaterials.entrySet()) shapeless.addIngredient(shape.stream()
                    .mapToInt(s -> s.length() - s.replace(entry.getKey().toString(), "").length())
                    .sum(), entry.getValue());
            return shapeless;
        }

        // Shaped
        ShapedRecipe shaped;
        if (SHAPED_RECIPE_CONSTRUCTOR != null && NAMESPACED_KEY_CONSTRUCTOR != null) {
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
        shaped.shape(shape.stream()
                .map(string -> string.replace("-", " "))
                .toArray(String[]::new));
        ingredientMaterials.forEach(shaped::setIngredient);
        return shaped;
    }

    /**
     * A class to hold the options for a file
     *
     * @param   <T> the type of the {@link Options} instance
     */
    public static class Options<T extends Options<T>> extends Stringable {
        /**
         * Whether the file can be empty. If false, the file will be deleted if it's empty when {@link #save()} is used
         */
        public boolean canBeEmpty = true;

        /**
         * Creates a new {@link Options} instance
         */
        public Options() {
            // Only exists to provide a Javadoc
        }

        /**
         * Loads a {@link Options} from the given {@link ConfigurationSection}
         *
         * @param   options the {@link Options} to load into
         * @param   section the {@link ConfigurationSection} to load from
         *
         * @return          the {@link Options} instance
         *
         * @param   <G>     the type of the {@link Options} instance
         */
        @NotNull
        public static <G extends Options<G>> G load(@NotNull G options, @NotNull ConfigurationSection section) {
            if (section.contains("canBeEmpty")) options.canBeEmpty = section.getBoolean("canBeEmpty");
            return options;
        }

        /**
         * Loads a {@link Options} from the given {@link ConfigurationSection}
         *
         * @param   section the {@link ConfigurationSection} to load from
         *
         * @return          the {@link Options} instance
         */
        @NotNull
        public static Options<?> load(@NotNull ConfigurationSection section) {
            final Options<?> options = new Options<>();
            if (section.contains("canBeEmpty")) options.canBeEmpty = section.getBoolean("canBeEmpty");
            return options;
        }

        /**
         * Sets the {@link #canBeEmpty}
         *
         * @param   canBeEmpty  {@link #canBeEmpty}
         *
         * @return              the {@link Options} instance
         */
        @NotNull
        public T canBeEmpty(boolean canBeEmpty) {
            this.canBeEmpty = canBeEmpty;
            return (T) this;
        }
    }
}
