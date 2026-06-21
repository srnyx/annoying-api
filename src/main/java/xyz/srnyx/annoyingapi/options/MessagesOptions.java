package xyz.srnyx.annoyingapi.options;

import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.file.okaeri.ConfigBuilder;
import xyz.srnyx.annoyingapi.message.AnnoyingMessages;
import xyz.srnyx.javautilities.parents.Stringable;

import java.util.Objects;
import java.util.function.Consumer;


/**
 * Represents the options for the messages file
 */
public class MessagesOptions extends Stringable {
    @NotNull public Consumer<ConfigBuilder> builder = build -> build
            .config(new AnnoyingMessages(Objects.requireNonNull(build.plugin)))
            .file("messages.yml");

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
