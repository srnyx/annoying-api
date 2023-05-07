package xyz.srnyx.annoyingapi.file;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.Plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * Gets a {@link Recipe} from the YAML
     *
     * @param   path    the path to get the recipe from
     * @param   name    the name of the recipe (only used in 1.12+ for the {@code NamespacedKey})
     * @param   result  {@link Recipe#getResult()}
     *
     * @return          the {@link Recipe} or {@code null} if it doesn't exist / is invalid / something went wrong
     */
    @Nullable
    public Recipe getRecipe(@NotNull String path, @Nullable String name, @NotNull ItemStack result) {
        final Object def = getDefault(path);
    	return getRecipe(path, name, result, def instanceof Recipe ? (Recipe) def : null);
    }

    /**
     * Gets a {@link Recipe} from the YAML
     *
     * @param   path    the path to get the recipe from
     * @param   name    the name of the recipe (only used in 1.12+ for the {@code NamespacedKey})
     * @param   result  {@link Recipe#getResult()}
     * @param   def     the default {@link Recipe} to return if the recipe doesn't exist / is invalid / something went wrong
     *
     * @return          the {@link Recipe} or the {@code def} if it doesn't exist / is invalid / something went wrong
     */
    @Nullable
    public Recipe getRecipe(@NotNull String path, @Nullable String name, @NotNull ItemStack result, @Nullable Recipe def) {
        // Get section, shape, and ingredients
    	final ConfigurationSection section = getConfigurationSection(path);
        if (section == null) return def;
        final ConfigurationSection ingredients = section.getConfigurationSection("ingredients");
        final List<String> shape = section.getStringList("shape").stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
        if (ingredients == null || shape.isEmpty()) return def;
        final Map<Character, Material> ingredientMaterials = new HashMap<>();
        for (final Map.Entry<String, Object> entry : ingredients.getValues(false).entrySet()) {
            final Material material = Material.matchMaterial(String.valueOf(entry.getValue()));
            if (material == null) {
                plugin.log(Level.WARNING, "&cInvalid material: &4" + entry.getValue());
                continue;
            }
            ingredientMaterials.put(entry.getKey().toUpperCase().charAt(0), material);
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
            try {
                // 1.12+
                shapeless = ShapelessRecipe.class.getConstructor(Class.forName("org.bukkit.NamespacedKey"), ItemStack.class).newInstance(Class.forName("org.bukkit.NamespacedKey").getConstructor(Plugin.class, String.class).newInstance(plugin, name), result);
            } catch (final NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
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
        try {
            // 1.12+
            shaped = ShapedRecipe.class.getConstructor(Class.forName("org.bukkit.NamespacedKey"), ItemStack.class).newInstance(Class.forName("org.bukkit.NamespacedKey").getConstructor(Plugin.class, String.class).newInstance(plugin, name), result);
        } catch (final NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
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
