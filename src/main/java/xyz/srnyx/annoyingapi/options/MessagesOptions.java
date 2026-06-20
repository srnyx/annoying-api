package xyz.srnyx.annoyingapi.options;

import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.file.okaeri.ConfigBuilder;
import xyz.srnyx.annoyingapi.message.AnnoyingMessages;
import xyz.srnyx.javautilities.parents.Stringable;

import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 * Represents the options for the messages file
 */
public class MessagesOptions extends Stringable {
    @NotNull public Consumer<ConfigBuilder> builder = build -> {
        if (build.plugin == null) throw new NullPointerException("Plugin cannot be null");
        build
                .file("messages.yml")
                .config(new AnnoyingMessages(build.plugin));
    };
    @NotNull public Supplier<AnnoyingMessages> provider = () -> {};

    /**
     * Constructs a new {@link MessagesOptions} instance with default values
     */
    public MessagesOptions() {
        // Only exists to give the constructor a Javadoc
    }

    @NotNull
    public MessagesOptions builder(@NotNull Consumer<ConfigBuilder> builder) {
        this.builder = this.builder.andThen(builder);
        return this;
    }
}
