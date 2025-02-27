package xyz.srnyx.annoyingapi.options;

import org.bukkit.configuration.ConfigurationSection;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.PluginPlatform;
import xyz.srnyx.annoyingapi.dependency.AnnoyingDependency;
import xyz.srnyx.annoyingapi.library.AnnoyingLibrary;

import xyz.srnyx.javautilities.parents.Stringable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 * Represents the general options for the plugin
 */
public class PluginOptions extends Stringable {
    /**
     * <i>{@code OPTIONAL}</i> The {@link AnnoyingLibrary AnnoyingLibraries} to load <b>before</b> initializing the plugin
     * <br>This is only needed if you have a library that is required to load before the plugin initializes, otherwise you can just load the library yourself when the plugin enables
     * <br><i>Loading these libraries will be one of the first (if not the first) things the plugin does</i>
     */
    @NotNull public List<AnnoyingLibrary> libraries = new ArrayList<>();
    /**
     * <i>{@code OPTIONAL}</i> The {@link AnnoyingDependency AnnoyingDependencies} to check for (add dependencies to this in the plugin's constructor)
     * <p>If you add a dependency to this OUTSIDE the constructor, it will not be checked
     * <p><i>This is <b>NOT</b> meant for optional dependencies, all of these dependencies will be downloaded/installed (even if {@link AnnoyingDependency#required} is {@code false})</i>
     */
    @NotNull @SuppressWarnings("CanBeFinal") public List<AnnoyingDependency> dependencies = new ArrayList<>();
    /**
     * <i>{@code RECOMMENDED}</i> The different {@link PluginPlatform platforms} the plugin is available on
     * <p>If not specified, the plugin will not be able to check for updates
     */
    @NotNull public PluginPlatform.Multi updatePlatforms = new PluginPlatform.Multi();

    /**
     * Constructs a new {@link PluginOptions} instance with default values
     */
    public PluginOptions() {
        // Only exists to give the constructor a Javadoc
    }

    /**
     * Loads the options from the specified {@link ConfigurationSection}
     *
     * @param   section the section to load the options from
     *
     * @return          the loaded options
     */
    @NotNull
    public static PluginOptions load(@NotNull ConfigurationSection section) {
        final PluginOptions options = new PluginOptions();
        options.dependencies.addAll(AnnoyingDependency.loadList(section, "dependencies"));
        options.updatePlatforms = PluginPlatform.Multi.load(section, "updatePlatforms");
        return options;
    }

    /**
     * Adds the specified {@link AnnoyingLibrary}s to {@link #libraries}
     *
     * @param   libraries   the libraries to add
     *
     * @return              the {@link PluginOptions} instance for chaining
     */
    @NotNull
    public PluginOptions libraries(@NotNull Collection<AnnoyingLibrary> libraries) {
        this.libraries.addAll(libraries);
        return this;
    }

    /**
     * Adds the specified {@link AnnoyingLibrary}s to {@link #libraries}
     *
     * @param   libraries   the libraries to add
     *
     * @return              the {@link PluginOptions} instance for chaining
     */
    @NotNull
    public PluginOptions libraries(@NotNull AnnoyingLibrary... libraries) {
        return libraries(Arrays.asList(libraries));
    }

    /**
     * Adds the specified {@link AnnoyingDependency}s to {@link #dependencies}
     *
     * @param   dependencies    the dependencies to add
     *
     * @return                  the {@link PluginOptions} instance for chaining
     */
    @NotNull
    public PluginOptions dependencies(@NotNull Collection<AnnoyingDependency> dependencies) {
        this.dependencies.addAll(dependencies);
        return this;
    }

    /**
     * Adds the specified {@link AnnoyingDependency}s to {@link #dependencies}
     *
     * @param   dependencies    the dependencies to add
     *
     * @return                  the {@link PluginOptions} instance for chaining
     */
    @NotNull
    public PluginOptions dependencies(@NotNull AnnoyingDependency... dependencies) {
        return dependencies(Arrays.asList(dependencies));
    }

    /**
     * Sets {@link #updatePlatforms}
     *
     * @param   updatePlatforms     the new {@link #updatePlatforms}
     *
     * @return                      the {@link PluginOptions} instance for chaining
     */
    @NotNull
    public PluginOptions updatePlatforms(@NotNull PluginPlatform.Multi updatePlatforms) {
        this.updatePlatforms = updatePlatforms;
        return this;
    }

    /**
     * Sets {@link #updatePlatforms}
     *
     * @param   updatePlatforms the new {@link #updatePlatforms}
     *
     * @return                  the {@link PluginOptions} instance for chaining
     */
    @NotNull
    public PluginOptions updatePlatforms(@NotNull Collection<PluginPlatform> updatePlatforms) {
        return updatePlatforms(new PluginPlatform.Multi(updatePlatforms));
    }

    /**
     * Sets {@link #updatePlatforms}
     *
     * @param   updatePlatforms the new {@link #updatePlatforms}
     *
     * @return                  the {@link PluginOptions} instance for chaining
     */
    @NotNull
    public PluginOptions updatePlatforms(@NotNull PluginPlatform... updatePlatforms) {
        return updatePlatforms(new PluginPlatform.Multi(updatePlatforms));
    }
}
