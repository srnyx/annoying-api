import kotlinx.serialization.json.Json
import xyz.srnyx.gradlegalaxy.data.annoyingapi.AnnoyingMetadata
import xyz.srnyx.gradlegalaxy.data.annoyingapi.Exclude
import xyz.srnyx.gradlegalaxy.data.annoyingapi.Relocation
import xyz.srnyx.gradlegalaxy.data.annoyingapi.RuntimeLibrary
import xyz.srnyx.gradlegalaxy.data.config.DependencyConfig
import xyz.srnyx.gradlegalaxy.data.config.JavaSetupConfig
import xyz.srnyx.gradlegalaxy.data.config.publishing.TextArtifact
import xyz.srnyx.gradlegalaxy.data.config.publishing.publishingSimpleConfig
import xyz.srnyx.gradlegalaxy.data.pom.DeveloperData
import xyz.srnyx.gradlegalaxy.data.pom.LicenseData
import xyz.srnyx.gradlegalaxy.enums.Repository
import xyz.srnyx.gradlegalaxy.enums.repository
import xyz.srnyx.gradlegalaxy.utility.dependencyRelocate
import xyz.srnyx.gradlegalaxy.utility.setupMC
import xyz.srnyx.gradlegalaxy.utility.setupPublishingEnv
import xyz.srnyx.gradlegalaxy.utility.spigotAPI


plugins {
    java
    `java-library`
    id("xyz.srnyx.gradle-galaxy") version "2.1.0"
    id("com.gradleup.shadow") version "9.4.2"
    id("net.kyori.blossom") version "2.2.0"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.4.1"
}

val javaVersion = JavaVersion.VERSION_17

spigotAPI(config = DependencyConfig(version = "1.8.8"))
setupMC(javaSetupConfig = JavaSetupConfig(
    group = "xyz.srnyx",
    version = "5.2.1",
    description = "General purpose API with tons of features",
    javaVersion = javaVersion))

// Libraries downloaded at runtime
val runtimeLibraries = listOf(
    RuntimeLibrary( // Technically not runtime, but better for consumers to not have to specify it
        name = "annotations",
        repositories = listOf(Repository.MAVEN_CENTRAL.url),
        group = "org.jetbrains",
        artifact = "annotations",
        version = "26.1.0"),
    RuntimeLibrary(
        name = "item_nbt_api",
        repositories = listOf(Repository.CODE_MC.url),
        group = "de.tr7zw",
        artifact = "item-nbt-api",
        version = "2.15.7",
        relocations = listOf(Relocation("de.tr7zw.changeme.nbtapi"))),
    RuntimeLibrary(
        name = "bstats",
        repositories = listOf(Repository.MAVEN_CENTRAL.url),
        group = "org.bstats",
        artifact = "bstats-bukkit",
        version = "3.2.1",
        relocations = listOf(Relocation("org.bstats"))),
    RuntimeLibrary(
        name = "faststats",
        repositories = listOf(
            Repository.FASTSTATS_RELEASES.url,
            Repository.FASTSTATS_SNAPSHOTS.url),
        group = "dev.faststats.metrics",
        artifact = "bukkit",
        version = "0.23.0",
        excludes = listOf(Exclude("com.google.code.gson", "gson")),
        relocations = listOf(Relocation("dev.faststats"))),
    RuntimeLibrary(
        name = "reflections",
        repositories = listOf(Repository.MAVEN_CENTRAL.url),
        group = "org.reflections",
        artifact = "reflections",
        version = "0.10.2",
        relocations = listOf(
            Relocation("javassist.", "{package}.libs.javassist."),
            Relocation("org.reflections"))),
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

// Repositories
repository(Repository.SRNYX_RELEASES, Repository.SRNYX_SNAPSHOTS, Repository.PLACEHOLDER_API, Repository.ALESSIO_DP)
for (library in runtimeLibraries) repository(*library.repositories.toTypedArray())

// Dependencies
dependencies {
    compileOnly("me.clip:placeholderapi:2.12.2")
    compileOnly("org.jetbrains:annotations:26.1.0")
    dependencyRelocate("net.byteflux:libby-bukkit:1.3.1", "net.byteflux.libby", configuration = "api")
    dependencyRelocate("xyz.srnyx:java-utilities:c53df5b", "xyz.srnyx.javautilities", configuration = "api")

    // Runtime libraries
    for (library in runtimeLibraries) compileOnlyApi("${library.group}:${library.artifact}:${library.version}") {
        library.excludes.forEach { exclude(it.group, it.module) }
    }
}

// Blossom (see java-templates module)
sourceSets.main { blossom.javaSources {
    property("annoying_api_version", version.toString())

    // Runtime libraries
    for (library in runtimeLibraries) property("${library.name}_version", library.version)
} }

// Publishing
setupPublishingEnv(publishingSimpleConfig(
    artifactId = "annoying-api",
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
            }.encodeToString(metadata)
        },
        classifier = "metadata",
        extension = "json"))))
