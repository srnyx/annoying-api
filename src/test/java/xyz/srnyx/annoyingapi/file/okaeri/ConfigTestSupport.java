package xyz.srnyx.annoyingapi.file.okaeri;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.Warning;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;


public final class ConfigTestSupport {
    private static final Logger TEST_LOGGER = Logger.getLogger("ConfigTestSupport");
    private static final Server SERVER = createServerProxy();

    private ConfigTestSupport() {}

    public static void bootstrapBukkit() {
        if (Bukkit.getServer() == null) Bukkit.setServer(SERVER);
    }

    @NotNull
    public static Path writeYaml(@NotNull Path directory, @NotNull String fileName, @NotNull String yaml) throws IOException {
        Files.createDirectories(directory);
        final Path configFile = directory.resolve(fileName);
        Files.writeString(configFile, yaml.stripIndent().trim() + System.lineSeparator(), StandardCharsets.UTF_8);
        return configFile;
    }

    @NotNull
    private static Server createServerProxy() {
        return (Server) Proxy.newProxyInstance(
                ConfigTestSupport.class.getClassLoader(),
                new Class<?>[]{Server.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getVersion" -> "git-Paper-1.18.2 (MC: 1.18.2)";
                    case "getBukkitVersion" -> "1.18.2-R0.1-SNAPSHOT";
                    case "getName" -> "Paper";
                    case "getLogger" -> TEST_LOGGER;
                    case "getWarningState" -> Warning.WarningState.DEFAULT;
                    default -> defaultValue(method.getReturnType());
                });
    }

    @Nullable
    private static Object defaultValue(@Nullable Class<?> type) {
        if (type == null || !type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0f;
        if (type == double.class) return 0d;
        if (type == char.class) return '\0';
        return null;
    }
}
