package xyz.srnyx.annoyingapi.dependency;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.plugin.Plugin;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * A class that registers commands using the Brigadier API
 * <p><b>All</b> credit for this class goes to <a href="https://spigotmc.org/resources/authors/623700">realEntity303</a>, author of <a href="https://spigotmc.org/resources/88135">PlugManX</a>
 */
public class AnnoyingCommandRegister {
    private boolean initialized = false;
    private Constructor<?> bukkitcommandWrapperConstructor;
    private Method registerMethod;
    private Method syncCommandsMethod;
    private Method aMethod;
    private Object commandDispatcher;

    /**
     * Initialize the command register
     */
    public AnnoyingCommandRegister() {
        // Get nmsVersion, bukkitcommandWrapperConstructor, registerMethod, & syncCommandsMethod
        final String nmsVersion;
        try {
            // nmsVersion
            nmsVersion = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
            // bukkitcommandWrapperConstructor
            this.bukkitcommandWrapperConstructor = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".command.BukkitCommandWrapper").getDeclaredConstructor(Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".CraftServer"), Command.class);
            this.bukkitcommandWrapperConstructor.setAccessible(true);
            // registerMethod
            this.registerMethod = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".command.BukkitCommandWrapper").getMethod("register", com.mojang.brigadier.CommandDispatcher.class, String.class);
            this.registerMethod.setAccessible(true);
            // syncCommandsMethod
            this.syncCommandsMethod = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".CraftServer").getDeclaredMethod("syncCommands");
            this.syncCommandsMethod.setAccessible(true);
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

        // Get vanillaCommandDispatcherField, getServerMethod, commandDispatcher, & aMethod
        try {
            // vanillaCommandDispatcherField
            final Field vanillaCommandDispatcherField = minecraftServerClass.getDeclaredField("vanillaCommandDispatcher");
            vanillaCommandDispatcherField.setAccessible(true);
            // getServerMethod
            final Method getServerMethod = minecraftServerClass.getMethod("getServer");
            getServerMethod.setAccessible(true);
            // commandDispatcher
            this.commandDispatcher = vanillaCommandDispatcherField.get(getServerMethod.invoke(minecraftServerClass));
            // aMethod
            this.aMethod = commandDispatcher.getClass().getDeclaredMethod("a");
            this.aMethod.setAccessible(true);
        } catch (final NoSuchFieldException | InvocationTargetException | RuntimeException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
            return;
        }

        initialized = true;
    }

    /**
     * Register a command from another plugin
     *
     * @param   command the command to register
     * @param   plugin  the plugin that owns the command
     */
    public void register(@NotNull Plugin plugin, @NotNull Command command) {
        if (initialized) try {
            this.registerMethod.invoke(this.bukkitcommandWrapperConstructor.newInstance(Bukkit.getServer(), command), this.aMethod.invoke(commandDispatcher), plugin.getName());
        } catch (final IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Refreshes the command list
     */
    public void sync() {
        if (initialized) try {
            this.syncCommandsMethod.invoke(Bukkit.getServer());
        } catch (final IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
