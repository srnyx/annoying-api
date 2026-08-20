package xyz.srnyx.annoyingapi.message;

import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.file.okaeri.ConfigBuilder;
import xyz.srnyx.annoyingapi.parents.Registrable;
import xyz.srnyx.javautilities.parents.Stringable;

import java.util.function.Consumer;


public abstract class MessagesProvider extends Registrable {
    @NotNull public final Defaults defaults = new Defaults();
    protected AnnoyingMessages messages;

    public void build() {
        final AnnoyingPlugin plugin = getAnnoyingPlugin();

        // Builder
        Consumer<ConfigBuilder> builder = build -> build
                .config(new AnnoyingMessages(plugin))
                .file("messages.yml");
        builder = builder.andThen(this::mutateBuilder);

        // Build
        messages = plugin.configLoader.build(builder);
    }

    public void mutateBuilder(@NotNull ConfigBuilder builder) {}
    
    @NotNull
    public AnnoyingMessages get() {
        return messages;
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
