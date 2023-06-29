package xyz.srnyx.annoyingapi.utility.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import net.md_5.bungee.api.chat.BaseComponent;

import org.bukkit.ChatColor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * A utility class for Adventure API
 */
public class AdventureUtility {
    /**
     * Converts a MiniMessage string to a {@link Component}
     *
     * @param   string  the string to convert
     *
     * @return          the converted string
     */
    @NotNull
    public static Component miniMessageToComponent(@Nullable String string) {
        if (string == null) return Component.empty();
        return MiniMessage.miniMessage().deserialize(string);
    }

    /**
     * Converts a legacy {@link ChatColor} string to a {@link TextComponent}
     *
     * @param   string  the string to convert
     *
     * @return          the converted string
     */
    @NotNull
    public static TextComponent legacyToComponent(@Nullable String string) {
        if (string == null) return Component.empty();
        return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
    }

    /**
     * Converts a {@link BaseComponent} array to a {@link Component}
     *
     * @param   components  the components to convert
     *
     * @return              the converted components
     */
    @NotNull
    public static Component baseComponentsToComponent(@NotNull BaseComponent[] components) {
        return BungeeComponentSerializer.legacy().deserialize(components);
    }

    /**
     * Constructs a new {@link AdventureUtility} instance (illegal)
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private AdventureUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
