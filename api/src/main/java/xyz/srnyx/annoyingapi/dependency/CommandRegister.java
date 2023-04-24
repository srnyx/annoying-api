package xyz.srnyx.annoyingapi.dependency;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.plugin.Plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * A class that registers commands using the Brigadier API
 * <p><b>All</b> credit for this class goes to <a href="https://spigotmc.org/resources/authors/623700">realEntity303</a>, author of <a href="https://spigotmc.org/resources/88135">PlugManX</a>, I just modified it to be a bit nicer
 */
public class CommandRegister {
    /**
     * The method used to sync commands
     */
    @Nullable private Method syncCommandsMethod;
    /**
     * The constructor used to create a BukkitCommandWrapper
     */
    @Nullable private Constructor<?> bukkitcommandWrapperConstructor;
    /**
     * The method used to register a command
     */
    @Nullable private Method registerMethod;
    /**
     * The result of the {@code a} method
     */
    @Nullable private Object aMethodResult;

    /**
     * Initialize the command register
     */
    public CommandRegister() {
        // Get nmsVersion, bukkitcommandWrapperConstructor, registerMethod, & syncCommandsMethod
        final String nmsVersion;
        try {
            // nmsVersion
            nmsVersion = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
            // syncCommandsMethod
            syncCommandsMethod = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".CraftServer").getDeclaredMethod("syncCommands");
            syncCommandsMethod.setAccessible(true);
            // bukkitcommandWrapperConstructor
            bukkitcommandWrapperConstructor = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".command.BukkitCommandWrapper").getDeclaredConstructor(Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".CraftServer"), Command.class);
            bukkitcommandWrapperConstructor.setAccessible(true);
            // registerMethod
            registerMethod = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".command.BukkitCommandWrapper").getMethod("register", com.mojang.brigadier.CommandDispatcher.class, String.class);
            registerMethod.setAccessible(true);
        } catch (final ArrayIndexOutOfBoundsException | ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
            return;
        }

        // Get minecraftServerClass
        Class<?> minecraftServerClass;
        try {
            minecraftServerClass = Class.forName("net.minecraft.server." + nmsVersion + ".MinecraftServer");
        } catch (final ClassNotFoundException e) {
            try {
                minecraftServerClass = Class.forName("net.minecraft.server.MinecraftServer");
            } catch (final ClassNotFoundException e2) {
                e2.addSuppressed(e);
                e2.printStackTrace();
                return;
            }
        }

        // Get vanillaCommandDispatcherField, getServerMethod, & aMethodResult
        try {
            // vanillaCommandDispatcherField
            final Field vanillaCommandDispatcherField = minecraftServerClass.getDeclaredField("vanillaCommandDispatcher");
            vanillaCommandDispatcherField.setAccessible(true);
            // getServerMethod
            final Method getServerMethod = minecraftServerClass.getMethod("getServer");
            getServerMethod.setAccessible(true);
            // aMethodResult
            final Object commandDispatcher = vanillaCommandDispatcherField.get(getServerMethod.invoke(minecraftServerClass));
            final Method aMethod = commandDispatcher.getClass().getDeclaredMethod("a");
            aMethod.setAccessible(true);
            aMethodResult = aMethod.invoke(commandDispatcher);
        } catch (final NoSuchFieldException | InvocationTargetException | RuntimeException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Register a command from another plugin
     *
     * @param   plugin  the plugin that owns the command
     * @param   command the command to register
     */
    public void register(@NotNull Plugin plugin, @NotNull Command command) {
        if (registerMethod != null && bukkitcommandWrapperConstructor != null && aMethodResult != null) try {
            registerMethod.invoke(bukkitcommandWrapperConstructor.newInstance(Bukkit.getServer(), command), aMethodResult, plugin.getName());
        } catch (final IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Refreshes the command list
     */
    public void sync() {
        if (syncCommandsMethod != null) try {
            syncCommandsMethod.invoke(Bukkit.getServer());
        } catch (final IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
