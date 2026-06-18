package xyz.srnyx.annoyingapi.file.okaeri.serdes;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.potion.RefPotionEffect.POTION_EFFECT_CONSTRUCTOR_6;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.potion.RefPotionEffect.POTION_EFFECT_HAS_ICON_METHOD;


public class PotionEffectSerializer implements ObjectSerializer<PotionEffect> {
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
        final PotionEffectType type = data.get("type", PotionEffectType.class);
        if (type == null) throw new IllegalArgumentException("Missing required field: type");

        // duration, amplifier, ambient, particles
        final int duration = data.get("duration", int.class);
        final int amplifier = data.get("amplifier", int.class);
        final boolean ambient = data.get("ambient", boolean.class);
        final boolean particles = data.get("particles", boolean.class);

        // 1.13+ icon
        final Boolean icon = data.get("icon", Boolean.class);
        if (icon != null && POTION_EFFECT_CONSTRUCTOR_6 != null) try {
            return POTION_EFFECT_CONSTRUCTOR_6.newInstance(type, duration, amplifier, ambient, particles, icon);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // 1.12.2- (or icon null)
        return new PotionEffect(type, duration, amplifier, ambient, particles);
    }
}
