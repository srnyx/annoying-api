package xyz.srnyx.annoyingapi.file;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.parents.Stringable;

import java.lang.reflect.InvocationTargetException;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefWorld.WORLD_PLAY_SOUND_METHOD;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.entity.RefPlayer.PLAYER_PLAY_SOUND_METHOD;


/**
 * Represents a {@link Sound} with a volume and pitch
 */
public class PlayableSound extends Stringable {
    /**
     * The {@link Sound} to play
     */
    @NotNull private final Sound sound;
    /**
     * The SoundCategory to play the {@link Sound} in
     */
    @Nullable private final Object category;
    /**
     * The volume to play the {@link Sound} at
     */
    private final float volume;
    /**
     * The pitch to play the {@link Sound} at
     */
    private final float pitch;

    /**
     * {@code 1.11+} Creates a new {@link PlayableSound} object
     *
     * @param   sound                           {@link #sound}
     * @param   category                        {@link #category}
     * @param   volume                          {@link #volume}
     * @param   pitch                           {@link #pitch}
     *
     * @see     #PlayableSound(Sound, float, float) 1.10.2 and below
     */
    public PlayableSound(@NotNull Sound sound, @Nullable Object category, float volume, float pitch) {
        this.sound = sound;
        this.category = category;
        this.volume = volume;
        this.pitch = pitch;
    }

    /**
     * {@code 1.11+} Creates a new {@link PlayableSound} object with a volume and pitch of 1
     *
     * @param   sound               {@link #sound}
     * @param   category            {@link #category}
     *
     * @see     #PlayableSound(Sound)   1.10.2 and below
     */
    public PlayableSound(@NotNull Sound sound, @Nullable Object category) {
        this(sound, category, 1, 1);
    }

    /**
     * Creates a new {@link PlayableSound} object
     *
     * @param   sound   {@link #sound}
     * @param   volume  {@link #volume}
     * @param   pitch   {@link #pitch}
     */
    public PlayableSound(@NotNull Sound sound, float volume, float pitch) {
        this(sound, null, volume, pitch);
    }

    /**
     * Creates a new {@link PlayableSound} object with a volume and pitch of 1
     *
     * @param   sound   {@link #sound}
     */
    public PlayableSound(@NotNull Sound sound) {
        this(sound, null, 1, 1);
    }

    /**
     * Plays the {@link Sound} at the given {@link Location} in the given {@link World}
     *
     * @param   world       the {@link World} to play the {@link Sound} in, or null to play in the default world
     * @param   location    the {@link Location} to play the {@link Sound} at, or null to play at the world's spawn
     */
    public void play(@Nullable World world, @Nullable Location location) {
        if (world == null) world = Bukkit.getWorlds().get(0);
        if (location == null) location = world.getSpawnLocation();

        // 1.11+ (SoundCategory)
        if (category != null && WORLD_PLAY_SOUND_METHOD != null) {
            try {
                WORLD_PLAY_SOUND_METHOD.invoke(world, location, sound, category, volume, pitch);
            } catch (final IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return;
        }

        // 1.10.2-
        world.playSound(location, sound, volume, pitch);
    }

    /**
     * Plays the {@link Sound} in the given {@link World} at the world's spawn
     *
     * @param   world   the {@link World} to play the {@link Sound} in
     */
    public void play(@Nullable World world) {
        play(world, null);
    }

    /**
     * Plays the {@link Sound} to the given {@link Player} at the given {@link Location}
     *
     * @param   player      the {@link Player} to play the {@link Sound} to
     * @param   location    the {@link Location} to play the {@link Sound} at, or null to play at the player's location
     */
    public void play(@NotNull Player player, @Nullable Location location) {
        if (location == null) location = player.getLocation();

        // 1.11+ (SoundCategory)
        if (category != null && PLAYER_PLAY_SOUND_METHOD != null) {
            try {
                PLAYER_PLAY_SOUND_METHOD.invoke(player, location, sound, category, volume, pitch);
            } catch (final IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return;
        }

        // 1.10.2-
        player.playSound(location, sound, volume, pitch);
    }

    /**
     * Plays the {@link Sound} to the given {@link Player} at the player's location
     *
     * @param   player  the {@link Player} to play the {@link Sound} to
     */
    public void play(@NotNull Player player) {
        play(player, null);
    }
}
