import xyz.srnyx.gradlegalaxy.data.pom.DeveloperData
import xyz.srnyx.gradlegalaxy.data.pom.LicenseData
import xyz.srnyx.gradlegalaxy.enums.Repository
import xyz.srnyx.gradlegalaxy.enums.repository
import xyz.srnyx.gradlegalaxy.utility.*


plugins {
    java
    `java-library`
    id("xyz.srnyx.gradle-galaxy") version "1.2.3"
    id("com.gradleup.shadow") version "8.3.0"
}

setupMC("xyz.srnyx", "5.0.0", "General purpose API with tons of features")
spigotAPI("1.8.8")

// Dependencies
repository(Repository.JITPACK, Repository.PLACEHOLDER_API, Repository.ALESSIO_DP, Repository.CODE_MC)
dependencies {
    compileOnly("me.clip", "placeholderapi", "2.11.3")
    compileOnly("de.tr7zw", "item-nbt-api", "2.13.2") // Downloaded on runtime
    compileOnlyApi("org.bstats", "bstats-bukkit", "3.0.2") // Downloaded on runtime
    compileOnlyApi("org.reflections", "reflections", "0.10.2") // Downloaded on runtime
    compileOnlyApi("org.jetbrains", "annotations", "24.1.0")
    implementationRelocate(project, "xyz.srnyx:java-utilities:1.1.0", "xyz.srnyx.javautilities")
    implementationRelocate(project, "net.byteflux:libby-bukkit:1.3.1", "net.byteflux.libby")
}

// Publishing
setupPublishing(
    artifactId = "annoying-api",
    url = "https://annoying-api.srnyx.com",
    licenses = listOf(LicenseData.MIT),
    developers = listOf(DeveloperData.srnyx))
