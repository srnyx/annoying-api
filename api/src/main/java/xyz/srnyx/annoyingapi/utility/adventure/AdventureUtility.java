package xyz.srnyx.annoyingapi.utility.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import net.md_5.bungee.api.chat.BaseComponent;

import org.bukkit.ChatColor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * A utility class for Adventure API
 */
public class AdventureUtility {
    /**
     * Converts a plain {@link String} to a {@link TextComponent}
     *
     * @param   string  the string to convert
     *
     * @return          the converted string
     */
    @NotNull
    public static TextComponent convertPlain(@Nullable String string) {
        if (string == null) return Component.empty();
        return PlainTextComponentSerializer.plainText().deserialize(string);
    }

    /**
     * Converts a {@link TextComponent} to a plain {@link String}
     *
     * @param   component   the component to convert
     *
     * @return              the converted component
     */
    @NotNull
    public static String convertPlain(@Nullable Component component) {
        if (component == null) return "";
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    /**
     * Converts a legacy {@link ChatColor} string to a {@link TextComponent}
     *
     * @param   string  the string to convert
     *
     * @return          the converted string
     */
    @NotNull
    public static TextComponent convertLegacy(@Nullable String string) {
        if (string == null) return Component.empty();
        return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
    }

    /**
     * Converts a {@link TextComponent} to a legacy {@link ChatColor} string
     *
     * @param   component   the component to convert
     *
     * @return              the converted component
     */
    @NotNull
    public static String convertLegacy(@Nullable Component component) {
        if (component == null) return "";
        return LegacyComponentSerializer.legacyAmpersand().serialize(component);
    }

    /**
     * Converts a MiniMessage string to a {@link Component}
     *
     * @param   string  the string to convert
     *
     * @return          the converted string
     */
    @NotNull
    public static Component convertMiniMessage(@Nullable String string) {
        if (string == null) return Component.empty();
        return MiniMessage.miniMessage().deserialize(string);
    }

    /**
     * Converts a {@link Component} to a MiniMessage string
     *
     * @param   component   the component to convert
     *
     * @return              the converted component
     */
    @NotNull
    public static String convertMiniMessage(@Nullable Component component) {
        if (component == null) return "";
        return MiniMessage.miniMessage().serialize(component);
    }

    /**
     * Converts a {@link BaseComponent} array to a {@link Component}
     *
     * @param   components  the components to convert
     *
     * @return              the converted components
     */
    @NotNull
    public static Component convertBaseComponents(@NotNull BaseComponent[] components) {
        return BungeeComponentSerializer.legacy().deserialize(components);
    }

    /**
     * Converts a {@link Component} to a {@link BaseComponent} array
     *
     * @param   component   the component to convert
     *
     * @return              the converted component
     */
    @NotNull
    public static BaseComponent[] convertBaseComponents(@NotNull Component component) {
        return BungeeComponentSerializer.legacy().serialize(component);
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
