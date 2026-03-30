import xyz.srnyx.gradlegalaxy.data.config.DependencyConfig
import xyz.srnyx.gradlegalaxy.data.config.JavaSetupConfig
import xyz.srnyx.gradlegalaxy.data.pom.DeveloperData
import xyz.srnyx.gradlegalaxy.data.pom.LicenseData
import xyz.srnyx.gradlegalaxy.enums.Repository
import xyz.srnyx.gradlegalaxy.enums.repository
import xyz.srnyx.gradlegalaxy.utility.*


plugins {
    java
    `java-library`
    id("xyz.srnyx.gradle-galaxy") version "2.0.2"
    id("com.gradleup.shadow") version "8.3.9"
    id("net.kyori.blossom") version "2.2.0"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.3"
}

spigotAPI(config = DependencyConfig(version = "1.8.8"))
setupMC(javaSetupConfig = JavaSetupConfig(
    group = "xyz.srnyx",
    version = "5.2.0",
    description = "General purpose API with tons of features"))

// Runtime dependency versions
val itemNbtApiVersion: String = "2.15.6"
val bStatsVersion: String = "3.2.1"
val reflectionsVersion: String = "0.10.2"
val h2Version: String = "2.2.224" // Don't update to keep support for Java 8
val postgreSqlVersion: String = "42.7.8"

// Blossom (see java-templates module)
sourceSets.main { blossom.javaSources {
    property("annoying_api_version", version.toString())
    property("item_nbt_api_version", itemNbtApiVersion)
    property("bstats_version", bStatsVersion)
    property("reflections_version", reflectionsVersion)
    property("h2_version", h2Version)
    property("postgresql_version", postgreSqlVersion)
} }

// Dependencies
repository(Repository.JITPACK, Repository.PLACEHOLDER_API, Repository.ALESSIO_DP, Repository.CODE_MC)
dependencies {
    compileOnly("me.clip", "placeholderapi", "2.11.6")
    compileOnly("de.tr7zw", "item-nbt-api", itemNbtApiVersion) // Downloaded on runtime
    compileOnlyApi("org.bstats", "bstats-bukkit", bStatsVersion) // Downloaded on runtime
    compileOnlyApi("org.reflections", "reflections", reflectionsVersion) // Downloaded on runtime
    compileOnlyApi("com.h2database", "h2", h2Version) // Downloaded on runtime (don't update to keep support for Java 8)
    compileOnlyApi("org.postgresql", "postgresql", postgreSqlVersion) // Downloaded on runtime
    compileOnlyApi("org.jetbrains", "annotations", "26.0.2")
    implementationRelocate("net.byteflux:libby-bukkit:1.3.1", "net.byteflux.libby")
    implementationRelocate("xyz.srnyx:java-utilities:5b11020f87", "xyz.srnyx.javautilities")
}

// Publishing
setupPublishing(
    artifactId = "annoying-api",
    url = "https://annoying-api.srnyx.com",
    licenses = listOf(LicenseData.MIT),
    developers = listOf(DeveloperData.srnyx))
