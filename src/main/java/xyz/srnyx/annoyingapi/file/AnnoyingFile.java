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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.reflection.org.bukkit.RefRegistry;
import xyz.srnyx.annoyingapi.reflection.org.bukkit.RefSoundCategory;
import xyz.srnyx.annoyingapi.utility.BukkitUtility;
import xyz.srnyx.annoyingapi.data.ItemData;
import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import xyz.srnyx.javautilities.FileUtility;
import xyz.srnyx.javautilities.parents.Stringable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
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
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.potion.RefPotionEffect.POTION_EFFECT_CONSTRUCTOR_6;


/**
 * Represents a file in the plugin's folder
 *
 * @param   <T> the type of the {@link AnnoyingFile} instance
 *
 * @see         AnnoyingResource
 * @see         AnnoyingData
 */
@SuppressWarnings("unchecked")
public class AnnoyingFile<T extends AnnoyingFile<T>> extends YamlConfiguration {
    /**
     * The {@link AnnoyingPlugin} instance
     */
    @NotNull public final AnnoyingPlugin plugin;
    /**
     * The {@link File} for the file
     */
    @NotNull public final File file;
    /**
     * The {@link Options} for the file
     */
    @NotNull protected final Options<?> fileOptions;

    /**
     * Constructs a new {@link AnnoyingFile}
     *
     * @param   plugin      {@link #plugin}
     * @param   file        {@link #file}
     * @param   fileOptions {@link #fileOptions}
     */
    public AnnoyingFile(@NotNull AnnoyingPlugin plugin, @NotNull File file, @Nullable Options<?> fileOptions) {
        this.plugin = plugin;
        this.file = file;
        this.fileOptions = fileOptions == null ? new Options<>() : fileOptions;
    }

    /**
     * Constructs a new {@link AnnoyingFile}
     *
     * @param   plugin  {@link #plugin}
     * @param   file    {@link #file}
     */
    public AnnoyingFile(@NotNull AnnoyingPlugin plugin, @NotNull File file) {
        this(plugin, file, null);
    }

    /**
     * Constructs a new {@link AnnoyingFile}
     *
     * @param   plugin          {@link #plugin}
     * @param   path            the path to the file (relative to the plugin's folder)
     * @param   fileOptions     {@link #fileOptions}
     */
    public AnnoyingFile(@NotNull AnnoyingPlugin plugin, @NotNull String path, @Nullable Options<?> fileOptions) {
        this(plugin, new File(plugin.getDataFolder(), path), fileOptions);
    }

    /**
     * Constructs a new {@link AnnoyingFile}
     *
     * @param   plugin  {@link #plugin}
     * @param   path    the path to the file (relative to the plugin's folder)
     */
    public AnnoyingFile(@NotNull AnnoyingPlugin plugin, @NotNull String path) {
        this(plugin, path, null);
    }

    @Override @NotNull
    public String toString() {
        return Stringable.toString(this);
    }

    /**
     * Creates the {@link #file}
     */
    public void create() {
        final Path filePath = file.toPath();
        try {
            Files.createDirectories(filePath.getParent());
            Files.createFile(filePath);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the YAML from the path. If the file doesn't exist and {@link Options#canBeEmpty <b>can't</b> be empty}, an empty {@link YamlConfiguration} will be used
     *
     * @return  whether the file was loaded successfully
     */
    public boolean load() {
        if (fileOptions.replace) { // Recreate file if it's set to replace
            create();
        } else if (!file.exists()) { // Create the file if it doesn't exist and can be empty
            if (fileOptions.canBeEmpty) {
                create();
            } else {
                return true;
            }
        }

        // Load
        try {
            load(file);
            return true;
        } catch (final IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes the {@link #file}
     *
     * @param   silentFail  whether to fail silently
     *
     * @return              whether the file was deleted successfully
     *
     * @see                 #delete()
     */
    public boolean delete(boolean silentFail) {
        return FileUtility.deleteFile(file.toPath(), silentFail);
    }

    /**
     * Deletes the {@link #file}, won't fail silently
     *
     * @return  whether the file was deleted successfully
     *
     * @see     #delete(boolean)
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean delete() {
        return delete(false);
    }

    /**
     * Sets a value in the YAML and returns the {@link AnnoyingFile} instance
     *
     * @param   path    the path to the node
     * @param   value   the value to set the node to
     *
     * @return          the {@link AnnoyingFile} instance
     */
    @NotNull
    public T setChain(@NotNull String path, @Nullable Object value) {
        set(path, value);
        return (T) this;
    }

    /**
     * Sets a value in the YAML and then {@link #save() saves} the file
     *
     * @param   path    the path to the node
     * @param   value   the value to set the node to
     *
     * @return          whether the file was saved successfully
     */
    public boolean setSave(@NotNull String path, @Nullable Object value) {
        set(path, value);
        return save();
    }

    /**
     * Saves the YAML to the {@link #file}
     *
     * @return  whether the file was saved successfully
     */
    public boolean save() {
        // Stop process if it's empty when it can't be
        if (!fileOptions.canBeEmpty && getKeys(true).isEmpty()) {
            // Delete file if it exists
            if (file.exists()) delete();
            return true;
        }

        // Create file if it can be empty and doesn't exist
        if (fileOptions.canBeEmpty && !file.exists()) create();

        // Save file
        try {
            save(file);
            return true;
        } catch (final IOException e) {
            e.printStackTrace();
            return false;
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
        AnnoyingPlugin.log(level, ChatColor.getLastColors(message) + file.getPath() + (key == null ? "" : ", " + key) + " | " + message);
    }

    /**
     * Gets the default value from the path
     *
     * @param   path    the path to the node
     *
     * @return          the default value or empty if it doesn't exist
     *
     * @param   <G>     the type of the default value
     */
    @NotNull
    public <G> Optional<G> getDef(@NotNull String path) {
        final Object value = getDefault(path);
        return value != null ? Optional.of((G) value) : Optional.empty();
    }

    /**
     * Get a {@link ConfigurationSection} from the path in an {@link Optional}
     *
     * @param   path    the path to the node
     *
     * @return          the {@link ConfigurationSection} or empty if it doesn't exist
     */
    @NotNull
    public Optional<ConfigurationSection> getConfigurationSectionOptional(@NotNull String path) {
        return Optional.ofNullable(getConfigurationSection(path));
    }

    /**
     * Gets a {@link Sound} from the path
     *
     * @param   path    the path to the node
     *
     * @return          the {@link Sound} or empty if it's invalid
     */
    @NotNull
    public Optional<Sound> getSound(@NotNull String path) {
        final String sound = getString(path);
        if (sound != null) try {
            return Optional.of(Sound.valueOf(sound.toUpperCase()));
        } catch (final IllegalArgumentException ignored) {}
        return getDef(path);
    }

    /**
     * Gets a {@link PlayableSound} from the path. See <a href="https://annoying-api.srnyx.com/wiki/file-objects">the wiki</a> for more information
     *
     * @param   path    the path to the node
     *
     * @return          the {@link PlayableSound} or empty if it's invalid
     */
    @NotNull
    public Optional<PlayableSound> getPlayableSound(@NotNull String path) {
        final Optional<PlayableSound> def = getDef(path);
        final Optional<Sound> sound = getSound(path + ".sound");
        if (!sound.isPresent()) return def;
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
        return Optional.of(new PlayableSound(sound.get(), category, (float) section.getDouble("volume", 1), (float) section.getDouble("pitch", 1)));
    }

    /**
     * Gets a {@link PotionEffect} from the path. See <a href="https://annoying-api.srnyx.com/wiki/file-objects">the wiki</a> for more information
     *
     * @param   path    the path to the node
     *
     * @return          the {@link PotionEffect} or empty if it's invalid
     */
    @NotNull
    public Optional<PotionEffect> getPotionEffect(@NotNull String path) {
        return getPotionEffect(path, true);
    }

    /**
     * Gets a {@link PotionEffect} from the path. See <a href="https://annoying-api.srnyx.com/wiki/file-objects">the wiki</a> for more information
     *
     * @param   path    the path to the node
     * @param   log     whether to log warnings if the potion effect is invalid
     *
     * @return          the {@link PotionEffect} or empty if it's invalid
     */
    @NotNull
    public Optional<PotionEffect> getPotionEffect(@NotNull String path, boolean log) {
        final Optional<PotionEffect> def = getDef(path);
        final ConfigurationSection section = getConfigurationSection(path);
        if (section == null) return def;

        // Get type name
        final String typeString = section.getString("type");
        if (typeString == null) {
            if (log) log(Level.WARNING, path, "&cInvalid potion effect, missing type");
            return def;
        }

        // Get type
        final Optional<PotionEffectType> typeOptional = RefRegistry.getEffect(typeString);
        if (!typeOptional.isPresent()) {
            if (log) log(Level.WARNING, path, "&cInvalid potion effect type: &4" + typeString);
            return def;
        }
        final PotionEffectType type = typeOptional.get();

        // Get duration, amplifier, ambient, & particles
        final int duration = section.getInt("duration", 1);
        final int amplifier = section.getInt("amplifier", 0);
        final boolean ambient = section.getBoolean("ambient", false);
        final boolean particles = section.getBoolean("particles", true);

        // 1.13+ icon
        if (POTION_EFFECT_CONSTRUCTOR_6 != null) try {
            return Optional.of(POTION_EFFECT_CONSTRUCTOR_6.newInstance(type, duration, amplifier, ambient, particles, section.getBoolean("icon", true)));
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        // 1.12.2-
        return Optional.of(new PotionEffect(type, duration, amplifier, ambient, particles));
    }

    /**
     * {@code 1.9+} Gets an {@code AttributeModifier} from the path. See <a href="https://annoying-api.srnyx.com/wiki/file-objects">the wiki</a> for more information
     *
     * @param   path    the path to the node
     *
     * @param   <G>     the {@code AttributeModifier} class
     *
     * @return          the {@code AttributeModifier} or empty if it's invalid
     */
    @NotNull
    public <G> Optional<G> getAttributeModifier(@NotNull String path) {
        return getAttributeModifier(path, true);
    }

    /**
     * {@code 1.9+} Gets an {@code AttributeModifier} from the path. See <a href="https://annoying-api.srnyx.com/wiki/file-objects">the wiki</a> for more information
     *
     * @param   path    the path to the node
     * @param   log     whether to log warnings if the attribute modifier is invalid
     *
     * @param   <G>     the {@code AttributeModifier} class
     *
     * @return          the {@code AttributeModifier} or empty if it's invalid
     */
    @NotNull @SuppressWarnings("unchecked")
    public <G> Optional<G> getAttributeModifier(@NotNull String path, boolean log) {
        final Optional<G> def = getDef(path);
        if (ATTRIBUTE_MODIFIER_OPERATION_ENUM == null) return def;
        final ConfigurationSection section = getConfigurationSection(path);
        if (section == null) return def;

        final String name = section.getString("name");
        final String operationString = section.getString("operation");
        if (name == null || operationString == null) {
            if (log) log(Level.WARNING, path, "&cInvalid attribute modifier, missing name and/or operation");
            return def;
        }

        // operation
        final Object operation;
        try {
            operation = Enum.valueOf(ATTRIBUTE_MODIFIER_OPERATION_ENUM, operationString);
        } catch (final IllegalArgumentException e) {
            if (log) log(Level.WARNING, path, "&cInvalid attribute modifier operation: &4" + operationString);
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
                if (log) log(Level.WARNING, path, "&cInvalid equipment slot: &4" + equipmentSlotString);
            }

            // Return
            try {
                return Optional.of((G) ATTRIBUTE_MODIFIER_CONSTRUCTOR_5.newInstance(UUID.randomUUID(), name, amount, operation, slot));
            } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return def;
            }
        }

        // Return
        if (ATTRIBUTE_MODIFIER_CONSTRUCTOR_3 != null) try {
            return Optional.of((G) ATTRIBUTE_MODIFIER_CONSTRUCTOR_3.newInstance(name, amount, operation));
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return def;
    }

    /**
     * Gets an {@link ItemStack} from the path. See <a href="https://annoying-api.srnyx.com/wiki/file-objects">the wiki</a> for more information
     * <br><i>If you want to disable warning/error logging, use {@link #getItemStackOptional(String, boolean)}</i>
     *
     * @param   path    the path to the node
     *
     * @return          the {@link ItemStack} or {@code null} if it's invalid
     */
    @Override @Nullable
    public ItemStack getItemStack(@NotNull String path) {
        return getItemStackOptional(path, true).orElse(null);
    }

    /**
     * Gets an {@link ItemStack} from the path. See <a href="https://annoying-api.srnyx.com/wiki/file-objects">the wiki</a> for more information
     * <br><i>If you want to disable warning/error logging, use {@link #getItemStackOptional(String, boolean)}</i>
     *
     * @param   path    the path to the node
     * @param   def     the default value
     *
     * @return          the {@link ItemStack} or {@code def} if it's invalid
     */
    @Override @Nullable
    public ItemStack getItemStack(@NotNull String path, @Nullable ItemStack def) {
        return getItemStackOptional(path, true).orElse(def);
    }

    /**
     * Gets an {@link ItemStack} from the path. See <a href="https://annoying-api.srnyx.com/wiki/file-objects">the wiki</a> for more information
     *
     * @param   path    the path to the node
     *
     * @return the {@link ItemStack} or empty if it's invalid
     */
    @NotNull
    public Optional<ItemStack> getItemStackOptional(@NotNull String path) {
        return getItemStackOptional(path, true);
    }

    /**
     * Gets an {@link ItemStack} from the path. See <a href="https://annoying-api.srnyx.com/wiki/file-objects">the wiki</a> for more information
     *
     * @param   path    the path to the node
     * @param   log     whether to log warnings if the item stack is invalid
     *
     * @return the {@link ItemStack} or empty if it's invalid
     */
    @NotNull
    public Optional<ItemStack> getItemStackOptional(@NotNull String path, boolean log) {
        final Optional<ItemStack> def = getDef(path);
        final ConfigurationSection section = getConfigurationSection(path);
        if (section == null) return def;

        // Get material name
        final String materialString = section.getString("material");
        if (materialString == null) {
            if (log) log(Level.WARNING, path, "&cInvalid material, missing material");
            return def;
        }

        // Get material
        final Material material = Material.matchMaterial(materialString);
        if (material == null) {
            if (log) log(Level.WARNING, path, "&cInvalid material for: &4" + materialString);
            return def;
        }

        // Get amount & damage
        final int amount = section.getInt("amount", 1);
        final int damage = section.getInt("damage", 0);

        // Material, amount, & durability (1.12.2-)
        final boolean useDamageable = DAMAGEABLE_CLASS != null && DAMAGEABLE_SET_DAMAGE_METHOD != null;
        final ItemStack item = useDamageable ? new ItemStack(material, amount) : new ItemStack(material, amount, (short) damage);

        // Durability (1.13+), name, lore, unbreakable, enchantments, flags, attribute modifiers, & custom model data
        final ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Durability (1.13+)
            if (useDamageable && DAMAGEABLE_CLASS.isInstance(meta)) try {
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
                final Optional<Enchantment> enchantment = RefRegistry.getEnchantment(enchantmentKey);
                if (!enchantment.isPresent()) {
                    if (log) log(Level.WARNING, path, "&cInvalid enchantment: &4" + enchantmentKey);
                    continue;
                }
                meta.addEnchant(enchantment.get(), enchantmentsSection.getInt(enchantmentKey), true);
            }

            // Flags
            section.getStringList("flags").stream()
                    .map(string -> {
                        try {
                            return ItemFlag.valueOf(string.toUpperCase());
                        } catch (final IllegalArgumentException e) {
                            if (log) log(Level.WARNING, section.getCurrentPath() + "." + "flags", "&cInvalid item flag: &4" + string);
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
                        if (log) log(Level.WARNING, pathString, "&cInvalid attribute: &4" + attributeKey);
                        continue;
                    }

                    // Get attribute modifier
                    final Optional<?> attributeModifier = getAttributeModifier(pathString);
                    if (!attributeModifier.isPresent()) continue;

                    // Add attribute modifier
                    try {
                        ITEM_META_ADD_ATTRIBUTE_MODIFIER.invoke(meta, attribute, attributeModifier.get());
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
        return Optional.of(dataUtility.target);
    }

    /**
     * Gets a {@link Recipe} from the YAML. See <a href="https://annoying-api.srnyx.com/wiki/file-objects">the wiki</a> for more information
     *
     * @param   path    the path to get the recipe from
     *
     * @return          the {@link Recipe} or {@code null} if it doesn't exist / is invalid / something went wrong
     */
    @NotNull
    public Optional<Recipe> getRecipe(@NotNull String path) {
        return getRecipe(path, null, null);
    }

    /**
     * Gets a {@link Recipe} from the YAML. See <a href="https://annoying-api.srnyx.com/wiki/file-objects">the wiki</a> for more information
     *
     * @param   path            the path to get the recipe from
     * @param   itemFunction    the function to apply to the {@link ItemStack} before returning it
     *
     * @return          the {@link Recipe} or empty if it's invalid
     */
    @NotNull
    public Optional<Recipe> getRecipe(@NotNull String path, @Nullable UnaryOperator<ItemStack> itemFunction) {
        return getRecipe(path, itemFunction, null);
    }

    /**
     * Gets a {@link Recipe} from the YAML. See <a href="https://annoying-api.srnyx.com/wiki/file-objects">the wiki</a> for more information
     *
     * @param   path            the path to get the recipe from
     * @param   itemFunction    the function to apply to the {@link ItemStack} before returning it
     * @param   name            the name of the recipe (only used in 1.12+ for the {@code NamespacedKey}), or {@code null} to use the node name
     *
     * @return          the {@link Recipe} or the {@code def} if it's invalid
     */
    @NotNull
    public Optional<Recipe> getRecipe(@NotNull String path, @Nullable UnaryOperator<ItemStack> itemFunction, @Nullable String name) {
        return getRecipe(path, itemFunction, name, true);
    }

    /**
     * Gets a {@link Recipe} from the YAML. See <a href="https://annoying-api.srnyx.com/wiki/file-objects">the wiki</a> for more information
     *
     * @param   path            the path to get the recipe from
     * @param   itemFunction    the function to apply to the {@link ItemStack} before returning it
     * @param   name            the name of the recipe (only used in 1.12+ for the {@code NamespacedKey}), or {@code null} to use the node name
     * @param   log             whether to log warnings if the recipe is invalid
     *
     * @return          the {@link Recipe} or the {@code def} if it's invalid
     */
    @NotNull
    public Optional<Recipe> getRecipe(@NotNull String path, @Nullable UnaryOperator<ItemStack> itemFunction, @Nullable String name, boolean log) {
        final Optional<Recipe> def = getDef(path);

        // section, shape, result, & ingredientMaterials
        final ConfigurationSection section = getConfigurationSection(path);
        if (section == null) return def;
        final ConfigurationSection ingredients = section.getConfigurationSection("ingredients");
        final List<String> shape = section.getStringList("shape").stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
        ItemStack result = getItemStack(path + ".result");
        if (ingredients == null || shape.isEmpty()) return def;
        final Map<Character, Material> ingredientMaterials = new HashMap<>();
        for (final Map.Entry<String, Object> entry : ingredients.getValues(false).entrySet()) {
            final String key = entry.getKey();
            final String value = String.valueOf(entry.getValue());
            final Material material = Material.matchMaterial(value);
            if (material == null) {
                if (log) log(Level.WARNING, ingredients.getCurrentPath() + "." + key, "&cInvalid material: &4" + value);
                continue;
            }
            ingredientMaterials.put(key.toUpperCase().charAt(0), material);
        }
        if (ingredientMaterials.isEmpty()) return def;

        // Apply itemFunction
        if (itemFunction != null) result = itemFunction.apply(result);
        if (result == null) return def;

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
            return Optional.of(shapeless);
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
        return Optional.of(shaped);
    }

    /**
     * A class to hold the options for a file
     * <br>
     * <br><b>Default options:</b>
     * <ul>
     *     <li>{@link #canBeEmpty} = {@code true}
     *     <li>{@link #replace} = {@code false}
     * </ul>
     *
     * @param   <G> the type of the {@link Options} instance
     */
    public static class Options<G extends Options<G>> extends Stringable {
        /**
         * Whether the file can be empty. If false, the file will be deleted if it's empty when {@link #save()} is used
         */
        public boolean canBeEmpty = true;
        /**
         * Whether to replace the file if it already exists. Works best with {@link #canBeEmpty} set to {@code true}
         */
        public boolean replace = false;

        /**
         * Creates a new {@link Options} instance
         */
        public Options() {
            // Only exists to give the constructor a Javadoc
        }

        /**
         * Loads a {@link Options} from the given {@link ConfigurationSection}
         *
         * @param   options the {@link Options} to load into
         * @param   section the {@link ConfigurationSection} to load from
         *
         * @return          the {@link Options} instance
         *
         * @param   <H>     the type of the {@link Options} instance
         */
        @NotNull
        public static <H extends Options<H>> H load(@NotNull H options, @NotNull ConfigurationSection section) {
            if (section.contains("canBeEmpty")) options.canBeEmpty = section.getBoolean("canBeEmpty");
            if (section.contains("replace")) options.replace = section.getBoolean("replace");
            return options;
        }

        /**
         * Loads a {@link Options} from the given {@link ConfigurationSection}
         *
         * @param   section the {@link ConfigurationSection} to load from
         *
         * @return          the {@link Options} instance
         *
         * @param   <R>     the type of the {@link Options} instance
         */
        @NotNull
        public static <R extends Options<R>> Options<R> load(@NotNull ConfigurationSection section) {
            final Options<R> options = new Options<>();
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
        public G canBeEmpty(boolean canBeEmpty) {
            this.canBeEmpty = canBeEmpty;
            return (G) this;
        }

        /**
         * Sets the {@link #replace}
         *
         * @param   replace {@link #replace}
         *
         * @return          the {@link Options} instance
         */
        @NotNull
        public G replace(boolean replace) {
            this.replace = replace;
            return (G) this;
        }
    }
}
