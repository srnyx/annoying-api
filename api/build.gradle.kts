import xyz.srnyx.gradlegalaxy.data.pom.DeveloperData
import xyz.srnyx.gradlegalaxy.data.pom.LicenseData
import xyz.srnyx.gradlegalaxy.enums.Repository
import xyz.srnyx.gradlegalaxy.enums.repository
import xyz.srnyx.gradlegalaxy.utility.implementationRelocate
import xyz.srnyx.gradlegalaxy.utility.setupPublishing


plugins {
    `java-library`
}

description = "General purpose API with tons of features"
repository(Repository.CODE_MC) // de.tr7zw:item-nbt-api

dependencies {
    compileOnlyApi("org.jetbrains", "annotations", "24.0.0")
    implementationRelocate(project, "org.bstats:bstats-bukkit:3.0.0")
    implementationRelocate(project, "de.tr7zw:item-nbt-api:2.11.3", "de.tr7zw.changeme.nbtapi") {
        exclude("de.tr7zw", "functional-annotations")
    }
}

setupPublishing(
    artifactId = "annoying-api",
    url = "https://annoying-api.srnyx.com",
    licenses = listOf(LicenseData.MIT),
    developers = listOf(DeveloperData.srnyx))
