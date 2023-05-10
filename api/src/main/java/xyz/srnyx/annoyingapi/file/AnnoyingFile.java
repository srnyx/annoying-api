package xyz.srnyx.annoyingapi.file;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.utility.AnnoyingUtility;
import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;


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
     * The {@link FileOptions} for the file
     */
    @NotNull protected final FileOptions fileOptions;

    /**
     * Constructs a new {@link AnnoyingFile}
     *
     * @param   plugin          {@link #plugin}
     * @param   path            {@link #path}
     * @param   fileOptions     {@link #fileOptions}
     */
    protected AnnoyingFile(@NotNull AnnoyingPlugin plugin, @NotNull String path, @Nullable FileOptions fileOptions) {
        this.plugin = plugin;
        this.path = path;
        this.file = new File(plugin.getDataFolder(), path);
        this.fileOptions = fileOptions == null ? new FileOptions() : fileOptions;
        load();
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
    public void setSave(@Nullable String path, @NotNull Object value) {
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
     *
     * @param   level   the {@link Level} of the message
     * @param   key     the key of the node that the message is about ({@code null} if it's not about a node)
     * @param   message the message to send
     */
    public void log(@NotNull Level level, @Nullable String key, @NotNull String message) {
        plugin.log(level, ChatColor.getLastColors(message) + path + (key == null ? "" : ", " + key) + " | " + message);
    }

    /**
     * Gets an {@link AttributeModifier} from the path
     *
     * @param   path    the path to the node
     *
     * @return          the {@link AttributeModifier} or {@code null} if it's invalid
     */
    @Nullable
    public AttributeModifier getAttributeModifier(@NotNull String path) {
        final Object def = getDefault(path);
        return getAttributeModifier(path, def instanceof AttributeModifier ? (AttributeModifier) def : null);
    }

    /**
     * Gets an {@link AttributeModifier} from the path
     *
     * @param   path    the path to the node
     * @param   def     the default value
     *
     * @return          the {@link AttributeModifier} or {@code def} if it's invalid
     */
    @Nullable
    public AttributeModifier getAttributeModifier(@NotNull String path, @Nullable AttributeModifier def) {
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
        final AttributeModifier.Operation operation;
        try {
            operation = AttributeModifier.Operation.valueOf(operationString);
        } catch (final IllegalArgumentException e) {
            log(Level.WARNING, path, "&cInvalid operation: &4" + operationString);
            return def;
        }

        // amount
        final double amount = section.getDouble("amount");

        // 1.13.2+
        if (ReflectionUtility.attributeModifierConstructor != null) {
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
                return ReflectionUtility.attributeModifierConstructor.newInstance(UUID.randomUUID(), name, amount, operation, slot);
            } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return def;
            }
        }

        // Return
        return new AttributeModifier(name, amount, operation);
    }

    /**
     * Gets an {@link ItemStack} from the path
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
     * Gets an {@link ItemStack} from the path
     *
     * @param   path    the path to the node
     * @param   def     the default value
     *
     * @return          the {@link ItemStack} or {@code def} if it's invalid
     */
    @Override @Nullable
    public ItemStack getItemStack(@NotNull String path, @Nullable ItemStack def) {
        final ConfigurationSection section = getConfigurationSection(path);
        if (section == null) return def;
        final String materialString = section.getString("material");
        if (materialString == null) return def;
        final Material material = Material.matchMaterial(materialString);
        if (material == null) return def;

        // Material, amount, and durability
        final ItemStack item = new ItemStack(material, section.getInt("amount", 1), (short) section.getInt("damage", 0));

        // Name, lore, unbreakable, enchantments, flags, attribute modifiers, and custom model data
        final ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Name
            final String name = section.getString("name");
            if (name != null) meta.setDisplayName(AnnoyingUtility.color(name));

            // Lore
            meta.setLore(section.getStringList("lore").stream()
                    .map(AnnoyingUtility::color)
                    .collect(Collectors.toList()));

            // Unbreakable
            meta.setUnbreakable(section.getBoolean("unbreakable", false));

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

            // Attribute modifiers
            if (ReflectionUtility.addAttributeModifierMethod != null) {
                final ConfigurationSection attributeModifiersSection = section.getConfigurationSection("attribute-modifiers");
                if (attributeModifiersSection != null)
                    for (final String attributeKey : attributeModifiersSection.getKeys(false)) {
                        final String pathString = attributeModifiersSection.getCurrentPath() + "." + attributeKey;

                        // Get attribute
                        final Attribute attribute;
                        try {
                            attribute = Attribute.valueOf(attributeKey);
                        } catch (final IllegalArgumentException e) {
                            log(Level.WARNING, pathString, "&cInvalid attribute: &4" + attributeKey);
                            continue;
                        }

                        // Get attribute modifier
                        final AttributeModifier attributeModifier = getAttributeModifier(pathString);
                        if (attributeModifier == null) continue;

                        // Add attribute modifier
                        try {
                            ReflectionUtility.addAttributeModifierMethod.invoke(meta, attribute, attributeModifier);
                        } catch (final IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
            }

            // Custom model data
            if (ReflectionUtility.setCustomModelDataMethod != null) {
                final int customModelData = section.getInt("custom-model-data");
                if (customModelData != 0) try {
                    ReflectionUtility.setCustomModelDataMethod.invoke(meta, customModelData);
                } catch (final IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

            // Set meta
            item.setItemMeta(meta);
        }

        // Return
        return item;
    }

    /**
     * Gets a {@link Recipe} from the YAML
     *
     * @param   path    the path to get the recipe from
     * @param   name    the name of the recipe (only used in 1.12+ for the {@code NamespacedKey})
     *
     * @return          the {@link Recipe} or {@code null} if it doesn't exist / is invalid / something went wrong
     */
    @Nullable
    public Recipe getRecipe(@NotNull String path, @Nullable String name) {
        final Object def = getDefault(path);
        return getRecipe(path, name, def instanceof Recipe ? (Recipe) def : null);
    }

    /**
     * Gets a {@link Recipe} from the YAML
     *
     * @param   path    the path to get the recipe from
     * @param   name    the name of the recipe (only used in 1.12+ for the {@code NamespacedKey})
     * @param   def     the default {@link Recipe} to return if the recipe doesn't exist / is invalid / something went wrong
     *
     * @return          the {@link Recipe} or the {@code def} if it doesn't exist / is invalid / something went wrong
     */
    @Nullable
    public Recipe getRecipe(@NotNull String path, @Nullable String name, @Nullable Recipe def) {
        // Get section, shape, and ingredients
        final ConfigurationSection section = getConfigurationSection(path);
        if (section == null) return def;
        final ConfigurationSection ingredients = section.getConfigurationSection("ingredients");
        final List<String> shape = section.getStringList("shape").stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
        final ItemStack result = getItemStack(path + ".result");
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

        // Set name if null
        if (name == null) {
            final String[] split = path.split("\\.");
            name = split[split.length - 1];
        }

        // Shapeless
        if (section.getBoolean("shapeless")) {
            ShapelessRecipe shapeless;
            if (ReflectionUtility.shapelessRecipeConstructor != null && ReflectionUtility.namespacedKeyConstructor != null) {
                try {
                    // 1.12+
                    shapeless = ReflectionUtility.shapelessRecipeConstructor.newInstance(ReflectionUtility.namespacedKeyConstructor.newInstance(plugin, name), result);
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
        if (ReflectionUtility.shapedRecipeConstructor != null && ReflectionUtility.namespacedKeyConstructor != null) {
            try {
                // 1.12+
                shaped = ReflectionUtility.shapedRecipeConstructor.newInstance(ReflectionUtility.namespacedKeyConstructor.newInstance(plugin, name), result);
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
     */
    public static class FileOptions {
        /**
         * Whether the file can be empty. If false, the file will be deleted if it's empty when {@link #save()} is used
         */
        public boolean canBeEmpty = true;

        /**
         * Sets the {@link #canBeEmpty}
         *
         * @param   canBeEmpty  {@link #canBeEmpty}
         *
         * @return              the {@link FileOptions} instance
         */
        @NotNull
        public FileOptions canBeEmpty(boolean canBeEmpty) {
            this.canBeEmpty = canBeEmpty;
            return this;
        }
    }
}
