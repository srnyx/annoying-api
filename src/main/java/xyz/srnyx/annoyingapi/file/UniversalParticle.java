package xyz.srnyx.annoyingapi.file;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.javautilities.manipulation.Mapper;

import java.util.logging.Level;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefParticle.PARTICLE_ENUM;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefWorld.WORLD_SPAWN_PARTICLE_METHOD;


public class UniversalParticle {
    @NotNull public final String name;
    /**
     * Either a Particle (1.9+) or an Effect (1.8.8)
     */
    @Nullable public final Enum particle;

    @SuppressWarnings("unchecked")
    public UniversalParticle(@NotNull String name) {
        this.name = name.toUpperCase();

        Enum particle = null;

        // Particle (1.9+)
        if (PARTICLE_ENUM != null) try {
            particle = (Enum) Mapper.toEnum(this.name, PARTICLE_ENUM).orElse(null);
        } catch (final Exception ignored) {}

        // Effect (1.8.8+)
        if (particle == null) {
            particle = Mapper.toEnum(this.name, Effect.class).orElse(null);
            if (particle == null) AnnoyingPlugin.log(Level.WARNING, "&cFailed to find particle: &4" + this.name);
        }

        this.particle = particle;
    }

    public UniversalParticle(@NotNull Enum particle) {
        this.name = particle.name();
        this.particle = particle;
    }

    /**
     * @param   count   no-op on 1.8.8
     * @param   offsetX no-op on 1.8.8
     * @param   offsetY no-op on 1.8.8
     * @param   offsetZ no-op on 1.8.8
     * @param   extra   no-op on 1.8.8
     */
    public void spawn(@NotNull Location location, int count, double offsetX, double offsetY, double offsetZ, double extra) {
        if (particle == null) return;

        // Effect (1.8.8+)
        if (particle instanceof Effect effect) {
            location.getWorld().playEffect(location, effect, 0);
            return;
        }

        // Particle (1.9+)
        if (WORLD_SPAWN_PARTICLE_METHOD != null) try {
            WORLD_SPAWN_PARTICLE_METHOD.invoke(location.getWorld(), particle, location, count, offsetX, offsetY, offsetZ, extra);
            return;
        } catch (final Exception ignored2) {}

        AnnoyingPlugin.log(Level.WARNING, "&cFailed to spawn particle: &4" + name);
    }
}
