package xyz.srnyx.annoyingapi.file.okaeri.serdes;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.file.UniversalParticle;


public class UniversalParticleSerializer implements ObjectSerializer<UniversalParticle> {
    @Override
    public boolean supports(@NotNull Class<?> type) {
        return UniversalParticle.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NotNull UniversalParticle object, @NotNull SerializationData data, @NotNull GenericsDeclaration generics) {
        data.setValue(object.name);
    }

    @Override @NotNull
    public UniversalParticle deserialize(@NotNull DeserializationData data, @NotNull GenericsDeclaration generics) {
        return new UniversalParticle(data.getValue(String.class));
    }
}
