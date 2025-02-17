import xyz.srnyx.gradlegalaxy.data.pom.DeveloperData
import xyz.srnyx.gradlegalaxy.data.pom.LicenseData
import xyz.srnyx.gradlegalaxy.enums.Repository
import xyz.srnyx.gradlegalaxy.enums.repository
import xyz.srnyx.gradlegalaxy.utility.*


plugins {
    java
    `java-library`
    id("xyz.srnyx.gradle-galaxy") version "1.3.3"
    id("com.gradleup.shadow") version "8.3.6"
    id("net.kyori.blossom") version "2.1.0"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.9"
}

setupMC("xyz.srnyx", "5.1.5", "General purpose API with tons of features")
spigotAPI("1.8.8")

// Blossom (see java-templates module)
sourceSets.main { blossom.javaSources { property("annoying_api_version", version.toString()) } }

// Dependencies
repository(Repository.JITPACK, Repository.PLACEHOLDER_API, Repository.ALESSIO_DP, Repository.CODE_MC)
dependencies {
    compileOnly("me.clip", "placeholderapi", "2.11.6")
    compileOnly("de.tr7zw", "item-nbt-api", "2.14.1") // Downloaded on runtime
    compileOnlyApi("org.bstats", "bstats-bukkit", "3.1.0") // Downloaded on runtime
    compileOnlyApi("org.reflections", "reflections", "0.10.2") // Downloaded on runtime
    compileOnlyApi("com.h2database", "h2", "2.2.224") // Downloaded on runtime (don't update to keep support for Java 8)
    compileOnlyApi("org.postgresql", "postgresql", "42.7.5") // Downloaded on runtime
    compileOnlyApi("org.jetbrains", "annotations", "26.0.2")
    implementationRelocate(project, "net.byteflux:libby-bukkit:1.3.1", "net.byteflux.libby")
    implementationRelocate(project, "xyz.srnyx:java-utilities:94d5d9c055", "xyz.srnyx.javautilities")
}

// Publishing
setupPublishing(
    artifactId = "annoying-api",
    url = "https://annoying-api.srnyx.com",
    licenses = listOf(LicenseData.MIT),
    developers = listOf(DeveloperData.srnyx))
