import xyz.srnyx.gradlegalaxy.data.pom.DeveloperData
import xyz.srnyx.gradlegalaxy.data.pom.LicenseData
import xyz.srnyx.gradlegalaxy.enums.Repository
import xyz.srnyx.gradlegalaxy.enums.repository
import xyz.srnyx.gradlegalaxy.utility.implementationRelocate
import xyz.srnyx.gradlegalaxy.utility.relocate
import xyz.srnyx.gradlegalaxy.utility.setupPublishing


plugins {
    `java-library`
}

description = "General purpose API with tons of features"

// Repositories
repository(Repository.JITPACK, Repository.CODE_MC)

// Dependencies
dependencies {
    compileOnlyApi("org.jetbrains", "annotations", "24.1.0")
    implementationRelocate(project, "xyz.srnyx:java-utilities:1.0.0", "xyz.srnyx.javautilities")
    implementationRelocate(project, "org.reflections:reflections:0.10.2") {
        exclude("com.google.code.findbugs", "jsr305")
        exclude("org.slf4j", "slf4j-api")
    }
    implementationRelocate(project, "org.bstats:bstats-bukkit:3.0.2")
    implementationRelocate(project, "de.tr7zw:item-nbt-api:2.12.2", "de.tr7zw.changeme.nbtapi") {
        exclude("de.tr7zw", "functional-annotations")
    }

    // Storage methods (MySQL & SQLite provided by Spigot, MariaDB uses MySQL)
    runtimeOnly("com.h2database:h2:2.2.220")
    runtimeOnly("org.postgresql:postgresql:42.7.3") {
        exclude("org.checkerframework")
    }
}

// Relocations
relocate("javassist")
relocate("org.h2")
relocate("org.postgresql")

// Publishing
setupPublishing(
    artifactId = "annoying-api",
    url = "https://annoying-api.srnyx.com",
    licenses = listOf(LicenseData.MIT),
    developers = listOf(DeveloperData.srnyx))
