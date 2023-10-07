package xyz.srnyx.annoyingapi.options;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.configuration.ConfigurationSection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPAPIExpansion;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.parents.Registrable;

import xyz.srnyx.javautilities.parents.Stringable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 * Represents the options for class registration (commands/listeners/etc...)
 */
public class RegistrationOptions extends Stringable {
    /**
     * The {@link AutomaticRegistration} options to automatically register {@link Registrable}s
     */
    @NotNull public AutomaticRegistration automaticRegistration = new AutomaticRegistration();
    /**
     * <i>{@code OPTIONAL}</i> The {@link Registrable}s to register when the plugin {@link AnnoyingPlugin#onEnable() enables}
     */
    @NotNull public Set<Registrable> toRegister = new HashSet<>();
    /**
     * <i>{@code OPTIONAL}</i> The {@link PlaceholderExpansion PAPI expansion} to register when the plugin {@link AnnoyingPlugin#onEnable() enables}
     * <p><i>Can also be a {@link AnnoyingPAPIExpansion}</i>
     */
    @NotNull public Supplier<Object> papiExpansionToRegister = () -> null;

    /**
     * Constructs a new {@link RegistrationOptions} instance with default values
     */
    public RegistrationOptions() {
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
    public static RegistrationOptions load(@NotNull ConfigurationSection section) {
        final RegistrationOptions options = new RegistrationOptions();
        if (section.contains("automaticRegistration")) options.automaticRegistration(AutomaticRegistration.load(section.getConfigurationSection("automaticRegistration")));
        return options;
    }

    /**
     * Casts the {@link #papiExpansionToRegister} to a {@link PlaceholderExpansion} and returns it
     *
     * @return  the {@link #papiExpansionToRegister} as a {@link PlaceholderExpansion} or {@code null} if it is not a {@link PlaceholderExpansion}
     */
    @Nullable
    public PlaceholderExpansion getPapiExpansionToRegister() {
        final Object expansion = papiExpansionToRegister.get();
        return expansion instanceof PlaceholderExpansion ? (PlaceholderExpansion) expansion : null;
    }

    /**
     * Sets the {@link #automaticRegistration}
     *
     * @param   automaticRegistration   the automatic registration
     *
     * @return                          this {@link RegistrationOptions} instance for chaining
     */
    @NotNull
    public RegistrationOptions automaticRegistration(@NotNull AutomaticRegistration automaticRegistration) {
        this.automaticRegistration = automaticRegistration;
        return this;
    }

    /**
     * Sets the {@link #automaticRegistration} using the specified {@link Consumer}
     *
     * @param   automaticRegistration   the automatic registration
     *
     * @return                          this {@link RegistrationOptions} instance for chaining
     */
    @NotNull
    public RegistrationOptions automaticRegistration(@NotNull Consumer<AutomaticRegistration> automaticRegistration) {
        automaticRegistration.accept(this.automaticRegistration);
        return this;
    }

    /**
     * Adds the specified {@link Registrable}s to {@link #toRegister}
     *
     * @param   toRegister  the {@link Registrable}s to add
     *
     * @return              this {@link RegistrationOptions} instance for chaining
     */
    @NotNull
    public RegistrationOptions toRegister(@NotNull Collection<Registrable> toRegister) {
        this.toRegister.addAll(toRegister);
        return this;
    }

    /**
     * Adds the specified {@link Registrable}s to {@link #toRegister}
     *
     * @param   toRegister  the {@link Registrable}s to add
     *
     * @return              this {@link RegistrationOptions} instance for chaining
     */
    @NotNull
    public RegistrationOptions toRegister(@NotNull Registrable... toRegister) {
        return toRegister(Arrays.asList(toRegister));
    }

    /**
     * Sets the {@link #papiExpansionToRegister}
     *
     * @param   papiExpansionToRegister the PAPI expansion to register
     *
     * @return                          this {@link RegistrationOptions} instance for chaining
     */
    @NotNull
    public RegistrationOptions papiExpansionToRegister(@NotNull Supplier<Object> papiExpansionToRegister) {
        this.papiExpansionToRegister = papiExpansionToRegister;
        return this;
    }

    /**
     * The automatic registration options
     */
    public static class AutomaticRegistration {
        /**
         * The packages to scan for {@link Registrable}s
         */
        @NotNull public final Set<String> packages = new HashSet<>();
        /**
         * The classes to ignore when scanning for {@link Registrable}s
         */
        @NotNull public final Set<Class<? extends Registrable>> ignoredClasses = new HashSet<>();

        /**
         * Constructs a new {@link AutomaticRegistration} instance with default values
         */
        public AutomaticRegistration() {
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
        public static AutomaticRegistration load(@NotNull ConfigurationSection section) {
            final AutomaticRegistration automaticRegistration = new AutomaticRegistration();
            if (section.contains("packages")) automaticRegistration.packages(section.getStringList("packages"));
            return automaticRegistration;
        }

        /**
         * Adds the specified packages to {@link #packages}
         *
         * @param   packages    the packages to add
         *
         * @return              this {@link AutomaticRegistration} instance for chaining
         */
        @NotNull
        public AutomaticRegistration packages(@NotNull Collection<String> packages) {
            this.packages.addAll(packages);
            return this;
        }

        /**
         * Adds the specified packages to {@link #packages}
         *
         * @param   packages    the packages to add
         *
         * @return              this {@link AutomaticRegistration} instance for chaining
         */
        @NotNull
        public AutomaticRegistration packages(@NotNull String... packages) {
            return packages(Arrays.asList(packages));
        }

        /**
         * Adds the specified classes to {@link #ignoredClasses}
         *
         * @param   ignoredClasses  the classes to add
         *
         * @return                  this {@link AutomaticRegistration} instance for chaining
         */
        @NotNull
        public AutomaticRegistration ignoredClasses(@NotNull Collection<Class<? extends Registrable>> ignoredClasses) {
            this.ignoredClasses.addAll(ignoredClasses);
            return this;
        }

        /**
         * Adds the specified classes to {@link #ignoredClasses}
         *
         * @param   ignoredClasses  the classes to add
         *
         * @return                  this {@link AutomaticRegistration} instance for chaining
         */
        @NotNull @SafeVarargs
        public final AutomaticRegistration ignoredClasses(@NotNull Class<? extends Registrable>... ignoredClasses) {
            return ignoredClasses(Arrays.asList(ignoredClasses));
        }
    }
}
