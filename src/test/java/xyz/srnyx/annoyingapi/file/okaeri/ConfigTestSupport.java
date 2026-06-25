package xyz.srnyx.annoyingapi.file.okaeri;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


public final class ConfigTestSupport {
    @NotNull
    public static Path writeYaml(@NotNull Path directory, @NotNull String fileName, @NotNull String yaml) throws IOException {
        Files.createDirectories(directory);
        final Path configFile = directory.resolve(fileName);
        Files.writeString(configFile, yaml.stripIndent().trim() + System.lineSeparator(), StandardCharsets.UTF_8);
        return configFile;
    }

    private ConfigTestSupport() {}
}
