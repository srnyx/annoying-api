package xyz.srnyx.annoyingapi.file.okaeri.serdes;

import com.cryptomorin.xseries.XSound;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.file.PlayableSound;
import xyz.srnyx.annoyingapi.reflection.org.bukkit.RefSoundCategory;

import java.util.Objects;


public class PlayableSoundSerializer implements ObjectSerializer<PlayableSound> {
    @Override
    public boolean supports(@NotNull Class<?> type) {
        return PlayableSound.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NotNull PlayableSound object, @NotNull SerializationData data, @NotNull GenericsDeclaration generics) {
        data.set("sound", XSound.of(object.sound).name());
        data.set("category", object.category != null ? object.category.toString() : null);
        data.set("volume", object.volume);
        data.set("pitch", object.pitch);
    }

    @Override @NotNull
    public PlayableSound deserialize(@NotNull DeserializationData data, @NotNull GenericsDeclaration generics) {
        // sound
        final String soundName = data.get("sound", String.class);
        if (soundName == null) throw new IllegalArgumentException("Missing required field: sound");

        // category
        final Enum<?> category = RefSoundCategory.SOUND_CATEGORY_ENUM != null ? data.get("category", RefSoundCategory.SOUND_CATEGORY_ENUM) : null;
        if (category == null) throw new IllegalArgumentException("Missing required field: category");

        // Get Sound (need to use XSeries because Sound isn't an enum in newer versions)
        final Sound sound = XSound.of(soundName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid sound: " + soundName))
                .get();

        // volume
        final Float volume = data.get("volume", Float.class);

        // pitch
        final Float pitch = data.get("pitch", Float.class);

        return new PlayableSound(Objects.requireNonNull(sound), category, volume, pitch);
    }
}
