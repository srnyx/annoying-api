package xyz.srnyx.annoyingapi.reflection.org.bukkit.entity;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.reflection.org.bukkit.RefSoundCategory;
import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Method;


/**
 * org.bukkit.entity.Player
 */
public class RefPlayer {
    /**
     * 1.11+ org.bukkit.entity.Player#sendTitle(String, String, int, int, int)
     */
    @Nullable public static final Method PLAYER_SEND_TITLE_METHOD = ReflectionUtility.getMethod(1, 11, 0, Player.class, "sendTitle", String.class, String.class, int.class, int.class, int.class);

    @Nullable public static final Method PLAYER_PLAY_SOUND_METHOD = ReflectionUtility.getMethod(1, 11, 0, Player.class, "playSound", Location.class, Sound.class, RefSoundCategory.SOUND_CATEGORY_ENUM, float.class, float.class);

    /**
     * This class cannot be instantiated
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private RefPlayer() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }

    /**
     * org.bukkit.entity.Player.Spigot
     */
    public static class RefSpigot {
        /**
         * 1.11+ org.bukkit.entity.Player.Spigot#sendMessage(net.md_5.bungee.api.ChatMessageType, net.md_5.bungee.api.chat.BaseComponent...)
         */
        @Nullable public static final Method PLAYER_SPIGOT_SEND_MESSAGE_METHOD = ReflectionUtility.getMethod(1, 11, 0, Player.Spigot.class, "sendMessage", ChatMessageType.class, BaseComponent[].class);

        /**
         * This class cannot be instantiated
         *
         * @throws  UnsupportedOperationException   if this class is instantiated
         */
        private RefSpigot() {
            throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
        }
    }
}
