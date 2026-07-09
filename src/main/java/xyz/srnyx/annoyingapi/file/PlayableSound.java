package xyz.srnyx.annoyingapi.file;

import com.cryptomorin.xseries.XSound;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.stats.Statable;
import xyz.srnyx.javautilities.manipulation.Mapper;
import xyz.srnyx.javautilities.parents.Stringable;

import java.util.Map;
import java.util.Objects;


/**
 * Represents a {@link Sound} with a category, volume, pitch
 * <br>Also has playing methods using {@link XSound}
 */
public class PlayableSound extends Stringable implements Statable {
    /**
     * The {@link Sound} to play as a {@link XSound}
     */
    @NotNull public XSound sound;
    /**
     * The SoundCategory to play the {@link XSound} in
     */
    @NotNull public XSound.Category category;
    /**
     * The volume to play the {@link XSound} at
     */
    public float volume;
    /**
     * The pitch to play the {@link XSound} at
     */
    public float pitch;

    /**
     * {@code 1.11+} Creates a new {@link PlayableSound} object
     *
     * @param   sound       {@link #sound}
     * @param   category    {@link #category}
     * @param   volume      {@link #volume}
     * @param   pitch       {@link #pitch}
     *
     * @see     #PlayableSound(XSound, float, float)    1.10.2 and below
     */
    public PlayableSound(@NotNull XSound sound, @Nullable XSound.Category category, @Nullable Float volume, @Nullable Float pitch) {
        this.sound = sound;
        this.category = Objects.requireNonNullElse(category, XSound.Category.MASTER);
        this.volume = Objects.requireNonNullElse(volume, 1.0f);
        this.pitch = Objects.requireNonNullElse(pitch, 1.0f);
    }

    /**
     * {@code 1.11+} Creates a new {@link PlayableSound} object with a volume and pitch of 1
     *
     * @param   sound       {@link #sound}
     * @param   category    {@link #category}
     *
     * @see     #PlayableSound(XSound)  1.10.2 and below
     */
    public PlayableSound(@NotNull XSound sound, @Nullable XSound.Category category) {
        this(sound, category, null, null);
    }

    /**
     * Creates a new {@link PlayableSound} object
     *
     * @param   sound   {@link #sound}
     * @param   volume  {@link #volume}
     * @param   pitch   {@link #pitch}
     */
    public PlayableSound(@NotNull XSound sound, float volume, float pitch) {
        this(sound, null, volume, pitch);
    }

    /**
     * Creates a new {@link PlayableSound} object with a volume and pitch of 1
     *
     * @param   sound   {@link #sound}
     */
    public PlayableSound(@NotNull XSound sound) {
        this(sound, null, null, null);
    }

    public PlayableSound(@NotNull Sound sound, @Nullable Enum<?> category, @Nullable Float volume, @Nullable Float pitch) {
        this(XSound.of(sound), getCategory(category), volume, pitch);
    }

    public PlayableSound(@NotNull Sound sound, @Nullable Enum<?> category) {
        this(XSound.of(sound), getCategory(category), null, null);
    }

    public PlayableSound(@NotNull Sound sound, float volume, float pitch) {
        this(XSound.of(sound), null, volume, pitch);
    }

    public PlayableSound(@NotNull Sound sound) {
        this(XSound.of(sound), null, null, null);
    }

    /**
     * Plays the {@link Sound} at the given {@link Location}
     *
     * @param   location    the {@link Location} to play the {@link Sound} at
     */
    public void play(@NotNull Location location) {
        sound.play(location, volume, pitch);
    }

    /**
     * Plays the {@link Sound} to the given {@link Player} at the given {@link Location}
     *
     * @param   player      the {@link Player} to play the {@link Sound} to
     * @param   location    the {@link Location} to play the {@link Sound} at, or null to play at the player's location
     */
    public void play(@NotNull Player player, @Nullable Location location) {
        sound.record()
                .inCategory(category)
                .withVolume(volume)
                .withPitch(pitch)
                .soundPlayer()
                .forPlayers(player)
                .atLocation(location)
                .play();
    }

    /**
     * Plays the {@link Sound} to the given {@link Player} at the player's location
     *
     * @param   player  the {@link Player} to play the {@link Sound} to
     */
    public void play(@NotNull Player player) {
        play(player, null);
    }

    @Override @NotNull
    public Map<String, Object> toStatMap() {
        return Map.of(
                "sound", sound.name(),
                "category", category.name(),
                "volume", volume,
                "pitch", pitch);
    }

    @Nullable
    private static XSound.Category getCategory(@Nullable Enum<?> category) {
        return category != null ? Mapper.toEnum(category.name(), XSound.Category.class).orElse(null) : null;
    }
}
