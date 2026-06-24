package xyz.srnyx.annoyingapi.options;

import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.file.okaeri.ConfigBuilder;
import xyz.srnyx.annoyingapi.message.AnnoyingMessages;
import xyz.srnyx.javautilities.parents.Stringable;

import java.util.function.Consumer;


/**
 * Represents the options for the messages file
 */
public class MessagesOptions extends Stringable {
    @NotNull public Consumer<ConfigBuilder> builder = build -> build
            .config(new AnnoyingMessages(build.plugin))
            .file("messages.yml");
    @NotNull public Defaults defaults = new Defaults();

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

    @NotNull
    public MessagesOptions defaults(@NotNull Consumer<Defaults> defaults) {
        defaults.accept(this.defaults);
        return this;
    }

    public static class Defaults extends Stringable {
        @NotNull public String prefix = "&3&lANNOYING &8&l| &b";
        @NotNull public String p = "&b";
        @NotNull public String s = "&3";

        @NotNull
        public Defaults prefix(@NotNull String prefix) {
            this.prefix = prefix;
            return this;
        }

        @NotNull
        public Defaults p(@NotNull String p) {
            this.p = p;
            return this;
        }

        @NotNull
        public Defaults s(@NotNull String s) {
            this.s = s;
            return this;
        }
    }
}
