package xyz.srnyx.annoyingapi.file.okaeri.serdes.duration;

import eu.okaeri.configs.serdes.SerdesContext;
import eu.okaeri.configs.serdes.commons.duration.DurationTransformer;
import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.utility.DurationUtility;

import java.time.Duration;


public class DurationSerializer extends DurationTransformer {
    @Override @NotNull
    public Duration leftToRight(@NotNull String data, @NotNull SerdesContext serdesContext) {
        // Run super transformer
        Duration duration = super.leftToRight(data, serdesContext);

        // TickFallback processing
        final boolean ticks = serdesContext.getFieldAnnotation(DurationTickFallback.class).isPresent();
        if (ticks) try {
            duration = DurationUtility.fromTicks(Long.parseLong(data));
        } catch (final NumberFormatException ignored) {}

        return duration;
    }
}
