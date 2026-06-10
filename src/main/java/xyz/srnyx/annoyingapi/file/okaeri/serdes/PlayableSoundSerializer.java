package xyz.srnyx.annoyingapi.file.okaeri.serdes;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.file.PlayableSound;
import xyz.srnyx.annoyingapi.reflection.org.bukkit.RefSoundCategory;
import xyz.srnyx.javautilities.manipulation.Mapper;


public class PlayableSoundSerializer implements ObjectSerializer<PlayableSound> {
    @Override
    public boolean supports(@NotNull Class<?> type) {
        return PlayableSound.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NotNull PlayableSound object, @NotNull SerializationData data, @NotNull GenericsDeclaration generics) {
        data.set("sound", object.sound.name());
        data.set("category", object.category != null ? object.category.name() : null);
        data.set("volume", object.volume);
        data.set("pitch", object.pitch);
    }

    @Override @Nullable
    public PlayableSound deserialize(@NotNull DeserializationData data, @NotNull GenericsDeclaration generics) {
        final String soundName = data.get("sound", String.class);
        final Enum<?> category = RefSoundCategory.SOUND_CATEGORY_ENUM != null ? data.get("category", RefSoundCategory.SOUND_CATEGORY_ENUM) : null;
        final Float volume = data.get("volume", Float.class);
        final Float pitch = data.get("pitch", Float.class);
        if (soundName == null || category == null) return null;

        // Get sound. Need to use toEnum to support modern enum (alternative is XSeries).
        //TODO: doesnt make sense this is needed cause wouldnt data.get("sound", Sound.class) do the same thing?
        final Sound sound = Mapper.toEnum(soundName, Sound.class).orElse(null);
        if (sound == null) return null;

        return new PlayableSound(sound, category, volume, pitch);
    }
}
