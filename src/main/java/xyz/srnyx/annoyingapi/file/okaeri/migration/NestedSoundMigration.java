package xyz.srnyx.annoyingapi.file.okaeri.migration;

import eu.okaeri.configs.migrate.builtin.NamedMigration;
import org.jetbrains.annotations.NotNull;

import static eu.okaeri.configs.migrate.ConfigMigrationDsl.*;


public abstract class NestedSoundMigration extends NamedMigration {
    /**
     * <pre>{@code
     * KEY:
     *   sound: SOUND
     *   category: CATEGORY
     *   volume: VOLUME
     *   pitch: PITCH
     * }</pre>
     * <p>to</p>
     * <pre>{@code
     * KEY:
     *   NESTED_KEY:
     *     sound: SOUND
     *     category: CATEGORY
     *     volume: VOLUME
     *     pitch: PITCH
     * }</pre>
     */
    public NestedSoundMigration(@NotNull String key, @NotNull String nestedKey) {
        super("migrates sound to nested structure inside sound key",
                when(
                        // Only migrate if sound not already nested
                        not(exists(key + ".sound.sound")),
                        multi(
                                move(key + ".sound", key + "." + nestedKey + ".sound"),
                                move(key + ".category", key + "." + nestedKey + ".category"),
                                move(key + ".volume", key + "." + nestedKey + ".volume"),
                                move(key + ".pitch", key + "." + nestedKey + ".pitch"))));
    }

    /**
     * <pre>{@code
     * KEY:
     *   sound: SOUND
     *   category: CATEGORY
     *   volume: VOLUME
     *   pitch: PITCH
     * }</pre>
     * <p>to</p>
     * <pre>{@code
     * KEY:
     *   sound:
     *     sound: SOUND
     *     category: CATEGORY
     *     volume: VOLUME
     *     pitch: PITCH
     * }</pre>
     */
    public NestedSoundMigration(@NotNull String key) {
        this(key, key + ".sound");
    }
}
