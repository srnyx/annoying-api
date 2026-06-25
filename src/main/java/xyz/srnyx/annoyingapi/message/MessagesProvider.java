package xyz.srnyx.annoyingapi.message;

import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.file.okaeri.ConfigBuilder;
import xyz.srnyx.annoyingapi.parents.Registrable;
import xyz.srnyx.javautilities.parents.Stringable;

import java.util.function.Consumer;
import java.util.function.Supplier;


public abstract class MessagesProvider extends Registrable implements Consumer<AnnoyingMessages>, Supplier<AnnoyingMessages> {
    @NotNull public Consumer<ConfigBuilder> builder = build -> build
            .config(new AnnoyingMessages(build.plugin))
            .file("messages.yml");
    @NotNull public Defaults defaults = new Defaults();

    public void build() {
        accept(getAnnoyingPlugin().configLoader.build(builder));
    }

    @NotNull
    public MessagesProvider builder(@NotNull Consumer<ConfigBuilder> builder) {
        this.builder = this.builder.andThen(builder);
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
