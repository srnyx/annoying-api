import kotlinx.serialization.json.Json
import xyz.srnyx.gradlegalaxy.data.annoyingapi.AnnoyingMetadata
import xyz.srnyx.gradlegalaxy.data.annoyingapi.Exclude
import xyz.srnyx.gradlegalaxy.data.annoyingapi.Relocation
import xyz.srnyx.gradlegalaxy.data.annoyingapi.RuntimeLibrary
import xyz.srnyx.gradlegalaxy.data.config.DependencyConfig
import xyz.srnyx.gradlegalaxy.data.config.JavaSetupConfig
import xyz.srnyx.gradlegalaxy.data.config.annoyingapi.GenerateRuntimeLibraryEnumConfig
import xyz.srnyx.gradlegalaxy.data.config.annoyingapi.RuntimeLibrariesConfig
import xyz.srnyx.gradlegalaxy.data.config.publishing.PublishingPlatformConfig
import xyz.srnyx.gradlegalaxy.data.config.publishing.TextArtifact
import xyz.srnyx.gradlegalaxy.data.config.publishing.publishingSimpleConfig
import xyz.srnyx.gradlegalaxy.data.platforms.PluginPlatform
import xyz.srnyx.gradlegalaxy.data.pom.DeveloperData
import xyz.srnyx.gradlegalaxy.data.pom.LicenseData
import xyz.srnyx.gradlegalaxy.enums.Repository
import xyz.srnyx.gradlegalaxy.enums.repository
import xyz.srnyx.gradlegalaxy.utility.*


plugins {
    java
    `java-library`
    id("xyz.srnyx.gradle-galaxy") version "1ae6fb2"
    id("com.gradleup.shadow") version "9.4.3"
    id("net.kyori.blossom") version "2.2.0"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.4.1" // For Blossom
    id("me.modmuss50.mod-publish-plugin") version "bf05e3d"
}

// Runtime libraries
val okaeriConfigsVersion: String = "df8ae69"
val bStatsVersion: String = "3.2.1"
val fastStatsVersion: String = "0.27.1"
val runtimeLibraries = listOf(
    RuntimeLibrary( // Technically not runtime, but better for consumers to not have to specify it
        name = "annotations",
        repositories = listOf(Repository.MAVEN_CENTRAL.url),
        group = "org.jetbrains",
        artifact = "annotations",
        version = "26.1.0"),
    RuntimeLibrary(
        name = "xseries",
        repositories = listOf(Repository.MAVEN_CENTRAL.url),
        group = "com.github.cryptomorin",
        artifact = "XSeries",
        version = "13.7.0",
        relocations = listOf(Relocation("com.cryptomorin.xseries"))),
    RuntimeLibrary(
        name = "okaeri_configs_core",
        repositories = listOf(Repository.SRNYX_SNAPSHOTS.url),
        group = "eu.okaeri",
        artifact = "okaeri-configs-core",
        version = okaeriConfigsVersion,
        relocations = listOf(Relocation("eu.okaeri"))),
    RuntimeLibrary(
        name = "okaeri_configs_yaml_bukkit",
        repositories = listOf(Repository.SRNYX_SNAPSHOTS.url),
        group = "eu.okaeri",
        artifact = "okaeri-configs-yaml-bukkit",
        version = okaeriConfigsVersion,
        relocations = listOf(Relocation("eu.okaeri")),
        dependencies = listOf("okaeri_configs_core")),
    RuntimeLibrary(
        name = "okaeri_configs_serdes_commons",
        repositories = listOf(Repository.SRNYX_SNAPSHOTS.url),
        group = "eu.okaeri",
        artifact = "okaeri-configs-serdes-commons",
        version = okaeriConfigsVersion,
        relocations = listOf(Relocation("eu.okaeri")),
        dependencies = listOf("okaeri_configs_core")),
    RuntimeLibrary(
        name = "okaeri_configs_serdes_bukkit",
        repositories = listOf(Repository.SRNYX_SNAPSHOTS.url),
        group = "eu.okaeri",
        artifact = "okaeri-configs-serdes-bukkit",
        version = okaeriConfigsVersion,
        relocations = listOf(Relocation("eu.okaeri")),
        dependencies = listOf(
            "okaeri_configs_core",
            "okaeri_configs_yaml_bukkit")),
    RuntimeLibrary(
        name = "okaeri_validator",
        repositories = listOf(Repository.OKAERI_RELEASES.url),
        group = "eu.okaeri",
        artifact = "okaeri-validator",
        version = "2.0.5",
        relocations = listOf(Relocation("eu.okaeri"))),
    RuntimeLibrary(
        name = "okaeri_configs_validator_okaeri",
        repositories = listOf(Repository.SRNYX_SNAPSHOTS.url),
        group = "eu.okaeri",
        artifact = "okaeri-configs-validator-okaeri",
        version = okaeriConfigsVersion,
        relocations = listOf(Relocation("eu.okaeri")),
        dependencies = listOf(
            "okaeri_configs_core",
            "okaeri_validator")),
    RuntimeLibrary(
        name = "item_nbt_api",
        repositories = listOf(Repository.CODE_MC.url),
        group = "de.tr7zw",
        artifact = "item-nbt-api",
        version = "2.15.7",
        relocations = listOf(Relocation("de.tr7zw.changeme.nbtapi"))),
    RuntimeLibrary(
        name = "bstats_base",
        repositories = listOf(Repository.MAVEN_CENTRAL.url),
        group = "org.bstats",
        artifact = "bstats-base",
        version = bStatsVersion,
        relocations = listOf(Relocation("org.bstats"))),
    RuntimeLibrary(
        name = "bstats_bukkit",
        repositories = listOf(Repository.MAVEN_CENTRAL.url),
        group = "org.bstats",
        artifact = "bstats-bukkit",
        version = bStatsVersion,
        relocations = listOf(Relocation("org.bstats")),
        dependencies = listOf("bstats_base")),
    RuntimeLibrary(
        name = "faststats_core",
        repositories = listOf(
            Repository.FASTSTATS_RELEASES.url,
            Repository.FASTSTATS_SNAPSHOTS.url),
        group = "dev.faststats.metrics",
        artifact = "core",
        version = fastStatsVersion,
        excludes = listOf(Exclude("com.google.code.gson", "gson")),
        relocations = listOf(Relocation("dev.faststats"))),
    RuntimeLibrary(
        name = "faststats_config",
        repositories = listOf(
            Repository.FASTSTATS_RELEASES.url,
            Repository.FASTSTATS_SNAPSHOTS.url),
        group = "dev.faststats.metrics",
        artifact = "config",
        version = fastStatsVersion,
        excludes = listOf(Exclude("com.google.code.gson", "gson")),
        relocations = listOf(Relocation("dev.faststats")),
        dependencies = listOf("faststats_core")),
    RuntimeLibrary(
        name = "faststats_bukkit",
        repositories = listOf(
            Repository.FASTSTATS_RELEASES.url,
            Repository.FASTSTATS_SNAPSHOTS.url),
        group = "dev.faststats.metrics",
        artifact = "bukkit",
        version = fastStatsVersion,
        excludes = listOf(Exclude("com.google.code.gson", "gson")),
        relocations = listOf(Relocation("dev.faststats")),
        dependencies = listOf(
            "faststats_core",
            "faststats_config")),
    RuntimeLibrary(
        name = "javassist",
        repositories = listOf(Repository.MAVEN_CENTRAL.url),
        group = "org.javassist",
        artifact = "javassist",
        version = "3.28.0-GA",
        relocations = listOf(Relocation("javassist.", "{package}.libs.javassist."))),
    RuntimeLibrary(
        name = "reflections",
        repositories = listOf(Repository.MAVEN_CENTRAL.url),
        group = "org.reflections",
        artifact = "reflections",
        version = "0.10.2",
        relocations = listOf(Relocation("org.reflections")),
        dependencies = listOf("javassist")),
    RuntimeLibrary(
        name = "h2",
        repositories = listOf(Repository.MAVEN_CENTRAL.url),
        group = "com.h2database",
        artifact = "h2",
        version = "2.2.224", // Don't update to keep support for Java 8
        relocations = listOf(Relocation("org.h2"))),
    RuntimeLibrary(
        name = "postgresql",
        repositories = listOf(Repository.MAVEN_CENTRAL.url),
        group = "org.postgresql",
        artifact = "postgresql",
        version = "42.7.11",
        relocations = listOf(Relocation("org.postgresql"))))

val javaVersion: JavaVersion = JavaVersion.VERSION_17

spigotAPI(config = DependencyConfig(version = "1.8.8"))
setupMC(javaSetupConfig = JavaSetupConfig(
    group = "xyz.srnyx",
    description = "General purpose API with tons of features",
    javaVersion = javaVersion))

// Process runtime libraries
processRuntimeLibraries(runtimeLibraries, RuntimeLibrariesConfig(
    configurations = listOf("compileOnlyApi", "testImplementation"),
    relocate = false))

// Generate runtime library enum
generateAnnoyingApiRuntimeLibraryEnum(runtimeLibraries, GenerateRuntimeLibraryEnumConfig(
    relocateImports = false))

// Repositories
repository(Repository.SRNYX_RELEASES, Repository.SRNYX_SNAPSHOTS, Repository.PLACEHOLDER_API, Repository.ALESSIO_DP, Repository.PAPER)

// Dependencies
dependencies {
    // Bundled
    dependencyRelocate("net.byteflux:libby-bukkit:1.3.1", "net.byteflux.libby", configuration = "api")
    dependencyRelocate("xyz.srnyx:java-utilities:c53df5b", "xyz.srnyx.javautilities", configuration = "api")

    // Optional
    compileOnly("me.clip:placeholderapi:2.12.2")
}

// Blossom (see java-templates module)
sourceSets.main {
    blossom.javaSources { property("annoying_api_version", version.toString()) }
}

// Testing
setupMockBukkit(
    junitBomConfig = DependencyConfig(version = "6.1.0"),
    mockBukkitDependencyConfig = DependencyConfig(version = "3.9.0"))

// Library publishing
setupPublishingEnv(publishingSimpleConfig(
    artifactId = "annoying-api",
    silenceMissingJavadocWarnings = true,
    url = "https://annoying-api.srnyx.com",
    licenses = listOf(LicenseData.MIT),
    developers = listOf(DeveloperData.srnyx),
    textArtifacts = listOf(TextArtifact(
        text = {
            val metadata = AnnoyingMetadata(
                packageName = "${project.group}.annoyingapi",
                javaVersion = javaVersion.majorVersion.toInt(),
                repositories = listOf(Repository.ALESSIO_DP.url),
                runtimeLibraries = runtimeLibraries,
                excludes = listOf(
                    Exclude("net.byteflux", "libby-bukkit"),
                    Exclude("xyz.srnyx", "java-utilities")))
            return@TextArtifact Json {
                prettyPrint = true
                prettyPrintIndent = "  "
            }.encodeToString(metadata)
        },
        classifier = "metadata",
        extension = "json"))))

// Platform publishing
setupPublishingPlatforms(
    config = PublishingPlatformConfig(
        platforms = mapOf(
            PluginPlatform.MODRINTH to "gzktm9GG",
            PluginPlatform.CURSEFORGE to "728930"),
        loaders = listOf("spigot", "paper", "purpur", "folia"),
        addAnnoyingApiDependency = false,
        modrinthAction = { optional("placeholderapi") }))
