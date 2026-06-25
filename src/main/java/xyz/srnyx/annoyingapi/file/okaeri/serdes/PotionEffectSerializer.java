package xyz.srnyx.annoyingapi.file.okaeri.serdes;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import eu.okaeri.configs.util.EnumMatcher;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.potion.RefPotionEffect.POTION_EFFECT_CONSTRUCTOR_1_13;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.potion.RefPotionEffect.POTION_EFFECT_HAS_ICON_METHOD;


public class PotionEffectSerializer implements ObjectSerializer<PotionEffect> {
    @NotNull private static final String[] POTION_EFFECT_TYPE_NAMES = Arrays.stream(PotionEffectType.values())
            .map(PotionEffectType::getName)
            .toArray(String[]::new);

    @Override
    public boolean supports(@NotNull Class<?> type) {
        return PotionEffect.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NotNull PotionEffect object, @NotNull SerializationData data, @NotNull GenericsDeclaration generics) {
        data.set("type", object.getType().getName());
        data.set("duration", object.getDuration());
        data.set("amplifier", object.getAmplifier());
        data.set("ambient", object.isAmbient());
        data.set("particles", object.hasParticles());

        // 1.13+ icon
        if (POTION_EFFECT_HAS_ICON_METHOD != null) try {
            data.set("icon", POTION_EFFECT_HAS_ICON_METHOD.invoke(object));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override @NotNull
    public PotionEffect deserialize(@NotNull DeserializationData data, @NotNull GenericsDeclaration generics) {
        // type
        final String typeName = data.get("type", String.class);
        if (typeName == null) throw new IllegalArgumentException("Missing required field: type");
        final PotionEffectType type = PotionEffectType.getByName(typeName);
        if (type == null) throw new IllegalArgumentException(EnumMatcher.suggest(typeName, POTION_EFFECT_TYPE_NAMES, 5));

        // duration, amplifier, ambient, particles
        final List<String> missing = new ArrayList<>();
        final Integer duration = data.get("duration", Integer.class);
        if (duration == null) missing.add("duration");
        final Integer amplifier = data.get("amplifier", Integer.class);
        if (amplifier == null) missing.add("amplifier");
        final Boolean ambient = data.get("ambient", Boolean.class);
        if (ambient == null) missing.add("ambient");
        final Boolean particles = data.get("particles", Boolean.class);
        if (particles == null) missing.add("particles");
        if (!missing.isEmpty()) throw new IllegalArgumentException("Missing required field(s): "+ String.join(", ", missing));

        // 1.13+ icon
        final Boolean icon = data.get("icon", Boolean.class);
        if (icon != null && POTION_EFFECT_CONSTRUCTOR_1_13 != null) try {
            return POTION_EFFECT_CONSTRUCTOR_1_13.newInstance(type, duration, amplifier, ambient, particles, icon);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // 1.12.2- (or icon null)
        return new PotionEffect(type, duration, amplifier, ambient, particles);
    }
}
