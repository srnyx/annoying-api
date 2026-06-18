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
import xyz.srnyx.gradlegalaxy.utility.getVersionString
import xyz.srnyx.gradlegalaxy.utility.setupMC
import xyz.srnyx.gradlegalaxy.utility.setupPublishingEnv
import xyz.srnyx.gradlegalaxy.utility.spigotAPI


plugins {
    java
    `java-library`
    id("xyz.srnyx.gradle-galaxy") version "400e6d5"
    id("com.gradleup.shadow") version "9.4.2"
    id("net.kyori.blossom") version "2.2.0"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.4.1"
}

val spigotVersion: String = "1.8.8"
val javaVersion: JavaVersion = JavaVersion.VERSION_17

spigotAPI(config = DependencyConfig(version = spigotVersion))
setupMC(javaSetupConfig = JavaSetupConfig(
    group = "xyz.srnyx",
    version = "5.2.1",
    description = "General purpose API with tons of features",
    javaVersion = javaVersion))

// Libraries downloaded at runtime
val okaeriConfigsVersion: String = "9d8531c"
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
        name = "okaeri_configs_yaml_bukkit",
        repositories = listOf(Repository.SRNYX_SNAPSHOTS.url),
        group = "eu.okaeri",
        artifact = "okaeri-configs-yaml-bukkit",
        version = okaeriConfigsVersion,
        relocations = listOf(Relocation("eu.okaeri"))),
    RuntimeLibrary(
        name = "okaeri_configs_serdes_bukkit",
        repositories = listOf(Repository.SRNYX_SNAPSHOTS.url),
        group = "eu.okaeri",
        artifact = "okaeri-configs-serdes-bukkit",
        version = okaeriConfigsVersion,
        relocations = listOf(Relocation("eu.okaeri"))),
    RuntimeLibrary(
        name = "okaeri_configs_serdes_commons",
        repositories = listOf(Repository.SRNYX_SNAPSHOTS.url),
        group = "eu.okaeri",
        artifact = "okaeri-configs-serdes-commons",
        version = okaeriConfigsVersion,
        relocations = listOf(Relocation("eu.okaeri"))),
    RuntimeLibrary(
        name = "okaeri_configs_validator_okaeri",
        repositories = listOf(Repository.SRNYX_SNAPSHOTS.url),
        group = "eu.okaeri",
        artifact = "okaeri-configs-validator-okaeri",
        version = okaeriConfigsVersion,
        relocations = listOf(Relocation("eu.okaeri"))),
    RuntimeLibrary(
        name = "item_nbt_api",
        repositories = listOf(Repository.CODE_MC.url),
        group = "de.tr7zw",
        artifact = "item-nbt-api",
        version = "2.15.7",
        relocations = listOf(Relocation("de.tr7zw.changeme.nbtapi"))),
    RuntimeLibrary(
        name = "bstats_bukkit",
        repositories = listOf(Repository.MAVEN_CENTRAL.url),
        group = "org.bstats",
        artifact = "bstats-bukkit",
        version = "3.2.1",
        relocations = listOf(Relocation("org.bstats"))),
    RuntimeLibrary(
        name = "faststats_bukkit",
        repositories = listOf(
            Repository.FASTSTATS_RELEASES.url,
            Repository.FASTSTATS_SNAPSHOTS.url),
        group = "dev.faststats.metrics",
        artifact = "bukkit",
        version = "0.27.0",
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

    // Unit tests
    testImplementation("org.spigotmc:spigot-api:${getVersionString(spigotVersion)}")
    testImplementation(platform("org.junit:junit-bom:6.1.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Runtime libraries
    for (library in runtimeLibraries) {
        val notation = "${library.group}:${library.artifact}:${library.version}"
        compileOnlyApi(notation) {
            library.excludes.forEach { exclude(it.group, it.module) }
        }

        // Unit tests
        testImplementation(notation) {
            library.excludes.forEach { exclude(it.group, it.module) }
        }
    }
}

// Unit tests
tasks.test {
    useJUnitPlatform()
}

// Blossom (see java-templates module)
sourceSets.main { blossom.javaSources {
    property("annoying_api_version", version.toString())

    // Runtime libraries
    for (library in runtimeLibraries) {
        library.repositories.forEachIndexed { index, repository ->
            property("${library.name}_repository_$index", repository)
        }
        property("${library.name}_group", library.group.dotsToBrackets())
        property("${library.name}_artifact", library.artifact)
        property("${library.name}_version", library.version)
        library.relocations.forEachIndexed { index, relocation ->
            property("${library.name}_relocation_${index}_from", relocation.from.dotsToBrackets())
            relocation.to?.let { property("${library.name}_relocation_${index}_to", it.processRelocationTo()) }
        }
    }
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

fun String.dotsToBrackets(): String = replace(".", "{}")

fun String.processRelocationTo(): String = replace("{package}.libs.", "").dotsToBrackets()
